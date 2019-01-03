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

public class ProcessionLengthWriter extends GnuplotWriter {

    final double maxProcessionLength = 6000.0;

    ProcessionLengthWriter(String baseFilename, Procession procession, Event event) throws IOException {
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
        SegmentedLinearRoute segmentedProcession = new SegmentedLinearRoute(100, maxProcessionLength);
        for (int processionSegment = 0 ; processionSegment < segmentedProcession.getNumberOfSegments() ; processionSegment++) {
            double distanceFromHead = segmentedProcession.getPositionOfSegmentStart(processionSegment);
            double positionOnRoute = procession.getHeadPosition() - distanceFromHead;
            int routeSegment = segmentedLinearRoute.getSegmentForLinearPosition(positionOnRoute);
            Segment segment = statistics.segments[routeSegment];

            double speed = segment.speed;
            if ( Double.isNaN(segment.speed) || Double.isInfinite(segment.speed) || segment.nParticipants <= 0 )
                speed = -1;
            if ( positionOnRoute < procession.getTailPosition() || positionOnRoute > procession.getHeadPosition())
                speed = -1;

            writeDataLine(
                    dateTime + "\t" +
                            convertPositionForOutput(distanceFromHead)+ "\t" +
                            convertSpeedForOutput(speed)
                    );
        }
    }

    @Override
    protected String getGnuplotTemplateName() {
        return "gnuplot-procession-length.tpl";
    }

    @Override
    protected void addOutputImageFilesToThisList(List<OutputImageFile> list) {
        list.add(new OutputImageFile(getPngFile(), "procession-length", 25));
    }

    @Override
    protected void specifyGnuplotCustomFields(Map<String, String> map) {
        map.put("PNG_FILE", getPngFile());
        map.put("MAX_PROCESSION_LENGTH", Double.toString(convertPositionForOutput(maxProcessionLength)));
    }

    private String getPngFile() {
        return baseFilename + ".png";
    }

}


