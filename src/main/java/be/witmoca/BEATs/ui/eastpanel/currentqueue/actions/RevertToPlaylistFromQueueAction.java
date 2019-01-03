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
* File: RevertToPlaylistFromQueueAction.java
* Created: 2018
*/
package be.witmoca.BEATs.ui.eastpanel.currentqueue.actions;

import java.awt.event.ActionEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JList;
import javax.swing.JOptionPane;

import be.witmoca.BEATs.connection.SQLConnection;
import be.witmoca.BEATs.connection.CommonSQL;
import be.witmoca.BEATs.connection.DataChangedType;
import be.witmoca.BEATs.ui.ApplicationWindow;
import be.witmoca.BEATs.ui.eastpanel.currentqueue.CurrentQueueListModel;
import be.witmoca.BEATs.utils.Lang;
import be.witmoca.BEATs.utils.UiIcon;

class RevertToPlaylistFromQueueAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
	private final JList<String> queue;

	protected RevertToPlaylistFromQueueAction(JList<String> Queue) {
		super(Lang.getUI("queue.revert"));
		this.putValue(Action.SMALL_ICON, UiIcon.REVERT.getIcon());
		queue = Queue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		int songOrder = -1;
		try {
			songOrder = ((CurrentQueueListModel) queue.getModel()).getSongOrderAt(queue.getSelectedIndex());
		} catch (ArrayIndexOutOfBoundsException e1) {
			return;
		}

		try {
			List<String> playlists = CommonSQL.getPlaylists();

			String[] options = playlists.toArray(new String[0]);
			String playlistName = (String) JOptionPane.showInputDialog(ApplicationWindow.getAPP_WINDOW(),
					Lang.getUI("queue.revert.to"), Lang.getUI("queue.revert.title"), JOptionPane.QUESTION_MESSAGE, null,
					options, options[0]);
			if (playlistName == null)
				return;

			String artist;
			String song;
			String comment;
			try (PreparedStatement sel = SQLConnection.getDbConn().prepareStatement(
					"SELECT ArtistName, Title, Comment FROM CurrentQueue,Song WHERE CurrentQueue.songId = Song.SongId AND SongOrder = ?")) {
				sel.setInt(1, songOrder);
				ResultSet rs = sel.executeQuery();
				if (!rs.next())
					throw new SQLException("No CurrentQueue entry found matching the Song Order " + songOrder);
				artist = rs.getString(1);
				song = rs.getString(2);
				comment = rs.getString(3);
			}

			CommonSQL.addSongInPlaylist(playlistName, artist, song, comment);
			CommonSQL.removeFromCurrentQueue(songOrder);

			SQLConnection.getDbConn()
					.commit(EnumSet.of(DataChangedType.SONGS_IN_PLAYLIST, DataChangedType.CURRENT_QUEUE));
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

	}

}
