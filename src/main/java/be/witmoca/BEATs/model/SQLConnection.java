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
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteConnection;

import be.witmoca.BEATs.Launch;

public class SQLConnection implements AutoCloseable {
	private final SQLiteConnection Db; // Internal Connection
	private static final String DB_LOC = Launch.APP_FOLDER + File.separator + "InternalStorage.db";
	private static final int APPLICATION_ID = 0x77776462;

	/**
	 * 
	 * @throws SQLException
	 *             when creating a Db has failed (critically)
	 */
	public SQLConnection() throws SQLException {
		// internal memory: "jdbc:sqlite::memory:" OR on disk: "jdbc:sqlite:"+DB_LOC
		Db = (SQLiteConnection) DriverManager.getConnection("jdbc:sqlite:" + DB_LOC, configSettings().toProperties());
		Db.setAutoCommit(false);
		createTables();
		Db.commit();
	}

	/**
	 * Creates a new SQLConnection and loads the db from path
	 */
	public SQLConnection(String loadPath) throws SQLException {
		this();
		try (Statement load = Db.createStatement()) {
			load.executeUpdate("restore from " + loadPath);
		}
		this.contentCheck();
	}

	private SQLiteConfig configSettings() {
		SQLiteConfig config = new SQLiteConfig();
		// Add application_id & user_version
		config.setUserVersion(Launch.APP_VERSION);
		config.setApplicationId(APPLICATION_ID);
		// Enforce foreign key correctness
		config.enforceForeignKeys(true);

		return config;
	}

	private void createTables() throws SQLException {
		try (Statement createEmptyTables = Db.createStatement()) {
			// Artist-Song
			createEmptyTables.executeUpdate(
					"CREATE TABLE IF NOT EXISTS Artist(ArtistName TEXT PRIMARY KEY, Local INTEGER NOT NULL)");
			createEmptyTables.executeUpdate(
					"CREATE TABLE IF NOT EXISTS Song(SongId INTEGER PRIMARY KEY, Title TEXT NOT NULL, ArtistName REFERENCES Artist NOT NULL)");
			// Other Base tables
			createEmptyTables.executeUpdate(
					"CREATE TABLE IF NOT EXISTS Playlist(PlaylistName TEXT PRIMARY KEY, TabOrder INTEGER NOT NULL UNIQUE)");
			createEmptyTables.executeUpdate(
					"CREATE TABLE IF NOT EXISTS Episode(EpisodeId INTEGER PRIMARY KEY, EpisodeDate INTEGER NOT NULL UNIQUE)");
			createEmptyTables.executeUpdate("CREATE TABLE IF NOT EXISTS Section(SectionName TEXT PRIMARY KEY)");
			// Relation Tables
			createEmptyTables.executeUpdate(
					"CREATE TABLE IF NOT EXISTS SongsInPlaylist(PlaylistName REFERENCES Playlist NOT NULL,Artist TEXT NOT NULL, Song TEXT NOT NULL, Comment TEXT)");
			createEmptyTables.executeUpdate(
					"CREATE TABLE IF NOT EXISTS CurrentQueue(SongOrder INTEGER PRIMARY KEY, SongId REFERENCES Song NOT NULL)");
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
			if (fileVersion > Launch.APP_VERSION) {
				// File is newer than app => update app
				throw new SQLException(
						"The requested database has a higher version number. Please update BEATs to a newer version.");
			} else if ((fileVersion >>> 6) < (Launch.APP_VERSION >>> 6)) {
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

	@Override
	public void close() throws SQLException {
		if (Db.isClosed())
			return;

		// Call optimize before closing
		try (Statement optimize = Db.createStatement()) {
			optimize.execute("PRAGMA optimize");
		}
		Db.commit();
		Db.close();
	}

	public SQLiteConnection getDb() {
		return Db;
	}
}
