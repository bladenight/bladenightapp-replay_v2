package de.greencity.bladenightapp.replay;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

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
		System.out.println("Reading " + file.getAbsolutePath() + "...");
		String jsonString = FileUtils.readFileToString(file, "UTF-8");
		System.out.println("Parsing...");
		logEntries = gson.fromJson(jsonString, LogEntry[].class);
		System.out.println("Sorting...");
		Arrays.sort(logEntries, new Comparator<LogEntry>() {
			public int compare(LogEntry o1, LogEntry o2) {
				return new Long(o2.serverTime).compareTo(o2.serverTime);
			}
		});
		System.out.println("Got " + logEntries.length + " log entries");
	}
	
	public LogEntry[] getEntries() {
		return logEntries;
	}

	private File file;
	private LogEntry[] logEntries;
}
