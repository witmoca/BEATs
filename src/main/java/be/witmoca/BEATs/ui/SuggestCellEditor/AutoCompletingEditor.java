/**
 * 
 */
package be.witmoca.BEATs.ui.SuggestCellEditor;

import java.awt.Color;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import be.witmoca.BEATs.utils.StringUtils;

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

	public AutoCompletingEditor(IMatcher matcher) {
		super(new JTextField());
		
		JTextField field = ((JTextField) getComponent());
		field.setBorder(new LineBorder(Color.black));
		
		field.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				if(e.getLength() > 0)
					SwingUtilities.invokeLater(new UpdateMatch(matcher, e.getDocument(), field));
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
			}
			
		});
	}
	
	static class UpdateMatch implements Runnable{
		private final IMatcher matcher;
		private final Document source;
		private final JTextComponent parent;
		
		public UpdateMatch(IMatcher match, Document doc, JTextComponent parent) {
			this.matcher = match;
			this.source = doc;
			this.parent = parent;
		}
		@Override
		public void run() {
			try {
				String original = source.getText(0, source.getLength());
				System.out.println(" orig: " + original);
				if(original.isEmpty())
					return;
				
				List<String> matches = matcher.match(StringUtils.filterPrefix(original).toLowerCase(), true);
				if(matches == null || matches.isEmpty())
					return;
				
				String topSuggestion = matches.get(0).toLowerCase();
				
				if(topSuggestion == null || original.length() == topSuggestion.length())
					return;
						
				source.insertString(original.length(), topSuggestion.substring(original.length()), null);
				parent.select(original.length(), topSuggestion.length());
				
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}
}
