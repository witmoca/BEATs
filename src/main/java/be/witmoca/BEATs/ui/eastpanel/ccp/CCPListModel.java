/**
 * 
 */
package be.witmoca.BEATs.ui.eastpanel.ccp;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import be.witmoca.BEATs.clipboard.TransferableSongList;
import be.witmoca.BEATs.connection.DataChangedListener;
import be.witmoca.BEATs.connection.DataChangedType;
import be.witmoca.BEATs.connection.SQLConnection;

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
* File: CCPListModel.java
* Created: 2018
*/
class CCPListModel implements ListModel<String>, DataChangedListener {
	private final TransferableSongList content = new TransferableSongList();
	private final List<ListDataListener> ldlList = new ArrayList<ListDataListener>();

	public CCPListModel() {
		this.tableChanged();
		SQLConnection.getDbConn().addDataChangedListener(this, EnumSet.of(DataChangedType.CCP));
	}

	@Override
	public int getSize() {
		return content.size();
	}

	@Override
	public String getElementAt(int index) {
		return content.getHumanReadable(index);
	}

	@Override
	public void addListDataListener(ListDataListener l) {
		ldlList.add(l);
	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		ldlList.remove(l);
	}

	@Override
	public void tableChanged() {
		try (PreparedStatement selCCP = SQLConnection.getDbConn()
				.prepareStatement("SELECT Artist, Song FROM ccp ORDER BY rowid")) {
			ResultSet rs = selCCP.executeQuery();

			content.clear();
			while (rs.next()) {
				content.addSong(rs.getString(1), rs.getString(2), 0);
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		// inform ListDataListeners
		ListDataEvent e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, this.getSize());
		for (ListDataListener ldl : ldlList) {
			ldl.contentsChanged(e);
		}
	}
}
