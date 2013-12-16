package de.greencity.bladenightapp.replay.log.local;

import java.io.BufferedInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;

import de.greencity.bladenightapp.procession.Procession;

public abstract class StatisticsWriter {

	StatisticsWriter(String baseFilename, Procession procession) throws IOException {
		this.procession = procession;
		this.baseFilename = baseFilename;
		this.dataFilePath  = baseFilename + ".log";
		this.gnuplotConfigPath  = baseFilename + ".gp";
		this.dataWriter = new FileWriter(dataFilePath);
		this.gnuplotConfigWriter = new FileWriter(gnuplotConfigPath);
	}

	public abstract void checkpoint(DateTime dateTime);

	public void finish() {
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
		writeGnuplotConfig(template, getGnuplotCustomFields());
	}
	
	private void writeGnuplotConfig(String template, Map<String, String> customFields) {
		Map<String, String> combinedFields = new HashMap<String, String>();
		combinedFields.put("BASE_FILENAME", baseFilename);
		combinedFields.put("DATA_FILE", dataFilePath);
		for (String v: customFields.values()) {
			combinedFields.put(v, customFields.get(v));
		}

		try {
			String generatedContent = replaceAll(template, combinedFields);
			gnuplotConfigWriter.write(generatedContent);
		} catch (IOException e) {
			getLog().error("Failed to write gnuplot config:", e);
		}
	}

	protected abstract String getGnuplotTemplateName();

	protected Map<String, String> getGnuplotCustomFields() {
		return new HashMap<String, String>();
	}

	
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


	protected Procession procession;

	protected String baseFilename;

	protected String dataFilePath;
	protected String gnuplotConfigPath;

	protected Writer dataWriter;
	protected Writer gnuplotConfigWriter;


	private static Log log;

	public static void setLog(Log log) {
		StatisticsWriter.log = log;
	}

	protected static Log getLog() {
		if (log == null)
			setLog(LogFactory.getLog(StatisticsWriter.class));
		return log;
	}

}
