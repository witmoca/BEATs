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
package be.witmoca.BEATs.ui.currentqueue;

import java.awt.event.ActionEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumSet;

import javax.swing.AbstractAction;
import javax.swing.JList;

import be.witmoca.BEATs.ApplicationManager;
import be.witmoca.BEATs.model.DataChangedListener;
import be.witmoca.BEATs.model.SQLObjectTransformer;

public class ArchiveAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
	private final JList<String> queue;
	
	public ArchiveAction(JList<String> Queue) {
		super("Archive");
		queue = Queue;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(queue.getModel().getSize() == 0)
			return;
		ArchivalDialog ad = new ArchivalDialog();
		// Archive action cancelled
		if(!ad.isValid())
			return;
		
		int episodeId = ad.getEpisode();
		String section = ad.getSection();
		
		try (PreparedStatement listCQ = ApplicationManager.getDB_CONN().prepareStatement("SELECT SongId, Comment FROM CurrentQueue ORDER BY SongOrder ASC")) {
			ResultSet rs = listCQ.executeQuery();
			while (rs.next())
				SQLObjectTransformer.addSongInArchive(rs.getInt(1), episodeId, section, rs.getString(2));
		} catch (SQLException e1) {
			e1.printStackTrace();
			return;
		}
		
		
		try (PreparedStatement listCQ = ApplicationManager.getDB_CONN().prepareStatement("DELETE FROM CurrentQueue")) {
			listCQ.executeUpdate();
			ApplicationManager.getDB_CONN().commit(EnumSet.of(DataChangedListener.DataType.SONGS_IN_ARCHIVE, DataChangedListener.DataType.CURRENT_QUEUE));
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}
}
