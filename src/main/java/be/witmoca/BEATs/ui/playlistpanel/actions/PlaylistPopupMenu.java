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
* File: PlaylistPopupMenu.java
* Created: 2018
*/
package be.witmoca.BEATs.ui.playlistpanel.actions;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

import be.witmoca.BEATs.clipboard.ClipboardActionFactory;


public class PlaylistPopupMenu extends JPopupMenu {
	private static final long serialVersionUID = 1L;
	
	public PlaylistPopupMenu(JTable assocTable) {
		super();
		this.add(new JMenuItem(ClipboardActionFactory.getCutAction(assocTable)));
		this.add(new JMenuItem(ClipboardActionFactory.getCopyAction(assocTable)));
		this.add(new JMenuItem(ClipboardActionFactory.getPasteAction(assocTable)));
		this.addSeparator();
		this.add(new JMenuItem(new DeleteAction(assocTable)));
	}
}
