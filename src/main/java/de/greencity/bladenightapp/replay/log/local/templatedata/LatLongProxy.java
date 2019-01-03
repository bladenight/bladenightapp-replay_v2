package de.greencity.bladenightapp.replay.log.local.templatedata;

import de.greencity.bladenightapp.routes.Route.LatLong;


public class LatLongProxy {
    public LatLongProxy(LatLong latLong) {
        this.latLong = latLong;
    }
    public double getLatitude() {
        return latLong.lat;
    }
    public double getLongitude() {
        return latLong.lon;
    }
    private LatLong latLong;
}
