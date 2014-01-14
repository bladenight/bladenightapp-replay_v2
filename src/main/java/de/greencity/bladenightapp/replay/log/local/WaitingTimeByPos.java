package de.greencity.bladenightapp.replay.log.local;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import de.greencity.bladenightapp.events.Event;
import de.greencity.bladenightapp.procession.Procession;
import de.greencity.bladenightapp.procession.SegmentedLinearRoute;
import de.greencity.bladenightapp.replay.log.local.templatedata.TemplateProxy;

public class WaitingTimeByPos extends ProcessionStatisticsWriterNew {

	WaitingTimeByPos(File basePath, Procession procession, Event event) throws IOException {
		super(basePath, procession, event);
	}

	@Override
	public void checkpoint(DateTime dateTime) {
		initSegmentsIfRequired();

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
		List<OutputEntry> entries = new ArrayList<OutputEntry>();
		for (int i = 0 ; i < segments.length ; i++) {
			double pos = segmentedLinearRoute.getPositionOfSegmentStart(i);
			double waitingTime = ((segments[i].lastSeenTimestamp - segments[i].firstSeenTimestamp)/(60*1000.0));
			entries.add(new OutputEntry(pos, waitingTime));
		}

		TemplateProxy templateProxy = new TemplateProxy("waiting-time-by-pos/waiting-time-by-pos.ftl.json");
		templateProxy.putData("entries", entries);
		String targetFileName = "waiting-time-by-pos.json";
		File targetFile = new File(basePath, targetFileName);
		templateProxy.generate(targetFile);
	}
	
	private void initSegmentsIfRequired() {
		if ( segments != null )
			return;
		this.segments = new Segment[200];
		for (int i = 0; i < segments.length ; i ++)
			segments[i] = new Segment();
	}

	static public class OutputEntry {
		public OutputEntry(double position, double waitingTime) {
			this.position = position;
			this.waitingTime = waitingTime;
		}
		public double position;
		public double waitingTime;
		public double getPosition() {
			return position;
		}
		public double getWaitingTime() {
			return waitingTime;
		} 
	};

	private static class Segment {
		public long firstSeenTimestamp;
		public long lastSeenTimestamp;
	}
	private Segment segments[];

}
