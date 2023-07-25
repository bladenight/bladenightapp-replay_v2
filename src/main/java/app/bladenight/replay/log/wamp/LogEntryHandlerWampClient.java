package app.bladenight.replay.log.wamp;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import app.bladenight.replay.log.LogEntryHandler;
import app.bladenight.replay.log.ParticipanLogFile;
import app.bladenight.wampv2.server.common.Channel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import app.bladenight.common.network.BladenightUrl;
import app.bladenight.common.network.messages.GpsInfo;
//import app.bladenight.wampv2.adapters.jetty.JettyClient;
import app.bladenight.wampv2.client.RpcResultReceiver;
import app.bladenight.wampv2.client.WampClient;

public class LogEntryHandlerWampClient implements LogEntryHandler {

    public LogEntryHandlerWampClient(URI uri,String authorisationKey) {
        this.serverUri = uri;
        this.wampClients = new HashMap<String, WampClient>();
        this.authorisationKey =authorisationKey;
    }

    @Override
    public void finish() {
        // TODO we should close the connections here
    }

    @Override
    public void handleLogEntry(ParticipanLogFile.LogEntry logEntry) throws Exception {
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
            GpsInfo gpsInfo = new GpsInfo(logEntry.deviceId, true, logEntry.latitude, logEntry.longitude,logEntry.realSpeed);
            wampClient.call(BladenightUrl.GET_REALTIME_UPDATE.getText(), receiver, gpsInfo);
        }
        catch (IOException e) {
            getLog().error("In RpcResultReceiver:onError()", e);
            wampClients.put(deviceId, wampClient);
        }

    }

    private WampClient createNewConnection() throws IOException  {
        WampClient wc= new WampClient(new Channel() {
            @Override
            public void handle(String message) throws IOException {

            }
        },this.serverUri,this.authorisationKey);
        return wc;
        /*JettyClient jettyClient = new JettyClient();
        jettyClient.connect(serverUri, "undefined");
        return jettyClient.getWampClient();*/
    }


    private URI serverUri;
    private String authorisationKey="";
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
