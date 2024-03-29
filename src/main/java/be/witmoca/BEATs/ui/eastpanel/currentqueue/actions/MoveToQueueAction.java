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
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JTable;

import be.witmoca.BEATs.connection.SQLConnection;
import be.witmoca.BEATs.connection.CommonSQL;
import be.witmoca.BEATs.connection.DataChangedType;
import be.witmoca.BEATs.ui.ApplicationWindow;
import be.witmoca.BEATs.ui.playlistpanel.PlaylistTableModel;
import be.witmoca.BEATs.utils.Lang;
import be.witmoca.BEATs.utils.OriginHelper;

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
			try {
				artistExists = CommonSQL.isArtistExisting(rawArtist);
			} catch (SQLException e2) {
				artistExists = false;
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
				CommonSQL.addArtist(rawArtist, OriginHelper.UNKNOWN_LOCALE_STRING);
			}
			
			// Check if origin of artist is unknown (existing with unknown locale, or new one)
			if (OriginHelper.UNKNOWN_LOCALE_STRING.equals(CommonSQL.getArtistOrigin(rawArtist).trim()) ) {
				// ask about country of origin
				
				// Translate all country codes into readable countries  + unknown option
				List<String> origins = Arrays.asList(OriginHelper.getDisplayOriginList());
				
				// Ask user
				String answerOrigin = (String) JOptionPane.showInputDialog(ApplicationWindow.getAPP_WINDOW(),
						rawArtist + ": " + Lang.getUI("queue.moveToQueue.unknownOrigin"),
						Lang.getUI("queue.moveToQueue.unknownOriginTitle"), JOptionPane.QUESTION_MESSAGE,
						null, origins.toArray(), origins.get(0));
				
				if (answerOrigin != null) {
					// Translate back to 2-letter & add
					CommonSQL.updateOriginOfArtist(OriginHelper.getOriginCodeFromDisplayString(answerOrigin), rawArtist);
				}
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
