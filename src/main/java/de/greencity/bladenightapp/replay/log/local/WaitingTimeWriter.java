package de.greencity.bladenightapp.replay.log.local;

import java.io.IOException;

import org.joda.time.DateTime;

import de.greencity.bladenightapp.events.Event;
import de.greencity.bladenightapp.procession.Procession;
import de.greencity.bladenightapp.procession.SegmentedLinearRoute;

public class WaitingTimeWriter extends StatisticsWriter {

	WaitingTimeWriter(String baseFilename, Procession procession, Event event) throws IOException {
		super(baseFilename, procession, event);
		this.segments = new Segment[100];
		for (int i = 0; i < segments.length ; i ++)
			segments[i] = new Segment();
	}

	@Override
	public void checkpoint(DateTime dateTime) {
		SegmentedLinearRoute segmentedLinearRoute = new SegmentedLinearRoute(segments.length, procession.getRoute().getLength());
		double headPos = procession.getHeadPosition();
		int headSegment = segmentedLinearRoute.getSegmentForLinearPosition(headPos);
		double tailPos = procession.getTailPosition();
		int tailSegment = segmentedLinearRoute.getSegmentForLinearPosition(tailPos);
		for (int i = tailSegment ; i <  headSegment; i++) {
			if ( segments[i].firstSeenTimestamp <= 0 ) {
				segments[i].firstSeenTimestamp = dateTime.getMillis();
			}
			if ( segments[i].lastSeenTimestamp <= 0 || (dateTime.getMillis() - segments[i].lastSeenTimestamp) < 5*60*1000 ) {
				segments[i].lastSeenTimestamp = dateTime.getMillis();
			}
		}
	}

	@Override
	public void finish() {
		SegmentedLinearRoute segmentedLinearRoute = new SegmentedLinearRoute(segments.length, procession.getRoute().getLength());
		for (int i = 0 ; i < segments.length ; i++) {
			double pos = segmentedLinearRoute.getPositionOfSegmentStart(i);
			writeDataLine(
					convertPositionForOutput(pos) + "\t" +
							((segments[i].lastSeenTimestamp - segments[i].firstSeenTimestamp)/(60*1000.0))
					);
		}
		super.finish();
	}
	
	@Override
	protected String getGnuplotTemplateName() {
		return "gnuplot-waiting-time.tpl";
	}

	private static class Segment {
		public long firstSeenTimestamp;
		public long lastSeenTimestamp;
	}
	private Segment segments[];

}
