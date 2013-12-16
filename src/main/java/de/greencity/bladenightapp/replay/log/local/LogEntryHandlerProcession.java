package de.greencity.bladenightapp.replay.log.local;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.Seconds;

import de.greencity.bladenightapp.procession.ParticipantInput;
import de.greencity.bladenightapp.procession.Procession;
import de.greencity.bladenightapp.replay.log.LogEntryHandler;
import de.greencity.bladenightapp.replay.log.ParticipanLogFile.LogEntry;
import de.greencity.bladenightapp.routes.Route;
import de.greencity.bladenightapp.time.ControlledClock;

public class LogEntryHandlerProcession implements LogEntryHandler {

	public LogEntryHandlerProcession(String filePrefix, Route route) throws IOException {
		procession = new Procession(controlledClock);
		procession.setRoute(route);

		this.writers = new ArrayList<StatisticsWriter>();
		this.writers.add(new HeadAndTailWriter(filePrefix + "-head-and-tail", procession));
		this.writers.add(new ProcessionLengthWriter(filePrefix + "-procession-length", procession));
		this.writers.add(new WaitingTimeWriter(filePrefix + "-waiting-time", procession));
		this.writers.add(new ProcessionProgressionWriter(filePrefix + "-procession-progression", procession));
	}

	@Override
	public void finish() {
		for (StatisticsWriter writer : this.writers) {
			writer.finish();
		}
	}

	@Override
	public void handleLogEntry(LogEntry logEntry) throws Exception {
		controlledClock.set(logEntry.dateTime.getMillis());

		ParticipantInput participantInput = new ParticipantInput(logEntry.deviceId, true, logEntry.latitude, logEntry.longitude); 
		procession.updateParticipant(participantInput);

		if ( lastPrintTime == null || Seconds.secondsBetween(lastPrintTime, logEntry.dateTime).getSeconds() > 30 ) {
			System.out.println("Checkpoint at: " +  logEntry.dateTime);

			procession.compute();
			lastPrintTime = logEntry.dateTime;
			for (StatisticsWriter writer : this.writers) {
				writer.checkpoint(logEntry.dateTime);
			}

		}
	}


	private Procession procession;

	private Log log;
	private DateTime lastPrintTime = null;
	private ControlledClock controlledClock = new ControlledClock();
	List<StatisticsWriter> writers;

	public void setLog(Log log) {
		this.log = log;
	}

	protected Log getLog() {
		if (log == null)
			setLog(LogFactory.getLog(LogEntryHandlerProcession.class));
		return log;
	}
}

