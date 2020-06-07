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
* File: CenterTabbedPane.java
* Created: 2018
*/
package be.witmoca.BEATs.ui;

import javax.swing.JTabbedPane;

import be.witmoca.BEATs.liveview.ConnectionsSetChangedListener;
import be.witmoca.BEATs.liveview.LiveViewDataClient;
import be.witmoca.BEATs.ui.archivepanel.ArchivePanel;
import be.witmoca.BEATs.ui.artistcatalog.ArtistCatalog;
import be.witmoca.BEATs.ui.liveview.LiveViewTabbedPane;
import be.witmoca.BEATs.ui.playlistpanel.PlaylistsTabbedPane;
import be.witmoca.BEATs.ui.songcatalog.SongCatalog;
import be.witmoca.BEATs.utils.Lang;

class CenterTabbedPane extends JTabbedPane implements ConnectionsSetChangedListener{
	private static final long serialVersionUID = 1L;
	private final int dynamicTabRange; // tab index where the dynamic range starts

	public CenterTabbedPane() {
		super(JTabbedPane.LEFT, JTabbedPane.SCROLL_TAB_LAYOUT);

		this.addTab(Lang.getUI("center.archive"), new ArchivePanel());
		this.addTab(Lang.getUI("center.playlists"), new PlaylistsTabbedPane());
		this.addTab(Lang.getUI("center.artistcatalog"), new ArtistCatalog());
		this.addTab(Lang.getUI("center.songcatalog"), new SongCatalog());
		
		dynamicTabRange = this.getTabCount();
		LiveViewDataClient.addConnectionsSetChangedListener(this);
	}

	@Override
	public void connectionsSetChanged() {
		// Cleanup inactive tabs
		for(int tab = this.getTabCount()-1 ; tab >= dynamicTabRange; tab--) {
			if(! ((LiveViewTabbedPane) this.getTabComponentAt(tab)).isLvdcActive()){
				this.removeTabAt(tab);
			}
		}
		
		// add  tabs as appropriate
		for(LiveViewDataClient lvdc : LiveViewDataClient.getConnections()) {
			if(lvdc.isActive() && this.indexOfTab(lvdc.getName()) == -1){
				this.addTab(lvdc.getName(), new LiveViewTabbedPane(lvdc));
			}
		}
	}
}
