package de.greencity.bladenightapp.replay.log;

import de.greencity.bladenightapp.replay.log.ParticipanLogFile.LogEntry;


public interface LogEntryHandler {

    public abstract void handleLogEntry(LogEntry logEntry) throws Exception;

    public abstract void finish();

}