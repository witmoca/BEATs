/**
 * 
 */
package be.witmoca.BEATs.ui.archivepanel.actions;

import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.EnumSet;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import be.witmoca.BEATs.connection.CommonSQL;
import be.witmoca.BEATs.connection.DataChangedType;
import be.witmoca.BEATs.connection.SQLConnection;
import be.witmoca.BEATs.ui.ApplicationWindow;
import be.witmoca.BEATs.ui.archivepanel.ArchiveTableModel;
import be.witmoca.BEATs.ui.components.SongTable;
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
* File: DeleteEntryAction.java
* Created: 2018
*/
class DeleteEntryAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
	private final SongTable archive;

	DeleteEntryAction(SongTable table) {
		super(Lang.getUI("action.delete"));
		this.putValue(Action.SMALL_ICON, UiIcon.DELETE.getIcon());
		archive = table;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		int indices[] = archive.getSelectedRows();
		if (indices.length == 0)
			return;

		if (JOptionPane.showConfirmDialog(ApplicationWindow.getAPP_WINDOW(), Lang.getUI("deleteAction.confirm"),
				Lang.getUI("deleteAction.confirmTitle"), JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION)
			return;
		
		ArchiveTableModel tm = ((ArchiveTableModel) archive.getModel());
		try {
			for(int i = indices.length - 1; i >= 0; i--) {
				CommonSQL.removeFromSongsInArchive(tm.getRowId(indices[i]));
			}		
			SQLConnection.getDbConn().commit(EnumSet.of(DataChangedType.SONGS_IN_ARCHIVE));
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

}
