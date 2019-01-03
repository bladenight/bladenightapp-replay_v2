package de.greencity.bladenightapp.replay.log.local;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;

import de.greencity.bladenightapp.events.Event;
import de.greencity.bladenightapp.procession.Procession;

public abstract class ProcessionStatisticsWriterDeprecated {
    public ProcessionStatisticsWriterDeprecated() {
        super();
    }

    public ProcessionStatisticsWriterDeprecated(Procession procession, Event event) {
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

    protected Event event;
    private static Log log;

    public static void setLog(Log log) {
        ProcessionStatisticsWriterDeprecated.log = log;
    }

    protected static Log getLog() {
        if (log == null)
            setLog(LogFactory.getLog(GnuplotWriter.class));
        return log;
    }

    protected Procession procession;


}