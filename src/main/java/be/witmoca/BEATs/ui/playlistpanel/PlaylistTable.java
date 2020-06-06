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
* File: PlaylistTable.java
* Created: 2018
*/
package be.witmoca.BEATs.ui.playlistpanel;

import javax.swing.table.TableRowSorter;

import be.witmoca.BEATs.clipboard.TransferableSongList;
import be.witmoca.BEATs.ui.components.SongTable;
import be.witmoca.BEATs.ui.components.SuggestCellEditor.AutoSuggestEditor;
import be.witmoca.BEATs.ui.eastpanel.currentqueue.actions.MoveToQueueAction;
import be.witmoca.BEATs.ui.playlistpanel.actions.PlayListKeyBindings;
import be.witmoca.BEATs.ui.playlistpanel.actions.PlaylistPopupMenu;
import be.witmoca.BEATs.ui.t4j.ButtonColumn;

class PlaylistTable extends SongTable {
	private static final long serialVersionUID = 1L;
	private final String playlistName;

	protected PlaylistTable(String PlaylistName) {
		super(new PlaylistTableModel(PlaylistName));
		this.playlistName = PlaylistName;

		// Add standard single column rowsorter
		TableRowSorter<PlaylistTableModel> srt = new TableRowSorter<PlaylistTableModel>(
				(PlaylistTableModel) this.getModel());
		srt.setSortable(3, false);
		srt.setMaxSortKeys(1);
		this.setRowSorter(srt);

		// right click menu
		this.setComponentPopupMenu(new PlaylistPopupMenu(this));
		// Translate the cells from the last column into buttons
		new ButtonColumn(this, new MoveToQueueAction(), 3);
		this.getColumnModel().getColumn(3).setMaxWidth(100);
		this.getColumnModel().getColumn(3).setResizable(false);

		// Drag and drop logic (no drag and drop, just Cut/Cop/Paste)
		this.setTransferHandler(new PlaylistTransferHandler());

		// Suggest support for artist and song column
		this.getColumnModel().getColumn(0).setCellEditor(AutoSuggestEditor.createArtistEditor());
		this.getColumnModel().getColumn(1).setCellEditor(AutoSuggestEditor.createSongEditor(0));

		// When a row is selected and a key is pressed, JTable starts editing.
		// With this functions, the cell editor gets focus (so blinking cursor, etc..)
		this.setSurrendersFocusOnKeystroke(true);

		// Register all keyboard shortcuts to be used on the table
		PlayListKeyBindings.RegisterKeyBindings(this);
	}

	@Override
	public TransferableSongList getSelectedSongs() {
		int indices[] = this.getSelectedRows();
		if (indices.length == 0)
			return null;
		
		// Don't copy/cut data while editing. This is considered 'weird' by users
		if(this.isEditing())
			return null;

		// protection against copying the empty 'addsong' row
		if (indices.length == 1 && (indices[0] + 1 >= this.getRowCount()))
			return null;

		TransferableSongList list = new TransferableSongList();

		if (!(this.getModel() instanceof PlaylistTableModel))
			return null;
		PlaylistTableModel model = (PlaylistTableModel) this.getModel();
		for (int i : indices) {
			// Skip the addRow row
			if(i + 1 >= this.getRowCount())
				continue;
			list.addSong((String) model.getValueAt(i, 0), (String) model.getValueAt(i, 1), model.getRowId(i));
		}
		return list;
	}

	public String getPlaylistName() {
		return playlistName;
	}
}
