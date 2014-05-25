package de.greencity.bladenightapp.replay.log.local;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import de.greencity.bladenightapp.events.Event;
import de.greencity.bladenightapp.math.MedianFinder;
import de.greencity.bladenightapp.procession.Procession;
import de.greencity.bladenightapp.procession.SegmentedLinearRoute;
import de.greencity.bladenightapp.procession.Statistics;
import de.greencity.bladenightapp.replay.log.local.templatedata.TemplateProxy;
import de.greencity.bladenightapp.routes.Route.LatLong;

public class SpeedByCoord extends ProcessionStatisticsWriter {

	SpeedByCoord(File basePath, Procession procession, Event event) throws IOException {
		super(basePath, procession, event);
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

		List<OutputEntry> entries = new ArrayList<OutputEntry>();

		for (int i = 0 ; i < segments.length ; i++) {
			double pos = segmentedLinearRoute.getPositionOfSegmentStart(i);
			LatLong latLong = procession.getRoute().convertLinearPositionToLatLong(pos);
			MedianFinder finder = segments[i].medianFinder;
			if ( finder.getTotalWeight() == 0 || finder.sampleCount() == 0 )
				continue;
			double median = finder.findMedian();
			if ( Double.isInfinite(median))
				continue;
			entries.add(new OutputEntry(latLong.lat, latLong.lon, median));
		}

		TemplateProxy templateProxy = new TemplateProxy("speed-by-coord/speed-by-coord.ftl.json");
		templateProxy.putData("entries", entries);
		String targetFileName = "speed-by-coord.json";
		File targetFile = new File(basePath, targetFileName);
		templateProxy.generate(targetFile);

	}

	static public class OutputEntry {
		public double latitude;
		public double longitude;
		public double speed;
		public OutputEntry(double lat, double lon, double speed) {
			this.latitude = lat;
			this.longitude = lon;
			this.speed = speed;
		}
		public double getLatitude() {
			return latitude;
		}
		public void setLatitude(double latitude) {
			this.latitude = latitude;
		}
		public double getLongitude() {
			return longitude;
		}
		public void setLongitude(double longitude) {
			this.longitude = longitude;
		}
		public double getSpeed() {
			return speed;
		}
		public void setSpeed(double speed) {
			this.speed = speed;
		}
	};

	private static class Segment {
		Segment() {
			medianFinder = new MedianFinder(); 
		}
		public MedianFinder medianFinder;
	}
	private Segment segments[];

}
