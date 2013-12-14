package de.greencity.bladenightapp.replay.log;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.greencity.bladenightapp.network.BladenightUrl;
import de.greencity.bladenightapp.network.messages.GpsInfo;
import de.greencity.bladenightapp.replay.log.ParticipanLogFile.LogEntry;
import fr.ocroquette.wampoc.adapters.jetty.JettyClient;
import fr.ocroquette.wampoc.client.RpcResultReceiver;
import fr.ocroquette.wampoc.client.WampClient;

public class LogEntryHandlerWampClient implements LogEntryHandler {

	public LogEntryHandlerWampClient(URI uri) {
		this.serverUri = uri; 
		this.wampClients = new HashMap<String, WampClient>();
	}

	@Override
	public void handleLogEntry(LogEntry logEntry) throws Exception {
		String deviceId = logEntry.deviceId;
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

		try {
			GpsInfo gpsInfo = new GpsInfo(logEntry.deviceId, true, logEntry.latitude, logEntry.longitude);
			wampClient.call(BladenightUrl.GET_REALTIME_UPDATE.getText(), receiver, gpsInfo);
		}
		catch (IOException e) {
			getLog().error("In RpcResultReceiver:onError()", e);
			wampClients.put(deviceId, wampClient);
		}

	}

	private WampClient createNewConnection() throws IOException  {
		JettyClient jettyClient = new JettyClient();
		jettyClient.connect(serverUri, "undefined");
		return jettyClient.getWampClient();
	}


	private URI serverUri;
	private Map<String, WampClient> wampClients;
	private Log log;

	public void setLog(Log log) {
		this.log = log;
	}

	protected Log getLog() {
		if (log == null)
			setLog(LogFactory.getLog(LogEntryHandlerWampClient.class));
		return log;
	}
}
