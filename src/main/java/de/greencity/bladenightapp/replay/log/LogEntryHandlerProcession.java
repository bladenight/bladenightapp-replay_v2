package de.greencity.bladenightapp.replay.log;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.Seconds;

import de.greencity.bladenightapp.procession.ParticipantInput;
import de.greencity.bladenightapp.procession.Procession;
import de.greencity.bladenightapp.procession.SegmentedLinearRoute;
import de.greencity.bladenightapp.procession.Statistics;
import de.greencity.bladenightapp.procession.Statistics.Segment;
import de.greencity.bladenightapp.replay.log.ParticipanLogFile.LogEntry;
import de.greencity.bladenightapp.routes.Route;
import de.greencity.bladenightapp.time.ControlledClock;

public class LogEntryHandlerProcession implements LogEntryHandler {

	public LogEntryHandlerProcession(Route route) throws IOException {
		procession = new Procession(controlledClock);
		procession.setRoute(route);
		writer = new FileWriter("file.txt");
	}

	@Override
	public void handleLogEntry(LogEntry logEntry) throws Exception {
		ParticipantInput participantInput = new ParticipantInput(logEntry.deviceId, true, logEntry.latitude, logEntry.longitude); 
		procession.updateParticipant(participantInput);
		controlledClock.set(logEntry.dateTime.getMillis());
		// System.out.println("Clock set to " +  logEntry.dateTime.getMillis());
		if ( lastPrintTime == null || Seconds.secondsBetween(lastPrintTime, logEntry.dateTime).getSeconds() > 30 ) {
			procession.compute();
			lastPrintTime = logEntry.dateTime; 
			// writeLine("LOG" + "\t" + logEntry.dateTime + "\t" + (long)procession.getTailPosition() + "\t" + (long)procession.getHeadPosition());
			printStatistics(logEntry.dateTime);
		}
	}


	private void printStatistics(DateTime reportTime) {
		Statistics statistics = procession.getStatistics();
		SegmentedLinearRoute segmentedLinearRoute = new SegmentedLinearRoute(statistics.segments.length, procession.getRoute().getLength());
		SegmentedLinearRoute segmentedProcession = new SegmentedLinearRoute(100, 5000.0);
		for (int processionSegment = 0 ; processionSegment < segmentedProcession.getNumberOfSegments() ; processionSegment++) {
			double positionInProcession = segmentedProcession.getPositionOfSegmentStart(processionSegment);
			int routeSegment = segmentedLinearRoute.getSegmentForLinearPosition(procession.getTailPosition() + positionInProcession);
			Segment segment = statistics.segments[routeSegment];

			double speed = segment.speed;
			if ( Double.isNaN(segment.speed) || Double.isInfinite(segment.speed) || segment.nParticipants <= 0 )
				speed = -1;
			writeLine("LOG" + "\t" +
					reportTime + "\t" +
					(long)(positionInProcession)+ "\t" +
					(long)speed + "\t" +
					(long)segmentedProcession.getPositionOfSegmentStart(routeSegment) + "\t" +
					(long)procession.getTailPosition()
					);
		}
	}

	private void printStatisticsBck1(DateTime reportTime) {
		Statistics statistics = procession.getStatistics();
		SegmentedLinearRoute segmentedLinearRoute = new SegmentedLinearRoute(statistics.segments.length, procession.getRoute().getLength());
		int n = 0;
		for (Segment segment : statistics.segments) {
			double speed = segment.speed;
			if ( Double.isNaN(segment.speed) || Double.isInfinite(segment.speed) || segment.nParticipants <= 0 )
				speed = -1;
			if ( segmentedLinearRoute.getPositionOfSegmentEnd(n) < procession.getTailPosition())
				speed = -1;
			if ( segmentedLinearRoute.getPositionOfSegmentStart(n) > procession.getHeadPosition())
				speed = -1;
			double pos = procession.getHeadPosition() - segmentedLinearRoute.getPositionOfSegmentStart(n);
			writeLine("LOG" + "\t" +
					reportTime + "\t" +
					(long)(pos)+ "\t" +
					(long)speed + "\t" +
					(long)segmentedLinearRoute.getPositionOfSegmentStart(n) + "\t" +
					(long)procession.getTailPosition()
					);
			n++;
		}
	}

	private void writeLine(String line) {
		try {
			writer.write(line + "\n");
			writer.flush();
		} catch (IOException e) {
			getLog().error("Failed to write to file: ", e);
		}
		System.out.println(line);
	}
	private Procession procession;

	private Log log;
	private DateTime lastPrintTime = null;
	private ControlledClock controlledClock = new ControlledClock();
	private Writer writer;

	public void setLog(Log log) {
		this.log = log;
	}

	protected Log getLog() {
		if (log == null)
			setLog(LogFactory.getLog(LogEntryHandlerProcession.class));
		return log;
	}

}

