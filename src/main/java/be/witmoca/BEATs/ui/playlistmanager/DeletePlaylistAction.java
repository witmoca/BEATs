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
* File: DeletePlaylistAction.java
* Created: 2018
*/
package be.witmoca.BEATs.ui.playlistmanager;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import javax.swing.AbstractAction;
import javax.swing.JList;
import javax.swing.JOptionPane;
import be.witmoca.BEATs.connection.CommonSQL;
import be.witmoca.BEATs.connection.DataChangedType;
import be.witmoca.BEATs.connection.SQLConnection;
import be.witmoca.BEATs.utils.Lang;
import be.witmoca.BEATs.utils.UiIcon;

public class DeletePlaylistAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
	private final JList<String> model;

	public DeletePlaylistAction(JList<String> model) {
		super(null,UiIcon.DELETE.getIcon());
		this.model = model;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if(!(e.getSource() instanceof Component))
			return;
		try {
			// Prepare for deletion
			String pName = model.getSelectedValue();
			if(pName == null)
				return;
			int count = CommonSQL.countSongsInPlaylist(pName);
			// Confirm if playlist still contains values
			if(count > 0) {
				int confirm = JOptionPane.showConfirmDialog((Component) e.getSource(), Lang.getUI("playlistManager.delete.confirm"), "", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if(confirm != JOptionPane.YES_OPTION)
					return; // Cancel
				// Delete contents
				CommonSQL.clearSongsInPlaylist(pName);
			}
			
			// Delete Playlist
			CommonSQL.removePlaylist(pName);
			// Optimise tab orders
			(new OptimiseTabOrderAction()).actionPerformed(null);
			SQLConnection.getDbConn().commit(DataChangedType.PLAYLIST_DATA_OPTS);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

}
