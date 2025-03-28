/**
 * 
 */
package be.witmoca.BEATs.liveshare;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import be.witmoca.BEATs.connection.CommonSQL;
import be.witmoca.BEATs.connection.SQLConnection;
import be.witmoca.BEATs.ui.components.PlaylistEntry;

/**
 * @author Witmoca
 *
 * This is class is supposed to be created once, and then updated
 *
 */
public class LiveShareSerializable implements Serializable {
	private static final long serialVersionUID = 1L;
	// HashMap = serializable
	private final HashMap<String, List<PlaylistEntry>> content = new HashMap<String, List<PlaylistEntry>>();

	private LiveShareSerializable() {
	}

	// Should never be run on the ADT
	public void UpdateContents() {
		// Delete content
		this.content.clear();
		
		// Check if SQL Connection is open
		try {
			if (SQLConnection.getDbConn().isClosed()) {
				// If closed, skip and make empty liveShareSerializable
				return;
			}
		} catch (SQLException e) {
			// If connection issue: also skip 
			return;
		}

		// Build content list from database
		List<String> playlistNames = new ArrayList<String>();
		try {
			playlistNames.addAll(CommonSQL.getPlaylists());
			try (PreparedStatement getValue = SQLConnection.getDbConn().prepareStatement(
					"SELECT rowid, Artist, Song, Comment FROM SongsInPlaylist WHERE PlaylistName = ? ORDER BY rowid")) {
				for (String playlistName : playlistNames) {
					List<PlaylistEntry> playlistcontent = new ArrayList<PlaylistEntry>();
					getValue.setString(1, playlistName);
					ResultSet value = getValue.executeQuery();
					while (value.next()) {
						playlistcontent.add(new PlaylistEntry(value.getInt(1), value.getString(2), value.getString(3),
								value.getString(4)));
					}
					this.content.put(playlistName, playlistcontent);
				}
			}
			
			
			// Fake playlist to display the CurrentQueue
			List<PlaylistEntry> currentQueueContent = new ArrayList<PlaylistEntry>();
			try (PreparedStatement getValue = SQLConnection.getDbConn().prepareStatement(
					"SELECT SongOrder, ArtistName, Title, Comment FROM CurrentQueue,Song WHERE CurrentQueue.SongId = Song.SongId ORDER BY SongOrder ASC")) {
				ResultSet value = getValue.executeQuery();
				while (value.next()) {
					currentQueueContent.add(new PlaylistEntry(value.getInt(1), value.getString(2), value.getString(3),
							value.getString(4)));
				}
			}
			
			this.content.put("(Played Queue)", currentQueueContent);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// May run on the EDT
	public static LiveShareSerializable createEmpty() {
		return new LiveShareSerializable();
	}

	public List<String> getPlaylists() {
		List<String> playlistNames = new ArrayList<String>(content.keySet());
		Collections.sort(playlistNames);
		return playlistNames;
	}
	
	public List<PlaylistEntry> getPlaylistContents(String playlistName){
		return content.getOrDefault(playlistName, Collections.emptyList());
	}
}
