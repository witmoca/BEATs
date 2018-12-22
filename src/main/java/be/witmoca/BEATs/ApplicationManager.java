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
import java.sql.SQLException;

import javax.swing.SwingUtilities;

import be.witmoca.BEATs.FileFilters.BEATsFileFilter;
import be.witmoca.BEATs.actions.ExitApplicationAction;
import be.witmoca.BEATs.connection.ConnectionException;
import be.witmoca.BEATs.connection.SQLConnection;
import be.witmoca.BEATs.ui.ApplicationWindow;

public class ApplicationManager {
	// Format : MMMmmmrrr with M = Major, m = minor & r = revision (do not lead with zeros, as this is interpreted as octal)
	public static final int APP_VERSION = 1000;

	private static ApplicationWindow APP_WINDOW = null;
	private static SQLConnection DB_CONN = null;

	// Start up
	public static void main(String[] args) {		
		// Get the file to load from the arguments
		File loadFile = extractFileFromArgs(args);
		
		
		try {
			// Initialise Files & folders
			FileManager.initFileTree();
			
			// Setup Internal memory (new or load from file)
			DB_CONN = new SQLConnection(loadFile);
			
		} catch (IOException | ConnectionException e) {
			fatalError(e);
			return;
			// TODO: add in user dialogues for the different methods of failure (all ConnectionException.ConnState types included)
		}
		
		// Create the GUI on the EDT
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				APP_WINDOW = new ApplicationWindow();
			}
		});
	}

	/**
	 * Fails the application and prints a stack trace in a dialog (only a valid
	 * function before any GUI is present)
	 * 
	 * @param e
	 */
	public static void fatalError(Exception e) {
		if (SwingUtilities.isEventDispatchThread()) {
			final StringWriter stacktraceW = new StringWriter();
			e.printStackTrace(new PrintWriter(stacktraceW, true));

			javax.swing.JOptionPane.showMessageDialog(null,
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
	 * Closes the current model and loads a new one.
	 * 
	 * @param fileToLoad
	 *            The Database to load. If this is null, the application will load
	 *            an empty file.
	 * @throws SQLException
	 */
	public static void changeModel(File load) {
		if(!SwingUtilities.isEventDispatchThread())
			throw new RuntimeException("Launch.changeModel called from outside the EDT!");
		
		// Do an 'exit'
		ExitApplicationAction eaa = new ExitApplicationAction();
		eaa.actionPerformed(new ActionEvent(ApplicationManager.class, ActionEvent.ACTION_PERFORMED, "changeModel"));
		if(!eaa.hasSucceeded()) {
			// Cancelled or Error
			return;
		}

		// Load new Database Connection
		try {
			DB_CONN = new SQLConnection(load);
			if(DB_CONN.isRecoveredDb()) {
				// TODO: display recovered DB message
			}
		} catch (ConnectionException e) {
			fatalError(e);
			return;
		}

		// Start GUI
		APP_WINDOW = new ApplicationWindow();
	}

	/**
	 * Searches for the first loadable database in the arguments
	 * 
	 * @param args the arguments passed to the application (usually the arguments of the main method)
	 * @return the file to load, {@code null} if none was found
	 */
	private static File extractFileFromArgs(String[] args) {
		for(String s : args) {
			// Check if string is valid
			s = s.trim();
			if(s.isEmpty())
				continue;
			
			File test = new File(s);
			// check if the string denotes an existing file
			if(!test.exists() || !test.isFile())
				continue;
			
			// Check if the file filter accepts this as a valid file
			if((new BEATsFileFilter()).accept(test))
				return test;
		}
		return null;
	}
	
	public static ApplicationWindow getAPP_WINDOW() {
		return APP_WINDOW;
	}

	public static SQLConnection getDB_CONN() {
		return DB_CONN;
	}
}
