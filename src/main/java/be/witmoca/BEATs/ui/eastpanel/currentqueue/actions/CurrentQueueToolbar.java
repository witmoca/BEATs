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
* File: CurrentQueueToolbar.java
* Created: 2018
*/
package be.witmoca.BEATs.ui.eastpanel.currentqueue.actions;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JToolBar;

public class CurrentQueueToolbar extends JToolBar {
	private static final long serialVersionUID = 1L;
	private final JList<String> queue;

	public CurrentQueueToolbar(JList<String> Queue) {
		super(JToolBar.HORIZONTAL);
		this.setFloatable(false);
		queue = Queue;

		this.add(new JButton(new RevertToPlaylistFromQueueAction(queue)));
		this.add(new ShowInfoAction(queue));
		this.add(new JButton(new ArchiveAction(queue)));
	}
}
