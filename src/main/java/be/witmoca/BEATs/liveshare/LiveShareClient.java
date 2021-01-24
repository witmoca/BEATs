/**
 * 
 */
package be.witmoca.BEATs.liveshare;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.swing.Timer;

import be.witmoca.BEATs.connection.DataChangedListener;
import be.witmoca.BEATs.utils.BEATsSettings;

/**
 * @author Witmoca
 *
 */
public class LiveShareClient implements ActionListener {
	private static final int CONN_TRY_TIMEOUT = 500; // try to connect for X milliseconds to every resolved server
	private static final int CONN_TIMEOUT_MS = 1000; // timeout between messages
	private static final int TIMER_UPDATE_PERIOD_S = 1; // Update frequency in seconds
	private static final int TIMER_INITIAL_DELAY_MS = 10 * 1000; // initial startup time (a few seconds to give slow CPU's time to load more naturally)
	private static final Set<ConnectionsSetChangedListener> cscListeners = Collections
			.synchronizedSet(new HashSet<ConnectionsSetChangedListener>());
	
	private static LiveShareClient lvc;
	
	private final Timer UPDATE_TIMER = new Timer((int) TimeUnit.SECONDS.toMillis(TIMER_UPDATE_PERIOD_S), this);
	private List<String> watchServers;
	private final Map<String, Socket> connectedClients = new HashMap<String, Socket>();	
	private final Map<String, ObjectOutputStream> outputStreams = new HashMap<String, ObjectOutputStream>();	
	private final Map<String, ObjectInputStream> inputStreams = new HashMap<String, ObjectInputStream>();	
	private final Map<String, LiveShareSerializable> content = Collections.synchronizedMap(new HashMap<String, LiveShareSerializable>());
	
	private final Set<DataChangedListener> dcListeners = Collections
			.synchronizedSet(new HashSet<DataChangedListener>());
	
	private LiveShareClient(boolean start)
	{
		if(start) {
			UPDATE_TIMER.setInitialDelay(TIMER_INITIAL_DELAY_MS);
			UPDATE_TIMER.start();
		}
	}

	public static void startClient(boolean start) {
		if(getLvc() == null)
			lvc = new LiveShareClient(start);
	}
	
	public static void stopClient() {
		getLvc().UPDATE_TIMER.stop();
	}
	
	public static LiveShareClient getLvc() {
		return lvc;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// Refresh server list
		watchServers = BEATsSettings.LIVESHARE_CLIENT_HOSTLIST.getListValue();
		
		// Prune servers that where deleted from watchlist
		
		Boolean connectionsChanged = cleanupConnections();
		/* Connect to more servers if possible */
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
		
		/* Update Data */
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
					if(updatedLss != null)
						content.put(server, updatedLss);
					else {
						connectedClients.get(server).close();
					}
				}
			} catch (SocketException e1) {
				System.err.println("LiveShareClient SocketException:\n" + e1);
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}			
		}
		
		connectionsChanged |= cleanupConnections();

		/* Connections Changed */
		if(connectionsChanged ) {
			fireConnectionsSetChanged();
		}
		
		if(connectedClients.size() > 0)
			fireDataChanged();
	}
	
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
	
	public List<String> getConnectedServerNames(){
		return Arrays.asList(connectedClients.keySet().toArray(new String[0]));
	}
	
	public static void addConnectionsSetChangedListener(ConnectionsSetChangedListener cscl) {
		synchronized (cscListeners) {
			cscListeners.add(cscl);
		}
	}

	public void fireConnectionsSetChanged() {
			List<ConnectionsSetChangedListener> notifylist = new ArrayList<ConnectionsSetChangedListener>();
			synchronized (cscListeners) {
				/*cleanup list */
				while(cscListeners.remove(null)); // remove all null elements
				notifylist.addAll(cscListeners);
			}
			for (ConnectionsSetChangedListener cscl : notifylist) {
				cscl.connectionsSetChanged(this);
		}
	}

	public void addDataChangedListener(DataChangedListener dcl) {
		synchronized (dcListeners) {
			dcListeners.add(dcl);
		}
	}
	
	public void fireDataChanged() {
		List<DataChangedListener> notifylist = new ArrayList<DataChangedListener>();
		synchronized (dcListeners) {
			// cleanup list
			notifylist.remove(null);
			notifylist.addAll(dcListeners);
		}
		for (DataChangedListener dcl : notifylist) {
			dcl.tableChanged();
		}
	}
	
	public LiveShareSerializable getContent(String serverName) {
		synchronized (content) {
			return this.content.getOrDefault(serverName, LiveShareSerializable.createEmpty());
		}
	}
}
