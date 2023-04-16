package app.bladenight.replay.log.local;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;

import app.bladenight.common.events.Event;
import app.bladenight.common.procession.Procession;

public abstract class ProcessionStatisticsWriter {
    public ProcessionStatisticsWriter(File basePath, Procession procession, Event event) {
        this.basePath = basePath;
        this.procession = procession;
        this.event = event;
    }

    public abstract void checkpoint(DateTime dateTime);

    public abstract void finish();

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    protected File newOutputFile(String name) {
        return new File(basePath, name);
    }


    protected Event event;
    protected File basePath;
    protected Procession procession;


    private static Log log;

    public static void setLog(Log log) {
        ProcessionStatisticsWriter.log = log;
    }

    protected static Log getLog() {
        if (log == null)
            setLog(LogFactory.getLog(GnuplotWriter.class));
        return log;
    }

}