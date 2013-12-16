package de.greencity.bladenightapp.replay.log.local;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;

import de.greencity.bladenightapp.procession.Procession;
import de.greencity.bladenightapp.procession.SegmentedLinearRoute;
import de.greencity.bladenightapp.procession.Statistics;
import de.greencity.bladenightapp.procession.Statistics.Segment;

public class ProcessionLengthWriter extends StatisticsWriter {

	final double maxProcessionLength = 6000.0;
	
	ProcessionLengthWriter(String baseFilename, Procession procession) throws IOException {
		super(baseFilename, procession);
	}

	@Override
	public void checkpoint(DateTime dateTime) {
		Statistics statistics = procession.getStatistics();
		SegmentedLinearRoute segmentedLinearRoute = new SegmentedLinearRoute(statistics.segments.length, procession.getRoute().getLength());
		SegmentedLinearRoute segmentedProcession = new SegmentedLinearRoute(100, maxProcessionLength);
		for (int processionSegment = 0 ; processionSegment < segmentedProcession.getNumberOfSegments() ; processionSegment++) {
			double positionInProcession = segmentedProcession.getPositionOfSegmentStart(processionSegment);
			int routeSegment = segmentedLinearRoute.getSegmentForLinearPosition(procession.getTailPosition() + positionInProcession);
			Segment segment = statistics.segments[routeSegment];

			double speed = segment.speed;
			if ( Double.isNaN(segment.speed) || Double.isInfinite(segment.speed) || segment.nParticipants <= 0 )
				speed = -1;
			writeDataLine(
					dateTime + "\t" +
							convertPositionForOutput(positionInProcession)+ "\t" +
							convertSpeedForOutput(speed)
					);
		}
	}

	@Override
	protected String getGnuplotTemplateName() {
		return "gnuplot-procession-length.tpl";
	}

	@Override
	protected Map<String, String> getGnuplotCustomFields() {
		Map<String, String> customFields = new HashMap<String, String>();
		customFields.put("MAX_PROCESSION_LENGTH", Double.toString(convertPositionForOutput(maxProcessionLength)));
		return customFields;
	}

}


