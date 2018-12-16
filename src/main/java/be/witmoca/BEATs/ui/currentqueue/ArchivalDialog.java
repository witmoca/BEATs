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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractSpinnerModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerListModel;

import be.witmoca.BEATs.Launch;

public class ArchivalDialog extends JDialog implements PropertyChangeListener{
	private static final long serialVersionUID = 1L;
	private static final String[] summeryColumnNames = { "Artist", "Song", "Comment" };
	
	private final JPanel cPane = new JPanel(new BorderLayout());
	private final JPanel entryPanel = new JPanel() ;
	private final JSpinner episodeId;
	private final JSpinner sectionId;
	
	private boolean valid = false;
	
	public ArchivalDialog() {
		super(Launch.getAPP_WINDOW(), "Archive", true);
		
		// create the entryPanel
		GroupLayout gLayout = new GroupLayout(entryPanel);
		entryPanel.setLayout(gLayout); 

		JLabel j1 = new JLabel("Episode");
		entryPanel.add(j1);
		episodeId = new JSpinner(new SpinnerEpisodeModel());
		entryPanel.add(episodeId);
		JComponent j2 = new JButton(new CreateNewEpisode(this, (SpinnerEpisodeModel) episodeId.getModel()));
		entryPanel.add(j2);
		
		JLabel j3 = new JLabel("Section Code");
		entryPanel.add(j3);
		sectionId = new JSpinner(new SpinnerListModel(loadSections()));
		entryPanel.add(sectionId);
		
		gLayout.setHorizontalGroup(gLayout.createSequentialGroup().addGroup(gLayout.createParallelGroup(GroupLayout.Alignment.TRAILING).addComponent(j1).addComponent(j3))
		.addGap(5).addGroup(gLayout.createParallelGroup(GroupLayout.Alignment.TRAILING).addComponent(episodeId).addComponent(sectionId)).addComponent(j2));
		
		gLayout.setVerticalGroup(gLayout.createSequentialGroup().addGroup(gLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(j1).addComponent(episodeId).addComponent(j2))
				.addGroup(gLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(j3).addComponent(sectionId)));
		
		// add the entryPanel
		cPane.add(entryPanel, BorderLayout.CENTER);
		 
		// add the summary view
		cPane.add(new JScrollPane(constructSummary(), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.EAST);
		
		// Include the created cPane as the message of an optionpane, set this optionpane as the content for the dialog
		// (optionpane just functions as a set of buttons here)
		JOptionPane oPane = new JOptionPane(cPane, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		this.setContentPane(oPane);
		
		oPane.addPropertyChangeListener("value",this);
		this.pack();
		this.setLocationRelativeTo(this.getParent());
		// setVisible DOES NOT RETURN BEFORE dispose() WHEN MODAL!
		this.setVisible(true);
	}
	
	public int getEpisode() {
		return (int) episodeId.getValue();
	}
	
	public String getSection() {
		return (String) sectionId.getValue();
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
	
	private static List<String> loadSections(){
		List<String> sections = new ArrayList<String>();
		try (PreparedStatement sel = Launch.getDB_CONN().prepareStatement("SELECT SectionName FROM Section ORDER BY SectionName ASC")) {

			ResultSet rs = sel.executeQuery();
			while (rs.next()) {
				sections.add(rs.getString(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return sections;
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

	public boolean isValid() {
		return valid;
	}
	
	class SpinnerEpisodeModel extends AbstractSpinnerModel {
		private static final long serialVersionUID = 1L;
		private List<Integer> episodeList;
		private int index;

		protected SpinnerEpisodeModel() {
			this.loadValues();
		}
		
		protected void loadValues() {
			episodeList = new ArrayList<Integer>();
			try (PreparedStatement findExclusions = Launch.getDB_CONN().prepareStatement("SELECT EpisodeId FROM Episode ORDER BY EpisodeId ASC")) {
				ResultSet rs = findExclusions.executeQuery();
				while (rs.next())
					episodeList.add(rs.getInt(1));
			} catch (SQLException e) {
				e.printStackTrace();
			}

			index = episodeList.size()-1;
		}

		@Override
		public Object getValue() {
			return episodeList.get(index);
		}

		@Override
		public void setValue(Object value) {
			if(!Integer.class.isInstance(value) || !episodeList.contains(value))
				throw new IllegalArgumentException(value + " is not an acceptable value");
			index = episodeList.indexOf(value);
			this.fireStateChanged();
		}

		@Override
		public Object getNextValue() {
			if(index+1 >= episodeList.size())
				return null;
			else
				return episodeList.get(index+1);
		}

		@Override
		public Object getPreviousValue() {
			if(index <= 0)
				return null;
			else
				return episodeList.get(index-1);
		}
	}
}
