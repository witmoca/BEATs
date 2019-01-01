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
* File: PMListModel.java
* Created: 2018
*/
package be.witmoca.BEATs.ui.playlistmanager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import be.witmoca.BEATs.connection.CommonSQL;
import be.witmoca.BEATs.connection.DataChangedListener;
import be.witmoca.BEATs.connection.DataChangedType;
import be.witmoca.BEATs.connection.SQLConnection;

class PMListModel implements ListModel<String>, DataChangedListener {
	private final List<String> content = new ArrayList<String>();
	private final List<ListDataListener> listeners = new ArrayList<ListDataListener>();
	
	public PMListModel() {
		SQLConnection.getDbConn().addDataChangedListener(this, EnumSet.of(DataChangedType.PLAYLIST));
		tableChanged();
	}

	@Override
	public void tableChanged() {
		// Load elements into list
		content.clear();
		try {
			content.addAll(CommonSQL.getPlaylists());		
		} catch (SQLException e) {
			e.printStackTrace();
		}
		this.notifyListeners();
	}

	@Override
	public int getSize() {
		return content.size();
	}

	@Override
	public String getElementAt(int index) {
		return content.get(index);
	}

	@Override
	public void addListDataListener(ListDataListener l) {
		listeners.add(l);	
	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		listeners.remove(l);		
	}
	
	private void notifyListeners() {
		for(ListDataListener l : listeners) {
			l.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, this.getSize()));
		}
	}
}
