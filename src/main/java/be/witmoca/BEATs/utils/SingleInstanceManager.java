/*
*
+===============================================================================+
|    BEATs (Burning Ember Archival Tool suite)                                  |
|    Copyright 2019 Jente Heremans                                              |
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
* File: SingleInstanceManager.java
* Created: 2019
*/
package be.witmoca.BEATs.utils;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import javax.swing.SwingUtilities;

import be.witmoca.BEATs.connection.actions.LoadFileAction;

public class SingleInstanceManager implements Runnable {
	private static final int PORT = 41526;
	private static SingleInstanceManager runningManager;
	
	private final ServerSocket ss;
	
	
	public static boolean start(File loadFile) {
		try {
			ServerSocket socket = new ServerSocket(PORT);
			SingleInstanceManager tmp = new SingleInstanceManager(socket);
			(new Thread(tmp)).start();
			// This is the only instance at this point (ServerSocket constructor fails otherwise)
			runningManager = tmp;
			return true; 
		} catch (BindException b) {
			if (loadFile != null) {
				// This is not the only instance => send data and exit
				try (Socket s = new Socket((String) null, PORT)) {
					PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
					pw.print(loadFile.getAbsolutePath());
					pw.flush();
					pw.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/*
	 * Stops the singleInstanceManager
	 * Necessary when restarting the application
	 */
	public static void stopSingleInstanceManager() {
		if(runningManager != null) {
			runningManager.stopRunning();
		}
	}
	
	private void stopRunning() {
		if(!ss.isClosed()) {
			try {
				ss.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private SingleInstanceManager(ServerSocket ss) {
		this.ss = ss;
		try {
			ss.setSoTimeout(20);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (!ss.isClosed()) {
			Socket s = null;
			try {
				// socket object to receive incoming client requests
				s = ss.accept();
				BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
				File f = new File(in.readLine());
				if (f.exists() && f.isFile()) {
					// Load the file that was send through
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							LoadFileAction.getLoadFileAction(f)
									.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "load"));
						}
					});
				}
			} catch (SocketTimeoutException e1) {
				// timeout => reloop
			} catch (IOException e) {
				if(!ss.isClosed()) {
					try {
						s.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					e.printStackTrace();
				}
			}
		}
		if(!ss.isClosed()) {
			try {
				ss.close();
			} catch (IOException e) {
			}
		}
	}
}
