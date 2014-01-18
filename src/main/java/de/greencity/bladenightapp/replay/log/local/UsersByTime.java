package de.greencity.bladenightapp.replay.log.local;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import de.greencity.bladenightapp.events.Event;
import de.greencity.bladenightapp.procession.Procession;
import de.greencity.bladenightapp.procession.Statistics;
import de.greencity.bladenightapp.procession.Statistics.Segment;
import de.greencity.bladenightapp.replay.log.local.templatedata.TemplateProxy;

public class UsersByTime extends ProcessionStatisticsWriter {

	UsersByTime(File basePath, Procession procession, Event event) throws IOException {
		super(basePath, procession, event);
	}

	@Override
	public void checkpoint(DateTime dateTime) {
		Statistics statistics = procession.getStatistics();
		int n = 0;
		if (statistics != null ) {
			for (Segment segment : statistics.segments ) {
				n += segment.nParticipants;
			}
		}
		entries.add(new OutputEntry(dateTime.toString(), n));
	}

	@Override
	public void finish() {
		TemplateProxy templateProxy = new TemplateProxy("users-by-time/users-by-time.ftl.json");
		templateProxy.putData("entries", entries);
		String targetFileName = "users-by-time.json";
		File targetFile = new File(basePath, targetFileName);
		templateProxy.generate(targetFile);
	} 

	static public class OutputEntry {
		public OutputEntry(String dateTimeStr, int users) {
			this.dateTimeStr = dateTimeStr;
			this.users = users;;
		}
		public String dateTimeStr;
		public int users;
		public String getDateTimeStr() {
			return dateTimeStr;
		}
		public double getUsers() {
			return users;
		} 
	};

	private List<OutputEntry> entries = new ArrayList<OutputEntry>();


}
