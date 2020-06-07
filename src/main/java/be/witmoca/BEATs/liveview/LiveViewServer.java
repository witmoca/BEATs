/**
 * 
 */
package be.witmoca.BEATs.liveview;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import be.witmoca.BEATs.utils.BEATsSettings;

/**
 * @author Witmoca
 *
 */
public class LiveViewServer implements Runnable{
	private static final int PORT = BEATsSettings.LIVESHARE_PORT.getIntValue();
	private final ServerSocket serverSocket;
	
	private LiveViewServer(ServerSocket socket) {
		this.serverSocket = socket;
	}
	
	public static void startServer() throws IOException {
		(new Thread(new LiveViewServer(new ServerSocket(PORT, 50 , InetAddress.getLocalHost())))).start();
	}
	
	@Override
	public void run() {
		while (true) {
			// socket object to receive incoming client requests
			try (Socket clientSocket = serverSocket.accept()) {
				try(BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))){
					// Catch incomming connection requests
					if(in.readLine().equals("<BEATS_CONNECT_REQUEST>")) {
						try(BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))) {
							// Start a new thread that will provide data and return the Port nr to the client
							int port = LiveViewDataServer.startNewDataServer();
							out.write("<BEATS_CONNECT_ACCEPTED>");
							out.newLine();
							out.write(Integer.toString(port));
							out.newLine();
							out.flush();
						}
					}
				}
			} catch (IOException e) {
				// Nothing to do here
			} 
		}		
	}
}
