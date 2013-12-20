package de.greencity.bladenightapp.replay.log.local;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import de.greencity.bladenightapp.events.Event;

public class HtmlWriter {
	public HtmlWriter(Map<Event, List<OutputImageFile>> map) {
		this.map = map;
	}

	public void write() throws IOException {
		writeIndex();
		// writeCss();
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
		FileWriter out =  new FileWriter("index.html");
		out.write("<html>");
		out.write( "<!doctype html>\n" );
		out.write( "<html lang='en'>\n" );

		out.write( "<head>\n" );
		out.write( "<meta charset='utf-8'>\n" );
		out.write( "<title>Index</title>\n" );
		out.write( "<link rel=\"stylesheet\" href=\"style.css\">\n" );
		out.write( "</head>\n\n" );

		out.write( "<body>\n" );
		out.write( "<table id=\"events-table\" >\n" );
		out.write( "<thead>\n" );
		out.write( "<tr>\n" );
		out.write( "<td>Termin</td>\n" );
		out.write( "<td>Strecke</td>\n" );
		out.write( "<td>Skaters</td>\n" );
		out.write( "</tr>\n" );
		out.write( "</thead>\n" );
		
		ArrayList<Event> list = getSortedEventList();
		for ( int index = 0 ; index < list.size() ; index++ ) {
			Event event = list.get(index);
			String htmlFileName = getHtmlFileNameForEvent(event);
			String startTd = "<td><a href=\"" + htmlFileName + "\">";  
			String endTd = "</a></td>\n";  
			out.write( "<tr>\n");
			out.write( startTd + getStartDateAsString(event) + endTd );
			out.write( startTd + event.getRouteName() + endTd );
			out.write( startTd + event.getParticipants() + endTd );
			out.write( "</tr>\n" );
			writeFileForEvent(list, index);
		}
		out.write( "</table>\n" );
		out.write( "</body>\n\n" );

		out.write( "</html>" );
		out.close();
	}

	//	public void writeCss() throws IOException {
	//		FileWriter out =  new FileWriter("style.css");
	//		out.write("table,th,td { border:1px solid black; }");
	//		out.close();
	//	}

	private String getHtmlLinkTo(Event event, String tag, String text) {
		if ( text == null )
			text = getStartDateAsString(event);
		if ( tag  == null ) 
			tag = "";
		if ( tag.length() > 0)
			tag = "#" + tag;
		return "<a href=\"" + getHtmlFileNameForEvent(event) + tag + "\">" + text + "</a>";
	}
	
	private void writeFileForEvent(ArrayList<Event> list, int index) throws IOException {
		Event event = list.get(index);
		// System.out.println(getHtmlFileNameForEvent(event));
		FileWriter out =  new FileWriter(getHtmlFileNameForEvent(event));
		out.write("<html>");
		out.write( "<!doctype html>\n" );
		out.write( "<html lang='en'>\n" );

		out.write( "<head>\n" );
		out.write( "<meta charset='utf-8'>\n" );
		out.write( "<title>Index</title>\n" );
		out.write( "<link rel=\"stylesheet\" href=\"style.css\">\n" );
		out.write( "</head>\n\n" );

		out.write( "<body>\n" );
		out.write( "<a href=\"index.html\">Zurueck zum Index</a>\n" );
		out.write( "<table>\n" );
		for ( OutputImageFile imageFile : getSortedOutputImageFiles(event)) {
			out.write( "<tr>\n");
			if ( index > 0 ) {
				out.write( "<td>" + getHtmlLinkTo(list.get(index-1), imageFile.tag, null ) + "</td>");
			}
			else {
				out.write( "<td><div style=\"width: 75px\"></td>");
			}
			out.write( "<td><img id=\"" + imageFile.tag + "\" src=\"" + imageFile.fileName  + "\"></td>\n" );
			if ( index < list.size() - 1 ) {
				out.write( "<td>" + getHtmlLinkTo(list.get(index+1), imageFile.tag, null ) + "</td>");
			}
			else {
				out.write( "<td>&nbsp;</td>");
			}
			out.write( "</tr>\n" );
		}
		out.write( "</table>\n" );
		out.write( "</body>\n" );
		out.write( "</html>\n" );
		out.close();
	}

	private String getHtmlFileNameForEvent(Event event) {
		return getStartDateAsString(event) + ".html";
	}

	private String getStartDateAsString(Event event) {
		return event.getStartDateAsString("dd.MM.yyy");
	}


	private Map<Event, List<OutputImageFile>> map;
}
