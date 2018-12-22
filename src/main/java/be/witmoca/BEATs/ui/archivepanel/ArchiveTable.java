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
* File: ArchiveTable.java
* Created: 2018
*/
package be.witmoca.BEATs.ui.archivepanel;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.TableModel;

import be.witmoca.BEATs.model.ArchiveTableModel;
import be.witmoca.BEATs.model.PlaylistEntry;
import be.witmoca.BEATs.model.TransferableSongs;
import be.witmoca.BEATs.ui.southpanel.SongTable;
import be.witmoca.BEATs.ui.t4j.MultisortTableHeaderCellRenderer;
import be.witmoca.BEATs.utils.UiUtils;

class ArchiveTable extends SongTable {
	private static final long serialVersionUID = 1L;

	public ArchiveTable() {
		super(new ArchiveTableModel());
		this.getTableHeader().setReorderingAllowed(false);
		
		

		// Add a rowsorter and render icons at the top to indicate sorting order
		this.setRowSorter(new ArchiveTableRowSorter<>(this.getModel()));
		this.getTableHeader().setDefaultRenderer(new MultisortTableHeaderCellRenderer());
		
		this.setDragEnabled(true);
		this.setTransferHandler(new ArchiveTransferHandler());
	}

	@Override
	public TransferableSongs getSelectedSongs() {
		int[] rowIndices = UiUtils.convertSelectionToModel(this.getSelectedRows(), this);
		TableModel model = this.getModel();
		
		List<PlaylistEntry> tfs = new ArrayList<PlaylistEntry>();		
		for(int i = 0; i < rowIndices.length; i++) {
			tfs.add(new PlaylistEntry(0, (String) model.getValueAt(rowIndices[i], 0), (String) model.getValueAt(rowIndices[i], 1), "" ) );
		}
		return new TransferableSongs(tfs);
	}
}
