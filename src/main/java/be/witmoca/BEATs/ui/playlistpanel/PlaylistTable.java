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
* File: PlaylistTable.java
* Created: 2018
*/
package be.witmoca.BEATs.ui.playlistpanel;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import be.witmoca.BEATs.model.PlaylistTableModel;

public class PlaylistTable extends JTable {
	private static final long serialVersionUID = 1L;

	protected PlaylistTable(String PlaylistName) {
		super(new PlaylistTableModel(PlaylistName));
		this.getTableHeader().setReorderingAllowed(false);
		this.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		// Add standard single column rowsorter
		TableRowSorter<PlaylistTableModel> srt = new TableRowSorter<PlaylistTableModel>((PlaylistTableModel) this.getModel());
		srt.setMaxSortKeys(1);
		this.setRowSorter(srt);
		
		this.setComponentPopupMenu(new PlaylistPopupMenu(this));
	}
	
	protected void setTabTitle(String tabTitle) {
		((PlaylistTableModel) this.getModel()).setPlaylistName(tabTitle);
	}
}
