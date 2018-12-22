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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import be.witmoca.BEATs.ApplicationManager;

public class ArchiveTableModel extends AbstractTableModel implements DataChangedListener {
	private static final long serialVersionUID = 1L;
	private static final String COLUMN_NAME[] = {"Artist","Song","Episode", "Section" ,"Comment"};
	private List<ArchiveEntry> archive = new ArrayList<ArchiveEntry>();
	
	public ArchiveTableModel() {
		super();
		
		ApplicationManager.getDB_CONN().addDataChangedListener(this, DataChangedListener.DataType.ARCHIVE_DATA_OPTS);
		this.tableChanged();
	}
	
	@Override
	public void tableChanged() {
		archive.clear(); // clear() is (probably) faster as the backing array doesn't get resized (just turned into null values), so reinserting goes fast
		try (PreparedStatement getValue = ApplicationManager.getDB_CONN().prepareStatement("SELECT ArtistName, Title, EpisodeId, SectionName, Comment FROM SongsInArchive,Song WHERE SongsInArchive.SongId = Song.SongId")) {
			ResultSet value = getValue.executeQuery();
			while(value.next()) {
				archive.add(new ArchiveEntry(value.getString(1), value.getString(2) ,value.getInt(3) ,value.getString(4), value.getString(5)));
			}
		} catch (SQLException e) {
		}
		this.fireTableDataChanged();
	}

	@Override
	public int getRowCount() {		
		return archive.size();
	}

	@Override
	public int getColumnCount() {
		return COLUMN_NAME.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return archive.get(rowIndex).getColumn(columnIndex);
	}
	
	

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return ArchiveEntry.getColumnType(columnIndex);
	}

	@Override
	public String getColumnName(int column) {
		return COLUMN_NAME[column];
	}
}
