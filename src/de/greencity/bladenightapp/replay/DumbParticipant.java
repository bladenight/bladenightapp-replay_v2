package de.greencity.bladenightapp.replay;

import java.io.IOException;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.greencity.bladenightapp.network.BladenightUrl;
import de.greencity.bladenightapp.network.messages.GpsInfo;
import de.greencity.bladenightapp.routes.Route.LatLong;
import fr.ocroquette.wampoc.client.RpcResultReceiver;
import fr.ocroquette.wampoc.client.WampClient;

public class DumbParticipant implements Runnable {
	private LinearPositionToLatLongInterface callbackInterface;

	DumbParticipant(WampClient client, LinearPositionToLatLongInterface callbackInterface, long updatePeriod) {
		this.updatePeriod = updatePeriod;
		this.callbackInterface = callbackInterface;
		this.wampClient = client;
		this.deviceId = UUID.randomUUID().toString(); 
	}
	
	@Override
	public void run() {
		startTime = System.currentTimeMillis();
		while(true) {
			// getLog().info(getDeviceId()+": getElapsedTime="+getElapsedTime());
			// getLog().info(getDeviceId()+": speed="+speed);
			double linearPosition = speed / 3.6 * getElapsedTime() / 1000.0; 
			// getLog().info(getDeviceId()+": linearPosition="+linearPosition);
			LatLong latLong = callbackInterface.convert(linearPosition);
			try {
				sendGpsInfo(latLong);
				Thread.sleep(updatePeriod);
			} catch (Exception e) {
				getLog().error(e.getMessage(),e);
				return;
			}
		}
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public void sendGpsInfo(LatLong latLong) throws IOException {
		getLog().info(getDeviceId()+": sending  ("+latLong+")");
		RpcResultReceiver receiver = new RpcResultReceiver() {
			@Override
			public void onSuccess() {
				// System.out.println("RpcResultReceiver:onSuccess()");
				// System.out.println(getPayload(RealTimeUpdateData.class));
			}

			@Override
			public void onError() {
				getLog().error("RpcResultReceiver:onError()");
				getLog().error(this.callErrorMessage);
			}
		};

		GpsInfo gpsInfo = new GpsInfo();
		gpsInfo.setLatitude(latLong.lat);
		gpsInfo.setLongitude(latLong.lon);
		gpsInfo.setDeviceId(deviceId);
		wampClient.call(BladenightUrl.PARTICIPANT_UPDATE.getText(), receiver, gpsInfo, GpsInfo.class);

	}
	
	public long getStartTime() {
		return startTime;
	}
	
	public long getElapsedTime() {
		return System.currentTimeMillis() - startTime;
	}
	
	public void setSpeed(double speed) {
		this.speed = speed;
	}
	
	public double getSpeed() {
		return speed;
	}

	private long startTime;
	private long updatePeriod;
	private double speed;
	private WampClient wampClient;
	private String deviceId;

	private static Log log;

	public static void setLog(Log log) {
		DumbParticipant.log = log;
	}

	protected static Log getLog() {
		if (log == null)
			setLog(LogFactory.getLog(DumbParticipant.class));
		return log;
	}
}
