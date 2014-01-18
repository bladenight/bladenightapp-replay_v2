package de.greencity.bladenightapp.replay.log.local;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.googlecode.jatl.Html;

import de.greencity.bladenightapp.events.Event;
import de.greencity.bladenightapp.replay.log.local.templatedata.EventProxy;
import de.greencity.bladenightapp.replay.log.local.templatedata.TemplateProxy;

public class HtmlWriter {
	public HtmlWriter(File basePath, Map<Event, List<OutputImageFile>> map) {
		this.map = map;
		this.basePath = basePath;
		basePath.mkdirs();
	}

	public void write() throws IOException {
		writeIndex();
		writeEventFiles();
		writeStaticFiles();
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

		TemplateProxy templateProxy = new TemplateProxy("html/index.ftl.html");
		templateProxy.putData("events", eventProxies);
		templateProxy.generate(newOutputFile("index.html"));
	}

	public void writeStaticFiles() throws IOException {
		Utils.copyResourceTo("/html/style.css", 										newOutputFile("style.css"));
		Utils.copyResourceTo("/jslibs/highcharts-helper.js", 							newOutputFile("highcharts-helper.js"));
		Utils.copyResourceTo("/jslibs/highcharts.js", 									newOutputFile("highcharts.js"));
		Utils.copyResourceTo("/jslibs/jquery-1.10.2.min.js", 							newOutputFile("jquery-1.10.2.min.js"));
		Utils.copyResourceTo("/jslibs/maplabel-compiled.js", 							newOutputFile("maplabel-compiled.js"));
		Utils.copyResourceTo("/users-by-time/users-by-time.js", 						newOutputFile("users-by-time.js"));
		Utils.copyResourceTo("/length-by-time/length-by-time.js", 						newOutputFile("length-by-time.js"));
		Utils.copyResourceTo("/speed-by-pos/speed-by-pos.js", 							newOutputFile("speed-by-pos.js"));
		Utils.copyResourceTo("/waiting-time-by-pos/waiting-time-by-pos.js", 			newOutputFile("waiting-time-by-pos.js"));
		Utils.copyResourceTo("/head-and-tail-pos-by-time/head-and-tail-pos-by-time.js", newOutputFile("head-and-tail-pos-by-time.js"));
	}


	private void writeEventFiles() {
		List<Event> sortedEvents = getSortedEventList();
		for (int index = 0 ; index < sortedEvents.size() ; index ++) {
			writeFileForEvent(sortedEvents, index);
		}
	}

	private void writeFileForEvent(List<Event> events, int index) {
		TemplateProxy templateProxy = new TemplateProxy("html/event.ftl.html");

		Event currentEvent = events.get(index);
		EventProxy currentEventProxy = new EventProxy(currentEvent);
		templateProxy.putData("currentEvent", currentEventProxy);

		if ( index > 0  )
			templateProxy.putData("previousEvent", new EventProxy(events.get(index-1)));

		if ( index < events.size() - 1 )
			templateProxy.putData("nextEvent", new EventProxy(events.get(index+1)));

		List<GraphItem> graphItems = new ArrayList<GraphItem>();
		graphItems.add(new GraphItem().div("head-and-tail-by-time")); 
		graphItems.add(new GraphItem().div("length-by-time")); 
		graphItems.add(new GraphItem().div("users-by-time")); 
		graphItems.add(new GraphItem().div("speed-by-pos")); 
		graphItems.add(new GraphItem().div("waiting-time-by-pos")); 
		graphItems.add(new GraphItem().img("procession-progression-speed")); 
		graphItems.add(new GraphItem().img("procession-progression-density")); 
		templateProxy.putData("graphItems", graphItems);

		templateProxy.generate(newOutputFile(currentEventProxy.getDateIso() + "/index.html"));
	}

	public static class GraphItem {
		public String getHtml() {
			return this.html;
		}
		public String getId() {
			return this.id;
		}
		public GraphItem div(final String id) {
			StringWriter writer = new StringWriter();

			new Html(writer) {{
				div().attr("id", id, "style", "width: 1000px; height: 700px;");
				done();
			}
			};
			this.id = id;
			this.html = writer.getBuffer().toString();
			return this;
		}
		private GraphItem img(final String id) {
			StringWriter writer = new StringWriter();

			new Html(writer) {{
				img().attr("id", id, "href", id+".png");
				done();
			}
			};
			this.id = id;
			this.html = writer.getBuffer().toString();
			return this;
		}
		private String id;
		private String html;
	}
	

	private File newOutputFile(String name) {
		return new File(basePath, name);
	}

	private Map<Event, List<OutputImageFile>> map;

	private File basePath;

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
