package de.greencity.bladenightapp.replay;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;

public class ParticipanLogFile {

	class LogEntry {
		String deviceId;
		public long serverTime; 
		public String serverTimeString; 
		public long clientTime;
		public double latitude; 
		public double longitude;
		public double accuracy;
		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}
		
	};

	public ParticipanLogFile(File file) {
		this.file = file;
	}

	public void load() throws IOException {
		Gson gson = new Gson();
		getLog().info("Reading " + file.getAbsolutePath() + "...");
		String jsonString = FileUtils.readFileToString(file, "UTF-8");
		getLog().info("Parsing...");
		logEntries = gson.fromJson(jsonString, LogEntry[].class);
		getLog().info("Sorting...");
		Arrays.sort(logEntries, new Comparator<LogEntry>() {
			public int compare(LogEntry o1, LogEntry o2) {
				return new Long(o2.serverTime).compareTo(o2.serverTime);
			}
		});
		getLog().info("Got " + logEntries.length + " log entries");
	}
	
	public LogEntry[] getEntries() {
		return logEntries;
	}

	private static Log log;

	public static void setLog(Log log) {
		ParticipanLogFile.log = log;
	}

	protected static Log getLog() {
		if (log == null)
			setLog(LogFactory.getLog(ParticipanLogFile.class));
		return log;
	}
	
	private File file;
	private LogEntry[] logEntries;
}
