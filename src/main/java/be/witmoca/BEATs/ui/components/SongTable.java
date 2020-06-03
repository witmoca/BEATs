/**
 * 
 */
package be.witmoca.BEATs.ui.components;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SwingUtilities;
import javax.swing.table.TableModel;

import be.witmoca.BEATs.clipboard.TransferableSong;

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
* File: SongTable.java
* Created: 2018
*/

/**
 * 
 * SongTable describes a JTable capable of transferring Songs for CCP or
 * drag-and-drop operation
 */
public abstract class SongTable extends JTable {
	private static final long serialVersionUID = 1L;

	public SongTable(TableModel model) {
		super(model); // Just fabulous, a supermodel!

		this.getTableHeader().setReorderingAllowed(false);

		
		this.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		// always fill viewport even when empty
		this.setFillsViewportHeight(true);

		// This stops the cells from editing without actually clicking the cells (just
		// typing)
		// Not a prefered method (especially the property), but no better one exists
		this.setFocusable(false);
		this.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

		// Table look
		this.setRowHeight(this.getRowHeight() + 2); // Add 2 pixels to row height so all characters are visible when
													// editing
	}

	@Override
	public Point getPopupLocation(MouseEvent e) {
		// Ensure JTable
		if (!(e.getSource() instanceof JTable))
			return null;
		
		JTable table = (JTable) e.getSource();
		int row = table.rowAtPoint(e.getPoint());
		int column = table.columnAtPoint(e.getPoint());
		
		// Popuplocation is always the bottomleft corner of the selected item
		if (row < 0)
			return null;

		if (column < 0)
			column = 2;
		
		// Change the selection depending on the row selection state
		// Selected = keep selection; row not selected = reset to this single line
		if(! table.isRowSelected(row)) {
			// If not selected, deselect all and set this line as selected
			table.changeSelection(row, column, false, false);
		}	

		// Calculate according to all rows
		Rectangle cell = this.getCellRect(row, column, false);
		return new Point(cell.x, cell.y + cell.height);
	}

	abstract public TransferableSong getSelectedSong();
	
	

	/**
	 * Returns the selected row indices converted by the RowSorter into model indices
	 * Sorted into ascending order
	 */
	@Override
	public int[] getSelectedRows() {
		int indices[] = super.getSelectedRows();
		if (indices.length <= 0)
			return new int[0];
		
		// No rowsorter = no convert
		if (this.getRowSorter() == null) {
			return indices;
		}
		
		// Convert indices
		int convI[] = new int[indices.length];
		RowSorter<?> rs = this.getRowSorter();
		
		// Loop through conversion
		try {
			for(int i = 0; i < indices.length; i++)
				convI[i] = rs.convertRowIndexToModel(indices[i]);
		} catch (Exception e) {
			// if one of the indices is invalid, consider the whole array invalid
			return new int[0];
		}
		
		// for easy looping purposes
		Arrays.sort(convI);
		return convI;
	}
}
