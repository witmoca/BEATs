/**
 * 
 */
package be.witmoca.BEATs.liveview;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import be.witmoca.BEATs.connection.CommonSQL;
import be.witmoca.BEATs.connection.SQLConnection;
import be.witmoca.BEATs.ui.components.PlaylistEntry;

/**
 * @author Witmoca
 *
 */
public class LiveViewSerializable implements Serializable, Cloneable {
	private static final long serialVersionUID = 1L;
	// HashMap = serializable
	private final HashMap<String, List<PlaylistEntry>> content = new HashMap<String, List<PlaylistEntry>>();
	private final List<String> playlistNames = new ArrayList<String>(); // double info, but array list is ordered!

	private LiveViewSerializable(boolean empty) throws SQLException {
		if (!empty) {
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

	public static LiveViewSerializable createSnapShot() {
		try {
			return new LiveViewSerializable(false);
		} catch (SQLException e) {
			return null;
		}
	}
	
	public static LiveViewSerializable createEmpty() {
		try {
			return new LiveViewSerializable(false);
		} catch (SQLException e) {
			return null;
		}
	}

	public List<String> getPlaylists() {
		return playlistNames;
	}
	
	public List<PlaylistEntry> getPlaylistContents(String playlistName){
		return content.get(playlistName);
	}

	@Override
	public LiveViewSerializable clone() {
		try {
			return (LiveViewSerializable) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
}
