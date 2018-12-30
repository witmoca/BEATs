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
* File: RenamePlaylistAction.java
* Created: 2018
*/
package be.witmoca.BEATs.ui.playlistmanager;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.EnumSet;

import javax.swing.AbstractAction;
import javax.swing.JList;
import javax.swing.JOptionPane;

import be.witmoca.BEATs.connection.CommonSQL;
import be.witmoca.BEATs.connection.DataChangedType;
import be.witmoca.BEATs.connection.SQLConnection;
import be.witmoca.BEATs.utils.Lang;
import be.witmoca.BEATs.utils.StringUtils;
import be.witmoca.BEATs.utils.UiIcon;

public class RenamePlaylistAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
	private final JList<String> model;
	
	public RenamePlaylistAction(JList<String> model) {
		super(null, UiIcon.EDIT.getIcon());
		this.model = model;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		String pName = model.getSelectedValue();
		if(pName == null)
			return;
		
		String newName = StringUtils.ToUpperCamelCase(JOptionPane.showInputDialog((Component) e.getSource(), Lang.getUI("playlistManager.rename.dialog") + ": "));
		if(newName.isEmpty())
			return;
		
		// Make sure tab orders are perfect
		(new OptimiseTabOrderAction()).actionPerformed(null);
		
		try {
			int order = CommonSQL.getPlaylists().indexOf(pName)+1;
			if(order < 0)
				throw new SQLException("Invalid value");
			CommonSQL.addPlaylist(newName, -1);
			CommonSQL.updatePlaylistReferences(newName, pName);
			CommonSQL.removePlaylist(pName);	
			CommonSQL.updatePlaylistOrder(newName, order);
			SQLConnection.getDbConn().commit(EnumSet.of(DataChangedType.PLAYLIST));
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}
}
