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

import javax.swing.table.TableRowSorter;

import be.witmoca.BEATs.clipboard.TransferableSong;
import be.witmoca.BEATs.ui.archivepanel.actions.ArchivePopupMenu;
import be.witmoca.BEATs.ui.components.EpisodeColumnRenderer;
import be.witmoca.BEATs.ui.components.SongTable;
import be.witmoca.BEATs.ui.components.SongTableCopyOnlyTransferHandler;
import be.witmoca.BEATs.ui.t4j.MultisortTableHeaderCellRenderer;

class ArchiveTable extends SongTable {
	private static final long serialVersionUID = 1L;

	public ArchiveTable() {
		super(new ArchiveTableModel());

		// Set custom renderer for the episode column
		this.getColumnModel().getColumn(2).setCellRenderer(new EpisodeColumnRenderer());

		// Add a rowsorter and render icons at the top to indicate sorting order
		TableRowSorter<?> sorter = new TableRowSorter<>(this.getModel());
		sorter.setSortsOnUpdates(true);
		sorter.setMaxSortKeys(2);
		this.setRowSorter(sorter);

		this.getTableHeader().setDefaultRenderer(new MultisortTableHeaderCellRenderer());

		this.setComponentPopupMenu(new ArchivePopupMenu(this));

		// Drag and drop logic (no drag and drop, just Cut/Cop/Paste)
		this.setTransferHandler(new SongTableCopyOnlyTransferHandler());
	}

	@Override
	public TransferableSong getSelectedSong() {
		int rowIndex = this.getSelectedRow();
		if (rowIndex < 0)
			return null;
		if (this.getRowSorter() != null)
			rowIndex = this.getRowSorter().convertRowIndexToModel(rowIndex);

		if (!(this.getModel() instanceof ArchiveTableModel))
			return null;
		ArchiveTableModel model = (ArchiveTableModel) this.getModel();

		// Since this is a copy only table, the RowID can be anything (0 here)
		return new TransferableSong((String) model.getValueAt(rowIndex, 0), (String) model.getValueAt(rowIndex, 1), 0);
	}
}
