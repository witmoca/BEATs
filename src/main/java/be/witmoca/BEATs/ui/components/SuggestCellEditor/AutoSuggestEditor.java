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
* File: AutoSuggestEditor.java
* Created: 2019
*/
package be.witmoca.BEATs.ui.components.SuggestCellEditor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.DefaultCellEditor;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.table.TableCellEditor;

public class AutoSuggestEditor extends DefaultCellEditor{
	private static final long serialVersionUID = 1L;
	private final JPopupMenu suggestions = new JPopupMenu();
	private final SuggestionUpdater updater;
	
	/**
	 * Creates an AutoSuggestEditor that tries to fill in known artists
	 * 
	 * @return the desired CellEditor
	 */
	public static TableCellEditor createArtistEditor() {
		return new AutoSuggestEditor(new ArtistMatcher());
	}

	/**
	 * Creates an AutoSuggestEditor that tries to fill in known song
	 * 
	 * @param column
	 *            the column of the table that is holding the artist column (only
	 *            songs from that artist will be shown)
	 * @return the desired CellEditor
	 */
	public static TableCellEditor createSongEditor(int column) {
		return new AutoSuggestEditor(new SongMatcher(column));
	}
	
	private AutoSuggestEditor(IMatcher matcher) {		
		super(new JTextField());
		
		JTextField jtf = ((JTextField) this.getComponent());
		jtf.setBorder(new LineBorder(Color.black));	
		
		updater = new SuggestionUpdater(matcher, jtf, suggestions);
		jtf.getDocument().addDocumentListener(updater);
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		if (table == null)
			return null;
		
		updater.setCellInfo(table, row, column);
		JTextField jtf = (JTextField) super.getTableCellEditorComponent(table, value, isSelected, row, column);	
		
		ShowListener sl = new ShowListener(suggestions, table, row, column);
		jtf.addFocusListener(sl);
		jtf.addAncestorListener(sl);		
		return jtf;
	}
	
	/**
	 * Shows/Hides the popupmenu depending on focus
	 */
	private static class ShowListener implements FocusListener, AncestorListener {
		private final JPopupMenu menu;
		private final JTable table;
		private final Point location;
		
		private ShowListener(JPopupMenu menu, JTable table, int row, int col) {
			this.menu = menu;
			this.table = table;
			
			Rectangle r = table.getCellRect(row, col, true);
			location = new Point(r.x, r.y + r.height);
		}
		
		@Override
		public void focusGained(FocusEvent e) {
			Point p = new Point(location);
			SwingUtilities.convertPointToScreen(p, table);
			menu.setLocation(new Point(p));
			menu.setVisible(true);
		}

		@Override
		public void focusLost(FocusEvent e) {
			((JTextField) e.getSource()).removeFocusListener(this);
			((JTextField) e.getSource()).removeAncestorListener(this);
			menu.setVisible(false);
		}

		@Override
		public void ancestorAdded(AncestorEvent event) {
		}

		@Override
		public void ancestorRemoved(AncestorEvent event) {
		}

		@Override
		public void ancestorMoved(AncestorEvent event) {
			// Moving an ancester invalidates the menu location
			// => close the menu (as if the focus was lost)
			Point p = new Point(location);
			SwingUtilities.convertPointToScreen(p, table);
			menu.setLocation(new Point(p));
			menu.setVisible(true);
		}
	}
}
