/**
 * 
 */
package be.witmoca.BEATs.ui.archivepanel.actions;

import java.awt.event.ActionEvent;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.EnumSet;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JTable;

import be.witmoca.BEATs.ApplicationManager;
import be.witmoca.BEATs.connection.DataChangedListener;
import be.witmoca.BEATs.ui.archivepanel.ArchiveTableModel;
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
	private final JTable archive;
	
	DeleteEntryAction(JTable table) {
		super("Delete");
		this.putValue(Action.SMALL_ICON, UiIcon.DELETE.getIcon());
		archive = table;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		int index = archive.getSelectedRow();
		if(index < 0)
			return;
		if(archive.getRowSorter() != null)
			index = archive.getRowSorter().convertRowIndexToModel(index);
		
		if (JOptionPane.showConfirmDialog(ApplicationManager.getAPP_WINDOW(),
				"Are you sure you want to delete row?", "Delete?",
				 JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION)
			return;
		
		int rowid = ((ArchiveTableModel) archive.getModel()).getRowId(index);
		
		try (PreparedStatement delLine = ApplicationManager.getDB_CONN().prepareStatement("DELETE FROM SongsInArchive WHERE rowid = ?")) {
			delLine.setInt(1, rowid);
			delLine.executeUpdate();
			
			ApplicationManager.getDB_CONN().commit(EnumSet.of(DataChangedListener.DataType.SONGS_IN_ARCHIVE));
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

}
