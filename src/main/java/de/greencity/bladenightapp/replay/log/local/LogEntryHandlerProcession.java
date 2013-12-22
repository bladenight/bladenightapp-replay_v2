package de.greencity.bladenightapp.replay.log.local;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.Seconds;

import de.greencity.bladenightapp.events.Event;
import de.greencity.bladenightapp.procession.ParticipantInput;
import de.greencity.bladenightapp.procession.Procession;
import de.greencity.bladenightapp.procession.tasks.ParticipantCollector;
import de.greencity.bladenightapp.replay.log.LogEntryHandler;
import de.greencity.bladenightapp.replay.log.ParticipanLogFile.LogEntry;
import de.greencity.bladenightapp.routes.Route;
import de.greencity.bladenightapp.time.ControlledClock;

public class LogEntryHandlerProcession implements LogEntryHandler {

	public LogEntryHandlerProcession(String filePrefix, Route route, Event event) throws IOException {
		procession = new Procession(controlledClock);
		procession.setRoute(route);

		this.writers = new ArrayList<ProcessionStatisticsWriter>();
		// this.writers.add(new HeadAndTailWriter(filePrefix + "-head-and-tail", procession, event));
		// this.writers.add(new ProcessionLengthWriter(filePrefix + "-procession-length", procession, event));
		// this.writers.add(new WaitingTimeWriter(filePrefix + "-waiting-time", procession, event));
		// this.writers.add(new ProcessionProgressionWriter(filePrefix + "-procession-progression", procession, event));
		// this.writers.add(new NumberOfUsersWriter(filePrefix + "-users", procession, event));
		this.writers.add(new JavascriptWriter(procession, event));
	}

	@Override
	public void finish() {
		for (ProcessionStatisticsWriter writer : this.writers) {
			writer.finish();
			// TODO refactor
			if ( writer instanceof GnuplotWriter )
				((GnuplotWriter)writer).addOutputImageFilesToThisList(outputImageFileList);
			// System.out.println("size="+outputImageFileList.size());
		}
	}

	@Override
	public void handleLogEntry(LogEntry logEntry) throws Exception {
		controlledClock.set(logEntry.dateTime.getMillis());

		ParticipantInput participantInput = new ParticipantInput(logEntry.deviceId, true, logEntry.latitude, logEntry.longitude); 
		procession.updateParticipant(participantInput);

		ParticipantCollector collector = createCollector(procession);
		if ( lastPrintTime == null || Seconds.secondsBetween(lastPrintTime, logEntry.dateTime).getSeconds() > 30 ) {
			System.out.println("Checkpoint at: " +  logEntry.dateTime);
			collector.collect();
			procession.compute();
			lastPrintTime = logEntry.dateTime;
			for (ProcessionStatisticsWriter writer : this.writers) {
				writer.checkpoint(logEntry.dateTime);
			}

		}
	}

	private ParticipantCollector createCollector(Procession procession) {
		long maxAbsoluteAge 			= 30000;
		double maxRelativeAgeFactor 	= 5.0;

		ParticipantCollector collector = new ParticipantCollector(procession);
		collector.setMaxAbsoluteAge(maxAbsoluteAge);
		collector.setMaxRelativeAgeFactor(maxRelativeAgeFactor);

		collector.setClock(controlledClock);

		return collector;
	}

	public List<OutputImageFile> getOutputImageFileList() {
		return outputImageFileList;
	}


	private Procession procession;

	private Log log;
	private DateTime lastPrintTime = null;
	private ControlledClock controlledClock = new ControlledClock();
	private List<ProcessionStatisticsWriter> writers;
	private List<OutputImageFile> outputImageFileList = new ArrayList<OutputImageFile>();

	public void setLog(Log log) {
		this.log = log;
	}

	protected Log getLog() {
		if (log == null)
			setLog(LogFactory.getLog(LogEntryHandlerProcession.class));
		return log;
	}

}

