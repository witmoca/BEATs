/**
 * 
 */
package be.witmoca.BEATs.liveview;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import be.witmoca.BEATs.utils.BEATsSettings;

/**
 * @author Witmoca
 *
 */
public class LiveViewServer implements Runnable {
	private static final int PORT = BEATsSettings.LIVESHARE_PORT.getIntValue();
	private final ServerSocket serverSocket;

	private LiveViewServer(ServerSocket socket) {
		this.serverSocket = socket;
	}

	public static void startServer() throws IOException {
		(new Thread(new LiveViewServer(new ServerSocket(PORT, 50, InetAddress.getLocalHost())))).start();
	}

	@Override
	public void run() {
		while (true) {
			// socket object to receive incoming client requests
			try (Socket clientSocket = serverSocket.accept();
					ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream())) {
				// ObjectOutputStream before Input! Flush oos first before constructing ois! (see JavaDoc)
				oos.flush();
				try(ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream())){
					// Catch incoming connection requests
					if (ois.readObject().equals(LiveViewMessage.BEATS_CONNECT_REQUEST)) {
						// start new connection handler and return the port for the client to connect to
						int port = LiveViewDataServer.startNewDataServer();
						oos.writeObject(LiveViewMessage.BEATS_CONNECT_ACCEPTED);
						oos.writeInt(port);
						oos.flush();
					}
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
			}
		}
	}
}
