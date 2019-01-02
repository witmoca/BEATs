/*
*
+===============================================================================+
|    BEATs (Burning Ember Archival Tool suite)                                  |
|    Copyright 2019 Jente Heremans                                              |
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
* File: CatalogModel.java
* Created: 2019
*/
package be.witmoca.BEATs.ui.artistcatalog;

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

public class CatalogModel extends AbstractTableModel implements DataChangedListener, ContainsEpisodeColumn {
	private static final long serialVersionUID = 1L;
	private static final String COLUMN_NAME[] = {Lang.getUI("col.artist"), Lang.getUI("col.count"), Lang.getUI("col.local"), Lang.getUI("catalog.lastEpisode")};
	private final List<row> content = new ArrayList<row>();

	public CatalogModel() {
		SQLConnection.getDbConn().addDataChangedListener(this, DataChangedType.ARCHIVE_DATA_OPTS);
		this.tableChanged();
	}

	@Override
	public void tableChanged() {
		content.clear(); // clear() is (probably) faster as the backing array doesn't get resized (just turned into null values), so reinserting goes fast
		try (PreparedStatement getValue = SQLConnection.getDbConn().prepareStatement("SELECT ArtistName, local FROM artist ORDER BY ArtistName")) {
			ResultSet value = getValue.executeQuery();
			// For every artist
			while(value.next()) {
				String artist = value.getString(1);
				boolean local = value.getBoolean(2);
				// Count frequency
				int count = 0;
				try (PreparedStatement getCount = SQLConnection.getDbConn().prepareStatement("SELECT count(*) FROM Artist, Song, SongsInArchive WHERE Artist.ArtistName = Song.ArtistName AND Song.SongId = SongsInArchive.SongId AND Artist.ArtistName = ?")) {
					getCount.setString(1, artist);
					ResultSet rs = getCount.executeQuery();
					if(rs.next())
						count = rs.getInt(1);
				}
				// Get last episode & date
				int episode = 0;
				LocalDate episodeDate = null;
				try (PreparedStatement getEpisode = SQLConnection.getDbConn().prepareStatement("SELECT SongsInArchive.EpisodeId, Max(EpisodeDate) FROM SongsInArchive, Song, Episode WHERE SongsInArchive.songId = Song.songId AND SongsInArchive.EpisodeId = Episode.EpisodeID AND ArtistName = ?")) {
					getEpisode.setString(1, artist);
					ResultSet rs = getEpisode.executeQuery();
					if(rs.next()) {
						episode = rs.getInt(1);
						episodeDate = LocalDate.ofEpochDay(rs.getLong(2));
					}
				}
				
				content.add(new row(artist, count, local, episode, episodeDate));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		this.fireTableDataChanged();
	}
	
	

	@Override
	public String getColumnName(int column) {
		return COLUMN_NAME[column];
	}
	
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		return content.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return COLUMN_NAME.length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return content.get(rowIndex).getColumn(columnIndex);
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return row.getColumnClass(columnIndex);
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}
	
	@Override
	public String getEpisodeDate(int row) {
		return content.get(row).getEpisodeDate();
	}

	private static class row {
		private final String artist;
		private final int count;
		private final boolean local;
		private final int episode;
		private final String episodeDate;

		public row(String artist, int count, boolean local, int episode, LocalDate episodeDate) {
			super();
			this.artist = artist;
			this.count = count;
			this.local = local;
			this.episode = episode;
			this.episodeDate = DateTimeFormatter.ofPattern("dd/MM/uu").format(episodeDate);
		}

		public Object getColumn(int i) {
			switch (i) {
			case 0:
				return artist;
			case 1:
				return count;
			case 2:
				return local;
			case 3:
				return episode;
			}
			return null;
		}

		public String getEpisodeDate() {
			return episodeDate;
		}

		static public Class<?> getColumnClass(int i) {
			switch (i) {
			case 0:
				return String.class;
			case 1:
			case 3:
				return Integer.class;
			case 2:
				return Boolean.class;
			}
			return Object.class;
		}
	}
}
