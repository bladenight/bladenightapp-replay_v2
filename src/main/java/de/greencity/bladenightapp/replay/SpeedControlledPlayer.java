package de.greencity.bladenightapp.replay;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.greencity.bladenightapp.geo.CoordinatesConversion;
import de.greencity.bladenightapp.network.BladenightUrl;
import de.greencity.bladenightapp.network.messages.LatLong;
import de.greencity.bladenightapp.network.messages.RouteMessage;
import de.greencity.bladenightapp.replay.SpeedControlledParticipant.SpeedMaster;
import fr.ocroquette.wampoc.adapters.jetty.JettyClient;
import fr.ocroquette.wampoc.client.RpcResultReceiver;
import fr.ocroquette.wampoc.client.WampClient;

public class SpeedControlledPlayer {
	SpeedControlledPlayer(URI uri) {
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
		String deviceIdPrefix = Long.toString(System.currentTimeMillis());
		
		try {
			getRoute();
		} catch (Exception e) {
			getLog().error(e.getMessage(), e);
			return;
		}
		
		final Random random = new Random();
		Stack<Thread> threads = new Stack<Thread>();
		for(int i=0; i<participantCount; i++) {
			final WampClient client;
			try {
				client = createNewConnection();
			} catch (Exception e) {
				getLog().error(e.getMessage(), e);
				return;
			}
			LinearPositionToLatLongInterface callbackInterface = new LinearPositionToLatLongInterface() {
				@Override
				public de.greencity.bladenightapp.routes.Route.LatLong convert(double linearPosition) {
					LatLong l = convertLinearPositionToLatLong(linearPosition);
					return new de.greencity.bladenightapp.routes.Route.LatLong(l.getLatitude(), l.getLongitude());
				}
			};
			final double usualSpeed = baseSpeed + speedVaration * random.nextDouble();
			SpeedMaster speedMaster = new SpeedMaster() {
				@Override
				public double speedAt(double linearPosition) {
					if ( linearPosition > 500 && linearPosition < 700 )
						return usualSpeed / 10.0;
					else
						return usualSpeed; 
				}
			};
			SpeedControlledParticipant participant = new SpeedControlledParticipant(client, callbackInterface, speedMaster, updatePeriod);
			participant.setDeviceId(deviceIdPrefix+"-ConstantSpeed-"+i);
			getLog().info("Starting a new participant ("+i+")");
			Thread t = new Thread(participant);
			threads.push(t);
			t.start();
			try {
				Thread.sleep(startPeriod);
			} catch (InterruptedException e) {
				getLog().error(e.getMessage(), e);
				return;
			}
		}
		while(!threads.empty()) {
			try {
				threads.pop().join();
			} catch (InterruptedException e) {
				getLog().error(e.getMessage(), e);
			}
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
							routeMessage = getPayload(RouteMessage.class);
							synchronized (signal) {
								signal.notify();
							}
						}

						@Override
						public void onError() {
							getLog().error("Could not get the route: " + callErrorMessage);
						}
					});
				} catch (IOException e) {
					getLog().error(e.getMessage(), e);
				}
			}
		};
		new Thread(runnable).start();
		synchronized (signal) {
			signal.wait();
		}
		getLog().info("Got route " + routeMessage);
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
	private RouteMessage routeMessage;

	private double baseSpeed 		= 30.0;
	private double speedVaration 	= 20.0;

	private int participantCount = 30;

	private double startPosition = 0.0;

	private long startPeriod = 5000;
	private int updatePeriod = 5000;

	
	private static Log log;

	public static void setLog(Log log) {
		SpeedControlledPlayer.log = log;
	}

	protected static Log getLog() {
		if (log == null)
			setLog(LogFactory.getLog(SpeedControlledPlayer.class));
		return log;
	}

}
