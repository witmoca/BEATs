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
* File: CreateNewEpisode.java
* Created: 2018
*/
package be.witmoca.BEATs.ui.currentqueue;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractSpinnerModel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import be.witmoca.BEATs.Launch;
import be.witmoca.BEATs.model.DataChangedListener;
import be.witmoca.BEATs.model.SQLObjectTransformer;
import be.witmoca.BEATs.ui.currentqueue.ArchivalDialog.SpinnerEpisodeModel;
import be.witmoca.BEATs.ui.t4j.LocalDateCombo;

public class CreateNewEpisode extends AbstractAction {
	private static final long serialVersionUID = 1L;

	private final Component parent;

	private boolean valid = false;
	private SpinnerEpisodeModel spinnerEpisodeModel;

	public CreateNewEpisode(Component parent, SpinnerEpisodeModel spinnerEpisodeModel) {
		super("+");
		this.parent = parent;
		this.spinnerEpisodeModel = spinnerEpisodeModel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		// Construct dialog layout
		JPanel dPanel = new JPanel(new GridLayout(0, 2));

		dPanel.add(new JLabel("Date of episode"));
		LocalDateCombo episodeDate = new LocalDateCombo(DateTimeFormatter.ofPattern("E d-MMM-uuuu"));
		dPanel.add(episodeDate);

		dPanel.add(new JLabel("Episode Id"));
		JSpinner episodeId = new JSpinner(new SpinnerNewEpisodeModel());
		dPanel.add(episodeId);

		valid = false;
		while (!valid) {
			if (JOptionPane.showConfirmDialog(parent, dPanel, "New Episode",
					JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION)
				return;
			
			if(!dateFree(episodeDate.getValue())) {
				JOptionPane.showMessageDialog(parent, "The selected date is already used for another episode! A single day can only be associated with one episode. Please select another date.", "Date already associated with an episode", JOptionPane.ERROR_MESSAGE);
				continue;
			}
			
			try {
				SQLObjectTransformer.addEpisode((int)episodeId.getValue(), episodeDate.getValue());
				Launch.getDB_CONN().commit(EnumSet.of(DataChangedListener.DataType.EPISODE));
			} catch (SQLException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(parent, e1.getLocalizedMessage(), e1.getClass().getName(), JOptionPane.ERROR_MESSAGE);
				continue;
			}
			
			this.spinnerEpisodeModel.loadValues();
			this.spinnerEpisodeModel.setValue(episodeId.getValue());
			valid = true;
		}
	}

	/**
	 *  Check if a date is free (as in not associated with an episodeId already)
	* @param ldate LocalDate instance to check
	* @return True if ldate is free, false if not or an error occurred
	 */
	private static boolean dateFree(LocalDate ldate) {
		try (PreparedStatement checkDate = Launch.getDB_CONN().prepareStatement("SELECT Episodeid FROM Episode WHERE EpisodeDate = ?")) {
			checkDate.setLong(1, ldate.toEpochDay());
			ResultSet rs = checkDate.executeQuery();
			if(!rs.next())
				return true;
			return false;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	class SpinnerNewEpisodeModel extends AbstractSpinnerModel {
		private static final long serialVersionUID = 1L;
		private List<Integer> exclusions = new ArrayList<Integer>();
		private int index;

		protected SpinnerNewEpisodeModel() {
			try (PreparedStatement findExclusions = Launch.getDB_CONN()
					.prepareStatement("SELECT EpisodeId FROM Episode ORDER BY EpisodeId ASC")) {
				ResultSet rs = findExclusions.executeQuery();
				while (rs.next())
					exclusions.add(rs.getInt(1));
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			// find first available episodeID
			index = 0;
			Object val = getNextValue();
			index = (val == null) ? 1 : (int) val;
		}

		@Override
		public Object getValue() {
			return index;
		}

		@Override
		public void setValue(Object value) {
			if(!Integer.class.isInstance(value) || exclusions.contains(value))
				throw new IllegalArgumentException(value + " is not an acceptable value");
			index = (int) value;
			this.fireStateChanged();
		}

		@Override
		public Object getNextValue() {
			int offset = 1;
			while (exclusions.contains(index + offset))
				offset += 1;
			return index+offset;
		}

		@Override
		public Object getPreviousValue() {
			int offset = 1;
			while (exclusions.contains(index - offset))
				offset += 1;
			return (index - offset <= 0) ? null : index-offset;
		}
	}
}
