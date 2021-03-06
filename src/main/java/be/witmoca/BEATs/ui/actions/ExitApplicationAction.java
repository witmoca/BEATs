/*
*
+===============================================================================+
|    BEATs (Burning Ember Archival Tool suite)                                  |
|    Copyright 2018 Jente Heremans                                              |
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
* File: ExitApplicationAction.java
* Created: 2018
*/
package be.witmoca.BEATs.ui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicBoolean;

import be.witmoca.BEATs.connection.ConnectionException;
import be.witmoca.BEATs.connection.SQLConnection;
import be.witmoca.BEATs.connection.actions.CheckFileSavedAction;
import be.witmoca.BEATs.discovery.DiscoveryServer;
import be.witmoca.BEATs.liveshare.LiveShareClient;
import be.witmoca.BEATs.liveshare.LiveShareDataServer;
import be.witmoca.BEATs.liveshare.LiveShareServer;
import be.witmoca.BEATs.ui.ApplicationWindow;
import be.witmoca.BEATs.utils.SingleInstanceManager;

public class ExitApplicationAction implements ActionListener {
	public static final Object endOfLifeLock = new Object();
	public static final AtomicBoolean restartAfterClose = new AtomicBoolean(true);
	
	
	public ExitApplicationAction(boolean restart) {
		restartAfterClose.set(restart);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		// Check if data has changed
		CheckFileSavedAction check = new CheckFileSavedAction();
		check.actionPerformed(e);
		// check if action is cancelled or allowed to continue
		if (!check.hasSucceeded())
			return;

		// KILL LiveShare Server & data providers
		LiveShareServer.closeServer();
		LiveShareDataServer.closeAllDataServers();
		
		// KILL LiveShare Client & data pollerss
		LiveShareClient.stopClient();
		
		// Kill Discovery
		DiscoveryServer.stopServer();

		// KILL DB Connection
		try {
			SQLConnection.getDbConn().close();
		} catch (ConnectionException e1) {
			e1.printStackTrace();
		}

		// Kill GUI
		synchronized(endOfLifeLock) {
			ApplicationWindow.getAPP_WINDOW().dispose();
			SingleInstanceManager.stopSingleInstanceManager();
			endOfLifeLock.notify();
		}
	}
}
