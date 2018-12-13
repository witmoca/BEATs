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
* File: PlaylistTableModel.java
* Created: 2018
*/
package be.witmoca.BEATs.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import be.witmoca.BEATs.Launch;

public class PlaylistTableModel extends AbstractTableModel implements DataChangedListener {
	private static final long serialVersionUID = 1L;
	private String PlaylistName;
	private static final String COLUMN_NAME[] = { "Artist", "Song", "Comment" };
	private List<PlaylistEntry> playlistList = null;

	public PlaylistTableModel(String playlistName) {
		super();
		this.setPlaylistName(playlistName);

		Launch.getDB_CONN().addDataChangedListener(this, DataChangedListener.DataType.PLAYLIST_DATA_OPTS);
		tableChanged();
	}

	@Override
	public void tableChanged() {
		playlistList = new ArrayList<PlaylistEntry>();
		try (PreparedStatement getValue = Launch.getDB_CONN()
				.prepareStatement("SELECT Artist, Song, Comment FROM SongsInPlaylist WHERE PlaylistName = ?")) {
			getValue.setString(1, PlaylistName);
			ResultSet value = getValue.executeQuery();
			while (value.next()) {
				playlistList.add(new PlaylistEntry(value.getString(1), value.getString(2), value.getString(3)));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		this.fireTableDataChanged();
	}

	@Override
	public int getRowCount() {
		return playlistList.size() + 1;
	}

	@Override
	public int getColumnCount() {
		return COLUMN_NAME.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		try {
			return playlistList.get(rowIndex).getColumn(columnIndex);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	@Override
	public String getColumnName(int column) {
		return COLUMN_NAME[column];
	}

	public void setPlaylistName(String playlistName) {
		PlaylistName = playlistName;
		tableChanged();
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if(rowIndex >= playlistList.size()) {
			// INSERT NEW
			if(((String) aValue).trim().isEmpty() )
				return;
			
			String ins[] = {"","",""};
			ins[columnIndex] = (String) aValue;
			try {
				SQLObjectTransformer.addSongInPlaylist(PlaylistName, ins[0], ins[1], ins[2]);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			
			// Load with current values and update with new one (in array)
			String values[] = new String[3];
			for(int i = 0; i < values.length ; i++) {
				values[i] = playlistList.get(rowIndex).getColumn(1);
			}
			values[columnIndex] = (String) aValue;
			
			try {
				// check if a delete isn't more appropriate (all empty cells)
				if(String.join("", values).trim().isEmpty()) {
					try (PreparedStatement updateVal = Launch.getDB_CONN().prepareStatement("DELETE FROM SongsInPlaylist WHERE PlaylistName = ? AND Artist = ? AND Song = ? AND Comment = ?")) {
						for(int i = 0; i < values.length; i++) {
							updateVal.setString(2 + i, playlistList.get(rowIndex).getColumn(i)); //old values
						}
						updateVal.setString(1, PlaylistName);
						updateVal.executeUpdate();
					}
				} else {
					try (PreparedStatement updateVal = Launch.getDB_CONN().prepareStatement("UPDATE SongsInPlaylist SET Artist = ?, Song = ?, Comment = ? WHERE PlaylistName = ? AND Artist = ? AND Song = ? AND Comment = ?")) {
						for(int i = 0; i < values.length; i++) {
							updateVal.setString(1 + i, values[i]); // new values
							updateVal.setString(5 + i, playlistList.get(rowIndex).getColumn(i)); //old values
						}
						updateVal.setString(4, PlaylistName);
						updateVal.executeUpdate();
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		this.tableChanged();
	}
}
