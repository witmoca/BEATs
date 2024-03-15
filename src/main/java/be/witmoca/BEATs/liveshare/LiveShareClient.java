/**
 * 
 */
package be.witmoca.BEATs.liveshare;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.SwingUtilities;

import be.witmoca.BEATs.connection.DataChangedListener;
import be.witmoca.BEATs.utils.BEATsSettings;

/**
 * @author Witmoca
 *
 */
public class LiveShareClient {
	private static final int CONN_TRY_TIMEOUT = 500; // try to connect for X milliseconds to every resolved server
	private static final int CONN_TIMEOUT_MS = 1000; // timeout between messages
	private static final int CONN_MAX_FAILS = BEATsSettings.LIVESHARE_CLIENT_ALLOWEDFAILS.getIntValue(); // Max amount of failed receives before connection is closed
	public static final int TIMER_UPDATE_PERIOD_MS = 1 * 1000; // Update frequency in miliseconds
	private static final int TIMER_INITIAL_DELAY_MS = 10 * 1000; // initial startup time (a few seconds to give slow CPU's time to load more naturally)
	private static final Set<ConnectionsSetChangedListener> cscListeners = Collections
			.synchronizedSet(new HashSet<ConnectionsSetChangedListener>());
	
	private static LiveShareClient lvc;
	
	private final Timer UPDATE_TIMER = new Timer(this.getClass().getName() + "_timer",false);
	private List<String> watchServers;

	private final Map<String, Socket> connectedClients = new HashMap<String, Socket>();	
	private final Map<String, ObjectOutputStream> outputStreams = new HashMap<String, ObjectOutputStream>();	
	private final Map<String, ObjectInputStream> inputStreams = new HashMap<String, ObjectInputStream>();	
	private final Map<String, LiveShareSerializable> content = Collections.synchronizedMap(new HashMap<String, LiveShareSerializable>());
	private final Map<String, Integer> receiveFails = new HashMap<String, Integer>();
	private final Map<String, Instant> lastSuccessfullReceipt = new HashMap<String,Instant>();
	
	private final Set<DataChangedListener> dcListeners = Collections
			.synchronizedSet(new HashSet<DataChangedListener>());
	
	private boolean isStopping = false;

	private LiveShareClient(boolean start)
	{
		if(start) {
			UPDATE_TIMER.schedule(new ClientUpdateTask(), TIMER_INITIAL_DELAY_MS, TIMER_UPDATE_PERIOD_MS);
		}
	}

	public static void startClient(boolean start) {
		if(getLvc() == null)
			lvc = new LiveShareClient(start);
	}
	
	public static void stopClient() {
		getLvc().isStopping = true;
		getLvc().UPDATE_TIMER.cancel();
	}
	
	public boolean isStopping() {
		return isStopping;
	}

	public static LiveShareClient getLvc() {
		return lvc;
	}
	
	/**
	 * Do not modify the list of WatchServers
	 *
	 * @return List of WatchServers
	 */
	public List<String> getWatchServers() {
		return watchServers;
	}

	public List<String> getConnectedServerNames(){
		return Arrays.asList(connectedClients.keySet().toArray(new String[0]));
	}
	
	public static void addConnectionsSetChangedListener(ConnectionsSetChangedListener cscl) {
		synchronized (cscListeners) {
			cscListeners.add(cscl);
		}
	}

	public void fireConnectionsSetChanged() {
		// Prepare the list
		List<ConnectionsSetChangedListener> notifylist = new ArrayList<ConnectionsSetChangedListener>();
		synchronized (cscListeners) {
			/*cleanup list */
			while(cscListeners.remove(null)); // remove all null elements
			notifylist.addAll(cscListeners);
		}

		//needs to happen on EDT (listeners are supposed to be GUI elements)
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					for (ConnectionsSetChangedListener cscl : notifylist) {
						cscl.connectionsSetChanged(LiveShareClient.this);
					}
				}
			});
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void addDataChangedListener(DataChangedListener dcl) {
		synchronized (dcListeners) {
			dcListeners.add(dcl);
		}
	}
	
	public void fireDataChanged() {
		// Prepare the list
		List<DataChangedListener> notifylist = new ArrayList<DataChangedListener>();
		synchronized (dcListeners) {
			// cleanup list
			notifylist.remove(null);
			notifylist.addAll(dcListeners);
		}

		//needs to happen on EDT (listeners are supposed to be GUI elements)
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					for (DataChangedListener dcl : notifylist) {
						dcl.tableChanged();
					}
				}
			});
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public LiveShareSerializable getContent(String serverName) {
		synchronized (content) {
			return this.content.getOrDefault(serverName, LiveShareSerializable.createEmpty());
		}
	}
	
	/**
	 *
	 * @param serverName
	 * @return Instant of last full receipt of content or null if no content received yet
	 */
	public Instant getLastSuccessfullReceipt(String serverName) {
		return lastSuccessfullReceipt.getOrDefault(serverName, null);
	}

	private class ClientUpdateTask extends TimerTask {
		private boolean hasFirstRun = false; // Set to true when run() is first executed
		
		private boolean cleanupConnections() {
			boolean connectionsChanged = false;
			/* Cleanup closed connections & servers deleted from watchlists */
			List<String> cleanup = new ArrayList<String>();
			for(String server : connectedClients.keySet()) {	
				if(connectedClients.get(server).isClosed() || !watchServers.contains(server)) {
					connectionsChanged = true;
					cleanup.add(server);
				}
			}
			for(String c: cleanup) {
				try {
					if(!connectedClients.get(c).isClosed()) {
						connectedClients.get(c).close();
					}
					connectedClients.remove(c);
					inputStreams.remove(c);
					outputStreams.remove(c);
					content.remove(c);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return connectionsChanged;
		}

		@Override
		public void run() {
			// Refresh server list
			watchServers = BEATsSettings.LIVESHARE_CLIENT_HOSTLIST.getListValue();
			if(!hasFirstRun) {
				hasFirstRun = true;
				// Init of the listeners. Specifically for the GUI elements, so they can draw themselves if required
				fireConnectionsSetChanged();
			}
			
			// Prune servers that where deleted from watchlist
			Boolean connectionsChanged = cleanupConnections();
			// Connect to more servers if possible 
			if(watchServers.size() != connectedClients.size()) {
				// Missing servers list (copy + detract connected)
				List<String> missing = new ArrayList<String>(watchServers);
				missing.removeAll(connectedClients.keySet());
				for(String newServer : missing) {
					InetSocketAddress isa = new InetSocketAddress(newServer, LiveShareServer.SERVER_PORT);
					// for every server that can be resolved => setup connection
					if (isa != null && isa.isUnresolved() == false) {
						try (Socket s = new Socket()) {
							s.connect(isa, CONN_TRY_TIMEOUT);
							s.setSoTimeout(CONN_TIMEOUT_MS);
							if (!s.isBound() || s.isClosed())
								continue;
							try (ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream())) {
								// ObjectOutputStream before Input! Flush oos first before constructing ois!
								// (see JavaDoc)
								oos.flush();
								oos.writeObject(LiveShareMessage.BEATS_CONNECT_REQUEST);
								oos.flush();
								try (ObjectInputStream ois = new ObjectInputStream(s.getInputStream())) {
									Object o = ois.readObject();
									if (o instanceof LiveShareMessage && LiveShareMessage.BEATS_CONNECT_ACCEPTED.equals(o)) {
										Socket ns = new Socket(isa.getAddress(), ois.readInt());
										connectedClients.put(newServer , ns);
										receiveFails.put(newServer, 0);
										ObjectOutputStream noos = new ObjectOutputStream(ns.getOutputStream());
										noos.flush();
										outputStreams.put(newServer, noos);
										inputStreams.put(newServer, new ObjectInputStream(ns.getInputStream()));
										content.put(newServer, LiveShareSerializable.createEmpty());
										connectionsChanged = true;
									}
								}
							}

						} catch (SocketTimeoutException e1) {
							
						} catch (IOException | ClassNotFoundException e1) {
							e1.printStackTrace();
						} 
					}
				}
			}
			
			connectionsChanged |= cleanupConnections();
			
			// Update Data 
			for(String server : connectedClients.keySet()) {
				try {
					ObjectOutputStream oos = outputStreams.get(server);
					oos.flush();
					ObjectInputStream ois = inputStreams.get(server);
					// Request update
					oos.writeObject(LiveShareMessage.BEATS_DATA_REQUEST_FULL);
					oos.flush();
					// process update
					if (LiveShareMessage.BEATS_DATA_REQUEST_FULL.equals(ois.readObject())) {
						// Should the read object be null (so no connection, bad data, timeout, etc) then close the connection (cleanup happens later)
						LiveShareSerializable updatedLss = (LiveShareSerializable) ois.readObject();
						if(updatedLss != null) {
							content.put(server, updatedLss);
							receiveFails.put(server, 0);
							lastSuccessfullReceipt.put(server, Instant.now());
						} else if(receiveFails.get(server) < CONN_MAX_FAILS) {
							receiveFails.put(server, receiveFails.get(server)+1);
						} else {
							connectedClients.get(server).close();
						}
					}
				} catch (EOFException | SocketException e1) {
					// SocketExceptions are common: peer not available, connection closed, etc
					// EOFExceptions are also common: the full serialised object did not make it (EOF is also fatal, the stream is in an indeterminate state)
					try {
						connectedClients.get(server).close();
					} catch (IOException e2) {
						System.err.println("[LiveShareClient] Failed to close socket for host " + connectedClients.get(server) + " after connection failure.");
						e2.printStackTrace();
					}
				} catch (IOException | ClassNotFoundException e1) {
					e1.printStackTrace();
					try {
						connectedClients.get(server).close();
					} catch (IOException e2) {
						System.err.println("[LiveShareClient] Failed to close socket for host " + connectedClients.get(server) + " after connection failure.");
						e2.printStackTrace();
					}
				}			
			}
			
			connectionsChanged |= cleanupConnections();

			// Connections Changed 
			if(connectionsChanged ) {
				fireConnectionsSetChanged();
			}
			
			if(connectedClients.size() > 0)
				fireDataChanged();
		}
	}
}
