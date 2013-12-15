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
		withinProcession = new FileWriter("log-within-procession.log");
		onRoute = new FileWriter("log-on-route.log");
		headAndTail = new FileWriter("log-head-and-tail.log");
		waitingTime = new FileWriter("log-waiting-time.log");

		transitSegments = new TransitSegment[100];
		for (int i = 0; i < transitSegments.length ; i ++)
			transitSegments[i] = new TransitSegment();
	}

	@Override
	public void finish() {
		try {
			withinProcession.close();
			onRoute.close();
			headAndTail.close();
			finishTransitSegments();
			waitingTime.close();
		} catch (IOException e) {
			getLog().error("Failed to close:" , e);
		}
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
			printHeadAndTail(logEntry.dateTime);
			printStatisticsWithinProcession(logEntry.dateTime);
			handleTransitSegments(logEntry.dateTime);
			System.out.println("Current time: " +  logEntry.dateTime);
		}
	}

	private void handleTransitSegments(DateTime dateTime) {
		SegmentedLinearRoute segmentedLinearRoute = new SegmentedLinearRoute(transitSegments.length, procession.getRoute().getLength());
		double headPos = procession.getHeadPosition();
		int headSegment = segmentedLinearRoute.getSegmentForLinearPosition(headPos);
		double tailPos = procession.getTailPosition();
		int tailSegment = segmentedLinearRoute.getSegmentForLinearPosition(tailPos);
		for (int i = tailSegment ; i <  headSegment; i++) {
			if ( transitSegments[i].firstSeenTimestamp <= 0 ) {
				transitSegments[i].firstSeenTimestamp = dateTime.getMillis();
				System.out.println("firstSeenTimestamp["+i+"] = " + dateTime.getMillis());
			}
			if ( transitSegments[i].lastSeenTimestamp <= 0 || (dateTime.getMillis() - transitSegments[i].lastSeenTimestamp) < 5*60*1000 ) {
				System.out.println("lastSeenTimestamp["+i+"] = " + dateTime.getMillis());
				transitSegments[i].lastSeenTimestamp = dateTime.getMillis();
			}
		}
	}

	private void finishTransitSegments() {
		SegmentedLinearRoute segmentedLinearRoute = new SegmentedLinearRoute(transitSegments.length, procession.getRoute().getLength());
		for (int i = 0 ; i < transitSegments.length ; i++) {
			double pos = segmentedLinearRoute.getPositionOfSegmentStart(i);
			writeLine(waitingTime,
					(long)pos + "\t" +
							((transitSegments[i].lastSeenTimestamp - transitSegments[i].firstSeenTimestamp)/(60*1000.0))
					);
		}
	}


	private void printHeadAndTail(DateTime reportTime) {
		writeLine(headAndTail,
				reportTime + "\t" +
						(long)procession.getTail().getLinearPosition() + "\t" +
						(long)procession.getTail().getLinearSpeed() + "\t" +
						(long)procession.getHead().getLinearPosition() + "\t" +
						(long)procession.getHead().getLinearSpeed()
				);
	}

	private void printStatisticsWithinProcession(DateTime reportTime) {
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
			writeLine(withinProcession,
					reportTime + "\t" +
							(long)(positionInProcession)+ "\t" +
							(long)speed
					);
		}
	}

	private void writeLine(Writer writer, String line) {
		try {
			writer.write(line + "\n");
		} catch (IOException e) {
			getLog().error("Failed to write to file: ", e);
		}
	}

	static class TransitSegment {
		public long firstSeenTimestamp;
		public long lastSeenTimestamp;
	}
	TransitSegment transitSegments[];

	private Procession procession;

	private Log log;
	private DateTime lastPrintTime = null;
	private ControlledClock controlledClock = new ControlledClock();
	private Writer withinProcession;
	private Writer onRoute;
	private Writer headAndTail;
	private Writer waitingTime;

	public void setLog(Log log) {
		this.log = log;
	}

	protected Log getLog() {
		if (log == null)
			setLog(LogFactory.getLog(LogEntryHandlerProcession.class));
		return log;
	}
}

