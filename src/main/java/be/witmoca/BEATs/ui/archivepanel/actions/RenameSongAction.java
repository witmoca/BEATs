/**
 * 
 */
package be.witmoca.BEATs.ui.archivepanel.actions;

import java.awt.event.ActionEvent;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.EnumSet;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

import be.witmoca.BEATs.ApplicationManager;
import be.witmoca.BEATs.connection.DataChangedListener;
import be.witmoca.BEATs.connection.SQLObjectTransformer;
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

		if (JOptionPane.showConfirmDialog(ApplicationManager.getAPP_WINDOW(), userPanel, "Rename",
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
			int newSongId = SQLObjectTransformer.addSong(renamed, artist);
			int oldSongId = SQLObjectTransformer.addSong(title, artist);
			
			// update archive references
			try (PreparedStatement updateArchive = ApplicationManager.getDB_CONN().prepareStatement("UPDATE SongsInArchive SET SongId = ? WHERE SongId = ?")) {
				updateArchive.setInt(1, newSongId);
				updateArchive.setInt(2, oldSongId);
				updateArchive.executeUpdate();
			}
			
			// update currentQueue
			try (PreparedStatement updateQueue = ApplicationManager.getDB_CONN().prepareStatement("UPDATE CurrentQueue SET SongId = ? WHERE SongId = ?")) {
				updateQueue.setInt(1, newSongId);
				updateQueue.setInt(2, oldSongId);
				updateQueue.executeUpdate();
			}
			
			// delete old songId
			try (PreparedStatement delSong = ApplicationManager.getDB_CONN().prepareStatement("DELETE FROM Song WHERE SongId = ?")) {
				delSong.setInt(1, oldSongId);
				delSong.executeUpdate();
			}			
			ApplicationManager.getDB_CONN().commit(EnumSet.of(DataChangedListener.DataType.SONG, DataChangedListener.DataType.CURRENT_QUEUE, DataChangedListener.DataType.SONGS_IN_ARCHIVE));
		} catch (SQLException e1) {
			e1.printStackTrace();
			return;
		}

		// Reselect the selection that now changed
		archive.setRowSelectionInterval(originalIndex, originalIndex);
	}
}
