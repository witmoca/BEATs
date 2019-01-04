/**
 * 
 */
package be.witmoca.BEATs.ui.components.SuggestCellEditor;

import java.awt.Color;
import java.awt.Component;
import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellEditor;

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
* File: SingeLineEditor.java
* Created: 2018
*/
public class AutoCompletingEditor extends DefaultCellEditor {
	private static final long serialVersionUID = 1L;

	private final TextFieldUpdater updater;
	/**
	 * Creates an AutoCompletingEditor that tries to fill in known artists
	 * 
	 * @return the desired CellEditor
	 */
	public static TableCellEditor createArtistEditor() {
		return new AutoCompletingEditor(new ArtistMatcher());
	}

	/**
	 * Creates an AutoCompletingEditor that tries to fill in known song
	 * 
	 * @param column
	 *            the column of the table that is holding the artist column (only
	 *            songs from that artist will be shown)
	 * @return the desired CellEditor
	 */
	public static TableCellEditor createSongEditor(int column) {
		return new AutoCompletingEditor(new SongMatcher(column));
	}

	protected AutoCompletingEditor(IMatcher matcher) {
		super(new JTextField());
		JTextField jtf = ((JTextField) this.getComponent());
		jtf.setBorder(new LineBorder(Color.black));
		
		
		updater = new TextFieldUpdater(matcher, jtf);
		jtf.getDocument().addDocumentListener(updater);
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		if (table == null)
			return null;
		
		updater.setCellInfo(table, row, column);
		return super.getTableCellEditorComponent(table, value, isSelected,row, column);
	}
}
