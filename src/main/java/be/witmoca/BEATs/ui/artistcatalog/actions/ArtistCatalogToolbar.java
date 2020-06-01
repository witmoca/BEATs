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
* File: PlaylistToolbar.java
* Created: 2018
*/
package be.witmoca.BEATs.ui.artistcatalog.actions;

import javax.swing.Box;
import javax.swing.JTable;
import javax.swing.JToolBar;

import be.witmoca.BEATs.clipboard.ClipboardActionFactory;

public class ArtistCatalogToolbar extends JToolBar {
	private static final long serialVersionUID = 1L;

	public ArtistCatalogToolbar(JTable table) {
		super(JToolBar.HORIZONTAL);

		this.setFloatable(false);
		this.add(ClipboardActionFactory.getCopyAction(table));

		// Beyond this point all goes on the right
		add(Box.createHorizontalGlue());
	}
}
