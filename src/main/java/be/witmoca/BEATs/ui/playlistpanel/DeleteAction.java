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
* File: DeleteAction.java
* Created: 2018
*/
package be.witmoca.BEATs.ui.playlistpanel;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import be.witmoca.BEATs.ApplicationManager;
import be.witmoca.BEATs.model.PlaylistTableModel;
import be.witmoca.BEATs.utils.UiUtils;

class DeleteAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
	private final JTable connectedTable;

	protected DeleteAction(JTable table) {
		super("Delete");
		this.putValue(Action.ACTION_COMMAND_KEY, "Playlist: Delete Song Selection");
		this.putValue(Action.SMALL_ICON, new ImageIcon(getClass().getClassLoader().getResource("Icons/recyclebin.png")));
		connectedTable = table;
		
		// Attach global hotkey
		connectedTable.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
				this.getValue(Action.ACTION_COMMAND_KEY));
		connectedTable.getActionMap().put(this.getValue(Action.ACTION_COMMAND_KEY),this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(connectedTable.getSelectedRowCount() == 0)
			return;
		if (JOptionPane.showConfirmDialog(ApplicationManager.getAPP_WINDOW(),
				"Are you sure you want to delete " + connectedTable.getSelectedRowCount() + " row(s)?", "Delete?",
				 JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION)
			return;

		int rowIndices[] = UiUtils.convertSelectionToModel(connectedTable.getSelectedRows(), connectedTable);
		// Make sure that we start deleting at the end of the list
		Arrays.sort(rowIndices);

		// Clearing all cells removes the row
		for (int i = rowIndices.length - 1; i >= 0; i--) {
				((PlaylistTableModel) connectedTable.getModel()).deleteRow(rowIndices[i]);
			
		}
	}
}