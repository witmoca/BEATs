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
* File: ArchiveTableModel.java
* Created: 2018
*/
package be.witmoca.BEATs.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import be.witmoca.BEATs.Launch;

public class ArchiveTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	private static final String COLUMN_NAME[] = {"Artist","Song","Episode - Section","Comment"};
	private List<ArchiveEntry> archive = new ArrayList<ArchiveEntry>();
	
	
	// Layout: "Artist, pre", "Song", "Episode + Section", "Comment" 
	
	public ArchiveTableModel() {
		super();
		this.reloadModel();
	}
	
	private void reloadModel() {
		try (Statement getValue = Launch.getDb().createStatement()) {
			ResultSet value = getValue.executeQuery("SELECT ArtistName, Title, (EpisodeId || ' (' || SectionName || ')'), Comment FROM SongsInArchive,Song WHERE SongsInArchive.SongId = Song.SongId");
			while(value.next()) {
				archive.add(new ArchiveEntry(value.getString(1), value.getString(2) ,value.getString(3) ,value.getString(4)));
			}
		} catch (SQLException e) {
		}
	}

	@Override
	public int getRowCount() {		
		return archive.size();
	}

	@Override
	public int getColumnCount() {
		return 4;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return archive.get(rowIndex).getColumn(columnIndex);
	}

	@Override
	public String getColumnName(int column) {
		return COLUMN_NAME[column];
	}
}
