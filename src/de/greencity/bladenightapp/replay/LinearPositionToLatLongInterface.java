package de.greencity.bladenightapp.replay;

import de.greencity.bladenightapp.routes.Route.LatLong;

public interface LinearPositionToLatLongInterface {
	public LatLong convert(double linearPosition);
}
