/**
 * 
 */
package be.witmoca.BEATs;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.concurrent.CompletionException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import be.witmoca.BEATs.model.SQLConnection;

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
* File: FileManager.java
* Created: 2018
*/
final public class FileManager {
	public static final String APP_FOLDER = System.getProperty("user.home") + File.separator + "BEATs";
	public static final String DB_LOC = APP_FOLDER + File.separator + "currentDocument.beats";

	/**
	 * Initialises the File/Folder tree needed for operation.
	 * 
	 * @throws IOException if the necessary tree could not be created.
	 */
	public static void initFileTree() throws IOException {
		try {
			// create root
			File root = new File(APP_FOLDER);
			root.mkdirs();
			if (!root.exists() || !root.isDirectory())
				throw new IOException("Root folder " + APP_FOLDER + " doesn't exist or is not a directory");
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	/**
	 * 
	 * @return {@code true} if a database exists (making recovery possible).
	 */
	public static boolean databaseExists() {
		return (new File(DB_LOC)).exists();
	}

	/**
	 * Attempts to recover the database. If no exception is thrown, the database is assumed to be recoverable.
	 * 
	 * 
	 * {@link FileManager#databaseExists()} should be checked to see if the database
	 * was deleted
	 * 
	 * @throws InterruptedException      if we're interrupted while waiting for the
	 *                                   event dispatching thread to finish (this
	 *                                   function executed on the EDT)
	 * @throws InvocationTargetException if an exception is thrown
	 */
	public static void recoverDatabase() throws InvocationTargetException, InterruptedException {
		SwingUtilities.invokeAndWait(() -> {
				try (SQLConnection rec = SQLConnection.recoverDatabase()) {
					
					JOptionPane.showMessageDialog(null, "Recovered database!\nBurning Ember detected an incorrect shutdown.\nThe database from that session has been recovered.", "Recovery", JOptionPane.WARNING_MESSAGE);
				} catch (SQLException e) {
					JOptionPane.showMessageDialog(null, "Corrupted database!\nBurning Ember detected an incorrect shutdown.\nThe database from that session has been corrupted.\nPlease recover this database manually or delete it.\nLocation: " + DB_LOC, "Recovery", JOptionPane.WARNING_MESSAGE);
					throw new CompletionException(e);
				}
		});
	}

	/**
	 * Deletes the internal storage database.
	 * 
	 * @throws IOException if the database could not be deleted.
	 */
	public static void deleteDb() throws IOException {
		if (!(new File(DB_LOC)).delete())
			throw new IOException("Could not cleanup BEATS internal storage (" + DB_LOC + ")");
	}

}
