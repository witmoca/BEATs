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

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import be.witmoca.BEATs.liveshare.ConnectionsSetChangedListener;
import be.witmoca.BEATs.liveshare.LiveShareClient;
import be.witmoca.BEATs.ui.archivepanel.ArchivePanel;
import be.witmoca.BEATs.ui.artistcatalog.ArtistCatalog;
import be.witmoca.BEATs.ui.liveshare.LiveShareStatusPanel;
import be.witmoca.BEATs.ui.liveshare.LiveShareTabbedPane;
import be.witmoca.BEATs.ui.playlistpanel.PlaylistsTabbedPane;
import be.witmoca.BEATs.ui.songcatalog.SongCatalog;
import be.witmoca.BEATs.utils.BEATsSettings;
import be.witmoca.BEATs.utils.Lang;

public class CenterTabbedPane extends JTabbedPane implements ConnectionsSetChangedListener{
	private static final long serialVersionUID = 1L;
	private final int dynamicTabRange; // tab index where the dynamic range starts
	private final ArchivePanel archivePanel = new ArchivePanel();

	public CenterTabbedPane() {
		super(JTabbedPane.LEFT, JTabbedPane.SCROLL_TAB_LAYOUT);

		this.addTab(Lang.getUI("center.archive"), archivePanel);
		this.addTab(Lang.getUI("center.playlists"), new PlaylistsTabbedPane());
		this.addTab(Lang.getUI("center.artistcatalog"), new ArtistCatalog());
		this.addTab(Lang.getUI("center.songcatalog"), new SongCatalog());
		
		dynamicTabRange = this.getTabCount();
		LiveShareClient.addConnectionsSetChangedListener(this);
	}

	public ArchivePanel getArchivePanel() {
		return archivePanel;
	}

	@Override
	public void connectionsSetChanged(LiveShareClient lsc) {
		// Either show all the watched servers or only the connected servers
		List<String> servers = BEATsSettings.LIVESHARE_CLIENT_ALWAYSVISIBLE.getBoolValue() ? lsc.getWatchServers() : lsc.getConnectedServerNames();

		// Cleanup inactive tabs
		for(int tab = this.getTabCount()-1 ; tab >= dynamicTabRange; tab--) {
			if(!servers.contains(this.getTitleAt(tab))){
				this.removeTabAt(tab);
			}
		}
		
		// add  tabs as appropriate
		for(String server : servers) {
			if(this.indexOfTab(server) == -1){
				// Wraps the LS tabbedPane and the StatusPanel in a JPanel, so they are shown above one another
				JPanel wrapperLS = new JPanel(new BorderLayout());
				wrapperLS.add(new LiveShareTabbedPane(lsc, server), BorderLayout.CENTER);
				wrapperLS.add(new LiveShareStatusPanel(lsc, server), BorderLayout.NORTH);
				this.addTab(server, wrapperLS);
			}
		}
	}
}
