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
package be.witmoca.BEATs.model;

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
import org.sqlite.SQLiteConnection;

import be.witmoca.BEATs.ApplicationManager;
import be.witmoca.BEATs.FileManager;

public class SQLConnection implements AutoCloseable {
	private final SQLiteConnection Db; // Internal Connection
	private static final int APPLICATION_ID = 0x77776462;
	private boolean changedState = false;
	private Map<DataChangedListener, EnumSet<DataChangedListener.DataType>> dataListeners = new HashMap<>();
	private boolean recovering = false;

	/**
	 * Creates a new internal storage and sets up the content
	 * 
	 * @param loadFile the File to load the content from, {@code null} if an empty
	 *                 database is desired.
	 * @param recovery set to {@code true} if the internal database is still present
	 *                 (= recovery)
	 * @throws SQLException if SQLConnection failed to instantiate
	 */
	public SQLConnection(File loadFile, boolean recovery) throws SQLException {
		Db = (SQLiteConnection) DriverManager.getConnection("jdbc:sqlite:" + FileManager.DB_LOC, configSettings().toProperties());

		try {
			Db.setAutoCommit(false);
			createTables();
			Db.commit();
			if (loadFile != null) {
				try (Statement load = Db.createStatement()) {
					load.executeUpdate("restore from " + loadFile.getAbsolutePath());
				}
			}
			this.contentCheck();
			this.vacuum();

			if (recovery)
				this.setChanged();
		} catch (SQLException e) {
			this.close();
			throw e;
		}
	}

	/**
	 * Internal method only! Used to open a connection to a (possibly) recoverable
	 * database.
	 * 
	 * @throws SQLException if recovery failed.
	 */
	private SQLConnection() throws SQLException {
		recovering = true;
		SQLiteConfig recoveryConfig = new SQLiteConfig();
		recoveryConfig.enforceForeignKeys(true);
	
		Db = (SQLiteConnection) DriverManager.getConnection("jdbc:sqlite:" + FileManager.DB_LOC, recoveryConfig.toProperties());
		Db.setAutoCommit(false);
		this.contentCheck();
	}

	/**
	 * Opens a connection to a (possibly) recoverable database.
	 * 
	 * @return the recovered database.
	 * @throws SQLException if recovery failed.
	 */
	public static SQLConnection recoverDatabase() throws SQLException {
		return new SQLConnection();
	}

	public void saveDatabase(String savePath) throws SQLException {
		// Call optimize first
		try (Statement optimize = Db.createStatement()) {
			optimize.execute("PRAGMA optimize");
		}

		try (Statement save = Db.createStatement()) {
			save.executeUpdate("backup to " + savePath);
		}
		this.setSaved();
	}

	private SQLiteConfig configSettings() {
		SQLiteConfig config = new SQLiteConfig();
		// Add application_id & user_version
		config.setUserVersion(ApplicationManager.APP_VERSION);
		config.setApplicationId(APPLICATION_ID);
		// Enforce foreign key correctness
		config.enforceForeignKeys(true);

		return config;
	}

	private void createTables() throws SQLException {
		try (Statement createEmptyTables = Db.createStatement()) {
			// Artist-Song
			createEmptyTables.executeUpdate(
					"CREATE TABLE IF NOT EXISTS Artist(ArtistName TEXT PRIMARY KEY COLLATE NOCASE, Local INTEGER NOT NULL)");
			createEmptyTables.executeUpdate(
					"CREATE TABLE IF NOT EXISTS Song(SongId INTEGER PRIMARY KEY, Title TEXT COLLATE NOCASE, ArtistName REFERENCES Artist, UNIQUE (Title, ArtistName))");
			// Other Base tables
			createEmptyTables.executeUpdate(
					"CREATE TABLE IF NOT EXISTS Playlist(PlaylistName TEXT PRIMARY KEY, TabOrder INTEGER NOT NULL UNIQUE)");
			createEmptyTables.executeUpdate(
					"CREATE TABLE IF NOT EXISTS Episode(EpisodeId INTEGER PRIMARY KEY, EpisodeDate INTEGER NOT NULL UNIQUE)");
			createEmptyTables.executeUpdate("CREATE TABLE IF NOT EXISTS Section(SectionName TEXT PRIMARY KEY)");
			createEmptyTables.executeUpdate(
					"CREATE TABLE IF NOT EXISTS ccp(Artist TEXT NOT NULL, Song TEXT NOT NULL, Comment TEXT)");
			// Relation Tables
			createEmptyTables.executeUpdate(
					"CREATE TABLE IF NOT EXISTS SongsInPlaylist(PlaylistName REFERENCES Playlist NOT NULL,Artist TEXT NOT NULL, Song TEXT NOT NULL, Comment TEXT)");
			createEmptyTables.executeUpdate(
					"CREATE TABLE IF NOT EXISTS CurrentQueue(SongOrder INTEGER PRIMARY KEY, SongId REFERENCES Song NOT NULL, Comment TEXT)");
			createEmptyTables.executeUpdate(
					"CREATE TABLE IF NOT EXISTS SongsInArchive(SongId REFERENCES Song NOT NULL, EpisodeId REFERENCES Episode NOT NULL, SectionName REFERENCES Section NOT NULL, Comment TEXT)");
		}
	}

	private void contentCheck() throws SQLException {

		// Check application_id
		try (Statement appIdCheck = Db.createStatement()) {
			ResultSet appIdResult = appIdCheck.executeQuery("PRAGMA application_id"); // always returns a value! (0 as
																						// default)
			if (appIdResult.getInt(1) != APPLICATION_ID)
				throw new SQLException("This is not a recognized database");
		}

		// Check user_version
		try (Statement versionCheck = Db.createStatement()) {
			ResultSet appversionCheckIdResult = versionCheck.executeQuery("PRAGMA user_version"); // always returns a
																									// value! (0 as
																									// default)
			int fileVersion = appversionCheckIdResult.getInt(1);
			if (fileVersion > ApplicationManager.APP_VERSION) {
				// File is newer than app => update app
				throw new SQLException(
						"The requested database has a higher version number. Please update BEATs to a newer version.");
			} else if ((fileVersion >>> 6) < (ApplicationManager.APP_VERSION >>> 6)) {
				// Major version of file < major version of app => not compatible
				throw new SQLException(
						"Could not load database because it is too old. Please try to import it instead.");
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
				throw new SQLException("Foreign key constraints violated. Violations: \n" + errorString);
			}
		}

		// Check integrity
		try (Statement integrityCheck = Db.createStatement()) {
			ResultSet integrityCheckResult = integrityCheck.executeQuery("PRAGMA integrity_check");
			if (!integrityCheckResult.next())
				throw new SQLException("Integrity check failed, no check results returned");

			String okString = integrityCheckResult.getString(1);
			// If returned value is not 'ok' -> Errors
			if (!okString.equalsIgnoreCase("ok")) {
				String errorString = okString + "\n";
				while (integrityCheckResult.next())
					errorString += integrityCheckResult.getString(1) + "\n";
				throw new SQLException("Integrity check failed. Violations: \n" + errorString);
			}
		}
	}

	private void vacuum() throws SQLException {
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
	}

	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return Db.prepareStatement(sql);
	}

	public synchronized void commit(EnumSet<DataChangedListener.DataType> eSet) throws SQLException {
		Db.commit();
		this.setChanged();
		this.notifyDataChangedListeners(eSet);
	}

	public void notifyDataChangedListeners(EnumSet<DataChangedListener.DataType> eSet) {
		ArrayList<DataChangedListener> clientsToNotify = new ArrayList<>();
		// First compile a list of clients to notify
		for (DataChangedListener dL : dataListeners.keySet()) {
			for (DataChangedListener.DataType dT : dataListeners.get(dL)) {
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
		if (!recovering) {
			try {
				FileManager.deleteDb();
			} catch (IOException e) {
				throw new SQLException(e);
			}
		}
	}

	public SQLiteConnection getDb() {
		return Db;
	}

	public boolean isChanged() {
		return changedState;
	}

	public void setSaved() {
		this.changedState = false;
	}

	private void setChanged() {
		this.changedState = true;
	}

	public synchronized void addDataChangedListener(DataChangedListener d, EnumSet<DataChangedListener.DataType> e) {
		dataListeners.put(d, e);
	}
}
