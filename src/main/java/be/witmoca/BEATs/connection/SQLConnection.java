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
* File: SQLConnection.java
* Created: 2018
*/
package be.witmoca.BEATs.connection;

import java.io.File;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteConfig.LockingMode;
import org.sqlite.SQLiteConnection;
import org.sqlite.SQLiteErrorCode;

import be.witmoca.BEATs.ApplicationManager;
import be.witmoca.BEATs.utils.ResourceLoader;

public class SQLConnection implements AutoCloseable {
	private static final int APPLICATION_ID = 0x77776462;
	private static final String DB_LOC = ResourceLoader.DB_LOC;
	private static boolean recoveredDb = false; 

	private static SQLConnection DbConn = null;

	private final SQLiteConnection Db; // Internal Connection
	private File currentFile; // Considered MetaData
	
	private boolean changedState = false;
	private boolean backupState = false;
	private Map<DataChangedListener, EnumSet<DataChangedType>> dataListeners = new HashMap<>();

	/**
	 * Loads a new internal Db as internal memory after closing the old one. See
	 * {@link SQLConnection#SQLConnection(File)}.
	 * 
	 * @see SQLConnection#SQLConnection(File)
	 * @param loadFile
	 * @throws ConnectionException
	 */
	public static void loadNewInternalDb(File loadFile) throws ConnectionException {
		Map<DataChangedListener, EnumSet<DataChangedType>> listenerMap = null;

		// deal with the old connection (and log any listeneres on that connection)
		if (DbConn != null) {
			listenerMap = new HashMap<>(DbConn.getDataListeners());
			try {
				DbConn.close();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new ConnectionException(ConnectionException.ConnState.GENERAL_EXCEPTION, e);
			}
		}

		// Create new connection
		DbConn = new SQLConnection(loadFile);

		// Attach the old listeners if there where any
		if (listenerMap != null) {
			for (DataChangedListener lst : listenerMap.keySet()) {
				DbConn.addDataChangedListener(lst, listenerMap.get(lst));
			}
		}

		// Notify listeners
		DbConn.announceDataRefresh();
		
		// Start backups
		BackupHandler.StartBackups();
	}

	/**
	 * Opens a connection to the internal database, performs a recovery if necessary
	 * (overrides loadFile behaviour), loads an external db into the internal db,
	 * Performs sanity checks and optimisation
	 * 
	 * @param loadFile the file to load or {@code null} for an empty db
	 * @throws ConnectionException thrown when the connection failed to establish
	 */
	private SQLConnection(File loadFile) throws ConnectionException {
		boolean dbExists = (new File(DB_LOC)).exists();
		this.currentFile = (loadFile == null ? null : loadFile.getAbsoluteFile());
		// Check if a lock exists on the the database (and create a connection to said
		// db)
		// This is done by execution code that changes on the tables (create tables
		// usually)
		// And waiting for an SQLITE_BUSY
		try {
			Db = (SQLiteConnection) DriverManager.getConnection("jdbc:sqlite:" + DB_LOC,
					configSettings(dbExists).toProperties());
			Db.setAutoCommit(false);
			createTables();
			Db.commit();
		} catch (SQLException e) {
			// all codes above 8 bits are extended codes (the lowest 8 bits still represent
			// the 'major' error code)
			if ((e.getErrorCode() & 255) == SQLiteErrorCode.SQLITE_BUSY.code) {
				// db is busy => another instance is running
				throw new ConnectionException(ConnectionException.ConnState.DB_ALREADY_LOCKED, e);
			}
			throw new ConnectionException(ConnectionException.ConnState.GENERAL_EXCEPTION, e);
		}

		// if beyond this point and dbExists => recovery
		if (dbExists) {
			recoveredDb = true;
			loadFile = null;
			this.setChanged();
		} else {
			recoveredDb = false;
		}
		if (loadFile != null) {
			try (Statement load = Db.createStatement()) {
				load.executeUpdate("restore from " + loadFile.getAbsolutePath());
			} catch (SQLException e) {
				try {
					this.close();
				} catch (SQLException e1) {
					throw new ConnectionException(ConnectionException.ConnState.GENERAL_EXCEPTION, e1);
				}
				throw new ConnectionException(ConnectionException.ConnState.GENERAL_EXCEPTION, e);
			}
		}

		this.contentCheck();
		this.vacuum();
	}

	public void saveDatabase(String savePath, boolean isBackup) throws SQLException {
		File saveFile = new File(savePath).getAbsoluteFile();
		
		// Call optimize first
		try (Statement optimize = Db.createStatement()) {
			optimize.execute("PRAGMA optimize");
		}

		try (Statement save = Db.createStatement()) {
			save.executeUpdate("backup to " + saveFile.getPath());
		}
		if(isBackup) {
			this.setBackedUp();
		} else {
			this.currentFile = saveFile;
			this.setSaved();
			
			// loadedLocation has been changed
			notifyDataChangedListeners(EnumSet.of(DataChangedType.META_DATA));
		}
	}

	/**
	 * Create the connection settings for the sqlite connection
	 * 
	 * @param existing {@code true} when the db already exists, {@code false} for
	 *                 new db's
	 * @return the configuration object
	 */
	private SQLiteConfig configSettings(boolean existing) {
		SQLiteConfig config = new SQLiteConfig();
		if (!existing) {
			// Add application_id & user_version (only add these on a new db)
			config.setUserVersion(ApplicationManager.APP_VERSION);
			config.setApplicationId(APPLICATION_ID);
		}
		// Enforce foreign key correctness
		config.enforceForeignKeys(true);
		// Make sure only one application has access to the database
		config.setBusyTimeout(0);
		config.setLockingMode(LockingMode.EXCLUSIVE);

		return config;
	}

	private void createTables() throws SQLException {
		try (Statement createEmptyTables = Db.createStatement()) {
			for (String createLine : ResourceLoader.ReadResource("Text/create.sql")) {
				createEmptyTables.executeUpdate(createLine);
			}
		}
	}

	private void contentCheck() throws ConnectionException {
		try {
			// Check application_id
			try (Statement appIdCheck = Db.createStatement()) {
				// always returns a value! (0 as default)
				ResultSet appIdResult = appIdCheck.executeQuery("PRAGMA application_id");
				if (appIdResult.getInt(1) != APPLICATION_ID)
					throw new ConnectionException(ConnectionException.ConnState.APP_ID_INVALID, null);
			}

			// Check user_version
			try (Statement versionCheck = Db.createStatement()) {
				ResultSet appversionCheckIdResult = versionCheck.executeQuery("PRAGMA user_version"); // always returns
																										// a
																										// value! (0 as
																										// default)
				int fileVersion = appversionCheckIdResult.getInt(1);
				if (fileVersion > ApplicationManager.APP_VERSION) {
					// File is newer than app => update app
					throw new ConnectionException(ConnectionException.ConnState.APP_OUTDATED, null);
				} else if ((fileVersion /1000000 ) < (ApplicationManager.APP_VERSION /1000000)) {
					// Major version of file < major version of app => not compatible
					throw new ConnectionException(ConnectionException.ConnState.DB_OUTDATED, null);
				}
			}

			// Check foreign keys
			try (Statement foreignKeyCheck = Db.createStatement()) {
				ResultSet foreignKeyResult = foreignKeyCheck.executeQuery("PRAGMA foreign_key_check");
				String errorString = "";
				while (foreignKeyResult.next()) {
					for (int i = 1; i <= foreignKeyResult.getMetaData().getColumnCount(); i++) {
						errorString += foreignKeyResult.getString(i) + " ";
					}
					errorString += "\n";
				}
				if (!errorString.equals("")) {
					throw new ConnectionException(ConnectionException.ConnState.FOREIGN_KEYS_CONSTRAINTS,
							new SQLException("Foreign key constraints violated. Violations: \n" + errorString));
				}
			}

			// Check integrity
			try (Statement integrityCheck = Db.createStatement()) {
				ResultSet integrityCheckResult = integrityCheck.executeQuery("PRAGMA integrity_check");
				if (!integrityCheckResult.next())
					throw new ConnectionException(ConnectionException.ConnState.INTEGRITY_FAILED,
							new SQLException("Integrity check failed, no check results returned"));

				String okString = integrityCheckResult.getString(1);
				// If returned value is not 'ok' -> Errors
				if (!okString.equalsIgnoreCase("ok")) {
					String errorString = okString + "\n";
					while (integrityCheckResult.next())
						errorString += integrityCheckResult.getString(1) + "\n";
					throw new ConnectionException(ConnectionException.ConnState.INTEGRITY_FAILED,
							new SQLException("Integrity check failed. Violations: \n" + errorString));
				}
			}
		} catch (SQLException e) {
			throw new ConnectionException(ConnectionException.ConnState.GENERAL_EXCEPTION, e);
		}
	}

	private void vacuum() throws ConnectionException {
		try {
			// No transaction can be open during vacuum
			// => easy workaround by putting the DB in autocommit for the duration of the
			// execution
			Db.commit();
			Db.setAutoCommit(true);
			// Rebuild database (restructure logically & pack into minimal amount of space)
			try (Statement vacuum = Db.createStatement()) {
				vacuum.execute("VACUUM");
			}
			Db.setAutoCommit(false);
		} catch (SQLException e) {
			throw new ConnectionException(ConnectionException.ConnState.VACUUM_FAILED, e);
		}
	}

	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return Db.prepareStatement(sql);
	}

	public synchronized void commit(EnumSet<DataChangedType> eSet) throws SQLException {
		Db.commit();
		this.setChanged();
		this.notifyDataChangedListeners(eSet);
	}

	public void announceDataRefresh() {
		notifyDataChangedListeners(DataChangedType.ALL_OPTS);
	}

	private void notifyDataChangedListeners(EnumSet<DataChangedType> eSet) {
		ArrayList<DataChangedListener> clientsToNotify = new ArrayList<>();
		// First compile a list of clients to notify
		for (DataChangedListener dL : dataListeners.keySet()) {
			for (DataChangedType dT : dataListeners.get(dL)) {
				if (eSet.contains(dT)) {
					clientsToNotify.add(dL);
					break;
				}
			}
		}

		// only now notify them (this might cause clients to add/modify/delete
		// DataChangedListeners, so this list has to be static)
		for (DataChangedListener client : clientsToNotify) {
			client.tableChanged();
		}
	}

	@Override
	public void close() throws SQLException {
		if (Db.isClosed())
			return;
		Db.commit();
		Db.close();
		BackupHandler.StopBackups();
		if (!(new File(DB_LOC)).delete())
			throw new SQLException(
					new IOException("Could not cleanup Burning Ember internal storage (" + DB_LOC + ")"));
	}

	public boolean isChanged() {
		return changedState;
	}

	private void setSaved() {
		this.changedState = false;
		this.backupState = false;
	}

	private void setChanged() {
		this.changedState = true;
		this.backupState = true;
	}

	public boolean isBackupNeeded() {
		return backupState;
	}

	private void setBackedUp() {
		this.backupState = false;
	}

	public synchronized void addDataChangedListener(DataChangedListener d, EnumSet<DataChangedType> e) {
		dataListeners.put(d, e);
	}

	public Map<DataChangedListener, EnumSet<DataChangedType>> getDataListeners() {
		return dataListeners;
	}

	public static boolean isRecoveredDb() {
		return recoveredDb;
	}

	public static SQLConnection getDbConn() {
		return DbConn;
	}

	public File getCurrentFile() {
		return currentFile;
	}
}
