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
* File: ChangeCommentAction.java
* Created: 2019
*/
package be.witmoca.BEATs.ui.archivepanel.actions;

import java.sql.SQLException;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import be.witmoca.BEATs.connection.CommonSQL;
import be.witmoca.BEATs.connection.DataChangedType;
import be.witmoca.BEATs.connection.SQLConnection;
import be.witmoca.BEATs.ui.ApplicationWindow;
import be.witmoca.BEATs.ui.archivepanel.ArchiveTableModel;
import be.witmoca.BEATs.ui.components.SongTable;
import be.witmoca.BEATs.utils.Lang;
import be.witmoca.BEATs.utils.StringUtils;
import be.witmoca.BEATs.utils.UiIcon;

class ChangeCommentAction extends MultisongChangeAbstractAction {
	private static final long serialVersionUID = 1L;

	ChangeCommentAction(SongTable table) {
		super(table, Lang.getUI("col.comment"));
		this.putValue(Action.SMALL_ICON, UiIcon.EDIT_W.getIcon());
	}

	@Override
	protected void actionPerform(int[] indices) {
		// PREPARE variables for user
		String comment = (String) getConnectedTable().getModel().getValueAt(indices[0], 4);
		if(indices.length > 1) {
			comment = "[multi]";
		}

		// USER UI interaction
		JPanel userPanel = new JPanel();
		JTextField newComment = new JTextField(comment);
		newComment.setColumns(30);
		userPanel.add(newComment);

		if (JOptionPane.showConfirmDialog(ApplicationWindow.getAPP_WINDOW(), userPanel, Lang.getUI("col.comment"),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
			return; // cancelled
		}

		// MAKE CHANGES
		String newString = StringUtils.ToUpperCamelCase(newComment.getText());
		if (comment.equalsIgnoreCase(newString))
			return;
		try {
			ArchiveTableModel atm = ((ArchiveTableModel) getConnectedTable().getModel());
			for(int i : indices) {
				CommonSQL.updateCommentInArchive(atm.getRowId(i), newString);
			}
			// Commit archive changes= Not exactly true, but true enough for our purposes.
			// We'll take the overhead as is.
			SQLConnection.getDbConn().commit(DataChangedType.ARCHIVE_DATA_OPTS);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}
}
