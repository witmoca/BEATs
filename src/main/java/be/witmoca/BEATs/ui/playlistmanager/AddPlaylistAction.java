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
* File: AddPlaylistAction.java
* Created: 2018
*/
package be.witmoca.BEATs.ui.playlistmanager;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.EnumSet;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import be.witmoca.BEATs.connection.CommonSQL;
import be.witmoca.BEATs.connection.DataChangedType;
import be.witmoca.BEATs.connection.SQLConnection;
import be.witmoca.BEATs.utils.Lang;
import be.witmoca.BEATs.utils.StringUtils;

class AddPlaylistAction extends AbstractAction {
	private static final long serialVersionUID = 1L;

	public AddPlaylistAction() {
		super("+");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof Component) {
			// input string for the name, sanitise & capitalise it
			String newName = StringUtils.ToUpperCamelCase(JOptionPane.showInputDialog((Component) e.getSource(),
					Lang.getUI("playlistManager.new.dialog") + ": "));
			if (newName.isEmpty())
				return;
			try {
				// Already exists?
				if (CommonSQL.getPlaylists().contains(newName)) {
					JOptionPane.showMessageDialog((Component) e.getSource(),
							Lang.getUI("playlistManager.new.existsalready"), "", JOptionPane.WARNING_MESSAGE);
					return;
				}
				// Create new playlist
				CommonSQL.addPlaylist(newName, -1);
				// commit
				SQLConnection.getDbConn().commit(EnumSet.of(DataChangedType.PLAYLIST));
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
	}

}
