package de.greencity.bladenightapp.replay.log.local;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import de.greencity.bladenightapp.events.Event;
import de.greencity.bladenightapp.procession.Procession;
import de.greencity.bladenightapp.replay.log.local.templatedata.LatLongProxy;
import de.greencity.bladenightapp.replay.log.local.templatedata.TemplateProxy;
import de.greencity.bladenightapp.routes.Route.LatLong;

public class JavascriptRouteWriter extends ProcessionStatisticsWriter {

	public JavascriptRouteWriter(File basePath, Procession procession, Event event) {
		super(basePath, procession, event);
	}

	@Override
	public void checkpoint(DateTime dateTime) {
	}

	@Override
	public void finish() {

		TemplateProxy templateProxy = new TemplateProxy("route.ftl.json");
		List<LatLongProxy> entries = new ArrayList<LatLongProxy>();
		for (LatLong latLong : procession.getRoute().getNodesLatLong()) {
			entries.add(new LatLongProxy(latLong));
		}
		templateProxy.putData("entries", entries);

		String fileName = "route.json";
		templateProxy.generate(newOutputFile(fileName));
	}

}
