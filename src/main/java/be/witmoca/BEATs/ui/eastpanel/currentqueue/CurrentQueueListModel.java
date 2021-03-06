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
* File: CurrentQueueListModel.java
* Created: 2018
*/
package be.witmoca.BEATs.ui.eastpanel.currentqueue;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.AbstractListModel;

import be.witmoca.BEATs.connection.DataChangedListener;
import be.witmoca.BEATs.connection.DataChangedType;
import be.witmoca.BEATs.connection.SQLConnection;

public class CurrentQueueListModel extends AbstractListModel<String> implements DataChangedListener {
	private static final long serialVersionUID = 1L;
	private final SortedMap<Integer, String> internalMap = new TreeMap<Integer, String>();

	public CurrentQueueListModel() {
		SQLConnection.getDbConn().addDataChangedListener(this, EnumSet.of(DataChangedType.CURRENT_QUEUE));
		this.tableChanged();
	}

	@Override
	public int getSize() {
		return internalMap.size();
	}

	@Override
	public String getElementAt(int index) {
		return internalMap.get(this.getSongOrderAt(index));
	}

	public int getSongOrderAt(int index) {
		return (int) internalMap.keySet().toArray()[index];
	}

	@Override
	public void tableChanged() {
		// Commit happend that changed the currentqueue => reload
		internalMap.clear();
		try (PreparedStatement getValue = SQLConnection.getDbConn().prepareStatement(
				"SELECT SongOrder, (ArtistName || ' - ' || Title) FROM CurrentQueue, Song WHERE CurrentQueue.SongId = Song.SongId ORDER BY SongOrder ASC")) {
			ResultSet value = getValue.executeQuery();
			while (value.next()) {
				internalMap.put(value.getInt(1), value.getString(2));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// notify listeners
		this.fireContentsChanged(this, 0, this.getSize());
	}

}
