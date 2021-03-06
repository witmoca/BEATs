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
* File: ArchiveAction.java
* Created: 2018
*/
package be.witmoca.BEATs.ui.eastpanel.currentqueue.actions;

import java.awt.event.ActionEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumSet;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JList;

import be.witmoca.BEATs.connection.SQLConnection;
import be.witmoca.BEATs.connection.CommonSQL;
import be.witmoca.BEATs.connection.DataChangedType;
import be.witmoca.BEATs.utils.Lang;
import be.witmoca.BEATs.utils.UiIcon;

class ArchiveAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
	private final JList<String> queue;

	ArchiveAction(JList<String> Queue) {
		super(Lang.getUI("queue.archive"));
		this.putValue(Action.SMALL_ICON, UiIcon.PROCEED.getIcon());
		queue = Queue;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (queue.getModel().getSize() == 0)
			return;
		ArchivalDialog ad = new ArchivalDialog();
		// Archive action cancelled
		if (!ad.isValid())
			return;

		int episodeId = ad.getEpisodeId();
		String Genre = ad.getGenre();

		try {
			// Make sure that the episode & genre exist
			CommonSQL.addEpisode(episodeId, ad.getEpisodeDate());
			CommonSQL.addGenre(Genre);

			// Add songs from Queue to Archive
			try (PreparedStatement listCQ = SQLConnection.getDbConn()
					.prepareStatement("SELECT SongId, Comment FROM CurrentQueue ORDER BY SongOrder ASC")) {
				ResultSet rs = listCQ.executeQuery();
				while (rs.next())
					CommonSQL.addSongInArchive(rs.getInt(1), episodeId, Genre, rs.getString(2));
			}

			// Clear the queue
			CommonSQL.clearCurrentQueue();

			// Commit changes
			SQLConnection.getDbConn().commit(EnumSet.of(DataChangedType.SONGS_IN_ARCHIVE, DataChangedType.CURRENT_QUEUE,
					DataChangedType.EPISODE, DataChangedType.GENRE));
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}
}
