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
package be.witmoca.BEATs.ui.currentqueue.actions;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import be.witmoca.BEATs.connection.CommonSQL;
import be.witmoca.BEATs.connection.SQLConnection;
import be.witmoca.BEATs.ui.ApplicationWindow;
import be.witmoca.BEATs.ui.t4j.LocalDateCombo;
import be.witmoca.BEATs.utils.Lang;

class ArchivalDialog extends JDialog implements ActionListener, PropertyChangeListener, ListDataListener {
	private static final long serialVersionUID = 1L;
	private static final String[] summeryColumnNames = { Lang.getUI("col.artist"), Lang.getUI("col.song"),
			Lang.getUI("col.comment") };

	private final JPanel cPane = new JPanel(new BorderLayout(10, 10));
	private final JPanel entryPanel = new JPanel(new GridLayout(2, 3, 5, 5));
	private final JPanel episodeBorder = new JPanel(new GridLayout(1, 1)); // It's recommended to only set borders on
																			// jpanels
	private final JPanel genreBorder = new JPanel(new GridLayout(1, 1));
	private final JFormattedTextField episodeId = new JFormattedTextField(NumberFormat.getIntegerInstance());
	private final LocalDateCombo episodeDate = new LocalDateCombo(DateTimeFormatter.ofPattern("E d-MMM-uuuu"));
	private final JComboBox<String> genreId;
	private final JButton okButton = new JButton(Lang.getUI("action.ok"));
	private final JButton cancelButton = new JButton(Lang.getUI("action.cancel"));

	private boolean valid = false;

	public ArchivalDialog() {
		super(ApplicationWindow.getAPP_WINDOW(), Lang.getUI("queue.archiving"), true);

		// LAYOUT
		// create the entryPanel
		// row 1
		entryPanel.add(new JLabel(Lang.getUI("col.episode")));
		entryPanel.add(new JLabel(Lang.getUI("col.date")));
		entryPanel.add(new JLabel(Lang.getUI("col.genre")));

		// row 2
		episodeBorder.add(episodeId);
		entryPanel.add(episodeBorder);
		episodeDate.setEditable(false);
		episodeDate.setEnabled(false);
		entryPanel.add(episodeDate);

		List<String> Genres = null;
		try {
			Genres = CommonSQL.getGenres();
		} catch (SQLException e) {
			Genres = new ArrayList<String>();
		}
		genreId = new JComboBox<String>(Genres.toArray(new String[0]));
		genreId.setEditable(true);
		genreBorder.add(genreId);
		entryPanel.add(genreBorder);

		entryPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

		// add the entryPanel
		cPane.add(new JScrollPane(entryPanel, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.NORTH);

		// add the summary view
		cPane.add(new JScrollPane(constructSummary(), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);

		// add buttons to the panel
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		cPane.add(buttonPanel, BorderLayout.SOUTH);

		cPane.setOpaque(true);
		cPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		this.setContentPane(cPane);

		// FUNCTIONALITY
		okButton.addActionListener(this);
		cancelButton.addActionListener(this);

		// episodeDate should follow episodeId
		episodeId.addPropertyChangeListener("value", this);

		// episodeId should follow episodeDate
		episodeDate.getModel().addListDataListener(this);

		// set initial value of episode id
		try {
			// Today's date is already associated with an episode?
			int id = CommonSQL.getEpisodeByDate(episodeDate.getValue());
			if (id < 0) {
				// No episode of today => set as new episode
				List<Integer> epiL = CommonSQL.getEpisodes();
				if (epiL.isEmpty()) {
					id = 1; // no previous existing => choose 1 as starting number
				} else {
					id = epiL.get(epiL.size() - 1) + 1; // equal to Max(EpisodeId) + 1
				}
			}
			episodeId.setValue(id);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// PREPARE FOR VIEW
		this.pack();
		this.setLocationRelativeTo(this.getParent());
		// setVisible DOES NOT RETURN BEFORE dispose() WHEN MODAL!
		this.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (cancelButton.equals(e.getSource())) {
			// cancelled
			this.dispose();
			return;
		}
		if (!okButton.equals(e.getSource()))
			return; // nothing interesting happened

		// Reset borders
		episodeBorder.setBorder(BorderFactory.createEmptyBorder());
		genreBorder.setBorder(BorderFactory.createEmptyBorder());

		// Check if all fields are valid
		// check episodeId
		if (episodeId.getValue() == null || !episodeId.isEditValid()) {
			episodeBorder.setBorder(BorderFactory.createLineBorder(Color.red));
			episodeId.requestFocusInWindow();
			return;
		}

		// check episodeDate (should be true)
		if (episodeDate.getValue() == null)
			return;

		if (genreId.getSelectedItem() == null || ((String) genreId.getSelectedItem()).trim().isEmpty()) {
			genreBorder.setBorder(BorderFactory.createLineBorder(Color.red));
			genreId.requestFocusInWindow();
			return;
		}

		valid = true;
		this.dispose();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// value of episodeId has changed
		if (!episodeId.isEditValid() || episodeId.getValue() == null) {
			return;
		}

		// Do not accept negatives
		int value = this.getEpisodeId();
		if (value < 0) {
			episodeId.setValue(Math.abs(value));
			return;
		}

		// set episodeDate accordingly
		try {
			LocalDate d = CommonSQL.getEpisodeDateById(value);
			if (d == null) {
				// this episode does not exist;
				episodeDate.setEnabled(true); // set enabled first! (for listDatalistener)
				episodeDate.setValue(LocalDate.now());
			} else {
				// this episode does exist;
				episodeDate.setEnabled(false); // set enabled first! (for listDatalistener)
				episodeDate.setValue(d);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
	}

	@Override
	public void intervalAdded(ListDataEvent e) {
		contentsChanged(e);
	}

	@Override
	public void intervalRemoved(ListDataEvent e) {
		// Don't care
	}

	@Override
	public void contentsChanged(ListDataEvent e) {
		// episodeDate has been changed
		if (!episodeDate.isEnabled() || episodeDate.getValue() == null)
			return; // if not enabled or no value, then don't really care

		try {
			int ep = CommonSQL.getEpisodeByDate(episodeDate.getValue());
			if (ep < 0) {
				return; // this is ok
			} else {
				// episode exists => change episodeId field accordingly
				episodeId.setValue(ep);
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

	/***
	 * Constructs a JTable containing a summary of the items in the currentQueue
	 * 
	 * @return constructed JTable
	 */
	private static JTable constructSummary() {
		try {
			Vector<Vector<String>> tableData = new Vector<Vector<String>>();
			try (PreparedStatement sel = SQLConnection.getDbConn().prepareStatement(
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

	public int getEpisodeId() {
		return ((Number) episodeId.getValue()).intValue(); // For some weird reason this can return different number
															// types (Long/Integer/int)
	}

	public LocalDate getEpisodeDate() {
		return episodeDate.getValue();
	}

	public String getGenre() {
		return ((String) genreId.getSelectedItem()).trim();
	}
}
