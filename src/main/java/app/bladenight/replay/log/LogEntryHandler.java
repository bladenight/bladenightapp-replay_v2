package app.bladenight.replay.log;


public interface LogEntryHandler {

    public abstract void handleLogEntry(ParticipanLogFile.LogEntry logEntry) throws Exception;

    public abstract void finish();

}