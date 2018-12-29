/**
 * 
 */
package be.witmoca.BEATs.ui.southpanel;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import be.witmoca.BEATs.connection.SQLConnection;
import be.witmoca.BEATs.utils.Lang;

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
class InfoPanel extends JPanel implements ListSelectionListener {
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
		super(new GridBagLayout());
		this.artistColumn = artistColumn;
		this.songColumn = songColumn;
		
		GridBagConstraints left = new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5,10,0,10), 0, 0);
		GridBagConstraints right = new GridBagConstraints(1, 0, 1, 1, 1, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(5,10,0,10), 0, 0);
		
		add(new JLabel(Lang.getUI("infoPanel.artist")), left);
		left.gridy += 1;
		left.insets = new Insets(0,10,0,10); // only the top one needs a top inset
		add(artistLabel, right);
		right.gridy += 1;
		right.insets = new Insets(0,10,0,10); // only the top one needs a top inset
		add(new JLabel(Lang.getUI("infoPanel.song")), left);
		left.gridy += 1;
		add(songLabel, right);
		right.gridy += 1;
		add(new JLabel(Lang.getUI("infoPanel.songLast")), left);
		left.gridy += 1;
		add(songLast, right);
		right.gridy += 1;
		add(new JLabel(Lang.getUI("infoPanel.artistLast")), left);
		left.gridy += 1;
		add(artistLast, right);
		right.gridy += 1;
		right.weighty = 1; // The last box (the most south one) takes all the whitespace (and postions the element in the north-west
		add(new JLabel(Lang.getUI("infoPanel.local")), left);
		add(artistLocal, right);

		tracking = trackingTable;
		trackingTable.getSelectionModel().addListSelectionListener(this);
		this.setBorder(BorderFactory.createLineBorder(Color.GRAY));
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
			artistLabel.setText(Lang.getUI("infoPanel.unknownArtist"));
			songLabel.setText(Lang.getUI("infoPanel.unknownSong"));
			songLast.setText("");
			artistLast.setText("");
			artistLocal.setText("");
			
			try (PreparedStatement selArtist = SQLConnection.getDbConn()
					.prepareStatement("SELECT Local, count(*) FROM Artist, Song, SongsInArchive WHERE Artist.ArtistName = Song.ArtistName AND Song.SongId = SongsInArchive.SongId AND Artist.ArtistName = ?")) {
				selArtist.setString(1, artist);
				ResultSet rs = selArtist.executeQuery();
				if(rs.next() && rs.getInt(2) != 0) {
					artistLocal.setText(rs.getBoolean(1) ? Lang.getUI("action.yes") : Lang.getUI("action.no"));
					artistLabel.setText(artist + " (" + rs.getInt(2) + " " + Lang.getUI("infoPanel.timesPlayed") + ")");
				} else {
					return;
				}
			}
			
			try (PreparedStatement selLastArtist = SQLConnection.getDbConn().prepareStatement("SELECT Title, SongsInArchive.EpisodeId, Max(EpisodeDate) FROM SongsInArchive, Song, Episode WHERE SongsInArchive.songId = Song.songId AND SongsInArchive.EpisodeId = Episode.EpisodeID AND ArtistName = ?")) {
				selLastArtist.setString(1, artist);
				ResultSet rs = selLastArtist.executeQuery();
				if(rs.next()) {
					artistLast.setText(rs.getInt(2) + " (" + DateTimeFormatter.ofPattern("dd/MM/uu").format(LocalDate.ofEpochDay(rs.getLong(3))) + ") - " + rs.getString(1));			
				} else {
					return;
				}
			}
			
			try (PreparedStatement selLastSong = SQLConnection.getDbConn().prepareStatement("SELECT SongsInArchive.EpisodeId, max(EpisodeDate), count(title) FROM SongsInArchive, Song, Episode WHERE SongsInArchive.songId = Song.songId AND SongsInArchive.EpisodeId = Episode.EpisodeID AND ArtistName = ? AND title = ?")) {
				selLastSong.setString(1, artist);
				selLastSong.setString(2, song);
				ResultSet rs = selLastSong.executeQuery();
				if(rs.next() && rs.getInt(3) != 0) {
					songLast.setText(rs.getInt(1) + " (" + DateTimeFormatter.ofPattern("dd/MM/uu").format(LocalDate.ofEpochDay(rs.getLong(2))) + ")");
					songLabel.setText(song + " ("  + rs.getInt(3) + " " + Lang.getUI("infoPanel.timesPlayed") +")");
				} else {
					return;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
