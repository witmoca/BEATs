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
* File: Launch.java
* Created: 2018
*/

package be.witmoca.BEATs;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import javax.swing.SwingUtilities;

import be.witmoca.BEATs.connection.actions.LoadFileAction;
import be.witmoca.BEATs.filefilters.BEATsFileFilter;
import be.witmoca.BEATs.liveshare.LiveShareClient;
import be.witmoca.BEATs.liveshare.LiveShareServer;
import be.witmoca.BEATs.ui.ApplicationWindow;
import be.witmoca.BEATs.ui.actions.ExitApplicationAction;
import be.witmoca.BEATs.utils.ResourceLoader;
import be.witmoca.BEATs.utils.SingleInstanceManager;
import be.witmoca.BEATs.utils.VersionChecker;
import be.witmoca.BEATs.utils.BEATsSettings;

public class ApplicationManager {
	private final File loadFile;

	public ApplicationManager(File loadFile) {
		this.loadFile = loadFile;
	}
	
	/**
	 * Start application
	 * @return true if application should be relaunched (restarted)
	 */
	public boolean launch() {		
		try {
			// Initialise Files & folders
			ResourceLoader.initFileTree();
			// Load (and install) userpreferences
			BEATsSettings.loadPreferences();
			// Check if single instance
			if (!SingleInstanceManager.start(loadFile))
				return false; // Already running
			// Register a new standard output
			ResourceLoader.registerStandardErrorLog();
			
			// Startup LiveShare server
			if (BEATsSettings.LIVESHARE_SERVER_ENABLED.getBoolValue())
				LiveShareServer.startServer();
			
			// Startup LiveShare client
			if (BEATsSettings.LIVESHARE_CLIENT_ENABLED.getBoolValue())
				LiveShareClient.startClient();
			
		} catch (IOException e) {
			fatalError(e);
			return false;
		}

		// Start the versionchecker
		VersionChecker.CheckVersion();
		
		// Create the GUI on the EDT
		FutureTask<ApplicationWindow> createGui = new FutureTask<ApplicationWindow>(new Callable<ApplicationWindow>() {
			@Override
			public ApplicationWindow call() throws Exception {
				// Load database
				LoadFileAction.getLoadFileAction(loadFile).actionPerformed(new ActionEvent(this, ActionEvent.ACTION_LAST, "load"));
				// create and return gui
				return ApplicationWindow.createAndShowUi();
			}
		});		

		SwingUtilities.invokeLater(createGui);
		
		// Wait for gui to be created
		ApplicationWindow appWindow;
		try {
			appWindow = createGui.get();
		} catch (InterruptedException | ExecutionException e1) {
			fatalError(e1);
			return false;
		} 
		
		// Make sure to reset previous value
		ExitApplicationAction.restartAfterClose.set(false);
		
		// wait for end of life
		synchronized(ExitApplicationAction.endOfLifeLock) {
			while(appWindow != null && appWindow.isVisible()) {
				try {
					ExitApplicationAction.endOfLifeLock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		return ExitApplicationAction.restartAfterClose.get();
	}
	

	// Start up
	public static void main(String[] args) {
		// Get the file to load from the arguments
		File loadFile = extractFileFromArgs(args);
		
		boolean restart = true;
		while (restart) {
			restart = (new ApplicationManager(loadFile)).launch();
		}
	}

	/**
	 * Fails the application and prints a stack trace in a dialog (only a valid
	 * function before any GUI is present)
	 * 
	 * @param e
	 */
	private static void fatalError(Exception e) {
		if (SwingUtilities.isEventDispatchThread()) {
			final StringWriter stacktraceW = new StringWriter();
			e.printStackTrace(new PrintWriter(stacktraceW, true));

			javax.swing.JOptionPane
					.showMessageDialog(null,
							e.getClass() + "\n" + e.getLocalizedMessage() + "\n\nStacktrace:\n"
									+ stacktraceW.getBuffer().toString(),
							"Fatal Error", javax.swing.JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		} else {
			// If not on EDT, then schedule for execution on EDT
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						fatalError(e);
					}
				});
			} catch (InvocationTargetException | InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * Searches for the first loadable database in the arguments
	 * 
	 * @param args the arguments passed to the application (usually the arguments of
	 *             the main method)
	 * @return the file to load, {@code null} if none was found
	 */
	private static File extractFileFromArgs(String[] args) {
		for (String s : args) {
			// Check if string is valid
			s = s.trim();
			if (s.isEmpty())
				continue;

			File test = new File(s);
			// check if the string denotes an existing file
			if (!test.exists() || !test.isFile())
				continue;

			// Check if the file filter accepts this as a valid file
			if ((new BEATsFileFilter()).accept(test))
				return test;
		}
		return null;
	}
}
