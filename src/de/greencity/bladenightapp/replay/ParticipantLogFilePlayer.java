package de.greencity.bladenightapp.replay;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;

import de.greencity.bladenightapp.network.BladenightUrl;
import de.greencity.bladenightapp.network.messages.GpsInfo;
import de.greencity.bladenightapp.replay.ParticipanLogFile.LogEntry;
import fr.ocroquette.wampoc.adapters.jetty.JettyClient;
import fr.ocroquette.wampoc.client.RpcResultReceiver;
import fr.ocroquette.wampoc.client.WampClient;

public class ParticipantLogFilePlayer {

	ParticipantLogFilePlayer(URI uri) {
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


	void replay() throws Exception  {
		Map<String, WampClient> wampClients = new HashMap<String, WampClient>();

		GpsInfo gpsInfo = new GpsInfo();

		long currentSimulatedTime = 0;
		for (int i=0; i<logEntries.length; i++) {
			LogEntry entry = logEntries[i];
			if ( entry.serverTime < fromDateTime.getMillis() || entry.serverTime > toDateTime.getMillis() )
				continue;
			// System.out.println("logEntry=" + entry);

			if ( currentSimulatedTime != 0 )
				Thread.sleep( (long) (( entry.serverTime - currentSimulatedTime ) / timeLapseFactor) );
			currentSimulatedTime = entry.serverTime;
			// System.out.println(new DateTime(currentSimulatedTime));

			String deviceId = entry.deviceId;
			WampClient wampClient = wampClients.get(deviceId);
			if ( wampClient == null ) {
				System.out.println("Creating connection for client " + deviceId);
				wampClient = createNewConnection();
				wampClients.put(deviceId, wampClient);
			}

			RpcResultReceiver receiver = new RpcResultReceiver() {
				@Override
				public void onSuccess() {
					// System.out.println("RpcResultReceiver:onSuccess()");
					// System.out.println(getPayload(RealTimeUpdateData.class));
				}

				@Override
				public void onError() {
					System.err.println("RpcResultReceiver:onError()");
					System.err.println(this.callErrorMessage);
				}
			};

			gpsInfo.setLatitude(logEntries[i].latitude);
			gpsInfo.setLongitude(logEntries[i].longitude);
			gpsInfo.setDeviceId(deviceId);
			wampClient.call(BladenightUrl.PARTICIPANT_UPDATE.getText(), receiver, gpsInfo, GpsInfo.class);
		}
	}

	private WampClient createNewConnection() throws URISyntaxException, Exception  {
		JettyClient jettyClient = new JettyClient();
		jettyClient.connect(serverUri, "undefined");

		return jettyClient.getWampClient();
	}


	private ParticipanLogFile.LogEntry[] logEntries;
	private double timeLapseFactor = 1.0;
	private URI serverUri;
	private DateTime fromDateTime;
	private DateTime toDateTime;

}
