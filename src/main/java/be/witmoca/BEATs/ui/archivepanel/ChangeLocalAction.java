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
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import be.witmoca.BEATs.ApplicationManager;
import be.witmoca.BEATs.model.DataChangedListener;
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
* File: RenameAristAction.java
* Created: 2018
*/
class ChangeLocalAction extends AbstractAction {
	private static final long serialVersionUID = 1L;

	private final JTable archive;

	ChangeLocalAction(JTable table) {
		super("Local");
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
		boolean local;

		try (PreparedStatement selLocal = ApplicationManager.getDB_CONN()
				.prepareStatement("SELECT local FROM Artist WHERE ArtistName = ?")) {
			selLocal.setString(1, artist);
			ResultSet rs = selLocal.executeQuery();
			if (!rs.next())
				return;
			local = rs.getBoolean(1);
		} catch (SQLException e2) {
			e2.printStackTrace();
			return;
		}

		// USER UI interaction
		JPanel userPanel = new JPanel();
		JCheckBox localBox = new JCheckBox(artist + " is local", local);
		userPanel.add(localBox);

		if (JOptionPane.showConfirmDialog(ApplicationManager.getAPP_WINDOW(), userPanel, "Rename",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
			return; // cancelled
		}

		if(localBox.isSelected() == local)
			return;
		
		// MAKE CHANGES
		// update artist
		try (PreparedStatement updateLocal = ApplicationManager.getDB_CONN()
				.prepareStatement("UPDATE Artist SET local = ? WHERE ArtistName = ?")) {
			updateLocal.setBoolean(1, localBox.isSelected());
			updateLocal.setString(2, artist);
			updateLocal.executeUpdate();
			ApplicationManager.getDB_CONN().commit(EnumSet.of(DataChangedListener.DataType.ARTIST));
		} catch (SQLException e1) {
			e1.printStackTrace();
			return;
		}

		// Reselect the selection that now changed
		archive.setRowSelectionInterval(originalIndex, originalIndex);
	}
}
