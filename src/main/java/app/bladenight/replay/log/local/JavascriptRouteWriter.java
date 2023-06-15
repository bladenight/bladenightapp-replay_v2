package app.bladenight.replay.log.local;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import app.bladenight.common.events.Event;
import app.bladenight.common.procession.Procession;
import app.bladenight.replay.log.local.templatedata.LatLongProxy;
import app.bladenight.replay.log.local.templatedata.TemplateProxy;
import app.bladenight.common.routes.Route.LatLong;

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
