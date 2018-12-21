/**
 * 
 */
package be.witmoca.BEATs;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import be.witmoca.BEATs.actions.SaveFileAction;
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
	 * Performs recovery when necessary.
	 * 
	 * @throws IOException if initialisation failed making application execution impossible.
	 */
	public static void initFileSystem() throws IOException {
		initFileTree();
		
		//check if recovery is needed
		if (databaseExists()) {
			try {
				recoverDatabase();
			} catch (InvocationTargetException e) {
				if(e.getTargetException() instanceof IOException)
					throw (IOException) e.getTargetException();
				else
					throw new IOException(e);
			} catch (InterruptedException e) {
				throw new IOException(e);
			} 
			// check if recovery made continuing possible
			if(databaseExists())
				throw new IOException("Recovery: database was not deleted.\nYou can find the database at " + DB_LOC);
		}
	}

	/**
	 * Initialises the File/Folder tree needed for operation.
	 * 
	 * @throws IOException if the necessary tree could not be created.
	 */
	private static void initFileTree() throws IOException {
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
	private static boolean databaseExists() {
		return (new File(DB_LOC)).exists();
	}

	/**
	 * Attempts to recover the database. If recoverable, the user is asked to save
	 * or delete. If not recoverable, the user is asked to delete or keep the
	 * corrupted database.
	 * 
	 * {@link FileManager#databaseExists()} should be checked to see if the database
	 * was deleted
	 * 
	 * @throws InterruptedException      if we're interrupted while waiting for the
	 *                                   event dispatching thread to finish (this
	 *                                   function executed on the EDT)
	 * @throws InvocationTargetException if an exception is thrown
	 */
	private static void recoverDatabase() throws InvocationTargetException, InterruptedException {
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				try (SQLConnection rec = SQLConnection.recoverDatabase()) {

					String message = "A recoverable database has been detected. This indicates that an error occurred and an incorrect shutdown occurred last time."
							+ "\nIt is highly recommended that you save this database before continuing. You can also choose to delete this database and continue normal operation.";
					int response = JOptionPane.showOptionDialog(null, message, "Recovery", JOptionPane.DEFAULT_OPTION,
							JOptionPane.PLAIN_MESSAGE, null, null, message);
					if (response != JOptionPane.CLOSED_OPTION
							|| JOptionPane.showConfirmDialog(null, "Are you sure you wish to delete?", "Delete",
									JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
						SaveFileAction sfa = new SaveFileAction();
						while (!sfa.hasSucceeded()) {
							sfa.actionPerformed(null);
							if (JOptionPane.showConfirmDialog(null,
									"You did not save the database.\nAre you sure you do not want do save the database? If you do not, it will be deleted.",
									"Not saved", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
								break;
							}
						}
					}
				} catch (SQLException e) {
					if (JOptionPane.showConfirmDialog(null,
							"An incorrect shutdown was detected.\nThis corrupted the database that was being worked upon.\nDo you wish to delete this database and continue normal operation?",
							"Corrupted", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
						return;
					}
				}
				try {
					deleteDb();
				} catch (IOException e) {
					// Delete db failed (catch this by polling again for a recoverable database)
				}
				return;
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
