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
* File: PlaylistManagerShowAction.java
* Created: 2018
*/
package be.witmoca.BEATs.ui.playlistmanager;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import be.witmoca.BEATs.ApplicationManager;
import be.witmoca.BEATs.model.DataChangedListener;

public class PlaylistManagerShowAction implements ActionListener {
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (!(e.getSource() instanceof Component)) {
			return;
		}

		PlaylistManagerPanel pmp = new PlaylistManagerPanel();
		// show dialog
		if (JOptionPane.showConfirmDialog((Component) e.getSource(), pmp, "Playlist Manager",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null) != JOptionPane.OK_OPTION)
			return;

		// not cancelled => update playlists
		List<String> newNames = pmp.getListModel().getNewPlaylists();
		List<String> oldNames = (new ReorderingListModel()).getNewPlaylists();
		if (newNames.containsAll(oldNames) && oldNames.containsAll(newNames))
			return;

		// divide into create, delete and update
		List<String> create = new ArrayList<String>();
		List<String> update = new ArrayList<String>();
		for (String s : newNames) {
			if (!oldNames.contains(s))
				create.add(s);
			else
				update.add(s);
		}

		List<String> delete = new ArrayList<String>();
		for (String s : oldNames) {
			if (!newNames.contains(s))
				delete.add(s);
		}

		// confirm dialog
		if (JOptionPane.showConfirmDialog((Component) e.getSource(),
				"Are you sure you wish to make the following changes:\nCreate playlist: " + create.size()
						+ "\nRemove playlist: " + delete.size() + "\nAnd possibly change the order of " + update.size()
						+ " playlists?",
				"Playlist Manager", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
				null) != JOptionPane.OK_OPTION)
			return;
		
		try {
			// start by removing
			for(String pName : delete) {
				// first delete all the playlist entries
				try (PreparedStatement delPS = ApplicationManager.getDB_CONN().prepareStatement("DELETE FROM SongsInPlaylist WHERE PlaylistName = ?")) {
					delPS.setString(1, pName);
					delPS.executeUpdate();
				}
				// then delete the playlist
				try (PreparedStatement delP = ApplicationManager.getDB_CONN().prepareStatement("DELETE FROM Playlist WHERE PlaylistName = ?")) {
					delP.setString(1, pName);
					delP.executeUpdate();
				}
			}
			// Move all existing tabOrders upwards, so that we can guarantee unique tabOrders
			// select max taborder
			int maxTab = 0;
			try (PreparedStatement selMaxTab = ApplicationManager.getDB_CONN()
					.prepareStatement("SELECT max(TabOrder) FROM playlist")) {
				ResultSet rs = selMaxTab.executeQuery();
				if (rs.next())
					maxTab = rs.getInt(1);
			}
			
			// move tabOrders up
			try (PreparedStatement tabUp = ApplicationManager.getDB_CONN().prepareStatement("UPDATE Playlist SET TabOrder = TabOrder + ?")) {
				tabUp.setInt(1, maxTab);
				tabUp.executeUpdate();
			}
			
			// Create the playlists
			for(String newP : create) {
				try (PreparedStatement addP = ApplicationManager.getDB_CONN().prepareStatement("INSERT INTO Playlist VALUES (?, ( SELECT coalesce(max(TabOrder)+1,1) FROM Playlist) )")) {
					addP.setString(1, newP);
					addP.executeUpdate();
				}
			}
			
			// Reorder all the playlists
			int i = 0;
			for(String name : newNames) {
				try (PreparedStatement order = ApplicationManager.getDB_CONN().prepareStatement("UPDATE Playlist SET TabOrder = ? WHERE PlaylistName = ?")) {
					order.setInt(1, i++);
					order.setString(2, name);
					order.executeUpdate();
				}
			}
			
			// commit the changes
			ApplicationManager.getDB_CONN().commit(DataChangedListener.DataType.PLAYLIST_DATA_OPTS);
		} catch (SQLException e1) {
			e1.printStackTrace();
			return;
		}
	}
}
