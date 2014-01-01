package de.greencity.bladenightapp.replay.log.local;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.joda.time.DateTime;

import de.greencity.bladenightapp.events.Event;
import de.greencity.bladenightapp.procession.Procession;
import de.greencity.bladenightapp.routes.Route;
import de.greencity.bladenightapp.routes.Route.LatLong;

public class JavascriptRouteWriter extends ProcessionStatisticsWriter {

	public JavascriptRouteWriter(Procession procession, Event event) {
		super(procession, event);
	}

	@Override
	public void checkpoint(DateTime dateTime) {
	}

	@Override
	public void finish() {
		String fileName = event.getStartDateAsString("yyyy-MM-dd") + "-route.js";

		PrintWriter writer;
		try {
			writer = new PrintWriter(new FileWriter(fileName));

			//			  var flightPlanCoordinates = [
			//			                               new google.maps.LatLng(37.772323, -122.214897),
			//			                               new google.maps.LatLng(21.291982, -157.821856),
			//			                               new google.maps.LatLng(-18.142599, 178.431),
			//			                               new google.maps.LatLng(-27.46758, 153.027892)
			//			                             ];

			writer.write("var routeNodes = [\n");

			Route route = procession.getRoute();
			for (LatLong latLong : route.getNodesLatLong() ) {
				// writer.write("new google.maps.LatLng("+latLong.lat + "\t,\t" + latLong.lon + "),\n");
				writer.format("new google.maps.LatLng(%3.5f , %3.5f),\n", latLong.lat, latLong.lon);
			}
			writer.write("];\n");

			writer.close();
		} catch (IOException e) {
			getLog().error("Failed to write to " + fileName, e);
			return;
		}
	}

}
