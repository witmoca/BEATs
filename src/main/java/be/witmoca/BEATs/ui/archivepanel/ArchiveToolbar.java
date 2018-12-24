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
* File: ArchiveToolbar.java
* Created: 2018
*/
package be.witmoca.BEATs.ui.archivepanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import be.witmoca.BEATs.ui.UiIcon;

class ArchiveToolbar extends JToolBar {
	private static final long serialVersionUID = 1L;

	private final JTextField searchTerm = new JTextField(20);
	private final JButton searchSubmit = new JButton("Search", UiIcon.SEARCH.getIcon());
	private final JTable archiveTable;

	
	public ArchiveToolbar(JTable table) {
		super("Archive Toolbar",JToolBar.HORIZONTAL);
		archiveTable = table;
		
		add(new DeleteEntryAction(table));
		add(new ChangeDateAction(table));
		add(new JButton(new RenameArtistAction(table)));
		add(new JButton(new RenameSongAction(table)));
		add(new JButton(new ChangeLocalAction(table)));
		
		// Beyond this point all goes on the right
		add(Box.createHorizontalGlue());
		
		// Adhere to the given column count in the text fields constructor
		searchTerm.setMaximumSize(searchTerm.getPreferredSize());
		this.add(searchTerm);
		this.addSeparator();
		this.add(searchSubmit);
		this.setFloatable(false);
		
		SortAction sa = new SortAction();
		// Search on ENTER while in textfield
		searchTerm.addActionListener(sa);
		// search on buttonpress
		searchSubmit.addActionListener(sa);
		
	}
	
	
	private class SortAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			String sTerm = searchTerm.getText().trim();
			if(!sTerm.isEmpty())
				((ArchiveTableRowSorter<?>) archiveTable.getRowSorter()).setRowFilter(new SearchRowFilter(sTerm));
			else
				((ArchiveTableRowSorter<?>) archiveTable.getRowSorter()).setRowFilter(null);
		}					
	}
}
