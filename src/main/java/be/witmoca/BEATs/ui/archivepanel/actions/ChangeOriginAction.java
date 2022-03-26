/**
 * 
 */
package be.witmoca.BEATs.ui.archivepanel.actions;

import java.sql.SQLException;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.table.TableModel;

import be.witmoca.BEATs.connection.CommonSQL;
import be.witmoca.BEATs.connection.DataChangedType;
import be.witmoca.BEATs.connection.SQLConnection;
import be.witmoca.BEATs.ui.ApplicationWindow;
import be.witmoca.BEATs.ui.components.SongTable;
import be.witmoca.BEATs.utils.Lang;
import be.witmoca.BEATs.utils.OriginHelper;
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
class ChangeOriginAction extends MultisongChangeAbstractAction {
	private static final long serialVersionUID = 1L;

	ChangeOriginAction(SongTable table) {
		super(table, Lang.getUI("col.origin"));
		this.putValue(Action.SMALL_ICON, UiIcon.EDIT_W.getIcon());
	}

	@Override
	protected void actionPerform(int[] indices) {
		// PREPARE variables for user
		Set<String> artists = new HashSet<String>();
		TableModel tm = getConnectedTable().getModel();
		for(int i: indices) {
			artists.add((String) tm.getValueAt(i, 0));
		}

		
		String firstOrigin;

		try {
			firstOrigin = CommonSQL.getArtistOrigin((String) tm.getValueAt(indices[0], 0));
		} catch (SQLException e2) {
			e2.printStackTrace();
			return;
		}

		// USER UI interaction
		JPanel userPanel = new JPanel();
		JComboBox<String> originBox = new JComboBox<>((String[]) OriginHelper.getDisplayOriginList().toArray());
		userPanel.add(originBox);

		if (JOptionPane.showConfirmDialog(ApplicationWindow.getAPP_WINDOW(), userPanel, Lang.getUI("col.origin"),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
			return; // cancelled
		}

		// Do nothing if there is a single select and it stays the same
		if (originBox.getSelectedItem() == firstOrigin && indices.length == 1)
			return;

		// MAKE CHANGES
		// update artists
		try {
			for(String artist : artists) {
				CommonSQL.updateOriginOfArtist(OriginHelper.getOriginCodeFromDisplayString((String) originBox.getSelectedItem()), artist);
			}
			SQLConnection.getDbConn().commit(EnumSet.of(DataChangedType.ARTIST));
		} catch (SQLException e1) {
			e1.printStackTrace();
			return;
		}
	}
}
