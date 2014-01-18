package de.greencity.bladenightapp.replay.log.local;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import de.greencity.bladenightapp.events.Event;
import de.greencity.bladenightapp.procession.Procession;
import de.greencity.bladenightapp.replay.log.local.templatedata.TemplateProxy;

public class HeadAndTailPosByTime extends ProcessionStatisticsWriter {

	public HeadAndTailPosByTime(File basePath, Procession procession, Event event) {
		super(basePath, procession, event);
	}

	@Override
	public void checkpoint(DateTime dateTime) {
		headPositions.add(new OutputEntry(dateTime.toString(), procession.getHeadPosition()));
		tailPositions.add(new OutputEntry(dateTime.toString(), procession.getTailPosition()));
	}

	@Override
	public void finish() {
		writeForHead();
		writeForTail();
	}

	private void writeForHead() {
		TemplateProxy templateProxy = new TemplateProxy("head-and-tail-pos-by-time/head-and-tail-pos-by-time.ftl.json");
		templateProxy.putData("entries", headPositions);
		String targetFileName = "head-pos-by-time.json";
		File targetFile = new File(basePath, targetFileName);
		templateProxy.generate(targetFile);
	}

	private void writeForTail() {
		TemplateProxy templateProxy = new TemplateProxy("head-and-tail-pos-by-time/head-and-tail-pos-by-time.ftl.json");
		templateProxy.putData("entries", tailPositions);
		String targetFileName = "tail-pos-by-time.json";
		File targetFile = new File(basePath, targetFileName);
		templateProxy.generate(targetFile);
	}

	

	static public class OutputEntry {
		public OutputEntry(String dateTimeStr, double position) {
			this.dateTimeStr = dateTimeStr;
			this.position = position;
		}
		public String dateTimeStr;
		public double position;
		public String getDateTimeStr() {
			return dateTimeStr;
		}
		public double getPosition() {
			return position;
		} 
	};

	private List<OutputEntry> headPositions = new ArrayList<OutputEntry>(); 
	private List<OutputEntry> tailPositions = new ArrayList<OutputEntry>(); 
}
