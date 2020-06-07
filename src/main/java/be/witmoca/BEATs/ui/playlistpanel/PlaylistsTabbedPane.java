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
* File: PlaylistsTabbedPane.java
* Created: 2018
*/
package be.witmoca.BEATs.ui.playlistpanel;

import java.sql.SQLException;
import java.util.EnumSet;

import javax.swing.JTabbedPane;

import be.witmoca.BEATs.connection.CommonSQL;
import be.witmoca.BEATs.connection.DataChangedListener;
import be.witmoca.BEATs.connection.DataChangedType;
import be.witmoca.BEATs.connection.SQLConnection;

public class PlaylistsTabbedPane extends JTabbedPane implements DataChangedListener {
	private static final long serialVersionUID = 1L;

	public PlaylistsTabbedPane() {
		super(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);

		this.tableChanged();
		SQLConnection.getDbConn().addDataChangedListener(this, EnumSet.of(DataChangedType.PLAYLIST));
	}

	@Override
	public void tableChanged() {
		this.removeAll();
		try {
			for (String playlist : CommonSQL.getPlaylists()) {
				this.addTab(playlist, new PlaylistPanel(playlist));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
