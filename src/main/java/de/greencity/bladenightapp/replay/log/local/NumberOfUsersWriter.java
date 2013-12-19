package de.greencity.bladenightapp.replay.log.local;

import java.io.IOException;

import org.joda.time.DateTime;

import de.greencity.bladenightapp.events.Event;
import de.greencity.bladenightapp.procession.Procession;
import de.greencity.bladenightapp.procession.Statistics;
import de.greencity.bladenightapp.procession.Statistics.Segment;

public class NumberOfUsersWriter extends StatisticsWriter {

	NumberOfUsersWriter(String baseFilename, Procession procession, Event event) throws IOException {
		super(baseFilename, procession, event);
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
		writeDataLine(
				dateTime + "\t" +
						n
				);
	}

	@Override
	protected String getGnuplotTemplateName() {
		return "gnuplot-number-of-users.tpl";
	}

}
