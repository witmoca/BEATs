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
package be.witmoca.BEATs.ui.liveshare;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import be.witmoca.BEATs.connection.DataChangedListener;
import be.witmoca.BEATs.liveshare.LiveShareClient;
import be.witmoca.BEATs.ui.components.PlaylistEntry;
import be.witmoca.BEATs.utils.Lang;

public class LiveShareTableModel extends AbstractTableModel implements DataChangedListener {
	private static final long serialVersionUID = 1L;
	private static final String COLUMN_NAME[] = { Lang.getUI("col.artist"), Lang.getUI("col.song"),
			Lang.getUI("col.comment")};
	
	private final List<PlaylistEntry> playlistList = new ArrayList<PlaylistEntry>();
	private final String playlistName;
	private final LiveShareClient lsc;
	private final String serverName;
	
	LiveShareTableModel(String playlistName, LiveShareClient lsc, String serverName) {
		super();
		this.playlistName = playlistName;
		this.lsc = lsc;
		this.serverName = serverName;
		
		tableChanged();
		lsc.addDataChangedListener(this);
	}

	@Override
	public void tableChanged() {
		List<PlaylistEntry> newItems = lsc.getContent(this.serverName).getPlaylistContents(playlistName);
		
		// This compares both lists completely. Make sure that the items have a fast (or at least fast failing) and correct equals function
		if(!playlistList.equals(newItems)) {
			// Update list
			playlistList.clear();
			playlistList.addAll(newItems);
			this.fireTableDataChanged();
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

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		return;
	}
}
