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
* File: PlaylistPanel.java
* Created: 2018
*/
package be.witmoca.BEATs.ui.playlistpanel;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import be.witmoca.BEATs.ui.playlistpanel.actions.PlaylistToolbar;
import be.witmoca.BEATs.ui.southpanel.SouthPanel;
import be.witmoca.BEATs.ui.t4j.RowNumberTable;

public class PlaylistPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private final PlaylistTable playlistTable;
	private final JScrollPane playlistScrollPane;
	private final JPanel southPanel;

	public PlaylistPanel(String title) {
		super(new BorderLayout());

		playlistTable = new PlaylistTable(title);
		southPanel = new SouthPanel(playlistTable, 0, 1);
		playlistScrollPane = new JScrollPane(playlistTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		this.add(new PlaylistToolbar(playlistTable), BorderLayout.NORTH);
		this.add(playlistScrollPane, BorderLayout.CENTER);
		this.add(southPanel, BorderLayout.SOUTH);

		// Row numbers
		JTable rowTable = new RowNumberTable(playlistTable, null);
		playlistScrollPane.setRowHeaderView(rowTable);
		playlistScrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, rowTable.getTableHeader());
	}
}
