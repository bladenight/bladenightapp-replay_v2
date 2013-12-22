package de.greencity.bladenightapp.replay.log.local;

import org.joda.time.DateTime;

import de.greencity.bladenightapp.events.Event;
import de.greencity.bladenightapp.math.MedianFinder;
import de.greencity.bladenightapp.math.MedianFinder.WeightedValue;
import de.greencity.bladenightapp.procession.Procession;
import de.greencity.bladenightapp.procession.SegmentedLinearRoute;
import de.greencity.bladenightapp.procession.Statistics;

public class JavascriptWriter extends ProcessionStatisticsWriter {

	public JavascriptWriter(Procession procession, Event event) {
		super(procession, event);
	}

	@Override
	public void checkpoint(DateTime dateTime) {
		Statistics statistics = procession.getStatistics();
		if (statistics == null || statistics.segments.length == 0)
			return;
		if (medianFinderBySegment == null)
			init(statistics.segments.length);
		if (statistics.segments.length != medianFinderBySegment.length) {
			getLog().error("JavascriptWriter: fatal Number of segment in the procession statistics changed");
			return;
		}
		SegmentedLinearRoute segmentedLinearRoute = new SegmentedLinearRoute(statistics.segments.length, procession.getRoute().getLength());
		for ( int i = 0 ; i < medianFinderBySegment.length ; i++ ) {
			
			double speed = statistics.segments[i].speed;
			double nParticipants = statistics.segments[i].nParticipants; 
			if ( Double.isNaN(speed) || Double.isInfinite(speed) || nParticipants <= 0 ) {
				continue;
			}
			if ( segmentedLinearRoute.getPositionOfSegmentEnd(i) < procession.getTailPosition() ||
					segmentedLinearRoute.getPositionOfSegmentStart(i) > procession.getHeadPosition()) {
				continue;
			}
			medianFinderBySegment[i].addWeightedValue(new WeightedValue(speed, nParticipants));
		}
	}

	@Override
	public void finish() {
		for ( int i = 0 ; i < this.medianFinderBySegment.length ; i ++ )
			if ( this.medianFinderBySegment[i].sampleCount() == 0 )
				System.out.println(i + "\t-");
			else
				System.out.println(i + "\t" + medianFinderBySegment[i].findMedian());
	}

	private void init(int n) {
		this.medianFinderBySegment = new MedianFinder[n];
		for (int i = 0; i < medianFinderBySegment.length ; i ++) {
			medianFinderBySegment[i] = new MedianFinder();
		}
	}


	private MedianFinder[] medianFinderBySegment;
}
