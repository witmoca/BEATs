/**
 * 
 */
package be.witmoca.BEATs.liveview;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.Timer;

import be.witmoca.BEATs.utils.BEATsSettings;

/**
 * @author Witmoca
 *
 */
public class LiveViewClient implements ActionListener {
	private static final int CONN_TRY_TIMEOUT = 500; // try to connect for X milliseconds to every server
	private static final int CONN_TIMEOUT_MS = 10 * 1000; // timeout between messages
	
	private static LiveViewClient lvc;
	
	private final Timer UPDATE_TIMER = new Timer((int) TimeUnit.SECONDS.toMillis(2), this);
	private final List<InetSocketAddress> watchServers = new ArrayList<InetSocketAddress>();

	
	private LiveViewClient()
	{
		// TODO better saving of IP & PORT
		// TODO save HOSTNAME instead of IP & resolve hostname
		String ip = BEATsSettings.LIVESHARE_CLIENT_IP_LIST.getStringValue();
		int port = BEATsSettings.LIVESHARE_CLIENT_PORT_LIST.getIntValue();
		InetSocketAddress isa = new InetSocketAddress(ip, port);
		watchServers.add(isa);

		UPDATE_TIMER.start();
	}

	public static void startClient() {
		lvc = new LiveViewClient();
	}
	
	public static void stopClient() {
		lvc.UPDATE_TIMER.stop();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		for (InetSocketAddress isa : watchServers) {
			// for every server that can be resolved and has no LiveViewDataClient associated
			if (isa.isUnresolved() == false && LiveViewDataClient.isClientRunning(isa.getAddress()) == false) {
				try (Socket s = new Socket()) {
					s.connect(isa, CONN_TRY_TIMEOUT);
					s.setSoTimeout(CONN_TIMEOUT_MS);
					if (!s.isBound() || s.isClosed())
						continue;
					try (ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream())) {
						// ObjectOutputStream before Input! Flush oos first before constructing ois!
						// (see JavaDoc)
						oos.flush();
						oos.writeObject(LiveViewMessage.BEATS_CONNECT_REQUEST);
						oos.flush();
						try (ObjectInputStream ois = new ObjectInputStream(s.getInputStream())) {
							Object o = ois.readObject();
							if (o instanceof LiveViewMessage && LiveViewMessage.BEATS_CONNECT_ACCEPTED.equals(o)) {
								LiveViewDataClient.startNewDataClient(isa.getAddress(), ois.readInt());
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
}
