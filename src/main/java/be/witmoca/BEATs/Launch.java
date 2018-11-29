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

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.SwingUtilities;

import be.witmoca.BEATs.model.SQLConnection;
import be.witmoca.BEATs.ui.ApplicationWindow;

public class Launch {
	public static final String APP_FOLDER = System.getProperty("user.home") + File.separator + "BEATs";
	// Format : MMMmmmrrr with M = Major, m = minor & r = revision
	public static final int APP_VERSION = 000001000;

	private static ApplicationWindow APP_WINDOW = null;
	private static SQLConnection DB_CONN = null;

	public static void main(String[] args) {
		if (DB_CONN != null) {
			fatalError(new Exception("DB_CONN already loaded! Duplicate instance?"));
			return;
		}

		// Preset internal folders
		(new File(APP_FOLDER)).mkdirs();

		// Setup Internal memory
		try {
			DB_CONN = new SQLConnection();
		} catch (SQLException e) {
			fatalError(e);
			return;
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
	 * function before any gui is present)
	 * 
	 * @param e
	 */
	public static void fatalError(Exception e) {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					final StringWriter stacktraceW = new StringWriter();
					e.printStackTrace(new PrintWriter(stacktraceW, true));

					javax.swing.JOptionPane.showMessageDialog(null,
							e.getClass() + "\n" + e.getLocalizedMessage() + "\n\nStacktrace:\n"
									+ stacktraceW.getBuffer().toString(),
							"Fatal Error", javax.swing.JOptionPane.ERROR_MESSAGE);
				}
			});
		} catch (InvocationTargetException | InterruptedException e1) {
			e1.printStackTrace();
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
	public static void changeModel(String fileToLoad) throws SQLException {
		// TODO: Check saved state
		DB_CONN.close();
		if (fileToLoad == null) {
			DB_CONN = new SQLConnection();
		} else {
			DB_CONN = new SQLConnection(fileToLoad);
		}
		// TODO: Refresh the GUI
	}

	public static ApplicationWindow getAPP_WINDOW() {
		return APP_WINDOW;
	}

	public static SQLConnection getDB_CONN() {
		return DB_CONN;
	}

	/**
	 * Shortcut to get the Db contained in DB_CONN
	 * 
	 * @return
	 */
	public static Connection getDb() {
		return getDB_CONN().getDb();
	}
}
