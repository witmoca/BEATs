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
* File: SuggestionUpdater.java
* Created: 2019
*/
package be.witmoca.BEATs.ui.components.SuggestCellEditor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

/**
 * Listens to the document and when necessary adds itself to the EDT for
 * execution
 */
public class SuggestionUpdater implements Runnable, DocumentListener {
	private final IMatcher matcher;
	private final JTextComponent parentField;
	private final JPopupMenu parentMenu;

	private JTable table;
	private int row;
	private int col;
	private Document source;
	private List<String> lastMatches;

	public SuggestionUpdater(IMatcher match, JTextComponent parentField, JPopupMenu parentMenu) {
		this.matcher = match;
		this.parentField = parentField;
		this.parentMenu = parentMenu;
	}

	@Override
	public void run() {
		try {
			String original = source.getText(0, source.getLength());

			// empty string => do nothing | SQL Query does not respect leading spaces => no
			// query
			if (original.isEmpty() || original.startsWith(" "))
				return;

			lastMatches = matcher.match(original.toLowerCase(), true, table, row, col);
			if (lastMatches == null || lastMatches.isEmpty())
				return;

			// Suggestion for TextField
			String topSuggestion = lastMatches.get(0).toLowerCase();
			if (topSuggestion == null || original.length() >= topSuggestion.length())
				return;

			// SelectionMenu Items
			// Empty menu
			parentMenu.removeAll();
			// Load items (max 7)
			SuggestMenuListener listener = new SuggestMenuListener(source);
			for (int i = 0; i < Math.min(7, lastMatches.size()); i++) {
				JMenuItem mi = new JMenuItem(lastMatches.get(i));
				mi.addActionListener(listener);
				parentMenu.add(mi);
			}
			parentMenu.pack();

			// Update parent TextField
			source.insertString(original.length(), topSuggestion.substring(original.length()), null);
			parentField.select(original.length(), topSuggestion.length());

		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		source = e.getDocument();
		if (e.getLength() > 0)
			SwingUtilities.invokeLater(this);
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		if (e.getLength() > 0) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					parentMenu.removeAll();
					parentMenu.pack();
					parentMenu.revalidate();
					parentMenu.repaint();
				}
			});
		}
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
	}

	public void setCellInfo(JTable table, int row, int column) {
		this.table = table;
		this.row = row;
		this.col = column;
	}

	private static class SuggestMenuListener implements ActionListener {
		private final Document doc;

		private SuggestMenuListener(Document d) {
			doc = d;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				doc.remove(0, doc.getLength());
				doc.insertString(0, e.getActionCommand(), null);
			} catch (BadLocationException e1) {
				e1.printStackTrace();
			}

		}
	}
}
