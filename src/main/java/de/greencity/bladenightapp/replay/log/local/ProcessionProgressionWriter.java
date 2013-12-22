package de.greencity.bladenightapp.replay.log.local;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import de.greencity.bladenightapp.events.Event;
import de.greencity.bladenightapp.procession.Procession;
import de.greencity.bladenightapp.procession.SegmentedLinearRoute;
import de.greencity.bladenightapp.procession.Statistics;
import de.greencity.bladenightapp.procession.Statistics.Segment;

public class ProcessionProgressionWriter extends GnuplotWriter {

	ProcessionProgressionWriter(String baseFilename, Procession procession, Event event) throws IOException {
		super(baseFilename, procession, event);
	}

	@Override
	public void checkpoint(DateTime dateTime) {
		Statistics statistics = procession.getStatistics();
		if ( statistics == null ) {
			getLog().debug("No statistics available");
			return;
		}
		SegmentedLinearRoute segmentedLinearRoute = new SegmentedLinearRoute(statistics.segments.length, procession.getRoute().getLength());
		for (int routeSegment = 0 ; routeSegment < statistics.segments.length ; routeSegment++) {
			double positionOnRoute = segmentedLinearRoute.getPositionOfSegmentStart(routeSegment);
			Segment segment = statistics.segments[routeSegment];
			double speed = segment.speed;
			double nParticipants = segment.nParticipants; 
			if ( Double.isNaN(speed) || Double.isInfinite(speed) || nParticipants <= 0 ) {
				speed = -1;
				nParticipants = 0;
			}
			if ( segmentedLinearRoute.getPositionOfSegmentEnd(routeSegment) < procession.getTailPosition() ||
					segmentedLinearRoute.getPositionOfSegmentStart(routeSegment) > procession.getHeadPosition()) {
				speed = -1;
				nParticipants = 0;
			}
			writeDataLine(
					dateTime + "\t" +
							convertPositionForOutput(positionOnRoute) + "\t" +
							convertSpeedForOutput(speed) + "\t" +
							nParticipants
					);
		}
	}

	@Override
	protected String getGnuplotTemplateName() {
		return "gnuplot-procession-progression.tpl";
	}

	@Override
	protected void addOutputImageFilesToThisList(List<OutputImageFile> list) {
		list.add(new OutputImageFile(getPngFileSpeed(), "procession-progression-speed", 30));
		list.add(new OutputImageFile(getPngFileDensity(), "procession-progression-density", 35));
	}

	@Override
	protected void specifyGnuplotCustomFields(Map<String, String> map) {
		map.put("PNG_FILE_SPEED", getPngFileDensity());
		map.put("PNG_FILE_DENSITY", getPngFileSpeed());
	}

	private String getPngFileDensity() {
		return baseFilename + "-density.png";
	}

	private String getPngFileSpeed() {
		return baseFilename + "-speed.png";
	}

}

