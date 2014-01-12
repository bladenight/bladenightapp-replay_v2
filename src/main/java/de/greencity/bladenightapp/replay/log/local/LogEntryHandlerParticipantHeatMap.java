package de.greencity.bladenightapp.replay.log.local;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.greencity.bladenightapp.events.Event;
import de.greencity.bladenightapp.replay.log.LogEntryHandler;
import de.greencity.bladenightapp.replay.log.ParticipanLogFile.LogEntry;
import de.greencity.bladenightapp.replay.log.local.templatedata.TemplateProxy;

public class LogEntryHandlerParticipantHeatMap implements LogEntryHandler {

	public LogEntryHandlerParticipantHeatMap(Event event) {
		this.event = event;
	}

	@Override
	public void finish() {
		TemplateProxy templateProxy = new TemplateProxy("heatmap.ftl.js");
		templateProxy.putData("entries", entries);
		templateProxy.generate(new File(event.getStartDateAsString("yyyy-MM-dd") + "-heatmap.js"));
	}

	@Override
	public void handleLogEntry(LogEntry logEntry) throws Exception {
		entries.add(new Entry(logEntry.latitude, logEntry.longitude));
	}

	static public class Entry {
		public Entry(double latitude, double longitude) {
			this.latitude = latitude;
			this.longitude = longitude;
		}
		public double latitude;
		public double longitude;
		public double getLatitude() {
			return latitude;
		}
		public double getLongitude() {
			return longitude;
		}
	}
	
	private List<Entry> entries = new ArrayList<Entry>();
	private Event event;
}

