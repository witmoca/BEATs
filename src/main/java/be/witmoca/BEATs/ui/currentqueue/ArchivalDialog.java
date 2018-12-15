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
* File: ArchivalDialog.java
* Created: 2018
*/
package be.witmoca.BEATs.ui.currentqueue;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;

import be.witmoca.BEATs.Launch;
import be.witmoca.BEATs.ui.t4j.LocalDateCombo;

public class ArchivalDialog extends JDialog implements PropertyChangeListener{
	private static final long serialVersionUID = 1L;
	private static final String[] summeryColumnNames = { "Artist", "Song", "Comment" };
	
	private final JPanel cPane = new JPanel(new BorderLayout());
	private final JPanel entryPanel = new JPanel() ;
	private final JSpinner episodeId;
	private final JButton newEpisode = new JButton("+");
	
	private boolean valid = false;
	
	public ArchivalDialog() {
		super(Launch.getAPP_WINDOW(), "Archive", true);
		
		// create the entryPanel
		entryPanel.setLayout(new GroupLayout(entryPanel)); 

		entryPanel.add(new JLabel("Episode"));
		int maxExisting = getMaxEpisode();
		episodeId = new JSpinner(new SpinnerNumberModel(maxExisting, 1, maxExisting, 1));
		entryPanel.add(episodeId);
		
		// add the entryPanel
		cPane.add(entryPanel, BorderLayout.CENTER);
		 
		// add the summary view
		cPane.add(new JScrollPane(constructSummary(), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.EAST);
		
		// Include the created cPane as the message of an optionpane, set this optionpane as the content for the dialog
		// (optionpane just functions as a set of buttons here)
		JOptionPane oPane = new JOptionPane(cPane, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		this.setContentPane(cPane);
		
		oPane.addPropertyChangeListener("value",this);
		this.pack();
		this.setLocationRelativeTo(this.getParent());
		// setVisible DOES NOT RETURN BEFORE dispose() WHEN MODAL!
		this.setVisible(true);
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(!this.isVisible())
			return;
		
		// Any other button than 'ok' pressed? => cancel/close screen
		if(evt.getNewValue() == JOptionPane.UNINITIALIZED_VALUE || (int) evt.getNewValue() != JOptionPane.OK_OPTION) {
			this.dispose();
			return;
		}
		
		valid = true;
		this.dispose();
	}
	

	/***
	 *  Constructs a JTable containing a summary of the items in the currentQueue
	 * @return constructed JTable
	 */
	private static JTable constructSummary() {
		try {
			Vector<Vector<String>> tableData = new Vector<Vector<String>>();
			try (PreparedStatement sel = Launch.getDB_CONN().prepareStatement(
					"SELECT ArtistName, Title, Comment FROM CurrentQueue,Song WHERE CurrentQueue.songId = Song.SongId ORDER BY SongOrder ASC")) {

				ResultSet rs = sel.executeQuery();
				while (rs.next()) {
					Vector<String> row = new Vector<String>();
					row.add(rs.getString(1));
					row.add(rs.getString(2));
					row.add(rs.getString(3));
					tableData.add(row);
				}
			}

			return new JTable(tableData, new Vector<String>(Arrays.asList(summeryColumnNames)));
		} catch (SQLException e) {
			String[] columns = { "Error" };
			String[][] data = { { e.getLocalizedMessage() } };
			return new JTable(data, columns);
		}
	}
	
	private static int getMaxEpisode() {
		try (PreparedStatement findMaxEpisode = Launch.getDB_CONN().prepareStatement("SELECT max(EpisodeId) FROM Episode")) {
			ResultSet rs = findMaxEpisode.executeQuery();
			if (rs.next())
				return rs.getInt(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return 0;
	}

	public boolean isValid() {
		return valid;
	}
}
