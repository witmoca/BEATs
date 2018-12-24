/**
 * 
 */
package be.witmoca.BEATs.ui.archivepanel;

import java.awt.event.ActionEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
import be.witmoca.BEATs.model.DataChangedListener;
import be.witmoca.BEATs.model.SQLObjectTransformer;
import be.witmoca.BEATs.ui.UiIcon;

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
* File: RenameAristAction.java
* Created: 2018
*/
class RenameArtistAction extends AbstractAction {
	private static final long serialVersionUID = 1L;

	private final JTable archive;

	RenameArtistAction(JTable table) {
		super("Artist");
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

		// USER UI interaction
		JPanel userPanel = new JPanel();
		userPanel.add(new JLabel("Change the spelling of the artist: "));
		JTextField newName = new JTextField(artist);
		newName.setColumns(30);
		userPanel.add(newName);

		if (JOptionPane.showConfirmDialog(ApplicationManager.getAPP_WINDOW(), userPanel, "Rename",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
			return; // cancelled
		}

		// MAKE CHANGES
		//
		// Update the PK is not possible (or a good idea)
		// So insert new artist, change all former references to the new one & delete
		// the old reference
		try {
			// First: insert (with original value of local)
			String renamed;
			try (PreparedStatement selLocal = ApplicationManager.getDB_CONN()
					.prepareStatement("SELECT local FROM Artist WHERE ArtistName = ?")) {
				selLocal.setString(1, artist);
				ResultSet rs = selLocal.executeQuery();
				if (!rs.next())
					return;
				renamed = SQLObjectTransformer.addArtist(newName.getText(), rs.getBoolean(1));
			}

			if (artist.equals(renamed))
				return;

			// updating references might lead to uniqueness errors => recreate new songs (or return their id's if they already exist) & update the archive and queue
			try (PreparedStatement selOldArtistSongs = ApplicationManager.getDB_CONN().prepareStatement("SELECT Title FROM Song WHERE ArtistName = ?")) {
				selOldArtistSongs.setString(1, artist);
				ResultSet rs1 = selOldArtistSongs.executeQuery();
				
				while(rs1.next()) {
					// for every song
					// create a new song with the new artistname (on conflict return existing songid)
					int newSongId = SQLObjectTransformer.addSong(rs1.getString(1), renamed);
					// get the id of the oldsong
					int oldSongId = SQLObjectTransformer.addSong(rs1.getString(1), artist);
					
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
				}
			}

			// delete old artist
			try (PreparedStatement delArtist = ApplicationManager.getDB_CONN()
					.prepareStatement("DELETE FROM Artist WHERE ArtistName = ?")) {
				delArtist.setString(1, artist);
				delArtist.executeUpdate();
			}

			ApplicationManager.getDB_CONN().commit(EnumSet.of(DataChangedListener.DataType.ARTIST,DataChangedListener.DataType.SONG, DataChangedListener.DataType.CURRENT_QUEUE,DataChangedListener.DataType.SONGS_IN_ARCHIVE));
		} catch (SQLException e1) {
			e1.printStackTrace();
			return;
		}

		// Reselect the selection that now changed
		archive.setRowSelectionInterval(originalIndex, originalIndex);
	}
}
