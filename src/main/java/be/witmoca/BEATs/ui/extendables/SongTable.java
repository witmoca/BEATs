/**
 * 
 */
package be.witmoca.BEATs.ui.extendables;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableModel;

import be.witmoca.BEATs.model.TransferableSongs;

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

		// CatWalk seems cosmically perfect. A super model & a mouse listener. This is getting a bit cringy though.
		this.addMouseListener(new CatWalk());
	}

	
	@Override
	public Point getPopupLocation(MouseEvent event) {
		// Popuplocation is always the bottomleft corner of the selected item
		int row = this.getSelectedRow();
		if (row < 0)
			return null;
		
		int column = this.getSelectedColumn();
		if (column < 0)
			column = 2;
		
		// Calculate according to all rows
		Rectangle cell = this.getCellRect(row, column, false);		
		return new Point(cell.x, cell.y + cell.height);
	}

	abstract public TransferableSongs getSelectedSongs();

	private static class CatWalk extends MouseAdapter {
		
		// add mouselistener: Rightclick (popupmenu open) also adjusts selector
		@Override
		public void mousePressed(MouseEvent e) {
			if (!(e.getSource() instanceof JTable))
				return;
			
			JTable table = (JTable) e.getSource();
			int row = table.rowAtPoint(e.getPoint());
			int column = table.columnAtPoint(e.getPoint());
			
			if(row >= 0 && column >= 0 && SwingUtilities.isRightMouseButton(e)) {
				table.changeSelection(row, column, false, false);
			}
		}
	}
}
