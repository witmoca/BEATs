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
* File: ChangeOrderAction.java
* Created: 2018
*/
package be.witmoca.BEATs.ui.playlistmanager;

import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JList;

import be.witmoca.BEATs.connection.CommonSQL;
import be.witmoca.BEATs.connection.DataChangedType;
import be.witmoca.BEATs.connection.SQLConnection;
import be.witmoca.BEATs.utils.UiIcon;

public class ChangeOrderAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
	private final int movement;
	private final JList<String> model;
	
	/**
	 * @param up {@code true} if up, {@code false} if down
	 */
	public ChangeOrderAction(boolean up, JList<String> model) {
		super(null, up ? UiIcon.UP.getIcon() : UiIcon.DOWN.getIcon());
		this.movement = up ? 1 : -1;
		this.model = model;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			// Make sure tab orders are perfect
			(new OptimiseTabOrderAction()).actionPerformed(null);
			
			// Get necessary values
			String pName = model.getSelectedValue();
			if(pName == null)
				return;
			List<String> pNames = CommonSQL.getPlaylists();
			int order = pNames.indexOf(pName) + 1;
			if(order == -1)
				throw new SQLException("Invalid value");
			// Check if we are trying to go beyond the max or min
			if(order - this.movement > pNames.size() || order - this.movement < 1)
				return;
			
			
			// Do a switcharoo (a => c, b => a, c => a)
			// With a as the position of pName, b as the position above/below, c as position -1
			CommonSQL.updatePlaylistOrder(pName, -1);
			CommonSQL.updatePlaylistOrder(pNames.get(order -1 -this.movement), order);
			CommonSQL.updatePlaylistOrder(pName, order - this.movement);
			
			// Reset the selection now that order has changed
			model.setSelectedIndex(order -1 - this.movement);
			
			SQLConnection.getDbConn().commit(EnumSet.of(DataChangedType.PLAYLIST));
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}
}
