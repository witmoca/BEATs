/**
 * 
 */
package be.witmoca.BEATs.ui.southpanel;

import java.awt.GridLayout;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import be.witmoca.BEATs.Launch;

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
* File: InfoPanel.java
* Created: 2018
*/
public class InfoPanel extends JPanel implements ListSelectionListener {
	private static final long serialVersionUID = 1L;
	private final JLabel songLabel = new JLabel("");
	private final JLabel artistLabel = new JLabel("");
	private final JLabel songLast = new JLabel("");
	private final JLabel artistLast = new JLabel("");
	private final JLabel artistLocal = new JLabel("");
	private final int artistColumn;
	private final int songColumn;

	private final JTable tracking;

	/**
	 * 
	 * @param trackingTable A JTable that gets its selection tracked and translated
	 *                      into information
	 * @param artistColumn  The column (of trackingTable) that contains a String
	 *                      representing an artist
	 * @param songColumn    The column (of trackingTable) that contains a String
	 *                      representing a song title
	 */
	public InfoPanel(JTable trackingTable, int artistColumn, int songColumn) {
		super(new GridLayout(0, 2));
		this.artistColumn = artistColumn;
		this.songColumn = songColumn;
		
		add(new JLabel("Artist (#played):"));
		add(artistLabel);
		add(new JLabel("Song (#played):"));
		add(songLabel);
		add(new JLabel("Song last played:"));
		add(songLast);
		add(new JLabel("Artist last played:"));
		add(artistLast);
		add(new JLabel("Artist is local:"));
		add(artistLocal);

		tracking = trackingTable;
		trackingTable.getSelectionModel().addListSelectionListener(this);
	}

	@Override
	public void valueChanged(ListSelectionEvent arg0) {
		if (arg0.getValueIsAdjusting())
			return;

		int index = tracking.getSelectedRow();
		// check for valid index
		if(index < 0)
			return;

		// compensate for a sorter
		if (tracking.getRowSorter() != null)
			index = tracking.getRowSorter().convertRowIndexToModel(index);

		Object aO = tracking.getModel().getValueAt(index, artistColumn);
		Object sO = tracking.getModel().getValueAt(index, songColumn);
		if (!(aO instanceof String) || !(sO instanceof String)) {
			return;
		}

		String artist = (String) aO;
		String song = (String) sO;
		
		try {
			// Default case: nothing found
			artistLabel.setText("unknown artist");
			songLabel.setText("unknown song");
			songLast.setText("");
			artistLast.setText("");
			artistLocal.setText("");
			
			try (PreparedStatement selArtist = Launch.getDB_CONN()
					.prepareStatement("SELECT Local, count(*) FROM Artist, Song, SongsInArchive WHERE Artist.ArtistName = Song.ArtistName AND Song.SongId = SongsInArchive.SongId AND Artist.ArtistName = ?")) {
				selArtist.setString(1, artist);
				ResultSet rs = selArtist.executeQuery();
				if(rs.next() && rs.getInt(2) != 0) {
					artistLocal.setText(rs.getBoolean(1) ? "Yes" : "No");
					artistLabel.setText(artist + " (played " + rs.getInt(2) + " times)");
				} else {
					return;
				}
			}
			
			try (PreparedStatement selLastArtist = Launch.getDB_CONN().prepareStatement("SELECT Title, SongsInArchive.EpisodeId, Max(EpisodeDate) FROM SongsInArchive, Song, Episode WHERE SongsInArchive.songId = Song.songId AND SongsInArchive.EpisodeId = Episode.EpisodeID AND ArtistName = ?")) {
				selLastArtist.setString(1, artist);
				ResultSet rs = selLastArtist.executeQuery();
				if(rs.next()) {
					artistLast.setText(rs.getInt(2) + " (" + DateTimeFormatter.ofPattern("dd/MM/uu").format(LocalDate.ofEpochDay(rs.getLong(3))) + ") - " + rs.getString(1));			
				} else {
					return;
				}
			}
			
			try (PreparedStatement selLastSong = Launch.getDB_CONN().prepareStatement("SELECT SongsInArchive.EpisodeId, max(EpisodeDate), count(title) FROM SongsInArchive, Song, Episode WHERE SongsInArchive.songId = Song.songId AND SongsInArchive.EpisodeId = Episode.EpisodeID AND ArtistName = ? AND title = ?")) {
				selLastSong.setString(1, artist);
				selLastSong.setString(2, song);
				ResultSet rs = selLastSong.executeQuery();
				if(rs.next() && rs.getInt(3) != 0) {
					songLast.setText(rs.getInt(1) + " (" + DateTimeFormatter.ofPattern("dd/MM/uu").format(LocalDate.ofEpochDay(rs.getLong(2))) + ")");
					songLabel.setText(song + " (played "  + rs.getInt(3) + " times)");
				} else {
					return;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
