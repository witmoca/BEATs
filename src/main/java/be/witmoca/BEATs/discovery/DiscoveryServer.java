/**
 * 
 */
package be.witmoca.BEATs.discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import be.witmoca.BEATs.utils.BEATsSettings;

/*
*
+===============================================================================+
|    BEATs (Burning Ember Archival Tool suite)                                  |
|    Copyright 2020 Jente Heremans                                              |
|                                                                               |
|    Licensed under the Apache License, Version 2.0 (the "License");            |
|    you may not use this file except in compliance with the License.           |
|    You may obtain a copy of the License at                                    |
|                                                                               |
|    http://www.apache.org/licenses/LICENSE-2.0                                 |
|                                                                               |
|    Unless required by applicable law or agreed to in writing, software        |
|    distributed under the License is distributed on an "AS IS" BASIS,          |
|    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   |
|    See the License for the specific language governing permissions and        |
|    limitations under the License.                                             |
+===============================================================================+
*
* File: DiscoveryServer.java
* Created: 2020
*/
public class DiscoveryServer implements Runnable {
	private static final int DISCOVERY_PORT = 41525;
	private static final String PING = "BEATS_CONNECT_PING";
	private static final String SEPARATOR = ";";
	private static final Long PRUNE_TIME_S = 5L;
	private static final int RECEIVE_TIMEOUT = 1000; // every second, timeout and prune discovered
	private static final Long PING_MIN_RESPONSETIME_NS = 500 * 1000 * 1000L; // Min. time between ping responses in nS
	private static final Long PING_BROADCAST_TIMER_MS = 300L;
	private static DiscoveryServer currentServ;
	private final AtomicBoolean turnedOn = new AtomicBoolean(true);
	private final DatagramPacket incoming;
	private final byte[] incomingBuffer = new byte[1500];
	private final byte[] pingMsg = ("BEATS_CONNECT_PING" + SEPARATOR
			+ BEATsSettings.LIVESHARE_SERVER_HOSTNAME.getStringValue()).getBytes();
	private final Map<String, LocalTime> discovered = Collections.synchronizedMap(new HashMap<String, LocalTime>());
	private Timer broadcastTimer;
	private DatagramSocket sendSocket;
	private DatagramSocket receiveSocket;

	/**
	 * Turn on server, do nothing if already on
	 */
	public static void startServer() {
		// already running?
		if (isRunning()) {
			return;
		}
		try {
			currentServ = new DiscoveryServer();
			(new Thread(currentServ)).start();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Turn off server
	 */
	public static void stopServer() {
		stopBroadcaster();
		if (currentServ != null) {
			currentServ.turnedOn.set(false);
			currentServ.receiveSocket.close(); // receive is always waiting, so force it closed
		}
	}

	private static boolean isRunning() {
		return (currentServ != null && currentServ.turnedOn.get() == true);
	}

	public static List<String> getDiscoveredSorted() {
		List<String> returnList = new ArrayList<String>();
		if (isRunning()) {
			synchronized (currentServ.discovered) {
				returnList.addAll(currentServ.discovered.keySet());
			}
		}
		returnList.sort(null);
		return returnList;
	}

	public DiscoveryServer() throws SocketException {
		this.incoming = new DatagramPacket(incomingBuffer, incomingBuffer.length);
	}

	@Override
	public void run() {
		while (turnedOn.get()) {
			try {
				// (re)start sockets if not open
				if (this.receiveSocket == null || this.receiveSocket.isClosed()) {
					this.receiveSocket = new DatagramSocket(DISCOVERY_PORT);
					this.receiveSocket.setSoTimeout(RECEIVE_TIMEOUT);
				}
				if (this.sendSocket == null || this.sendSocket.isClosed()) {
					this.sendSocket = new DatagramSocket();
				}

				// Prune list of stagnant values
				Set<String> pruneSet = new HashSet<String>();
				synchronized (discovered) {
					discovered.forEach((h, t) -> {
						if (t.plusSeconds(PRUNE_TIME_S).isBefore(LocalTime.now())) {
							pruneSet.add(h);
						}
					});
					for (String host : pruneSet) {
						discovered.remove(host);
					}
				}

				// Receive
				this.receiveSocket.receive(incoming);
				String received = new String(incoming.getData());
				String[] pieces = received.split(SEPARATOR);
				// Ignore if size of message not okay
				if (pieces.length != 2)
					continue;
				// Ignore if not a ping message
				if (!pieces[0].trim().equals(PING))
					continue;

				String newEntry = pieces[1].trim();
				LocalTime lastSeen = discovered.get(newEntry);

				// only update if new OR enough time has passed
				if (lastSeen == null || lastSeen.plusNanos(PING_MIN_RESPONSETIME_NS).isBefore(LocalTime.now())) {
					discovered.put(newEntry, LocalTime.now());
					sendPingResponse(incoming.getAddress());
				}
			} catch (SocketTimeoutException e) {
				// ignore, timeout has happend (it's time to prune!)
			} catch (SocketException e) {
				// Only print stacktrace if DiscoveryServer isn't shutting down
				if (turnedOn.get()) {
					e.printStackTrace();
				}
				// Socket will be restarted automatically in next loop
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// Before terminating, make sure that no broadcaster is running
		stopBroadcaster();
	}

	/**
	 * Send a ping response to a single receiver
	 * 
	 * @param receiver
	 */
	private void sendPingResponse(InetAddress receiver) {
		synchronized (sendSocket) {
			DatagramPacket pingPacket = new DatagramPacket(pingMsg, pingMsg.length, receiver, DISCOVERY_PORT);
			try {
				sendSocket.send(pingPacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void startBroadcaster() {
		if (isRunning()) {
			if (currentServ.broadcastTimer != null)
				currentServ.broadcastTimer.cancel();
			currentServ.broadcastTimer = new Timer(true);
			currentServ.broadcastTimer.schedule(getBroadcaster(currentServ), PING_BROADCAST_TIMER_MS,
					PING_BROADCAST_TIMER_MS);
		}
	}

	public static void stopBroadcaster() {
		if (currentServ != null && currentServ.broadcastTimer != null) {
			currentServ.broadcastTimer.cancel();
		}
	}

	/**
	 * Send a ping broadcast
	 * 
	 * @param ias
	 */
	private void sendBroadcast(List<InterfaceAddress> ias) {
		synchronized (sendSocket) {
			for (InterfaceAddress ia : ias) {
				InetAddress broadcast = ia.getBroadcast();
				DatagramPacket pingPacket = new DatagramPacket(pingMsg, pingMsg.length, broadcast, DISCOVERY_PORT);
				try {
					sendSocket.send(pingPacket);
				} catch (IOException e) {
					System.err.println("Sending failed to ip adress " + broadcast + " on interface " + ia.getAddress());
					e.printStackTrace();
				}
			}
		}
	}

	private static TimerTask getBroadcaster(DiscoveryServer ds) {
		return new TimerTask() {
			@Override
			public void run() {
				try {
					Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
					List<InterfaceAddress> ias = new ArrayList<InterfaceAddress>();
					while (interfaces.hasMoreElements()) {
						NetworkInterface ni = interfaces.nextElement();
						if (ni.isUp() && !ni.isLoopback()) {
							for (InterfaceAddress ia : ni.getInterfaceAddresses()) {
								// interface should have broadcast (filters out ipv6 as well)
								// Ignore if broadcast address starts with 0. (0.0.0.0/8 = defined as "current
								// network", used in i.e. OVPN)
								if (ia.getBroadcast() != null && !ia.getBroadcast().getHostAddress().startsWith("0.") ) {
									ias.add(ia);
								}
							}
						}
					}

					ds.sendBroadcast(ias);
				} catch (SocketException e) {
					e.printStackTrace();
				}
			}
		};
	}
}
