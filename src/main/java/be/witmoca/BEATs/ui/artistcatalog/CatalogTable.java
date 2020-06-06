/*
*
+===============================================================================+
|    BEATs (Burning Ember Archival Tool suite)                                  |
|    Copyright 2019 Jente Heremans                                              |
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
* File: CatalogTable.java
* Created: 2019
*/
package be.witmoca.BEATs.ui.artistcatalog;

import javax.swing.JLabel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

import be.witmoca.BEATs.clipboard.TransferableSong;
import be.witmoca.BEATs.ui.artistcatalog.actions.ArtistCatalogKeyBindings;
import be.witmoca.BEATs.ui.artistcatalog.actions.ArtistCatalogPopupMenu;
import be.witmoca.BEATs.ui.components.EpisodeColumnRenderer;
import be.witmoca.BEATs.ui.components.SongTable;
import be.witmoca.BEATs.ui.components.SongTableCopyOnlyTransferHandler;
import be.witmoca.BEATs.ui.t4j.MultisortTableHeaderCellRenderer;

class CatalogTable extends SongTable {
	private static final long serialVersionUID = 1L;

	public CatalogTable() {
		super(new CatalogModel());

		// right click menu
		this.setComponentPopupMenu(new ArtistCatalogPopupMenu(this));

		// CCP Handler
		this.setTransferHandler(new SongTableCopyOnlyTransferHandler());

		// Set custom renderer for the episode column
		this.getColumnModel().getColumn(3).setCellRenderer(new EpisodeColumnRenderer());
		// Make the numberRender align left
		TableCellRenderer numberR = this.getDefaultRenderer(Number.class);
		if (numberR instanceof JLabel) {
			((JLabel) numberR).setHorizontalAlignment(JLabel.LEFT);
		}

		// Add a rowsorter and render icons at the top to indicate sorting order
		TableRowSorter<?> sorter = new TableRowSorter<>(this.getModel());
		sorter.setSortsOnUpdates(true);
		sorter.setMaxSortKeys(2);
		this.setRowSorter(sorter);

		this.getTableHeader().setDefaultRenderer(new MultisortTableHeaderCellRenderer());
		
		// Register all keyboard shortcuts to be used on the table
		ArtistCatalogKeyBindings.RegisterKeyBindings(this);
	}

	@Override
	public TransferableSong getSelectedSong() {
		int rowIndex = this.getSelectedRow();
		if (rowIndex < 0)
			return null;
		if (this.getRowSorter() != null)
			rowIndex = this.getRowSorter().convertRowIndexToModel(rowIndex);

		if (!(this.getModel() instanceof CatalogModel) || (rowIndex+1) >= this.getRowCount())
			return null;
		CatalogModel model = (CatalogModel) this.getModel();

		// Copy only, so RowID can be anything (0 here); SongID is empty as there are no songIds
		return new TransferableSong((String) model.getValueAt(rowIndex, 1), "",0);
	}

}
