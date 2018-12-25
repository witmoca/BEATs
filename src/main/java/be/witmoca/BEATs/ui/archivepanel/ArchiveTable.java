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

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import be.witmoca.BEATs.clipboard.TransferableSong;
import be.witmoca.BEATs.model.ArchiveTableModel;
import be.witmoca.BEATs.ui.extendables.SongTable;
import be.witmoca.BEATs.ui.t4j.MultisortTableHeaderCellRenderer;

class ArchiveTable extends SongTable {
	private static final long serialVersionUID = 1L;

	public ArchiveTable() {
		super(new ArchiveTableModel());
		
		// Set custom renderer for the episode column
		this.getColumnModel().getColumn(2).setCellRenderer(new EpisodeRenderer());

		// Add a rowsorter and render icons at the top to indicate sorting order
		this.setRowSorter(new ArchiveTableRowSorter<>(this.getModel()));
		this.getTableHeader().setDefaultRenderer(new MultisortTableHeaderCellRenderer());
		
		this.setComponentPopupMenu(new ArchivePopupMenu(this));
		
		// Drag and drop logic (no drag and drop, just Cut/Cop/Paste)
		this.setTransferHandler(new ArchiveTransferHandler());
	}

	@Override
	public TransferableSong getSelectedSong() {
		int rowIndex = this.getSelectedRow();
		if(rowIndex < 0)
			return null;
		if(this.getRowSorter() != null)
			rowIndex = this.getRowSorter().convertRowIndexToModel(rowIndex);
		
		if(!(this.getModel() instanceof ArchiveTableModel))
			return null;
		ArchiveTableModel model = (ArchiveTableModel) this.getModel();

		return new TransferableSong((String) model.getValueAt(rowIndex, 0),(String) model.getValueAt(rowIndex, 1), model.getRowId(rowIndex));
	}
	
	private static class EpisodeRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			if(table == null || !(table.getModel() instanceof ArchiveTableModel) )
				return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			
			if(table.getRowSorter() != null)
				row = table.getRowSorter().convertRowIndexToModel(row);
			
			return super.getTableCellRendererComponent(table, value + " (" +  ((ArchiveTableModel) table.getModel()).getEpisodeDate(row) + ")", isSelected, hasFocus, row, column);
		}
	}
}
