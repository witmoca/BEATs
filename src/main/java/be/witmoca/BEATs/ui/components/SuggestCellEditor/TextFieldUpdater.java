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
* File: TextFieldUpdater.java
* Created: 2019
*/
package be.witmoca.BEATs.ui.components.SuggestCellEditor;

import java.util.List;

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
public class TextFieldUpdater implements Runnable, DocumentListener, SuggestionUpdater {
	private final IMatcher matcher;
	private final JTextComponent parent;
	
	private JTable table;
	private int row;
	private int col;
	private Document source;

	public TextFieldUpdater(IMatcher match, JTextComponent parent) {
		this.matcher = match;
		this.parent = parent;
	}

	@Override
	public void run() {
		try {
			String original = source.getText(0, source.getLength());

			// empty string => do nothing | SQL Query does not respect leading spaces => no
			// query
			if (original.isEmpty() || original.startsWith(" "))
				return;

			List<String> matches = matcher.match(original.toLowerCase(), true, table, row, col);
			if (matches == null || matches.isEmpty())
				return;

			String topSuggestion = matches.get(0).toLowerCase();

			if (topSuggestion == null || original.length() >= topSuggestion.length())
				return;

			source.insertString(original.length(), topSuggestion.substring(original.length()), null);
			parent.select(original.length(), topSuggestion.length());

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
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
	}

	@Override
	public void setCellInfo(JTable table, int row, int column) {
		this.table = table;
		this.row = row;
		this.col = column;
	}
}
