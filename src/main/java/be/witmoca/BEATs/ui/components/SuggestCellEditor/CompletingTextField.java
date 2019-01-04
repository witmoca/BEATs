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
* File: CompletingTextfield.java
* Created: 2019
*/
package be.witmoca.BEATs.ui.components.SuggestCellEditor;

import java.awt.Color;
import java.util.List;

import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;


class CompletingTextField extends JTextField {
	private static final long serialVersionUID = 1L;

	private final IMatcher matcher;

	private UpdateMatch currentUpdater;

	CompletingTextField(IMatcher matcher) {
		this.setBorder(new LineBorder(Color.black));
		this.matcher = matcher;
	}

	void setFocusOn(JTable table, int row, int col) {
		// Remove old updater
		if (currentUpdater != null)
			this.getDocument().removeDocumentListener(currentUpdater);

		// register new updater
		currentUpdater = new UpdateMatch(matcher, this.getDocument(), this, table, row, col);
		this.getDocument().addDocumentListener(currentUpdater);
	}
	
	/**
	 * Listens to the document and when necessary adds itself to the EDT for
	 * execution
	 */
	private static class UpdateMatch implements Runnable, DocumentListener {
		private final IMatcher matcher;
		private final Document source;
		private final JTextComponent parent;
		private final JTable table;
		private final int row;
		private final int col;

		private UpdateMatch(IMatcher match, Document doc, JTextComponent parent, JTable table, int row, int col) {
			this.matcher = match;
			this.source = doc;
			this.parent = parent;
			this.table = table;
			this.row = row;
			this.col = col;
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
			if (e.getLength() > 0)
				SwingUtilities.invokeLater(this);
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
		}
	}
}