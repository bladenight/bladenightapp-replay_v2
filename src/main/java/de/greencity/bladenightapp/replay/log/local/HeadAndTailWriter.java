package de.greencity.bladenightapp.replay.log.local;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import de.greencity.bladenightapp.events.Event;
import de.greencity.bladenightapp.procession.Procession;

public class HeadAndTailWriter extends GnuplotWriter {

	HeadAndTailWriter(String baseFilename, Procession procession, Event event) throws IOException {
		super(baseFilename, procession, event);
	}

	@Override
	public void checkpoint(DateTime dateTime) {
		writeDataLine(
				dateTime + "\t" +
						convertPositionForOutput(procession.getTail().getLinearPosition()) + "\t" +
						convertSpeedForOutput(procession.getTail().getLinearSpeed()) + "\t" +
						convertPositionForOutput(procession.getHead().getLinearPosition())+ "\t" +
						convertSpeedForOutput(procession.getHead().getLinearSpeed())
				);
	}

	@Override
	protected String getGnuplotTemplateName() {
		return "gnuplot-head-and-tail.tpl";
	}

	@Override
	protected void addOutputImageFilesToThisList(List<OutputImageFile> list) {
		list.add(new OutputImageFile(getPngFile(), "head-and-tail", 10));
	}

	@Override
	protected void specifyGnuplotCustomFields(Map<String, String> map) {
		map.put("PNG_FILE", getPngFile());
	}

	private String getPngFile() {
		return baseFilename + ".png";
	}
}
