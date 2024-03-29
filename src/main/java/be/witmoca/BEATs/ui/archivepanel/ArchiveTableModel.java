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
package be.witmoca.BEATs.ui.archivepanel;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import be.witmoca.BEATs.connection.DataChangedListener;
import be.witmoca.BEATs.connection.DataChangedType;
import be.witmoca.BEATs.connection.SQLConnection;
import be.witmoca.BEATs.ui.components.ContainsEpisodeColumn;
import be.witmoca.BEATs.utils.Lang;
import be.witmoca.BEATs.utils.OriginHelper;

public class ArchiveTableModel extends AbstractTableModel implements DataChangedListener, ContainsEpisodeColumn {
	private static final long serialVersionUID = 1L;
	private static final String COLUMN_NAME[] = { Lang.getUI("col.artist"), Lang.getUI("col.song"),
			Lang.getUI("col.episode"), Lang.getUI("col.genre"), Lang.getUI("col.comment"), Lang.getUI("col.origin")};
	private List<ArchiveEntry> archive = new ArrayList<ArchiveEntry>();

	public ArchiveTableModel() {
		super();

		SQLConnection.getDbConn().addDataChangedListener(this, DataChangedType.ARCHIVE_DATA_OPTS);
		this.tableChanged();
	}

	@Override
	public void tableChanged() {
		archive.clear(); // clear() is (probably) faster as the backing array doesn't get resized (just
							// turned into null values), so reinserting goes fast
		try (PreparedStatement getValue = SQLConnection.getDbConn().prepareStatement(
				"SELECT SongsInArchive.rowid, Song.ArtistName, Title, SongsInArchive.EpisodeId, EpisodeDate, GenreName, Comment, Origin FROM SongsInArchive,Song,Episode,Artist WHERE SongsInArchive.SongId = Song.SongId AND SongsInArchive.EpisodeId = Episode.EpisodeId AND Song.ArtistName = Artist.ArtistName")) {
			ResultSet value = getValue.executeQuery();
			while (value.next()) {
				archive.add(new ArchiveEntry(value.getInt(1), value.getString(2), value.getString(3), value.getInt(4),
						value.getInt(5), value.getString(6), value.getString(7), OriginHelper.getDisplayStringFromOriginCode(value.getString(8))));
			}
		} catch (SQLException e) {
			e.printStackTrace();
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

	public int getRowId(int row) {
		return archive.get(row).getROWID();
	}

	@Override
	public String getEpisodeDate(int row) {
		return archive.get(row).getDate();
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return ArchiveEntry.getColumnType(columnIndex);
	}

	@Override
	public String getColumnName(int column) {
		return COLUMN_NAME[column];
	}

	private static class ArchiveEntry {
		private final int ROWID;
		private final String ARTIST;
		private final String SONG;
		private final int EPISODE;
		private final String EPISODE_DATE;
		private final String Genre;
		private final String COMMENT;
		private final String ORIGIN;

		ArchiveEntry(int rowid, String artist, String song, int episode, int epDate, String Genre, String comment, String origin) {
			this.ROWID = rowid;
			this.ARTIST = artist;
			this.SONG = song;
			this.EPISODE = episode;
			this.EPISODE_DATE = DateTimeFormatter.ofPattern("dd/MM/uu").format(LocalDate.ofEpochDay(epDate));
			this.Genre = Genre;
			this.COMMENT = comment;
			this.ORIGIN = origin;
		}

		Object getColumn(int i) {
			switch (i) {
			case 0:
				return this.ARTIST;
			case 1:
				return this.SONG;
			case 2:
				return this.EPISODE;
			case 3:
				return this.Genre;
			case 4:
				return this.COMMENT;
			case 5:
				return this.ORIGIN;
			default:
				return null;
			}
		}

		static Class<?> getColumnType(int column) {
			switch (column) {
			case 2:
				return Integer.class;
			default:
				return String.class;
			}
		}

		public int getROWID() {
			return ROWID;
		}

		public String getDate() {
			return EPISODE_DATE;
		}
	}
}
