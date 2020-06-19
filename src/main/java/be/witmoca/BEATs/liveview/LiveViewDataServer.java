/**
 * 
 */
package be.witmoca.BEATs.liveview;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import be.witmoca.BEATs.connection.DataChangedListener;
import be.witmoca.BEATs.connection.DataChangedType;
import be.witmoca.BEATs.connection.SQLConnection;
import be.witmoca.BEATs.utils.BEATsSettings;

/**
 * @author Witmoca
 *
 */
public class LiveViewDataServer implements Runnable, DataChangedListener {
	private static final AtomicInteger connectionCount = new AtomicInteger(0);
	private static final AtomicBoolean playlistChanged = new AtomicBoolean(true);
	private static final int CONN_TIMEOUT_MS = 10 * 1000;

	private static final Set<LiveViewDataServer> connections = Collections
			.synchronizedSet(new HashSet<LiveViewDataServer>());

	private final ServerSocket serverSocket;
	private Socket clientSocket_local;

	/**
	 * Spins off a dataserver
	 * 
	 * @return Port of the DataServer
	 * @throws IOException
	 */
	public static int startNewDataServer() throws IOException {
		// Anatomically check: over connection limit?
		if (connectionCount.getAndIncrement() >= BEATsSettings.LIVESHARE_SERVER_MAXCONNECTIONS.getIntValue()) {
			connectionCount.decrementAndGet();
			throw new IOException("Maximum connections limit reached!");
		}

		ServerSocket ss = new ServerSocket(0, 50); // Do not specifiy a host ip here! => The server should be visible on all Networks on the device
		(new Thread(new LiveViewDataServer(ss))).start();
		return ss.getLocalPort();
	}
	
	public static void closeAllDataServers() {
		synchronized(connections) {
			for(LiveViewDataServer lvds : connections) {
				try {
					if(!lvds.serverSocket.isClosed())
						lvds.serverSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * LiveViewServer guarantees that only one constructor is running at a time
	 * 
	 * @throws SocketException When connection limit reached
	 * 
	 */
	private LiveViewDataServer(ServerSocket socket) throws SocketException {
		this.serverSocket = socket;
	}

	@Override
	public void run() {
		connections.add(this);
		try {
			// Sets the timeout, so that serverSocket.accept() doesn't wait endlessly
			serverSocket.setSoTimeout(CONN_TIMEOUT_MS);
			// socket object to receive incoming client requests
			try (Socket clientSocket = serverSocket.accept()) {
				// keep track of the clienSocket (for metadata)
				this.clientSocket_local = clientSocket;
				// Set timeout
				clientSocket.setSoTimeout(CONN_TIMEOUT_MS);
				SQLConnection.getDbConn().addDataChangedListener(this, DataChangedType.PLAYLIST_DATA_OPTS);
				try (ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream())){
					// ObjectOutputStream before Input! Flush oos first before constructing ois! (see JavaDoc)
					oos.flush();
					try(ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream())){
						while (!clientSocket.isClosed()) {
							// Catch incoming connection requests
							LiveViewMessage request = (LiveViewMessage) ois.readObject();
							// Stop execution on timeout
							if(request == null)
								break;
							switch (request) {
								case BEATS_DATA_REQUEST_FULL:
									oos.writeObject(LiveViewMessage.BEATS_DATA_REQUEST_FULL);
									oos.writeObject(LiveViewSerializable.createSnapShot());
									oos.flush();
									playlistChanged.set(false);
									break;
								case BEATS_DATA_CHECK_CHANGED:
									oos.writeObject(LiveViewMessage.BEATS_DATA_CHECK_CHANGED);
									oos.writeBoolean(playlistChanged.get());
									oos.flush();
									break;
							default:
								break;							
							}
						}
					}
				}
			}
		} catch (SocketException e1) {
		} catch (IOException e) {
		} catch (ClassNotFoundException e) {
		}
		// Close datastream when done or error occurred
		connections.remove(this);
		connectionCount.decrementAndGet();
	}

	public String getClientHostName() {
		if (clientSocket_local != null && clientSocket_local.getInetAddress() != null)
			return this.clientSocket_local.getInetAddress().getHostName();
		return "-Not Connected-";
	}

	public String getClientIp() {
		if (clientSocket_local != null && clientSocket_local.getInetAddress() != null && !clientSocket_local.isClosed())
			return this.clientSocket_local.getInetAddress().getHostAddress();
		return "-Not Connected-";
	}

	@Override
	public void tableChanged() {
		playlistChanged.set(true);
	}

	/**
	 * Thread-safe: makes a copy of the connections set at this moment
	 * @return the connections
	 */
	public static LiveViewDataServer[] getConnections() {
		LiveViewDataServer[] cons;
		synchronized(connections) {
			cons = connections.toArray(new LiveViewDataServer[0]);
		}
		return cons;
	}
}
