/**
 * 
 */
package be.witmoca.BEATs.ui.archivepanel.actions;

import java.awt.event.ActionEvent;
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

import be.witmoca.BEATs.connection.CommonSQL;
import be.witmoca.BEATs.connection.DataChangedType;
import be.witmoca.BEATs.connection.SQLConnection;
import be.witmoca.BEATs.ui.ApplicationWindow;
import be.witmoca.BEATs.ui.t4j.LocalDateCombo;
import be.witmoca.BEATs.utils.Lang;
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
* File: ChangeDateAction.java
* Created: 2018
*/
class ChangeDateAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
	
	private final JTable archive;
	
	ChangeDateAction(JTable table) {
		super(Lang.getUI("changeDateAction"));
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
		
		try {
			date = CommonSQL.getEpisodeDateById(episode);
		} catch (SQLException e1) {
			e1.printStackTrace();
			return;
		}
		if(date == null)
			return;
		
		// USER UI interaction
		JPanel userPanel = new JPanel();
		userPanel.add(new JLabel(Lang.getUI("changeDateAction.descr") + ": " + episode));
		LocalDateCombo episodeDate = new LocalDateCombo(date, DateTimeFormatter.ofPattern("E d-MMM-uuuu"));
		userPanel.add(episodeDate);
				
		
		if(JOptionPane.showConfirmDialog(ApplicationWindow.getAPP_WINDOW(), userPanel, Lang.getUI("changeDateAction"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
			return; // cancelled
		}
		
		// update Date = upDate! Get it? Ugh I'm disgusted with myself for that one.
		// EpisodeDates are unique => Constraint Violation if the episode exists already. So Ignore in that case
		try {
			CommonSQL.updateEpisodeDate(episode, episodeDate.getValue());
			SQLConnection.getDbConn().commit(EnumSet.of(DataChangedType.EPISODE));
		} catch (SQLException e1) {
			e1.printStackTrace();
			return;
		}
		
		// Reselect the selection that now changed
		archive.setRowSelectionInterval(originalIndex, originalIndex);
	}
}
