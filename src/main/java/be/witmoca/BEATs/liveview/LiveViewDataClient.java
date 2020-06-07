/**
 * 
 */
package be.witmoca.BEATs.liveview;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.Timer;

/**
 * @author Witmoca
 *
 */
public class LiveViewDataClient implements ActionListener {
	private static final int CONN_TIMEOUT_MS = 10 * 1000;
	protected static final Set<LiveViewDataClient> connections = Collections
			.synchronizedSet(new HashSet<LiveViewDataClient>());

	private final Timer UPDATE_TIMER = new Timer((int) TimeUnit.SECONDS.toMillis(1), this);
	private final Socket s;
	private final ObjectOutputStream oos;
	private final ObjectInputStream ois;
	private final AtomicBoolean updatePlaylist = new AtomicBoolean(true);

	private LiveViewSerializable content = LiveViewSerializable.createEmpty();

	private LiveViewDataClient(Socket s) throws IOException {
		this.s = s;
		s.setSoTimeout(CONN_TIMEOUT_MS);
		oos = new ObjectOutputStream(s.getOutputStream());
		// ObjectOutputStream before Input! Flush oos first before constructing ois!
		// (see JavaDoc)
		oos.flush();
		ois = new ObjectInputStream(s.getInputStream());

		UPDATE_TIMER.start();
	}

	public static synchronized void startNewDataClient(InetAddress address, int port) {
		if (!isClientRunning(address)) {
			try {
				connections.add(new LiveViewDataClient(new Socket(address, port)));
			} catch (IOException e) {
			}
		}
	}

	/**
	 * 
	 * @param address
	 * @return True if a client for this address is already running
	 */
	public static boolean isClientRunning(InetAddress address) {
		cleanupConnections();
		synchronized (connections) {
			// check if an lvdc exists for this address
			for (LiveViewDataClient lvdc : connections) {
				if (lvdc.getInetAddress().equals(address)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private static void cleanupConnections() {
		synchronized(connections) {
			// create list of LVDC's to delete
			List<LiveViewDataClient> gcList = new ArrayList<LiveViewDataClient>();
			for (LiveViewDataClient lvdc : connections) {
				if(lvdc.s.isClosed()) {
					gcList.add(lvdc);
				}
			}
			connections.removeAll(gcList);
		}
	}

	public InetAddress getInetAddress() {
		return s.getInetAddress();
	}
	
	public LiveViewSerializable getContent() {
		synchronized(content) {
			return this.getContent().clone();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (s.isClosed()) {
			// stop this client
			UPDATE_TIMER.stop();
			UPDATE_TIMER.removeActionListener(this);
			connections.remove(this);
			return;
		}
		try {
			// clean buffer
			oos.flush();
			// check if data updated
			oos.writeObject(LiveViewMessage.BEATS_DATA_CHECK_CHANGED);
			oos.flush();
			if (LiveViewMessage.BEATS_DATA_CHECK_CHANGED.equals(ois.readObject()) && ois.readBoolean() == true) {
				updatePlaylist.set(true);
			}
			// update data if necessary
			if (updatePlaylist.get()) {
				oos.writeObject(LiveViewMessage.BEATS_DATA_REQUEST_FULL);
				oos.flush();
				if (LiveViewMessage.BEATS_DATA_REQUEST_FULL.equals(ois.readObject())) {
					synchronized (content) {
						content = (LiveViewSerializable) ois.readObject();
						System.out.println(content.getPlaylists());
					}
				}
				updatePlaylist.set(false);
			}

		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
	}
}
