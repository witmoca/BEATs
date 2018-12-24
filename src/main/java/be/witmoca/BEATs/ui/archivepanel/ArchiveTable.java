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
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
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
		
		// Only a single line is allowed (some tools depend on this being true)
		this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		// Set custom renderer for the episode column
		this.getColumnModel().getColumn(2).setCellRenderer(new EpisodeRenderer());

		// Add a rowsorter and render icons at the top to indicate sorting order
		this.setRowSorter(new ArchiveTableRowSorter<>(this.getModel()));
		this.getTableHeader().setDefaultRenderer(new MultisortTableHeaderCellRenderer());
		
		this.setComponentPopupMenu(new ArchivePopupMenu(this));
		
		// Drag n drop logic
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
