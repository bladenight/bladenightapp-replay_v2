package de.greencity.bladenightapp.replay.log.local;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import de.greencity.bladenightapp.events.Event;
import de.greencity.bladenightapp.procession.Procession;
import de.greencity.bladenightapp.procession.Statistics;
import de.greencity.bladenightapp.replay.log.local.templatedata.TemplateProxy;

public class LengthByTime extends ProcessionStatisticsWriter {

	LengthByTime(File basePath, Procession procession, Event event) throws IOException {
		super(basePath, procession, event);
	}

	@Override
	public void checkpoint(DateTime dateTime) {
		Statistics statistics = procession.getStatistics();
		if ( statistics == null ) {
			getLog().debug("No statistics available");
			return;
		}
		if ( procession.getTailPosition() >= procession.getHeadPosition())
			return;
		entries.add(new OutputEntry(dateTime.toString(), (int)(procession.getHeadPosition() - procession.getTailPosition())));
	}

	@Override
	public void finish() {
		TemplateProxy templateProxy = new TemplateProxy("length-by-time/length-by-time.ftl.json");
		templateProxy.putData("entries", entries);
		String targetFileName = "length-by-time.json";
		File targetFile = new File(basePath, targetFileName);
		templateProxy.generate(targetFile);
	} 

	static public class OutputEntry {
		public OutputEntry(String dateTimeStr, double length) {
			this.dateTimeStr = dateTimeStr;
			this.length = length;
		}
		public String dateTimeStr;
		public double length;
		public String getDateTimeStr() {
			return dateTimeStr;
		}
		public double getLength() {
			return length;
		} 
	};

	private List<OutputEntry> entries = new ArrayList<OutputEntry>();


}
