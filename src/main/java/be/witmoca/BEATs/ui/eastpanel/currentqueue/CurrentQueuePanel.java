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
* File: CurrentQueuePanel.java
* Created: 2018
*/
package be.witmoca.BEATs.ui.eastpanel.currentqueue;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import be.witmoca.BEATs.ui.eastpanel.currentqueue.actions.CurrentQueuePopupMenu;
import be.witmoca.BEATs.ui.eastpanel.currentqueue.actions.CurrentQueueToolbar;
import be.witmoca.BEATs.utils.Lang;

public class CurrentQueuePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private final JList<String> Queue = new CurrentQueueList(new CurrentQueueListModel());

	public CurrentQueuePanel() {
		super(new BorderLayout());

		final JButton title = new JButton(Lang.getUI("queue.titleLabel"));
		title.setFont(title.getFont().deriveFont(22F));
		title.setEnabled(false);
		add(title, BorderLayout.NORTH);

		final JScrollPane sPane = new JScrollPane(Queue, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		sPane.setPreferredSize(new Dimension(0, Integer.MAX_VALUE));
		add(sPane, BorderLayout.CENTER);

		// Update the view if the contents should become to big to display
		Queue.getModel().addListDataListener(new ListDataListener() {
			@Override
			public void intervalAdded(ListDataEvent e) {
				revalidate();
			}

			@Override
			public void intervalRemoved(ListDataEvent e) {
				revalidate();
			}

			@Override
			public void contentsChanged(ListDataEvent e) {
				revalidate();
			}
		});

		// Add the popupmenu
		Queue.setComponentPopupMenu(new CurrentQueuePopupMenu(Queue));

		// Add the toolbar
		add(new CurrentQueueToolbar(Queue), BorderLayout.SOUTH);
	}
}
