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

import java.util.ArrayList;
import java.util.List;

import javax.swing.DropMode;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import be.witmoca.BEATs.model.PlaylistEntry;
import be.witmoca.BEATs.model.PlaylistTableModel;
import be.witmoca.BEATs.model.TransferableSongs;
import be.witmoca.BEATs.ui.currentqueue.MoveToQueueAction;
import be.witmoca.BEATs.ui.southpanel.SongTable;
import be.witmoca.BEATs.ui.t4j.ButtonColumn;
import be.witmoca.BEATs.utils.UiUtils;

public class PlaylistTable extends SongTable {
	private static final long serialVersionUID = 1L;
	private final String playlistName;

	protected PlaylistTable(String PlaylistName) {
		super(new PlaylistTableModel(PlaylistName));
		this.playlistName = PlaylistName;
		this.getTableHeader().setReorderingAllowed(false);
		this.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		// Add standard single column rowsorter
		TableRowSorter<PlaylistTableModel> srt = new TableRowSorter<PlaylistTableModel>((PlaylistTableModel) this.getModel());
		srt.setSortable(3, true);
		srt.setMaxSortKeys(1);
		this.setRowSorter(srt);
		
		this.setComponentPopupMenu(new PlaylistPopupMenu(this));
		new ButtonColumn(this, new MoveToQueueAction(), 3);
		
		this.setDragEnabled(true);
		this.setDropMode(DropMode.USE_SELECTION);
		this.setTransferHandler(new PlaylistTransferHandler());
	}
	
	protected void setTabTitle(String tabTitle) {
		((PlaylistTableModel) this.getModel()).setPlaylistName(tabTitle);
	}

	@Override
	public TransferableSongs getSelectedSongs() {
		int[] rowIndices = UiUtils.convertSelectionToModel(this.getSelectedRows(), this);
		TableModel model = this.getModel();
		
		List<PlaylistEntry> tfs = new ArrayList<PlaylistEntry>();		
		for(int i = 0; i < rowIndices.length; i++) {
			PlaylistEntry pe = new PlaylistEntry( (String) model.getValueAt(rowIndices[i], 0), (String) model.getValueAt(rowIndices[i], 1), (String) model.getValueAt(rowIndices[i], 2) );
			if(!pe.isEmpty())
				tfs.add(pe);
		}
		return new TransferableSongs(tfs);
	}

	public String getPlaylistName() {
		return playlistName;
	}
}
