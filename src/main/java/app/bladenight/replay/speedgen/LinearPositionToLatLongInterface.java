package app.bladenight.replay.speedgen;

import app.bladenight.common.routes.Route.LatLong;

public interface LinearPositionToLatLongInterface {
    public LatLong convert(double linearPosition);
}
