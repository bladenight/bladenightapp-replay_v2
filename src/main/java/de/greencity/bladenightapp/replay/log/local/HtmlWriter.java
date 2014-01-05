package de.greencity.bladenightapp.replay.log.local;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.greencity.bladenightapp.events.Event;
import de.greencity.bladenightapp.replay.log.local.templates.EventProxy;
import de.greencity.bladenightapp.replay.log.local.templates.TemplateProxy;

public class HtmlWriter {
	public HtmlWriter(Map<Event, List<OutputImageFile>> map) {
		this.map = map;
	}

	public void write() throws IOException {
		writeIndex();
		writeEventFiles();
		writeCss();
	}

	public ArrayList<Event> getSortedEventList() {
		Comparator<Event> comparator = new Comparator<Event>() {
			public int compare(Event e1, Event e2) {
				return e1.getStartDate().compareTo(e2.getStartDate());
			}
		};

		ArrayList<Event> list = new ArrayList<Event>(map.keySet());
		Collections.sort(list, comparator);
		return list;
	}

	public SortedSet<OutputImageFile> getSortedOutputImageFiles(Event event) {
		Comparator<OutputImageFile> comparator = new Comparator<OutputImageFile>() {
			public int compare(OutputImageFile e1, OutputImageFile e2) {
				return Integer.valueOf(e1.order).compareTo(e2.order);
			}
		};
		TreeSet<OutputImageFile> treeSet = new TreeSet<OutputImageFile>(comparator);
		treeSet.addAll(map.get(event));
		return treeSet;
	}


	private void writeIndex() throws IOException {

		List<EventProxy> eventProxies = new ArrayList<EventProxy>();

		for (Event event : getSortedEventList()) {
			eventProxies.add(new EventProxy(event));
		}

		TemplateProxy templateProxy = new TemplateProxy("index.ftl.html");
		templateProxy.putData("events", eventProxies);
		templateProxy.generate(new File("index.html"));
	}

	public void writeCss() throws IOException {
		new TemplateProxy("style.ftl.css").generate(new File("style.css"));
	}

	private void writeEventFiles() {
		List<Event> sortedEvents = getSortedEventList();
		for (int index = 0 ; index < sortedEvents.size() ; index ++) {
			writeFileForEvent(sortedEvents, index);
		}
	}

	private void writeFileForEvent(List<Event> events, int index) {
		TemplateProxy templateProxy = new TemplateProxy("event.ftl.html");

		Event currentEvent = events.get(index);
		EventProxy currentEventProxy = new EventProxy(currentEvent);
		templateProxy.putData("currentEvent", currentEventProxy);
		
		if ( index > 0  )
			templateProxy.putData("previousEvent", new EventProxy(events.get(index-1)));
		
		if ( index < events.size() - 1 )
			templateProxy.putData("nextEvent", new EventProxy(events.get(index+1)));
		
		List<String> images = new ArrayList<String>();
		images.add("head-and-tail");
		images.add("number-of-users");
		images.add("speed-on-segments");
		images.add("procession-length");
		images.add("procession-progression-speed");
		images.add("procession-progression-density");
		images.add("waiting-time");
		templateProxy.putData("images", images);
		
		templateProxy.generate(new File(currentEventProxy.getDateIso() + ".html"));
	}

	private Map<Event, List<OutputImageFile>> map;

	private static Log log;

	public static void setLog(Log log) {
		HtmlWriter.log = log;
	}

	protected static Log getLog() {
		if (log == null)
			setLog(LogFactory.getLog(HtmlWriter.class));
		return log;
	}
}
