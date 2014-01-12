package de.greencity.bladenightapp.replay.log.local;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;

import de.greencity.bladenightapp.events.Event;
import de.greencity.bladenightapp.procession.Procession;

public abstract class ProcessionStatisticsWriterNew {
	public ProcessionStatisticsWriterNew(File basePath, Procession procession, Event event) {
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
		ProcessionStatisticsWriterNew.log = log;
	}

	protected static Log getLog() {
		if (log == null)
			setLog(LogFactory.getLog(GnuplotWriter.class));
		return log;
	}

}