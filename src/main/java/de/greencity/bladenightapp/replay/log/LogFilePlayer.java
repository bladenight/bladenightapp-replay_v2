package de.greencity.bladenightapp.replay.log;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;

import de.greencity.bladenightapp.replay.log.ParticipanLogFile.LogEntry;
import de.greencity.bladenightapp.time.Sleep;

public class LogFilePlayer {
	public LogFilePlayer(LogEntryHandler logEntryHandler) {
		super();
		this.logEntryHandler = logEntryHandler;
	}

	public void readLogEntries(File file) throws IOException {
		getLog().info("Reading log file...");
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

	public void replay() throws InterruptedException {
		DateTime currentSimulatedTime = null;
		getLog().info("Starting replay...");
		for (LogEntry logEntry : logEntries ) {
			if ( logEntry.dateTime.isBefore(fromDateTime) || logEntry.dateTime.isAfter(toDateTime) )
				continue;

			if ( currentSimulatedTime != null ) {
				if ( logEntry.dateTime.isBefore(currentSimulatedTime))
					getLog().error("Clock skew detected: " + logEntry.dateTime + " < " + currentSimulatedTime);
				else
					Sleep.sleep( (long) (( logEntry.dateTime.getMillis() - currentSimulatedTime.getMillis() ) / timeLapseFactor) );
			}

			currentSimulatedTime = logEntry.dateTime;

			getLog().info("Current simulated time: " + new DateTime(currentSimulatedTime) );

			try {
				logEntryHandler.handleLogEntry(logEntry);
			} catch (Exception e) {
				getLog().error("Handler failed to process log entry " + logEntry, e);
			}
		}
		logEntryHandler.finish();
	}
	
	private double timeLapseFactor = 1.0;
	private DateTime fromDateTime;
	private DateTime toDateTime;
	private List<ParticipanLogFile.LogEntry> logEntries;
	private LogEntryHandler logEntryHandler;


	private Log log;

	public void setLog(Log log) {
		this.log = log;
	}

	protected Log getLog() {
		if (log == null)
			setLog(LogFactory.getLog(LogFilePlayer.class));
		return log;
	}

}