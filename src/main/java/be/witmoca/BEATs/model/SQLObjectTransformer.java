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
* File: SQLObjectTransformer.java
* Created: 2018
*/
package be.witmoca.BEATs.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import be.witmoca.BEATs.Launch;
import be.witmoca.BEATs.utils.StringUtils;

public class SQLObjectTransformer {
	
	public static void addEpisode(int episodeID, LocalDate episodeDate) throws SQLException {
		try(PreparedStatement add = Launch.getDB_CONN().prepareStatement("INSERT OR IGNORE INTO episode VALUES (?, ?)")){
			add.setInt(1, episodeID);
			// Actually an int is more than enough for the next few thousands of years (sqlite will take care of the type so no need to convert)
			add.setLong(2, episodeDate.toEpochDay());
			add.executeUpdate();
		}
	}
	
	public static void addSection(String sectionCode) throws SQLException {
		try(PreparedStatement add = Launch.getDB_CONN().prepareStatement("INSERT OR IGNORE INTO section VALUES (?)")){
			add.setString(1, sectionCode);
			add.executeUpdate();
		}
	}
	
	public static void addArtist(String artistName, boolean local) throws SQLException {
		try(PreparedStatement add = Launch.getDB_CONN().prepareStatement("INSERT OR IGNORE INTO artist VALUES (?, ?)")){
			add.setString(1, StringUtils.filterPrefix(artistName));
			add.setBoolean(2, local);
			add.executeUpdate();
		}
	}
	
	public static int addSong(String title, String artistName) throws SQLException {
		try(PreparedStatement add = Launch.getDB_CONN().prepareStatement("INSERT OR IGNORE INTO song(Title, ArtistName) VALUES (?, ?)")){
			add.setString(1, title);
			add.setString(2, StringUtils.filterPrefix(artistName));
			add.executeUpdate();
		}
		try(PreparedStatement getId = Launch.getDB_CONN().prepareStatement("SELECT songId From song WHERE Title = ? AND ArtistName = ?")){
			getId.setString(1, title);
			getId.setString(2, StringUtils.filterPrefix(artistName));
			ResultSet rs = getId.executeQuery();
			if(!rs.next()) {
				throw new SQLException("SongId expected but not returned for (" + title + "," + StringUtils.filterPrefix(artistName) + ")");
			}
			return rs.getInt(1);
		}
	}
	
	public static void addSongInArchive(int songId, int episodeId, String section, String comment) throws SQLException {
		try(PreparedStatement add = Launch.getDB_CONN().prepareStatement("INSERT INTO SongsInArchive VALUES (?, ?, ?, ?)")){
			add.setInt(1, songId);
			add.setInt(2, episodeId);
			add.setString(3, section);
			add.setString(4, comment);
			add.executeUpdate();
		}
	}
	
	/**
	 *  
	* @param playlistName
	* @param tabOrder Integer representing the taborder. Must be unique (-1 automatically assigns max+1)
	* @throws SQLException
	 */
	public static void addPlaylist(String playlistName, int tabOrder) throws SQLException {
		if(tabOrder <= 0) {
			try(PreparedStatement getMaxTab = Launch.getDB_CONN().prepareStatement("SELECT max(tabOrder) FROM playlist")){
				ResultSet rs = getMaxTab.executeQuery();
				if(!rs.next()) {
					tabOrder = 0;
				}
				tabOrder = rs.getInt(1)+1;
			}
		}
		
		try(PreparedStatement add = Launch.getDB_CONN().prepareStatement("INSERT OR IGNORE INTO playlist VALUES (?, ?)")){
			add.setString(1, playlistName);
			add.setInt(2, tabOrder);
			add.executeUpdate();
		}
	}
	
	public static void addSongInPlaylist(String playlistName, String artist, String song, String comment) throws SQLException {
		try(PreparedStatement add = Launch.getDB_CONN().prepareStatement("INSERT INTO SongsInPlaylist VALUES (?, ?, ?, ?)")){
			add.setString(1, playlistName);
			add.setString(2, artist);
			add.setString(3, song);
			add.setString(4, comment);
			add.executeUpdate();
		}
	}
}
