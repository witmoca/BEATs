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
package be.witmoca.BEATs.ui;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JTabbedPane;

import be.witmoca.BEATs.Launch;

public class PlaylistsTabbedPane extends JTabbedPane {
	private static final long serialVersionUID = 1L;
	public static final String TITLE = "Playlists"; 

	public PlaylistsTabbedPane() {
		super(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
		
		this.reloadModel();
	}
	
	private void reloadModel() {
		try (Statement getValue = Launch.getDb().createStatement()) {
			ResultSet value = getValue.executeQuery("SELECT PlaylistName FROM Playlist ORDER BY TabOrder");
			while(value.next()) {
				if(value.getRow() <= this.getTabCount()) {
					this.setTitleAt(value.getRow()-1, value.getString(1));
				} else {
					this.addTab(value.getString(1), new PlaylistPanel(this , value.getString(1)));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
}
