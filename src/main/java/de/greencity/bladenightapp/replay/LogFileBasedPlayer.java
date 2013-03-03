package de.greencity.bladenightapp.replay;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;

import de.greencity.bladenightapp.network.BladenightUrl;
import de.greencity.bladenightapp.network.messages.GpsInfo;
import de.greencity.bladenightapp.replay.ParticipanLogFile.LogEntry;
import de.greencity.bladenightapp.time.Sleep;
import fr.ocroquette.wampoc.adapters.jetty.JettyClient;
import fr.ocroquette.wampoc.client.RpcResultReceiver;
import fr.ocroquette.wampoc.client.WampClient;

public class LogFileBasedPlayer {

	LogFileBasedPlayer(URI uri) {
		this.serverUri = uri; 
	}
	
	public void readLogEntries(File file)
			throws IOException {
		ParticipanLogFile logFile = new ParticipanLogFile(file);
		logFile.load();
		logEntries = logFile.getEntries();
	}

	public DateTime getFromDateTime() {
		return fromDateTime;
	}

	public void setFromDateTime(DateTime fromDateTime) {
		this.fromDateTime = fromDateTime;
	}

	public DateTime getToDateTime() {
		return toDateTime;
	}

	public void setToDateTime(DateTime toDateTime) {
		this.toDateTime = toDateTime;
	}

	public double getTimeLapseFactor() {
		return timeLapseFactor;
	}

	public void setTimeLapseFactor(double timeLapseFactor) {
		this.timeLapseFactor = timeLapseFactor;
	}


	void replay() throws URISyntaxException, InterruptedException, IOException  {
		Map<String, WampClient> wampClients = new HashMap<String, WampClient>();

		GpsInfo gpsInfo = new GpsInfo();

		long currentSimulatedTime = 0;
		for (int i=0; i<logEntries.length; i++) {
			LogEntry entry = logEntries[i];
			if ( entry.serverTime < fromDateTime.getMillis() || entry.serverTime > toDateTime.getMillis() )
				continue;

			if ( currentSimulatedTime != 0 ) {
				if ( entry.serverTime < currentSimulatedTime )
					getLog().error("Clock skew detected: " + entry.serverTime + " < " + currentSimulatedTime);
				else
					Sleep.sleep( (long) (( entry.serverTime - currentSimulatedTime ) / timeLapseFactor) );
			}
			
			currentSimulatedTime = entry.serverTime;

			getLog().info("Current simulated time: " + new DateTime(currentSimulatedTime) );

			String deviceId = entry.deviceId;
			WampClient wampClient = wampClients.get(deviceId);
			if ( wampClient == null ) {
				getLog().info("Creating connection for client " + deviceId);
				wampClient = createNewConnection();
				wampClients.put(deviceId, wampClient);
			}

			RpcResultReceiver receiver = new RpcResultReceiver() {
				@Override
				public void onSuccess() {
				}

				@Override
				public void onError() {
					getLog().error("RpcResultReceiver:onError() : " + this.callErrorMessage);
				}
			};

			gpsInfo.setLatitude(logEntries[i].latitude);
			gpsInfo.setLongitude(logEntries[i].longitude);
			gpsInfo.setDeviceId(deviceId);
			try {
				wampClient.call(BladenightUrl.PARTICIPANT_UPDATE.getText(), receiver, gpsInfo, GpsInfo.class);
			}
			catch (IOException e) {
				getLog().error("In RpcResultReceiver:onError()", e);
				wampClients.put(deviceId, wampClient);
			}
		}
	}

	private WampClient createNewConnection() throws IOException  {
		JettyClient jettyClient = new JettyClient();
		jettyClient.connect(serverUri, "undefined");
		return jettyClient.getWampClient();
	}


	private ParticipanLogFile.LogEntry[] logEntries;
	private double timeLapseFactor = 1.0;
	private URI serverUri;
	private DateTime fromDateTime;
	private DateTime toDateTime;

	private static Log log;

	public static void setLog(Log log) {
		LogFileBasedPlayer.log = log;
	}

	protected static Log getLog() {
		if (log == null)
			setLog(LogFactory.getLog(LogFileBasedPlayer.class));
		return log;
	}
}
