package de.greencity.bladenightapp.replay.log.local;

import java.io.IOException;

import org.joda.time.DateTime;

import de.greencity.bladenightapp.procession.Procession;
import de.greencity.bladenightapp.procession.SegmentedLinearRoute;
import de.greencity.bladenightapp.procession.Statistics;
import de.greencity.bladenightapp.procession.Statistics.Segment;

public class ProcessionProgressionWriter extends StatisticsWriter {

	ProcessionProgressionWriter(String baseFilename, Procession procession) throws IOException {
		super(baseFilename, procession);
	}

	@Override
	public void checkpoint(DateTime dateTime) {
		Statistics statistics = procession.getStatistics();
		SegmentedLinearRoute segmentedLinearRoute = new SegmentedLinearRoute(statistics.segments.length, procession.getRoute().getLength());
		for (int routeSegment = 0 ; routeSegment < statistics.segments.length ; routeSegment++) {
			double positionOnRoute = segmentedLinearRoute.getPositionOfSegmentStart(routeSegment);
			Segment segment = statistics.segments[routeSegment];
			double speed = segment.speed;
			if ( Double.isNaN(segment.speed) || Double.isInfinite(segment.speed) || segment.nParticipants <= 0 )
				speed = -1;
			writeDataLine(
					dateTime + "\t" +
							convertPositionForOutput(positionOnRoute)+ "\t" +
							convertSpeedForOutput(speed)
					);
		}
	}

	@Override
	protected String getGnuplotTemplateName() {
		return "gnuplot-procession-progression.tpl";
	}

}

