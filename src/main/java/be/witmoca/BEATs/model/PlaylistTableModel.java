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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import be.witmoca.BEATs.Launch;

public class PlaylistTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	private String PlaylistName;
	private static final String COLUMN_NAME[] = {"Artist","Song","Comment"};
	private List<PlaylistEntry> playlistList = null;

	public PlaylistTableModel(String playlistName) {
		super();
		this.setPlaylistName(playlistName);
	}

	private void reloadModel() {
		playlistList = new ArrayList<PlaylistEntry>();
		try (Statement getValue = Launch.getDb().createStatement()) {
			ResultSet value = getValue.executeQuery("SELECT Artist, Song, Comment FROM SongsInPlaylist WHERE PlaylistName = '" + PlaylistName + "'");
			while(value.next()) {
				playlistList.add(new PlaylistEntry(value.getString(1), value.getString(2) ,value.getString(3)));
			}
		} catch (SQLException e) {
		}
	}

	@Override
	public int getRowCount() {		
		return playlistList.size();
	}

	@Override
	public int getColumnCount() {
		return COLUMN_NAME.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return playlistList.get(rowIndex).getColumn(columnIndex);
	}

	@Override
	public String getColumnName(int column) {
		return COLUMN_NAME[column];
	}

	public void setPlaylistName(String playlistName) {
		PlaylistName = playlistName;
		this.reloadModel();
	}

}
