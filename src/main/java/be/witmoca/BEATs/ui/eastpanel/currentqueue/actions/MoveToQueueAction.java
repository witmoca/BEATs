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
* File: MoveToQueueAction.java
* Created: 2018
*/
package be.witmoca.BEATs.ui.eastpanel.currentqueue.actions;

import java.awt.event.ActionEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumSet;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JTable;

import be.witmoca.BEATs.connection.SQLConnection;
import be.witmoca.BEATs.connection.CommonSQL;
import be.witmoca.BEATs.connection.DataChangedType;
import be.witmoca.BEATs.ui.ApplicationWindow;
import be.witmoca.BEATs.ui.playlistpanel.PlaylistTableModel;
import be.witmoca.BEATs.utils.Lang;

public class MoveToQueueAction extends AbstractAction {
	private static final long serialVersionUID = 1L;

	@Override
	public void actionPerformed(ActionEvent e) {
		int row = Integer.valueOf(e.getActionCommand());
		JTable source = (JTable) e.getSource();

		// copy info
		String rawArtist = (String) source.getModel().getValueAt(row, 0);
		String rawSong = (String) source.getModel().getValueAt(row, 1);
		String rawComment = "";
		try {
			rawComment = ((String) source.getModel().getValueAt(row, 2)).trim();
		} catch (NullPointerException e1) {
		}
		if (rawArtist.isEmpty() || rawSong.isEmpty()) {
			return;
		}
		try {
			// Check if artist exists already
			boolean artistExists = false;
			try (PreparedStatement findArtist = SQLConnection.getDbConn()
					.prepareStatement("SELECT count(*) FROM artist WHERE ArtistName = ?")) {
				findArtist.setString(1, rawArtist);
				ResultSet rs = findArtist.executeQuery();
				if (rs.next() && rs.getInt(1) > 0)
					artistExists = true;
			}

			// if artist exist, find song
			int songId = -1;
			if (artistExists) {
				try (PreparedStatement findSong = SQLConnection.getDbConn()
						.prepareStatement("SELECT songId FROM Song WHERE ArtistName = ? AND Title = ?")) {
					findSong.setString(1, rawArtist);
					findSong.setString(2, rawSong);
					ResultSet rs = findSong.executeQuery();
					if (rs.next())
						songId = rs.getInt(1);
				}
			} else {
				// create new artist if he doesn't exist
				// ask if artist is local
				String options[] = { Lang.getUI("queue.moveToQueue.local"), Lang.getUI("queue.moveToQueue.notlocal"),
						Lang.getUI("action.cancel") };
				int answerLocal = JOptionPane.showOptionDialog(ApplicationWindow.getAPP_WINDOW(),
						rawArtist + " " + Lang.getUI("queue.moveToQueue.newArtist"),
						Lang.getUI("queue.moveToQueue.newArtistTitle"), JOptionPane.YES_NO_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, options, 2);
				if (answerLocal == JOptionPane.CANCEL_OPTION || answerLocal == JOptionPane.CLOSED_OPTION) {
					return; // CANCEL
				}
				CommonSQL.addArtist(rawArtist, (answerLocal == 0));
			}

			// create song if it doesn't exist
			if (songId < 0) {
				songId = CommonSQL.addSong(rawSong, rawArtist);
			}

			// Add the new set to the currentQueue
			CommonSQL.addCurrentQueue(songId, rawComment);

			// delete row
			((PlaylistTableModel) source.getModel()).deleteRow(row);

			// commit
			SQLConnection.getDbConn()
					.commit(EnumSet.of(DataChangedType.SONGS_IN_PLAYLIST, DataChangedType.CURRENT_QUEUE));
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

}
