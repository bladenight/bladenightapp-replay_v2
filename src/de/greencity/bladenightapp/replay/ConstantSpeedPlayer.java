package de.greencity.bladenightapp.replay;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import de.greencity.bladenightapp.geo.CoordinatesConversion;
import de.greencity.bladenightapp.network.BladenightUrl;
import de.greencity.bladenightapp.network.messages.LatLong;
import de.greencity.bladenightapp.network.messages.RouteMessage;
import fr.ocroquette.wampoc.adapters.jetty.JettyClient;
import fr.ocroquette.wampoc.client.RpcResultReceiver;
import fr.ocroquette.wampoc.client.WampClient;

public class ConstantSpeedPlayer {
	ConstantSpeedPlayer(URI uri) {
		this.serverUri = uri; 
	}

	public double getBaseSpeed() {
		return baseSpeed;
	}
	public void setBaseSpeed(double baseSpeed) {
		this.baseSpeed = baseSpeed;
	}
	public double getSpeedVaration() {
		return speedVaration;
	}
	public void setSpeedVaration(double speedVaration) {
		this.speedVaration = speedVaration;
	}
	public int getParticipantCount() {
		return participantCount;
	}
	public void setParticipantCount(int participantCount) {
		this.participantCount = participantCount;
	}
	public long getStartPeriod() {
		return startPeriod;
	}
	public void setStartPeriod(long startPeriod) {
		this.startPeriod = startPeriod;
	}
	public double getStartPosition() {
		return startPosition;
	}
	public void setStartPosition(double startPosition) {
		this.startPosition = startPosition;
	}

	public void play() {
		try {
			getRoute();
			System.out.println(convertLinearPositionToLatLong(1000));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void getRoute() throws URISyntaxException, Exception {
		final Object signal = new Object();
		final WampClient client = createNewConnection();
		Runnable runnable = new Runnable() {
			public void run() {
				try {
					client.call(BladenightUrl.GET_ACTIVE_ROUTE.getText(), new RpcResultReceiver() {

						@Override
						public void onSuccess() {
							System.out.println(this.callResultMessage);
							routeMessage = getPayload(RouteMessage.class);
							synchronized (signal) {
								signal.notify();
							}
						}

						@Override
						public void onError() {
							System.err.println("Could not get the route: " + callErrorMessage);
						}
					});
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		new Thread(runnable).start();
		synchronized (signal) {
			signal.wait();
		}
		System.out.println("Got route " + routeMessage);
	}

	private WampClient createNewConnection() throws URISyntaxException, Exception  {
		JettyClient jettyClient = new JettyClient();
		jettyClient.connect(serverUri, "undefined");

		return jettyClient.getWampClient();
	}

	public LatLong convertLinearPositionToLatLong(double linearPosition) {
		double currentSegmentSum = 0.0;
		List<LatLong> nodes = routeMessage.getNodes(); 
		for ( int nodeIndex = 0 ; nodeIndex < nodes.size()-1; nodeIndex++) {
			LatLong node1 = nodes.get(nodeIndex);
			LatLong node2 = nodes.get(nodeIndex+1);

			double segmentLength = CoordinatesConversion.getOrthodromicDistance(node1.getLatitude(), node1.getLongitude(), node2.getLatitude(), node2.getLongitude());
			// node1.distance(node2);
			double missingLength = linearPosition - currentSegmentSum;
			if ( missingLength <= segmentLength  ) {
				double positionOnSegment = missingLength / segmentLength;
				// TODO this is mathematically not correct, but good enough on short distances for now 
				double lat = node1.getLatitude() + positionOnSegment * (node2.getLatitude() - node1.getLatitude() );
				double lon = node1.getLongitude() + positionOnSegment * (node2.getLongitude() - node1.getLongitude() );
				return new LatLong(lat,lon);
			}
			currentSegmentSum += segmentLength;
		}
		// Looks like the requested position is after the end of the route.
		return nodes.get(nodes.size()-1);
	}


	private URI serverUri;
	RouteMessage routeMessage;

	private double baseSpeed = 30;
	private double speedVaration = 20;
	private int participantCount = 30;
	private long startPeriod = 5000;
	private double startPosition = 0;


}
