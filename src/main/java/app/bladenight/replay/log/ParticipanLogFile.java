package app.bladenight.replay.log;

import app.bladenight.common.valuelogger.ValueReader;
import app.bladenight.common.valuelogger.ValueReader.Consumer;
import app.bladenight.common.valuelogger.ValueReader.Entry;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParticipanLogFile {

    public class LogEntry {
        public String deviceId;
        public DateTime dateTime;
        public double latitude;
        public double longitude;
        public double accuracy;
        public double realSpeed;

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this);
        }

    }

    ;

    public ParticipanLogFile(File file) {
        this.file = file;
    }

    public void load() throws IOException {
        getLog().info("Reading " + file.getAbsolutePath() + "...");

        logEntries = new ArrayList<LogEntry>();
        Consumer consumer = new Consumer() {
            @Override
            public void consume(Entry entry) {
                LogEntry logEntry = new LogEntry();
                logEntry.deviceId = entry.getString("did");
                logEntry.dateTime = new DateTime(entry.getString("ts"));
                logEntry.latitude = entry.getDouble("la");
                logEntry.longitude = entry.getDouble("lo");
                logEntry.accuracy = entry.getDouble("ac");
                logEntry.realSpeed = entry.getDouble("rsp");
                logEntries.add(logEntry);
            }
        };

        ValueReader valueReader = new ValueReader(file, consumer);
        valueReader.read();

        getLog().info("Got " + logEntries.size() + " log entries");
    }

    public List<LogEntry> getEntries() {
        return Collections.unmodifiableList(logEntries);
    }

    private static Log log;

    public static void setLog(Log log) {
        ParticipanLogFile.log = log;
    }

    protected static Log getLog() {
        if (log == null)
            setLog(LogFactory.getLog(ParticipanLogFile.class));
        return log;
    }

    private File file;
    private List<LogEntry> logEntries;
}
