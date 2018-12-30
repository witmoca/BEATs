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
* File: OptimiseTabOrderAction.java
* Created: 2018
*/
package be.witmoca.BEATs.ui.playlistmanager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;

import be.witmoca.BEATs.connection.CommonSQL;

public class OptimiseTabOrderAction implements ActionListener {
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			List<String> pNames = CommonSQL.getPlaylists();
			int maxTab = CommonSQL.getMaxTabOrderFromPlaylist();
			// Already optimal
			if(pNames.size() == maxTab)
				return;
			
			for(int i = 0; i < pNames.size(); i++) {
				CommonSQL.updatePlaylistOrder(pNames.get(i), i+1);
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}
}
