/**
 * 
 */
package be.witmoca.BEATs.ui.archivepanel.actions;

import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.EnumSet;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

import be.witmoca.BEATs.connection.SQLConnection;
import be.witmoca.BEATs.ui.ApplicationWindow;
import be.witmoca.BEATs.connection.CommonSQL;
import be.witmoca.BEATs.connection.DataChangedType;
import be.witmoca.BEATs.utils.StringUtils;
import be.witmoca.BEATs.utils.UiIcon;

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
* File: RenameSongAction.java
* Created: 2018
*/
class RenameSongAction extends AbstractAction {
	private static final long serialVersionUID = 1L;

	private final JTable archive;

	RenameSongAction(JTable table) {
		super("Song");
		this.putValue(Action.SMALL_ICON, UiIcon.EDIT_W.getIcon());
		archive = table;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		int index = archive.getSelectedRow();
		int originalIndex = index;
		if (index < 0)
			return;
		if (archive.getRowSorter() != null)
			index = archive.getRowSorter().convertRowIndexToModel(index);

		// PREPARE variables for user
		String artist = (String) archive.getModel().getValueAt(index, 0);
		String title = (String) archive.getModel().getValueAt(index, 1);

		// USER UI interaction
		JPanel userPanel = new JPanel();
		userPanel.add(new JLabel("Change the spelling of the song: "));
		JTextField newName = new JTextField(title);
		newName.setColumns(30);
		userPanel.add(newName);

		if (JOptionPane.showConfirmDialog(ApplicationWindow.getAPP_WINDOW(), userPanel, "Rename",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
			return; // cancelled
		}

		// MAKE CHANGES

		String renamed = StringUtils.ToUpperCamelCase(newName.getText());
		if(title.equals(renamed))
			return;

		try {
			// Updating is not enough (or a good idea) => PK errors when the new one already exists (eg joining 2 different spellings of the same word together)
			
			// create new song (old exists and new might exist => still returns the id)
			int newSongId = CommonSQL.addSong(renamed, artist);
			int oldSongId = CommonSQL.addSong(title, artist);
			
			// update all occurrences of oldSongId (currently: archive & currentqueue)
			CommonSQL.updateAllSongIdReferences(oldSongId, newSongId);
	
			// delete old songId
			CommonSQL.removeSong(oldSongId);		
			
			SQLConnection.getDbConn().commit(EnumSet.of(DataChangedType.SONG, DataChangedType.CURRENT_QUEUE, DataChangedType.SONGS_IN_ARCHIVE));
		} catch (SQLException e1) {
			e1.printStackTrace();
			return;
		}

		// Reselect the selection that now changed
		archive.setRowSelectionInterval(originalIndex, originalIndex);
	}
}
