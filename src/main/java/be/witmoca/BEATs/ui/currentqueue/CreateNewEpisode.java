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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractSpinnerModel;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import be.witmoca.BEATs.Launch;
import be.witmoca.BEATs.ui.t4j.LocalDateCombo;

public class CreateNewEpisode extends AbstractAction {
	private static final long serialVersionUID = 1L;

	private final Component parent;
	private final int maxEpisode;

	private boolean valid = false;

	public CreateNewEpisode(Component parent, int maxEpisode) {
		super("+");
		this.parent = parent;
		this.maxEpisode = maxEpisode;
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
		JSpinner episodeId = new JSpinner(new SpinnerNewEpisodeModel(maxEpisode));
		dPanel.add(episodeId);

		while (!valid) {
			if (JOptionPane.showConfirmDialog(parent, null, "New Episode",
					JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION)
				return;
			
			if(episodeDate.getValue())
		}

	}

	private static Date getMaxDate() {
		try (PreparedStatement findExclusions = Launch.getDB_CONN().prepareStatement("SELECT max(EpisodeDate) FROM Episode")) {
			ResultSet rs = findExclusions.executeQuery();
			if(!rs.next())
				return null;
			return new Date(rs.getLong(1));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	class SpinnerNewEpisodeModel extends AbstractSpinnerModel {
		private static final long serialVersionUID = 1L;
		private List<Integer> exclusions = new ArrayList<Integer>();
		private int index;

		protected SpinnerNewEpisodeModel(int startIndex) {
			try (PreparedStatement findExclusions = Launch.getDB_CONN()
					.prepareStatement("SELECT EpisodeId FROM Episode ORDER BY ASC")) {
				ResultSet rs = findExclusions.executeQuery();
				while (rs.next())
					exclusions.add(rs.getInt(1));
			} catch (SQLException e) {
				e.printStackTrace();
			}

			this.setValue(startIndex);
		}

		@Override
		public Object getValue() {
			return index;
		}

		@Override
		public void setValue(Object value) {
			index = (int) value;
			// make sure the index is valid by going back and forth
			this.getNextValue();
			this.getPreviousValue();
			// if we stranded on a lower number than the initial value => go forward again
			if ((int) this.getValue() < (int) value)
				this.getNextValue();
			this.fireStateChanged();
		}

		@Override
		public Object getNextValue() {
			index += 1;
			while (exclusions.contains(index))
				index += 1;
			this.fireStateChanged();
			return index;
		}

		@Override
		public Object getPreviousValue() {
			index -= 1;
			while (exclusions.contains(index))
				index -= 1;
			this.fireStateChanged();
			if (index >= 0)
				return index;
			else {
				return this.getNextValue();
			}
		}

	}
}
