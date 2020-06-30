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
import be.witmoca.BEATs.discovery.DiscoveryServer;
import be.witmoca.BEATs.utils.BEATsSettings;

/**
 * @author Witmoca
 *
 */
public class LiveShareClient implements ActionListener {
	private static final int CONN_TRY_TIMEOUT = 500; // try to connect for X milliseconds to every resolved server
	private static final int CONN_TIMEOUT_MS = 1000; // timeout between messages
	
	private static LiveShareClient lvc;
	
	private final Timer UPDATE_TIMER = new Timer((int) TimeUnit.SECONDS.toMillis(1), this);
	private final List<String> watchServers = BEATsSettings.LIVESHARE_CLIENT_HOSTLIST.getListValue();
	private final Map<String, Socket> connectedClients = new HashMap<String, Socket>();	
	private final Map<String, ObjectOutputStream> outputStreams = new HashMap<String, ObjectOutputStream>();	
	private final Map<String, ObjectInputStream> inputStreams = new HashMap<String, ObjectInputStream>();	
	private final Map<String, LiveShareSerializable> content = Collections.synchronizedMap(new HashMap<String, LiveShareSerializable>());
	private final Set<ConnectionsSetChangedListener> cscListeners = Collections
			.synchronizedSet(new HashSet<ConnectionsSetChangedListener>());
	private final Set<DataChangedListener> dcListeners = Collections
			.synchronizedSet(new HashSet<DataChangedListener>());
	
	private LiveShareClient()
	{
		UPDATE_TIMER.start();
	}

	public static void startClient() {
		lvc = new LiveShareClient();
	}
	
	public static void stopClient() {
		lvc.UPDATE_TIMER.stop();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Boolean connectionsChanged = false;
		/* Connect to more servers if possible */
		if(watchServers.size() != connectedClients.size()) {
			// turn on broadcaster to find servers
			DiscoveryServer.startBroadcaster();
			// Missing servers list (copy + detract connected)
			List<String> missing = Arrays.asList(watchServers.toArray(new String[0]));
			missing.removeAll(connectedClients.keySet());
			for(String newServer : missing) {
				InetSocketAddress isa = DiscoveryServer.resolveHostToAddress(newServer);
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
		} else {
			// No need to find anymore servers
			DiscoveryServer.stopBroadcaster();
		}
		
		
		/* Cleanup connections */
		List<String> cleanup = new ArrayList<String>();
		for(String server : connectedClients.keySet()) {	
			if(connectedClients.get(server).isClosed()) {
				cleanup.add(server);
				connectionsChanged = true;
			}
		}
		for(String c: cleanup) {
			connectedClients.remove(c);
			inputStreams.remove(c);
			outputStreams.remove(c);
			content.remove(c);
		}
		
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
					content.put(server, (LiveShareSerializable) ois.readObject());
				}			
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}			
		}

		/* Connections Changed */
		if(connectionsChanged ) {
			fireConnectionsSetChanged();
		}
		
		if(connectedClients.size() > 0)
			fireDataChanged();
	}
	
	public List<String> getConnectedServerNames(){
		return  Arrays.asList(connectedClients.keySet().toArray(new String[0]));
	}
	
	public static void addConnectionsSetChangedListener(ConnectionsSetChangedListener cscl) {
		synchronized (lvc.cscListeners) {
			lvc.cscListeners.add(cscl);
		}
	}

	public static void fireConnectionsSetChanged() {
		List<ConnectionsSetChangedListener> notifylist = new ArrayList<ConnectionsSetChangedListener>();
		synchronized (lvc.cscListeners) {
			/*cleanup list */
			lvc.cscListeners.remove(null);
			notifylist.addAll(lvc.cscListeners);
		}
		for (ConnectionsSetChangedListener cscl : notifylist) {
			cscl.connectionsSetChanged(lvc);
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
			return this.content.get(serverName);
		}
	}
}
