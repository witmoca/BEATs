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
package be.witmoca.BEATs.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.swing.AbstractListModel;

import be.witmoca.BEATs.Launch;

public class CurrentQueueListModel extends AbstractListModel<String> implements DataChangedListener {
	private static final long serialVersionUID = 1L;
	private final List<String> internalList = new ArrayList<String>();

	public CurrentQueueListModel() {
		Launch.getDB_CONN().addDataChangedListener(this, EnumSet.of(DataChangedListener.DataType.CURRENT_QUEUE));
		this.tableChanged();
	}

	@Override
	public int getSize() {
		return internalList.size();
	}

	@Override
	public String getElementAt(int index) {
		return internalList.get(index);
	}

	@Override
	public void tableChanged() {
		// Commit happend that changed the currentqueue => reload
		internalList.clear();
		try (PreparedStatement getValue = Launch.getDB_CONN().prepareStatement("SELECT (ArtistName || ' - ' || Title) FROM CurrentQueue, Song WHERE CurrentQueue.SongId = Song.SongId ORDER BY SongOrder ASC")) {
			ResultSet value = getValue.executeQuery();
			while(value.next()) {
				internalList.add(value.getString(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		// notify listeners
		this.fireContentsChanged(this, 0, this.getSize());
	}


}
