/**
 * 
 */
package be.witmoca.BEATs.liveview;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import be.witmoca.BEATs.utils.BEATsSettings;

/**
 * @author Witmoca
 *
 */
public class LiveViewServer implements Runnable {
	private static final int PORT = BEATsSettings.LIVESHARE_PORT.getIntValue();
	private static LiveViewServer currentServer;
	private final ServerSocket serverSocket;

	private LiveViewServer(ServerSocket socket) {
		this.serverSocket = socket;
	}

	public static void startServer() throws IOException {
		currentServer = new LiveViewServer(new ServerSocket(PORT, 50)); // Do not specifiy a host ip here! => The server should be visible on all Networks on the device
		(new Thread(currentServer)).start();
	}
	
	public static void closeServer() {
		if(currentServer != null && !currentServer.serverSocket.isClosed()) {
			try {
				currentServer.serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run() {
		while (!serverSocket.isClosed()) {
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
			} catch (EOFException e) {
				// ignore (some other program might have tried to connect or data just didn't get through fully)
			} catch (IOException e) {
				if(!serverSocket.isClosed()) {
					e.printStackTrace();
				}
			}
		}
	}
}
