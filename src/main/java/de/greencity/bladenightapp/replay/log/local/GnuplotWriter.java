package de.greencity.bladenightapp.replay.log.local;

import java.io.BufferedInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.greencity.bladenightapp.events.Event;
import de.greencity.bladenightapp.procession.Procession;

public abstract class GnuplotWriter extends ProcessionStatisticsWriter {

	GnuplotWriter(String baseFilename, Procession procession, Event event) throws IOException {
		super(procession, event);
		this.baseFilename = baseFilename;
		this.dataFilePath  = baseFilename + ".log";
		this.gnuplotConfigPath  = baseFilename + ".gp";
		this.dataWriter = new FileWriter(dataFilePath);
		this.gnuplotConfigWriter = new FileWriter(gnuplotConfigPath);
	}

	public void finish()
	{
		writeGnuplotConfigFromResource();
		try {
			dataWriter.close();
		}
		catch (Exception e) {
			getLog().error("Failed to close writer: ",e);
		}
		try {
			gnuplotConfigWriter.close();
		}
		catch (Exception e) {
			getLog().error("Failed to close writer: ",e);
		}
	}
	
	protected void writeDataLine(String line) {
		try {
			dataWriter.write(line + "\n");
		} catch (IOException e) {
			getLog().error("Failed to write to file: ", e);
		}
	}

	protected void writeGnuplotConfigFromResource() {
		String template = "";
		try {
			template = readTextFileFromResource(HeadAndTailWriter.class, getGnuplotTemplateName());
		} catch (IOException e) {
			getLog().error("Failed to read template:" , e);
		}
		writeGnuplotConfig(template);
	}

	protected void writeGnuplotConfig(String template) {
		Map<String, String> customFields = new HashMap<String, String>();
		specifyGnuplotCustomFields(customFields);
		writeGnuplotConfig(template, customFields);
	}

	private void writeGnuplotConfig(String template, Map<String, String> customFields) {
		Map<String, String> combinedFields = new HashMap<String, String>();
		combinedFields.put("BASE_FILENAME", baseFilename);
		combinedFields.put("DATA_FILE", dataFilePath);
		if ( event != null ) {
			String eventdate = event.getStartDate().toString("dd.MM.yyyy");
			String routeName = event.getRouteName();
			String participants = Integer.toString(event.getParticipants());
			combinedFields.put("EVENT_DATE", eventdate);
			combinedFields.put("EVENT_ROUTE", routeName);
			combinedFields.put("EVENT_PARTICIPANTS", participants);
			StringBuilder eventInfoLabelsBuilder = new StringBuilder();
			eventInfoLabelsBuilder.append("set label \"" + eventdate + "\" at screen 0.1, screen 0.9 front\n");
			eventInfoLabelsBuilder.append("set label \"Strecke : " + routeName + "\" at screen 0.1, screen 0.87 front\n");
			eventInfoLabelsBuilder.append("set label \"" + participants + " skaters\" at screen 0.1, screen 0.84 front\n");
			combinedFields.put("EVENT_INFO_LABELS", eventInfoLabelsBuilder.toString());
		}
		else {
			combinedFields.put("EVENT_DATE", "-");
			combinedFields.put("EVENT_ROUTE", "-");
			combinedFields.put("EVENT_PARTICIPANTS", "-");
		}

		for (String v: customFields.keySet()) {
			combinedFields.put(v, customFields.get(v));
		}

		try {
			String generatedContent = replaceAll(template, combinedFields);
			gnuplotConfigWriter.write(generatedContent);
		} catch (IOException e) {
			getLog().error("Failed to write gnuplot config:", e);
		}
	}

	abstract protected void addOutputImageFilesToThisList(List<OutputImageFile> list);

	abstract protected String getGnuplotTemplateName();

	abstract protected void specifyGnuplotCustomFields(Map<String, String> map);

	public static String replaceAll(String text, Map<String, String> params) {
		return replaceAll(text, params, '%', '%');
	}

	public static String replaceAll(String text, Map<String, String> params, char leading, char trailing) {
		String pattern = "";
		if (leading != 0) {
			pattern += leading;
		}
		pattern += "(\\w+)";
		if (trailing != 0) {
			pattern += trailing;
		}
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(text);
		boolean result = m.find();
		if (result) {
			StringBuffer sb = new StringBuffer();
			do {
				String replacement = params.get(m.group(1));
				if (replacement == null) {
					replacement = m.group();
				}
				m.appendReplacement(sb, replacement);
				result = m.find();
			} while (result);
			m.appendTail(sb);
			return sb.toString();
		}
		return text;
	}

	public static String readTextFileFromResource(Class<?> clazz, String resourceName) throws IOException {
		StringBuffer sb = new StringBuffer(1024);
		BufferedInputStream inStream = new BufferedInputStream(clazz.getClassLoader().getResourceAsStream(resourceName));
		byte[] chars = new byte[1024];
		int bytesRead = 0;
		while( (bytesRead = inStream.read(chars)) > -1){
			sb.append(new String(chars, 0, bytesRead));
		}
		inStream.close();
		return sb.toString();
	}

	protected double convertPositionForOutput(double position) {
		return ((long)position) / 1000.0;
	}

	protected double convertSpeedForOutput(double speed) {
		return ((long)(speed*10.0) / 10.0);
	}

	protected String baseFilename;

	protected String dataFilePath;
	protected String gnuplotConfigPath;

	protected Writer dataWriter;
	protected Writer gnuplotConfigWriter;


}
