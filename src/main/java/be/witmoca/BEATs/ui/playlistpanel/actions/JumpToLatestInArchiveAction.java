/*
*
+===============================================================================+
|    BEATs (Burning Ember Archival Tool suite)                                  |
|    Copyright 2019 Jente Heremans                                              |
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
* File: PrintTableAction.java
* Created: 2019
*/
package be.witmoca.BEATs.ui.playlistpanel.actions;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.sql.SQLException;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;

import be.witmoca.BEATs.connection.CommonSQL;
import be.witmoca.BEATs.ui.ApplicationWindow;
import be.witmoca.BEATs.ui.CenterTabbedPane;
import be.witmoca.BEATs.ui.components.SongTable;
import be.witmoca.BEATs.ui.playlistpanel.PlaylistTableModel;
import be.witmoca.BEATs.utils.Lang;

class JumpToLatestInArchiveAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
	private final JTable table;

	JumpToLatestInArchiveAction(SongTable table) {
		super(Lang.getUI("jumpToLatestInArchiveAction.action"));
		this.table = table;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		int indices[] = table.getSelectedRows();
		if (indices.length == 0)
			return;
		if (indices.length != 1) {
			JOptionPane.showMessageDialog(ApplicationWindow.getAPP_WINDOW(), Lang.getUI("jumpToLatestInArchiveAction.tooManySelectedError"), "", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		// Only one entry is selected, get this entry
		PlaylistTableModel tm = ((PlaylistTableModel) table.getModel());
		String artist = ((String) tm.getValueAt(indices[0], 0)).trim(); // get Artist
		if (artist == "") {
			return;
		}
		
		// Check if artist exists
		try {
			if (! CommonSQL.isArtistExisting(artist)) {
				return;
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
			return;
		}

		
		CenterTabbedPane centerPane =  ApplicationWindow.getAPP_WINDOW().getCenterPanel();
		// Find the last entry for the artist (based on episode nr). This executes in O(N) time
		TableModel model = centerPane.getArchivePanel().getArchiveTable().getModel();
		int highestIndex = -1;
		int highestEpisode = -1;
		for(int i = 0 ; i < model.getRowCount(); i++) {
			if(model.getValueAt(i, 0).equals(artist)) {
				int episode = (int) model.getValueAt(i, 2);
				if (episode > highestEpisode) {
					highestIndex = i;
					highestEpisode = episode;
				}
			}
		}
		
		if (highestIndex != -1) {
			// Set Archive tab as selected
			centerPane.setSelectedIndex(0);
			// Reset to the default sorting order (by artist, then by episode in ascending order)
			centerPane.getArchivePanel().resetToDefaultSort();
			// Convert index to actual index (rowsorter)
			highestIndex = centerPane.getArchivePanel().getArchiveTable().getRowSorter().convertRowIndexToView(highestIndex);
			// Select the correct entry
			centerPane.getArchivePanel().getArchiveTable().getSelectionModel().setSelectionInterval(highestIndex, highestIndex);
			// Scroll to the right height
			centerPane.getArchivePanel().getArchiveTable().scrollRectToVisible(new Rectangle(centerPane.getArchivePanel().getArchiveTable().getCellRect(highestIndex, 0, true)));
		}
	}
}
