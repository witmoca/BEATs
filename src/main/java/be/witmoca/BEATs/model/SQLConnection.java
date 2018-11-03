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
import java.sql.SQLException;
import java.sql.Statement;

import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteConnection;

import be.witmoca.BEATs.Launch;

public class SQLConnection implements AutoCloseable {
	private final SQLiteConnection Db; // Internal Connection
	private static final String DB_LOC = Launch.APP_FOLDER + File.separator + "InternalStorage.db";
	private static final int APPLICATION_ID = 0;

	/**
	 *  
	 * @throws SQLException when creating a Db has failed (critically)
	 */
	public SQLConnection() throws SQLException {
		// internal memory: "jdbc:sqlite::memory:" OR on disk: "jdbc:sqlite:"+DB_LOC
		Db = (SQLiteConnection) DriverManager.getConnection("jdbc:sqlite:"+DB_LOC, configSettings().toProperties());
		Db.setAutoCommit(false);
		createTables();
		//contentCheck();
		
		// TODO: TEST CONTENT
		try (Statement testSet = Db.createStatement()) {
			testSet.executeUpdate("INSERT INTO Episode VALUES (1,0)");
			testSet.executeUpdate("INSERT INTO Episode VALUES (2,100)");
			testSet.executeUpdate("INSERT INTO Section VALUES ('SPC')");
			testSet.executeUpdate("INSERT INTO Section VALUES ('CL')");
			for(int i = 1; i <= 3000; i++)
				testSet.executeUpdate("INSERT INTO Artist VALUES ('a" +  i + "',0)");
			for(int i = 1; i < 12000; i++) {
				testSet.executeUpdate("INSERT INTO Song VALUES ("+i+",'s" + i + "','a" + ((i % 3000)+1) + "')");			
				testSet.executeUpdate("INSERT INTO SongsInArchive VALUES (" + i + ",1,'SPC','')");
			}
			testSet.executeUpdate("INSERT INTO Playlist VALUES ('Classic vandaag', 0)");
			testSet.executeUpdate("INSERT INTO Playlist VALUES ('Poll', 1)");
			testSet.executeUpdate("INSERT INTO SongsInPlaylist VALUES ('Classic vandaag', 'NA1', 'NS1', 'no comment')");
			testSet.executeUpdate("INSERT INTO SongsInPlaylist VALUES ('Classic vandaag', 'NA2', 'NS2', 'local comment')");
			testSet.executeUpdate("INSERT INTO SongsInPlaylist VALUES ('Poll', 'PollArt1', 'PollSong1', 'local')");
			testSet.executeUpdate("INSERT INTO SongsInPlaylist VALUES ('Poll', 'PollArt2', 'PollSong2', 'comment')");
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Db.commit();
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
/*
	private void contentCheck() throws SQLException {

		// Check application_id
		try (Statement appIdCheck = Db.createStatement()) {
			ResultSet appIdResult = appIdCheck.executeQuery("PRAGMA application_id"); // always returns a value! (0 as
																						// default)
			if (appIdResult.getInt(1) != WWDBProperties.getApplicationId())
				throw new SQLException("Application Id of file not correct(should be "
						+ WWDBProperties.getApplicationId()
						+ "). \nFiles with an incorrect or non-existant application_id are not considered a wwdb file.");
		}

		// Check user_version
		try (Statement versionCheck = INTERNAL_MEMORY.createStatement()) {
			ResultSet appversionCheckIdResult = versionCheck.executeQuery("PRAGMA user_version"); // always returns a
																									// value! (0 as
																									// default)
			if (!WWDBProperties.compatabilityCheck(appversionCheckIdResult.getInt(1)))
				throw new SQLException(
						"Version number is considered incompatible. \nUpdate your application or file according to version number. \nFile version: "
								+ WWDBProperties.versionInFormat(appversionCheckIdResult.getInt(1))
								+ " \nApplication version: " + WWDBProperties.versionInFormat());
		}

		// Check foreign keys
		try (Statement foreignKeyCheck = INTERNAL_MEMORY.createStatement()) {
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
		try (Statement integrityCheck = INTERNAL_MEMORY.createStatement()) {
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
*/
	@Override
	public void close() {
		try {
			if (Db.isClosed())
				return;

			// Call optimize before closing
			try (Statement optimize = Db.createStatement()) {
				optimize.execute("PRAGMA optimize");
			}
			Db.commit();
			Db.close();

		} catch (Exception e) {
			// Silently ignore
		}
	}

	public SQLiteConnection getDb() {
		return Db;
	}
}
