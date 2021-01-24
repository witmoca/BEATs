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
 */
public class LiveShareSerializable implements Serializable, Cloneable {
	private static final long serialVersionUID = 1L;
	// HashMap = serializable
	private final HashMap<String, List<PlaylistEntry>> content = new HashMap<String, List<PlaylistEntry>>();

	private LiveShareSerializable(boolean empty) throws SQLException {
		if (!empty) {
			List<String> playlistNames = new ArrayList<String>();
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
		}
	}

	public static LiveShareSerializable createSnapShot() {
		try {
			return new LiveShareSerializable(false);
		} catch (SQLException e) {
			return null;
		}
	}
	
	public static LiveShareSerializable createEmpty() {
		try {
			return new LiveShareSerializable(true);
		} catch (SQLException e) {
			return null;
		}
	}

	public List<String> getPlaylists() {
		List<String> playlistNames = new ArrayList<String>(content.keySet());
		Collections.sort(playlistNames);
		return playlistNames;
	}
	
	public List<PlaylistEntry> getPlaylistContents(String playlistName){
		return content.getOrDefault(playlistName, Collections.emptyList());
	}

	@Override
	public LiveShareSerializable clone() {
		try {
			return (LiveShareSerializable) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
}
