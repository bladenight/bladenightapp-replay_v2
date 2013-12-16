package de.greencity.bladenightapp.replay.log.local;

import java.io.IOException;

import org.joda.time.DateTime;

import de.greencity.bladenightapp.procession.Procession;

public class HeadAndTailWriter extends StatisticsWriter {

	HeadAndTailWriter(String baseFilename, Procession procession) throws IOException {
		super(baseFilename, procession);
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

}
