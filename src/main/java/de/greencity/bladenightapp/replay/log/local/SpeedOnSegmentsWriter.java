package de.greencity.bladenightapp.replay.log.local;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import de.greencity.bladenightapp.events.Event;
import de.greencity.bladenightapp.math.MedianFinder;
import de.greencity.bladenightapp.procession.Procession;
import de.greencity.bladenightapp.procession.SegmentedLinearRoute;
import de.greencity.bladenightapp.procession.Statistics;

public class SpeedOnSegmentsWriter extends GnuplotWriter {

	SpeedOnSegmentsWriter(String baseFilename, Procession procession, Event event) throws IOException {
		super(baseFilename, procession, event);
	}

	@Override
	public void checkpoint(DateTime dateTime) {
		Statistics statistics = procession.getStatistics();
		initSegmentsIfRequired();
		if (segments==null)
			return;
		for (int i = 0 ; i <  statistics.segments.length; i++) {
			if (statistics.segments[i].nParticipants > 0 )
				segments[i].medianFinder.addWeightedValue(statistics.segments[i].speed, statistics.segments[i].nParticipants);
		}
	}

	private void initSegmentsIfRequired() {
		Statistics statistics = procession.getStatistics();
		if ( statistics == null )
			return;
		if ( segments != null ) {
			if ( segments.length != statistics.segments.length ) {
				getLog().error("Number of segments in statistics changed " + segments.length + " / " + statistics.segments.length);
			}
			return;
		}
		this.segments = new Segment[statistics.segments.length];
		for (int i = 0; i < segments.length ; i ++)
			segments[i] = new Segment();
	}

	@Override
	public void finish() {
		SegmentedLinearRoute segmentedLinearRoute = new SegmentedLinearRoute(segments.length, procession.getRoute().getLength());
		for (int i = 0 ; i < segments.length ; i++) {
			double pos = segmentedLinearRoute.getPositionOfSegmentStart(i);
			if ( segments[i].medianFinder.getTotalWeight() == 0 || segments[i].medianFinder.sampleCount() == 0 )
				continue;
			writeDataLine(
					convertPositionForOutput(pos) + "\t" +
							segments[i].medianFinder.findMedian()
					);
		}
		super.finish();
	}
	
	@Override
	protected String getGnuplotTemplateName() {
		return "gnuplot-speed-on-segments.tpl";
	}

	@Override
	protected void addOutputImageFilesToThisList(List<OutputImageFile> list) {
		list.add(new OutputImageFile(getPngFile(), "waiting-time", 50));
	}

	@Override
	protected void specifyGnuplotCustomFields(Map<String, String> map) {
		map.put("PNG_FILE", getPngFile());
	}

	private String getPngFile() {
		return baseFilename + ".png";
	}


	private static class Segment {
		Segment() {
			medianFinder = new MedianFinder(); 
		}
		public MedianFinder medianFinder;
	}
	private Segment segments[];

}
