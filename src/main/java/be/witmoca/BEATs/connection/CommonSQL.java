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
package be.witmoca.BEATs.connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import be.witmoca.BEATs.utils.StringUtils;

public class CommonSQL {

	public static String addArtist(String artistName, boolean local) throws SQLException {
		try (PreparedStatement add = SQLConnection.getDbConn()
				.prepareStatement("INSERT OR IGNORE INTO artist VALUES (?, ?)")) {
			String artist = StringUtils.ToUpperCamelCase(StringUtils.filterPrefix(artistName));
			add.setString(1, artist);
			add.setBoolean(2, local);
			add.executeUpdate();
			return artist;
		}
	}
	
	public static boolean isArtistLocal(String artistName) throws SQLException {
		try (PreparedStatement selLocal = SQLConnection.getDbConn().prepareStatement("SELECT local FROM Artist WHERE ArtistName = ?")) {
			selLocal.setString(1, artistName);
			ResultSet rs = selLocal.executeQuery();
			if (!rs.next())
				throw new SQLException(artistName + " is not present in artist table!");
			return rs.getBoolean(1);
		}
	}
	
	public static void updateLocalityOfArtist(boolean local, String artist) throws SQLException {
		try (PreparedStatement updateLocal = SQLConnection.getDbConn()
				.prepareStatement("UPDATE Artist SET local = ? WHERE ArtistName = ?")) {
			updateLocal.setBoolean(1, local);
			updateLocal.setString(2, artist);
			updateLocal.executeUpdate();
		}
	}
	
	public static void removeArtist(String artist) throws SQLException {
		try (PreparedStatement delArtist = SQLConnection.getDbConn()
				.prepareStatement("DELETE FROM Artist WHERE ArtistName = ?")) {
			delArtist.setString(1, artist);
			delArtist.executeUpdate();
		}
	}

	public static int addSong(String title, String artistName) throws SQLException {
		title = StringUtils.ToUpperCamelCase(title);
		artistName = StringUtils.ToUpperCamelCase(StringUtils.filterPrefix(artistName));

		try (PreparedStatement add = SQLConnection.getDbConn()
				.prepareStatement("INSERT OR IGNORE INTO song(Title, ArtistName) VALUES (?, ?)")) {
			add.setString(1, title);
			add.setString(2, artistName);
			add.executeUpdate();
		}
		try (PreparedStatement getId = SQLConnection.getDbConn()
				.prepareStatement("SELECT songId From song WHERE Title = ? AND ArtistName = ?")) {
			getId.setString(1, title);
			getId.setString(2, artistName);
			ResultSet rs = getId.executeQuery();
			if (!rs.next()) {
				throw new SQLException("SongId expected but not returned for (" + title + "," + artistName + ")");
			}
			return rs.getInt(1);
		}
	}
	
	/**
	 * 
	 * @param artist
	 * @return list of song titles from said artist, ordered alphabetically
	 * @throws SQLException
	 */
	public static List<String> getAllSongTitlesOfArtist(String artist) throws SQLException{
		List<String> result = new ArrayList<String>();
		try (PreparedStatement selSongs = SQLConnection.getDbConn().prepareStatement("SELECT Title FROM Song WHERE ArtistName = ? ORDER BY Title")) {
			selSongs.setString(1, artist);
			ResultSet rs = selSongs.executeQuery();
			while(rs.next())
				result.add(rs.getString(1));
		}
		return result;
	}
	
	/**
	 * Changes all references from oldSongId to newSongId
	 * @param oldSongId
	 * @param newSongId
	 * @throws SQLException
	 */
	public static void updateAllSongIdReferences(int oldSongId, int newSongId) throws SQLException {
		try (PreparedStatement updateArchive = SQLConnection.getDbConn().prepareStatement("UPDATE SongsInArchive SET SongId = ? WHERE SongId = ?")) {
			updateArchive.setInt(1, newSongId);
			updateArchive.setInt(2, oldSongId);
			updateArchive.executeUpdate();
		}
		
		try (PreparedStatement updateQueue = SQLConnection.getDbConn().prepareStatement("UPDATE CurrentQueue SET SongId = ? WHERE SongId = ?")) {
			updateQueue.setInt(1, newSongId);
			updateQueue.setInt(2, oldSongId);
			updateQueue.executeUpdate();
		}
	}
	
	public static void removeSong(int songid) throws SQLException {
		try (PreparedStatement delSong = SQLConnection.getDbConn().prepareStatement("DELETE FROM Song WHERE SongId = ?")) {
			delSong.setInt(1, songid);
			delSong.executeUpdate();
		}	
	}

	public static void addGenre(String Genre) throws SQLException {
		try (PreparedStatement add = SQLConnection.getDbConn()
				.prepareStatement("INSERT OR IGNORE INTO Genre VALUES (?)")) {
			add.setString(1, Genre);
			add.executeUpdate();
		}
	}

	/**
	 * 
	 * @return List of Genres, ordered by name
	 * @throws SQLException
	 */
	public static List<String> getGenres() throws SQLException {
		List<String> result = new ArrayList<String>();
		try (PreparedStatement sel = SQLConnection.getDbConn()
				.prepareStatement("SELECT GenreName FROM Genre ORDER BY GenreName ASC")) {
			ResultSet rs = sel.executeQuery();
			while (rs.next()) {
				result.add(rs.getString(1));
			}
		}
		return result;
	}

	public static void addEpisode(int episodeID, LocalDate episodeDate) throws SQLException {
		try (PreparedStatement add = SQLConnection.getDbConn()
				.prepareStatement("INSERT OR IGNORE INTO episode VALUES (?, ?)")) {
			add.setInt(1, episodeID);
			// Actually an int is more than enough for the next few thousands of years
			// (sqlite will take care of the type so no need to convert)
			add.setLong(2, episodeDate.toEpochDay());
			add.executeUpdate();
		}
	}

	/**
	 * 
	 * @return List of episodes ordered in ascending order
	 * @throws SQLException
	 */
	public static List<Integer> getEpisodes() throws SQLException {
		List<Integer> result = new ArrayList<Integer>();
		try (PreparedStatement getList = SQLConnection.getDbConn()
				.prepareStatement("SELECT EpisodeId FROM Episode ORDER BY EpisodeId ASC")) {
			ResultSet rs = getList.executeQuery();
			while (rs.next())
				result.add(rs.getInt(1));
		}
		return result;
	}

	/**
	 * 
	 * @param ldate
	 * @return the episode id belonging to the date, -1 if no result was found
	 * @throws SQLException
	 */
	public static int getEpisodeByDate(LocalDate ldate) throws SQLException {
		try (PreparedStatement getId = SQLConnection.getDbConn()
				.prepareStatement("SELECT Episodeid FROM Episode WHERE EpisodeDate = ?")) {
			getId.setLong(1, ldate.toEpochDay());
			ResultSet rs = getId.executeQuery();
			if (!rs.next())
				return -1;
			return rs.getInt(1);
		}
	}
	
	public static LocalDate getEpisodeDateById(int id) throws SQLException {
		try (PreparedStatement selDate = SQLConnection.getDbConn().prepareStatement("SELECT episodeDate FROM episode WHERE episodeId = ?")) {
			selDate.setInt(1, id);
			ResultSet rs = selDate.executeQuery();
			if(!rs.next())
				return null;
			return LocalDate.ofEpochDay(rs.getInt(1));
		} 
	}
	
	public static void updateEpisodeDate(int episodeId, LocalDate newDate) throws SQLException {
		try (PreparedStatement upDate = SQLConnection.getDbConn().prepareStatement("UPDATE OR IGNORE episode SET episodeDate = ? WHERE episodeId = ?")) {
			upDate.setLong(1, newDate.toEpochDay());
			upDate.setInt(2, episodeId);
			upDate.executeUpdate();
			SQLConnection.getDbConn().commit(EnumSet.of(DataChangedType.EPISODE));
		}
	}

	public static void addSongInArchive(int songId, int episodeId, String Genre, String comment) throws SQLException {
		try (PreparedStatement add = SQLConnection.getDbConn().prepareStatement(
				"INSERT INTO SongsInArchive (SongId, EpisodeId, GenreName, Comment) VALUES (?, ?, ?, ?)")) {
			add.setInt(1, songId);
			add.setInt(2, episodeId);
			add.setString(3, Genre);
			add.setString(4, StringUtils.ToUpperCamelCase(comment));
			add.executeUpdate();
		}
	}
	
	public static void removeFromSongsInArchive(int rowid) throws SQLException {
		try (PreparedStatement delLine = SQLConnection.getDbConn().prepareStatement("DELETE FROM SongsInArchive WHERE rowid = ?")) {
			delLine.setInt(1, rowid);
			delLine.executeUpdate();
		}
	}

	/**
	 * 
	 * @param playlistName
	 * @param tabOrder     Integer representing the taborder. Must be unique (-1
	 *                     automatically assigns max+1)
	 * @throws SQLException
	 */
	public static void addPlaylist(String playlistName, int tabOrder) throws SQLException {
		if (tabOrder <= 0) {
			tabOrder = getMaxTabOrderFromPlaylist() + 1;
		}

		try (PreparedStatement add = SQLConnection.getDbConn()
				.prepareStatement("INSERT OR IGNORE INTO playlist VALUES (?, ?)")) {
			add.setString(1, playlistName);
			add.setInt(2, tabOrder);
			add.executeUpdate();
		}
	}

	/**
	 * 
	 * @return the maximum tabOrder present in the SQL table, or 0 if empty
	 * @throws SQLException
	 */
	public static int getMaxTabOrderFromPlaylist() throws SQLException {
		try (PreparedStatement selMaxTab = SQLConnection.getDbConn()
				.prepareStatement("SELECT max(TabOrder) FROM playlist")) {
			ResultSet rs = selMaxTab.executeQuery();
			if (rs.next())
				return rs.getInt(1);
			return 0;
		}
	}
	
	public static int getTabOrder(String playlistName) throws SQLException {
		try (PreparedStatement selTab = SQLConnection.getDbConn().prepareStatement("SELECT taborder FROM playlist WHERE PlaylistName = ?")) {
			selTab.setString(1, playlistName);
			ResultSet rs = selTab.executeQuery();
			if (rs.next())
				return rs.getInt(1);
			return 0;
		}
	}

	/**
	 * 
	 * @return List of PlaylistNames ordered by TabOrder
	 * @throws SQLException
	 */
	public static List<String> getPlaylists() throws SQLException {
		List<String> result = new ArrayList<String>();
		try (PreparedStatement getValue = SQLConnection.getDbConn()
				.prepareStatement("SELECT PlaylistName FROM Playlist ORDER BY TabOrder")) {
			ResultSet value = getValue.executeQuery();
			while (value.next()) {
				result.add(value.getString(1));
			}
		}
		return result;
	}
	
	public static void updatePlaylistOrder(String playlistName, int tabOrder) throws SQLException {
		try (PreparedStatement upT = SQLConnection.getDbConn().prepareStatement("UPDATE Playlist SET TabOrder = ? WHERE PlaylistName = ?")) {
			upT.setInt(1, tabOrder);
			upT.setString(2, playlistName);
			upT.executeUpdate();
		}
	}

	public static void removePlaylist(String playlistName) throws SQLException {
		try (PreparedStatement delP = SQLConnection.getDbConn()
				.prepareStatement("DELETE FROM Playlist WHERE PlaylistName = ?")) {
			delP.setString(1, playlistName);
			delP.executeUpdate();
		}
	}

	public static void addSongInPlaylist(String playlistName, String artist, String song, String comment)
			throws SQLException {
		try (PreparedStatement add = SQLConnection.getDbConn().prepareStatement(
				"INSERT INTO SongsInPlaylist (PlaylistName ,Artist , Song, Comment) VALUES (?, ?, ?, ?)")) {
			add.setString(1, playlistName);
			add.setString(2, StringUtils.ToUpperCamelCase(StringUtils.filterPrefix(artist)));
			add.setString(3, StringUtils.ToUpperCamelCase(song));
			add.setString(4, StringUtils.ToUpperCamelCase(comment));
			add.executeUpdate();
		}
	}

	public static void updateSongsInPlaylist(int rowid, String artist, String song, String comment)
			throws SQLException {
		try (PreparedStatement updateVal = SQLConnection.getDbConn()
				.prepareStatement("UPDATE SongsInPlaylist SET Artist = ?, Song = ?, Comment = ? WHERE rowid = ?")) {
			updateVal.setString(1, artist);
			updateVal.setString(2, song);
			updateVal.setString(3, comment);
			updateVal.setInt(4, rowid);
			updateVal.executeUpdate();
		}
	}
	
	public static void updatePlaylistReferences(String newName, String oldName) throws SQLException {
		try (PreparedStatement updateVal = SQLConnection.getDbConn().prepareStatement("UPDATE SongsInPlaylist SET PlaylistName = ? WHERE PlaylistName = ?")) {
			updateVal.setString(1, newName);
			updateVal.setString(2, oldName);
			updateVal.executeUpdate();
		}
	}
	
	public static int countSongsInPlaylist(String playlistName) throws SQLException {
		try (PreparedStatement selSong = SQLConnection.getDbConn().prepareStatement("SELECT count(*) FROM SongsInPlaylist WHERE PlaylistName = ?")) {
			selSong.setString(1, playlistName);
			ResultSet rs = selSong.executeQuery();
			if(rs.next())
				return rs.getInt(1);
		}
		return 0;
	}

	public static void removeFromSongsInPlaylist(int rowid) throws SQLException {
		try (PreparedStatement delRow = SQLConnection.getDbConn()
				.prepareStatement("DELETE FROM SongsInPlaylist WHERE rowid = ?")) {
			delRow.setInt(1, rowid);
			delRow.executeUpdate();
		}
	}

	public static void clearSongsInPlaylist(String playlistName) throws SQLException {
		try (PreparedStatement delPS = SQLConnection.getDbConn()
				.prepareStatement("DELETE FROM SongsInPlaylist WHERE PlaylistName = ?")) {
			delPS.setString(1, playlistName);
			delPS.executeUpdate();
		}
	}

	public static void addCurrentQueue(int songId, String comment) throws SQLException {
		try (PreparedStatement add = SQLConnection.getDbConn()
				.prepareStatement("INSERT INTO CurrentQueue (SongId, Comment) VALUES (?, ?)")) {
			add.setInt(1, songId);
			add.setString(2, comment);
			add.executeUpdate();
		}
	}

	public static void clearCurrentQueue() throws SQLException {
		try (PreparedStatement delCQ = SQLConnection.getDbConn().prepareStatement("DELETE FROM CurrentQueue")) {
			delCQ.executeUpdate();
		}
	}

	public static void removeFromCurrentQueue(int songOrder) throws SQLException {
		try (PreparedStatement del = SQLConnection.getDbConn()
				.prepareStatement("DELETE FROM CurrentQueue WHERE SongOrder = ?")) {
			del.setInt(1, songOrder);
			del.executeUpdate();
		}
	}
}
