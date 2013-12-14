package de.greencity.bladenightapp.replay.log;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.Seconds;

import de.greencity.bladenightapp.procession.ParticipantInput;
import de.greencity.bladenightapp.procession.Procession;
import de.greencity.bladenightapp.replay.log.ParticipanLogFile.LogEntry;

public class LogEntryHandlerProcession implements LogEntryHandler {

	public LogEntryHandlerProcession(Procession procession) {
		this.procession = procession;
	}

	@Override
	public void handleLogEntry(LogEntry logEntry) throws Exception {
		ParticipantInput participantInput = new ParticipantInput(logEntry.deviceId, true, logEntry.latitude, logEntry.longitude); 
		procession.updateParticipant(participantInput);
		if ( lastPrintTime == null || Seconds.secondsBetween(lastPrintTime, logEntry.dateTime).getSeconds() > 10 ) {
			procession.compute();
			lastPrintTime = logEntry.dateTime; 
			System.out.println("LOG" + "\t" + logEntry.dateTime + "\t" + procession.getTailPosition() + "\t" + procession.getHeadPosition());
		}
	}


	private Procession procession;

	private Log log;
	private DateTime lastPrintTime = null;

	public void setLog(Log log) {
		this.log = log;
	}

	protected Log getLog() {
		if (log == null)
			setLog(LogFactory.getLog(LogEntryHandlerProcession.class));
		return log;
	}

}
