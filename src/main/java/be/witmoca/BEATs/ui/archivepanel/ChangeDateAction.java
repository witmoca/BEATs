/**
 * 
 */
package be.witmoca.BEATs.ui.archivepanel;

import java.awt.event.ActionEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;

import be.witmoca.BEATs.ApplicationManager;
import be.witmoca.BEATs.model.DataChangedListener;
import be.witmoca.BEATs.ui.UiIcon;
import be.witmoca.BEATs.ui.t4j.LocalDateCombo;

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
* File: ChangeDateAction.java
* Created: 2018
*/
class ChangeDateAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
	
	private final JTable archive;
	
	ChangeDateAction(JTable table) {
		super("Change Date");
		this.putValue(Action.SMALL_ICON, UiIcon.CALENDAR.getIcon());
		archive = table;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		int index = archive.getSelectedRow();
		int originalIndex = index;
		if(index < 0)
			return;
		if(archive.getRowSorter() != null)
			index = archive.getRowSorter().convertRowIndexToModel(index);
		
		// PREPARE veriables for user
		int episode = (int) archive.getModel().getValueAt(index, 2);
		LocalDate date;
		
		try (PreparedStatement selDate = ApplicationManager.getDB_CONN().prepareStatement("SELECT episodeDate FROM episode WHERE episodeId = ?")) {
			selDate.setInt(1, episode);
			ResultSet rs = selDate.executeQuery();
			if(!rs.next())
				return;
			date = LocalDate.ofEpochDay(rs.getInt(1));
		} catch (SQLException e1) {
			e1.printStackTrace();
			return;
		}
		
		// USER UI interaction
		JPanel userPanel = new JPanel();
		userPanel.add(new JLabel("Change the date for episode: " + episode));
		LocalDateCombo episodeDate = new LocalDateCombo(date, DateTimeFormatter.ofPattern("E d-MMM-uuuu"));
		userPanel.add(episodeDate);
				
		
		if(JOptionPane.showConfirmDialog(ApplicationManager.getAPP_WINDOW(), userPanel,"Change Date", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
			return; // cancelled
		}
		
		// MAKE CHANGES
		date = episodeDate.getValue();
		
		// update Date = upDate! Get it? Ugh I'm disgusted with myself for that one.
		// EpisodeDates are unique => Constraint Violation if the episode exists already. So Ignore in that case
		try (PreparedStatement upDate = ApplicationManager.getDB_CONN().prepareStatement("UPDATE OR IGNORE episode SET episodeDate = ? WHERE episodeId = ?")) {
			upDate.setLong(1, date.toEpochDay());
			upDate.setInt(2, episode);
			upDate.executeUpdate();
			ApplicationManager.getDB_CONN().commit(EnumSet.of(DataChangedListener.DataType.EPISODE));
		} catch (SQLException e1) {
			e1.printStackTrace();
			return;
		}
		
		// Reselect the selection that now changed
		archive.setRowSelectionInterval(originalIndex, originalIndex);
	}
}
