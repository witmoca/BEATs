/**
 * 
 */
package be.witmoca.BEATs.ui.southpanel;

import java.awt.GridLayout;
import javax.swing.JPanel;

import be.witmoca.BEATs.ui.extendables.SongTable;

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
* File: SouthPanel.java
* Created: 2018
*/
public class SouthPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	

	/**
	 * 
	 * @param trackingTable The table to track for the InfoPanel
	 * @param infoArtistColumn column of the table that holds the artist (artist should be in String format)
	 * @param infoSongTitleColumn column of the table that holds the song title (title should be in String format)
	 */
	public SouthPanel(SongTable trackingTable, int infoArtistColumn, int infoSongTitleColumn) {
		super(new GridLayout(0,2));	
		
		this.add(new CCPPanel());
		this.add(new InfoPanel(trackingTable, infoArtistColumn, infoSongTitleColumn));
		//this.setBorder(BorderFactory.createLineBorder(Color.GRAY));
	}
}
