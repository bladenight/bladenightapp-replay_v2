package app.bladenight.replay.log.local;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import app.bladenight.replay.log.LogEntryHandler;
import app.bladenight.replay.log.ParticipanLogFile;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.Seconds;

import app.bladenight.common.events.Event;
import app.bladenight.common.procession.ParticipantInput;
import app.bladenight.common.procession.Procession;
import app.bladenight.common.procession.tasks.ParticipantCollector;
import app.bladenight.common.routes.Route;
import app.bladenight.common.time.ControlledClock;

public class LogEntryHandlerProcession implements LogEntryHandler {

    public LogEntryHandlerProcession(File basePath, Route route, Event event) throws IOException {
        procession = new Procession(controlledClock);
        procession.setRoute(route);

        basePath.mkdirs();
        this.writers = new ArrayList<ProcessionStatisticsWriter>();
//      this.writers.add(new ProcessionLengthWriter(filePrefix + "-procession-length", procession, event));
//      this.writers.add(new ProcessionProgressionWriter(filePrefix + "-procession-progression", procession, event));
        this.writers.add(new UsersByTime(basePath, procession, event));
        this.writers.add(new JavascriptRouteWriter(basePath, procession, event));
        this.writers.add(new HeadAndTailPosByTime(basePath, procession, event));
        this.writers.add(new SpeedByPos(basePath, procession, event));
        this.writers.add(new SpeedByCoord(basePath, procession, event));
        this.writers.add(new WaitingTimeByPos(basePath, procession, event));
        this.writers.add(new LengthByTime(basePath, procession, event));
    }

    @Override
    public void finish() {
        for (ProcessionStatisticsWriter writer : this.writers) {
            writer.finish();
            // TODO refactor
//          if ( writer instanceof GnuplotWriter )
//              ((GnuplotWriter)writer).addOutputImageFilesToThisList(outputImageFileList);
            // System.out.println("size="+outputImageFileList.size());
        }
    }

    @Override
    public void handleLogEntry(ParticipanLogFile.LogEntry logEntry) throws Exception {
        controlledClock.set(logEntry.dateTime.getMillis());

        ParticipantInput participantInput = new ParticipantInput(logEntry.deviceId, true, logEntry.latitude, logEntry.longitude);
        procession.updateParticipant(participantInput);

        ParticipantCollector collector = createCollector(procession);
        if ( lastPrintTime == null || Seconds.secondsBetween(lastPrintTime, logEntry.dateTime).getSeconds() > 30 ) {
            System.out.println("Checkpoint at: " +  logEntry.dateTime);
            collector.collect();
            procession.compute();
            lastPrintTime = logEntry.dateTime;
            for (ProcessionStatisticsWriter writer : this.writers) {
                writer.checkpoint(logEntry.dateTime);
            }

        }
    }

    private ParticipantCollector createCollector(Procession procession) {
        long maxAbsoluteAge             = 30000;
        double maxRelativeAgeFactor     = 5.0;

        ParticipantCollector collector = new ParticipantCollector(procession);
        collector.setMaxAbsoluteAge(maxAbsoluteAge);
        collector.setMaxRelativeAgeFactor(maxRelativeAgeFactor);

        collector.setClock(controlledClock);

        return collector;
    }

    public List<OutputImageFile> getOutputImageFileList() {
        return outputImageFileList;
    }


    private Procession procession;

    private Log log;
    private DateTime lastPrintTime = null;
    private ControlledClock controlledClock = new ControlledClock();
    private List<ProcessionStatisticsWriter> writers;
    private List<OutputImageFile> outputImageFileList = new ArrayList<OutputImageFile>();

    public void setLog(Log log) {
        this.log = log;
    }

    protected Log getLog() {
        if (log == null)
            setLog(LogFactory.getLog(LogEntryHandlerProcession.class));
        return log;
    }

}

