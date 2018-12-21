/**
 * 
 */
package be.witmoca.BEATs.ui.currentqueue;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import be.witmoca.BEATs.ApplicationManager;
import be.witmoca.BEATs.model.CurrentQueueListModel;

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
* File: ShowInfoAction.java
* Created: 2018
*/
public class ShowInfoAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
	private final JList<String> queue;
	
	public ShowInfoAction(JList<String> queue) {
		super("<html>&#128712; Song Info</html>");
		this.queue = queue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		int selIndex = queue.getSelectedIndex();
		if (selIndex < 0)
			return;
		
		// songOrder is the primary key for a song in the CurrentQueue
		int songOrder = ((CurrentQueueListModel) queue.getModel()).getSongOrderAt(selIndex);

		JLabel artist = new JLabel();
		JLabel song = new JLabel();
		JLabel comment = new JLabel();
		JLabel artistCount = new JLabel();
		JLabel songCount = new JLabel();

		try {
			// Get song data
			try (PreparedStatement getSongData = ApplicationManager.getDB_CONN().prepareStatement(
					"SELECT ArtistName, Title, Comment FROM CurrentQueue, Song WHERE CurrentQueue.SongId = Song.SongId AND CurrentQueue.SongOrder = ?")) {
				getSongData.setInt(1, songOrder);
				ResultSet rs = getSongData.executeQuery();
				if (!rs.next())
					throw new SQLException("CurrentQueue does not contain selected song");
				artist.setText(rs.getString(1));
				song.setText(rs.getString(2));
				comment.setText(rs.getString(3));
			}
			
			// get artist count
			try (PreparedStatement selArtist = ApplicationManager.getDB_CONN().prepareStatement("SELECT count(*) FROM SongsInArchive,Song WHERE SongsInArchive.SongId = Song.SongId AND ArtistName = ?")){
				selArtist.setString(1, artist.getText());
				ResultSet rs = selArtist.executeQuery();
				if(!rs.next())
					artistCount.setText("0");
				else
					artistCount.setText("" + rs.getInt(1));
			}
			
			// get song count
			try (PreparedStatement selSong = ApplicationManager.getDB_CONN().prepareStatement("SELECT count(*) FROM SongsInArchive,Song WHERE SongsInArchive.SongId = Song.SongId AND ArtistName = ? AND Title = ?")){
				selSong.setString(1, artist.getText());
				selSong.setString(2, song.getText());
				ResultSet rs = selSong.executeQuery();
				if(!rs.next())
					songCount.setText("0");
				else
					songCount.setText("" + rs.getInt(1));
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
			return;
		}
		
		JPanel message = new JPanel(new GridLayout(0,2));
		message.add(new JLabel("Artist: "));
		message.add(artist);
		message.add(new JLabel("Song: "));
		message.add(song);
		message.add(new JLabel("Comment: "));
		message.add(comment);
		message.add(new JLabel("Times Artist Was Played: "));
		message.add(artistCount);
		message.add(new JLabel("Times Song Was Played: "));
		message.add(songCount);
		JOptionPane.showMessageDialog(ApplicationManager.getAPP_WINDOW(), message, "Song Info", JOptionPane.PLAIN_MESSAGE);
	}

}
