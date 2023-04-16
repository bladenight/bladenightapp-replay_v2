package app.bladenight.replay.speedgen;

import java.io.IOException;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import app.bladenight.common.network.BladenightUrl;
import app.bladenight.common.network.messages.GpsInfo;
import app.bladenight.common.routes.Route.LatLong;
import app.bladenight.common.time.Sleep;
import app.bladenight.wampv2.client.RpcResultReceiver;
import app.bladenight.wampv2.client.WampClient;

public class SpeedControlledParticipant implements Runnable {

    public interface SpeedMaster {
        public double speedAt(double linearPosition);
    }

    SpeedControlledParticipant(WampClient client, LinearPositionToLatLongInterface callbackInterface, SpeedMaster speedMaster, long updatePeriod) {
        this.updatePeriod = updatePeriod;
        this.callbackInterface = callbackInterface;
        this.speedMaster = speedMaster;
        this.wampClient = client;
        this.deviceId = UUID.randomUUID().toString();
    }

    @Override
    public void run() {
        double linearPosition = startPosition;
        startTime = System.currentTimeMillis();
        long sleptTime = 0;
        while(true) {
            double speed = speedMaster.speedAt(linearPosition);
            linearPosition += speed / 3.6 * sleptTime / 1000.0;
            LatLong latLong = callbackInterface.convert(linearPosition);
            try {
                sendGpsInfo(latLong);
                long timeBeforeSleep = System.currentTimeMillis();
                Sleep.sleep(updatePeriod);
                sleptTime = System.currentTimeMillis() - timeBeforeSleep;
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
        gpsInfo.isParticipating(true);
        wampClient.call(BladenightUrl.GET_REALTIME_UPDATE.getText(), receiver, gpsInfo);

    }

    public long getStartTime() {
        return startTime;
    }

    public long getElapsedTime() {
        return System.currentTimeMillis() - startTime;
    }

    private SpeedMaster speedMaster;
    private LinearPositionToLatLongInterface callbackInterface;
    private long startTime;
    private long updatePeriod;
    private double startPosition;
    private WampClient wampClient;
    private String deviceId;

    private static Log log;

    public static void setLog(Log log) {
        SpeedControlledParticipant.log = log;
    }

    protected static Log getLog() {
        if (log == null)
            setLog(LogFactory.getLog(SpeedControlledParticipant.class));
        return log;
    }
}
