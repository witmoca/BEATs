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
* File: PlaylistsTabbedPane.java
* Created: 2018
*/
package be.witmoca.BEATs.ui.liveshare;

import java.util.List;

import javax.swing.JTabbedPane;

import be.witmoca.BEATs.connection.DataChangedListener;
import be.witmoca.BEATs.liveshare.LiveShareDataClient;

public class LiveShareTabbedPane extends JTabbedPane implements DataChangedListener {
	private static final long serialVersionUID = 1L;
	private final LiveShareDataClient lvdc;
	
	public LiveShareTabbedPane(LiveShareDataClient lvdc) {
		super(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
		this.lvdc = lvdc;

		this.tableChanged();
		lvdc.addDataChangedListener(this);
	}

	@Override
	public void tableChanged() {
		List<String> playlistNames = this.lvdc.getContent().getPlaylists();

		// Remove deleted tabs 
		for(int tabIndex = this.getTabCount() - 1; tabIndex >= 0 ; tabIndex--) {
			String tabName = this.getTitleAt(tabIndex);
			if(!playlistNames.contains(tabName)){
				this.remove(tabIndex);
			} 
		}
		
		// Add new tabs in the correct places
		for(int tabIndex = 0; tabIndex < playlistNames.size(); tabIndex++) {
			
			String tabName = tabIndex < this.getTabCount() ? this.getTitleAt(tabIndex) : "";
			String expectedName = playlistNames.get(tabIndex);
			if(!expectedName.equals(tabName)){
				this.insertTab(expectedName, null, new LiveSharePanel(expectedName,lvdc), null, tabIndex);
			}
		}
		// In case any mishaps occur
		if(playlistNames.size() < this.getTabCount()) {
			System.err.println("LiveShareTabbedPane getTabCount higher than amount of playlistNames!");
			for(int i = this.getTabCount()-1 ; i >= playlistNames.size()  ; i--) {
				this.remove(i);
			}
		}
	}
	
	public boolean isLvdcActive() {
		return this.lvdc.isActive();
	}
}
