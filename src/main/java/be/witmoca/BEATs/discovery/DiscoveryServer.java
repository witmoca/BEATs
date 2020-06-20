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
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
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
	private static final String PING = "BEATS_CONNECT_PING";
	private static final String SEPARATOR = ";";
	private static final Long PRUNE_TIME_S = 30L;
	private static final Long PING_MIN_RESPONSETIME_NS = 500 * 1000 * 1000L; // Min. time between ping responses in nS
	private static final Long PING_BROADCAST_TIMER_MS = 1L;
	private static DiscoveryServer currentServ;
	private final AtomicBoolean turnedOn = new AtomicBoolean(true);
	private final DatagramSocket sendSocket;
	private final DatagramSocket receiveSocket;
	private final DatagramPacket incoming;
	private final byte[] incomingBuffer = new byte[1500];
	private final byte[] pingMsg = ("BEATS_CONNECT_PING" + SEPARATOR + BEATsSettings.LIVESHARE_SERVER_HOSTNAME.getStringValue() + SEPARATOR + BEATsSettings.LIVESHARE_SERVER_PORT.getStringValue()).getBytes();
	private final int discoveryPort =  BEATsSettings.DISCOVERY_PORT.getIntValue();
	private final List<DiscoveryListEntry> discovered = Collections.synchronizedList(new ArrayList<DiscoveryListEntry>());
	private final Timer broadcastTimer = new Timer(true);
	
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
		if (currentServ != null) {
			currentServ.turnedOn.set(false);
			currentServ.receiveSocket.close(); // receive is always waiting, so force it closed
		}
	}
	
	private static boolean isRunning() {
		return (currentServ != null && currentServ.turnedOn.get() == true);
	}
	
	public static List<DiscoveryListEntry> getDiscovered(){
		List<DiscoveryListEntry> returnlist = new ArrayList<DiscoveryListEntry>();
		if(isRunning()) {
			synchronized(currentServ.discovered) {
				returnlist.addAll(currentServ.discovered);
			}
		}
		return returnlist;
	}

	public DiscoveryServer() throws SocketException {
		this.receiveSocket = new DatagramSocket(discoveryPort);
		this.sendSocket = new DatagramSocket();
		this.incoming = new DatagramPacket(incomingBuffer, incomingBuffer.length);
	}

	@Override
	public void run() {
		while (turnedOn.get()) {
			try {
				// Receive
				this.receiveSocket.receive(incoming);
				String received = new String(incoming.getData());
				String[] pieces = received.split(SEPARATOR);
				// Ignore if size of message not okay
				if(pieces.length != 3)
					continue;
				// Ignore if not a ping message
				if(!pieces[0].trim().equals(PING))
					continue;

				DiscoveryListEntry newEntry = new DiscoveryListEntry(pieces[1].trim(), Integer.parseInt(pieces[2].trim()), incoming.getAddress().getHostAddress());
				DiscoveryListEntry resolved = resolveHost(newEntry.getHostname());
				
				
				// Host is known already?
				if(resolved != null) {
					// only update when enough time has passed
					if(resolved.getTimestamp().plusNanos(PING_MIN_RESPONSETIME_NS).isBefore(LocalTime.now())) {
						// replace existing info about this host
						deleteEntry(pieces[1]);
						discovered.add(newEntry);
						sendPingResponse(incoming.getAddress());
					}
					// If host is known, but too recent than ignore it
				} else {
					// totally new entry
					discovered.add(newEntry);
				}
				
			} catch (NumberFormatException e){
				e.printStackTrace();
				// port wasn't a number? => ignore packet
			} catch (SocketException e) {
				// Only print stacktrace if DiscoveryServer isn't shutting down
				if(turnedOn.get()) {
					e.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// Before terminating, make sure that no broadcaster is running
		stopBroadcaster();
	}
	
	/**
	 * 
	 * @param hostname The hostname to resolve
	 * @return The entry containing the resolved hostname, or null when not found
	 */
	private DiscoveryListEntry resolveHost(String hostname) {
		synchronized(discovered) {
			// prune all entry's in discovered
			LocalTime pruneLimit = LocalTime.now().minusSeconds(PRUNE_TIME_S);
			for(int i = discovered.size()-1 ; i >= 0; i--) {
				if(discovered.get(i).getTimestamp().isBefore(pruneLimit)) {
					discovered.remove(i);
				}
			}
			
			// find  entry
			for(DiscoveryListEntry dle : discovered) {
				if(dle.getHostname().equals(hostname)) {
					return dle;
				}
			}
			return null;
		}
	}
	
	private void deleteEntry(String hostname) {
		synchronized(discovered) {
			for(int i = discovered.size()-1 ; i >= 0; i--) {
				if(discovered.get(i).getHostname().equals(hostname)) {
					discovered.remove(i);
					return;
				}
			}
		}
	}
	
	/**
	 * Send a ping response to a single receiver
	 * @param reciever
	 */
	private void sendPingResponse(InetAddress reciever) {
		synchronized(sendSocket) {
			DatagramPacket pingPacket = new DatagramPacket(pingMsg, pingMsg.length, reciever , discoveryPort);
			try {
				sendSocket.send(pingPacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void startBroadcaster() {
		if(isRunning()) {
			currentServ.broadcastTimer.schedule(getBroadcaster(currentServ), PING_BROADCAST_TIMER_MS, PING_BROADCAST_TIMER_MS);
		}
	}
	
	public static void stopBroadcaster() {
		if(isRunning()) {
			currentServ.broadcastTimer.cancel();
		}
	}
	
	/**
	 * Send a ping broadcast
	 * @param ias
	 */
	private void sendBroadcast(List<InterfaceAddress> ias) {
		synchronized(sendSocket) {
			for(InterfaceAddress ia : ias) {
				InetAddress broadcast = ia.getBroadcast();
				DatagramPacket pingPacket = new DatagramPacket(pingMsg, pingMsg.length, broadcast, discoveryPort);
				try {
					sendSocket.send(pingPacket);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static TimerTask getBroadcaster(DiscoveryServer ds) {
		return new TimerTask() {
			@Override
			public void run() {
				List<InterfaceAddress> ias = new ArrayList<InterfaceAddress>();	
				try {
					Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
					while(interfaces.hasMoreElements()) {
						NetworkInterface ni = interfaces.nextElement();
						if(ni.isUp() && !ni.isLoopback()) {
							for(InterfaceAddress ia : ni.getInterfaceAddresses()) {
								// interface should have broadcast (filters out ipv6 as well)
								if(ia.getBroadcast() != null) {
									ias.add(ia);
								}
							}
						}
					}
				} catch (SocketException e1) {
					e1.printStackTrace();
				}
				ds.sendBroadcast(ias);
			}
		};
	}
}
