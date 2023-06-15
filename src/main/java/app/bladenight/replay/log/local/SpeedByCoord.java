package app.bladenight.replay.log.local;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import app.bladenight.common.events.Event;
import app.bladenight.common.math.MedianFinder;
import app.bladenight.common.procession.Procession;
import app.bladenight.common.procession.SegmentedLinearRoute;
import app.bladenight.common.procession.Statistics;
import app.bladenight.replay.log.local.templatedata.TemplateProxy;
import app.bladenight.common.routes.Route.LatLong;

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

            MedianFinder finder = segments[i].medianFinder;
            if ( finder.getTotalWeight() == 0 || finder.sampleCount() == 0 )
                continue;
            double median = finder.findMedian();
            if ( Double.isInfinite(median))
                continue;

            double positionStart = segmentedLinearRoute.getPositionOfSegmentStart(i);
            double positionEnd = segmentedLinearRoute.getPositionOfSegmentEnd(i);

            List<LatLong> list = procession.getRoute().getPartialRoute(positionStart, positionEnd);
            int upTo = ( i == segments.length - 1 ? list.size() : list.size() - 1 );
            for (int listIndex = 0 ; listIndex < upTo ; listIndex ++) {
                LatLong latLong = list.get(listIndex);
                entries.add(new OutputEntry(latLong.lat, latLong.lon, median));
            }

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
