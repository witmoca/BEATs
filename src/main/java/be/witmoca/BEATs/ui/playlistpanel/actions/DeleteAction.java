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
package be.witmoca.BEATs.ui.playlistpanel.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import be.witmoca.BEATs.ui.ApplicationWindow;
import be.witmoca.BEATs.ui.components.SongTable;
import be.witmoca.BEATs.ui.playlistpanel.PlaylistTableModel;
import be.witmoca.BEATs.utils.Lang;
import be.witmoca.BEATs.utils.UiIcon;

class DeleteAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
	private final SongTable connectedTable;

	protected DeleteAction(SongTable table) {
		super(Lang.getUI("action.delete"));
		this.putValue(Action.SMALL_ICON, UiIcon.DELETE.getIcon());
		connectedTable = table;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		int indices[] = connectedTable.getSelectedRows();
		if (indices.length == 0)
			return;
		

		if (JOptionPane.showConfirmDialog(ApplicationWindow.getAPP_WINDOW(), Lang.getUI("deleteAction.confirm"),
				Lang.getUI("deleteAction.confirmTitle"), JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION)
			return;

		PlaylistTableModel tm = ((PlaylistTableModel) connectedTable.getModel());
		for(int i = indices.length - 1; i >= 0; i--) {
			tm.deleteRow(indices[i]);
		}
	}
}