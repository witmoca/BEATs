/**
 * 
 */
package be.witmoca.BEATs.ui.eastpanel.ccp;

import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import be.witmoca.BEATs.clipboard.ClipboardTransferHandler;
import be.witmoca.BEATs.utils.Lang;

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
* File: CCPPanel.java
* Created: 2018
*/

/**
 * UI representation of the Cut/Copy/Paste container for songs
 */
public class CCPPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	public CCPPanel() {
		super(new BorderLayout());

		final JButton title = new JButton(Lang.getUI("ccp.titleLabel"));
		title.setFont(title.getFont().deriveFont(22F));
		title.setEnabled(false);
		add(title, BorderLayout.NORTH);

		JList<String> ccpList = new JList<>(new CCPListModel());
		ccpList.getSelectionModel().addListSelectionListener(new CCPUpdater(ccpList));
		ccpList.setVisibleRowCount(10);
		ccpList.setSelectedIndex(0);

		final JScrollPane sPane = new JScrollPane(ccpList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		sPane.setPreferredSize(ccpList.getPreferredScrollableViewportSize());
		add(sPane, BorderLayout.CENTER);
	}

	private static class CCPUpdater implements ListSelectionListener {
		private final JList<?> list;

		public CCPUpdater(JList<?> list) {
			this.list = list;
			ClipboardTransferHandler.addListSelectionListener(this);
		}

		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (ClipboardTransferHandler.class.equals(e.getSource()) && list.getSelectedIndex() != e.getFirstIndex()) {
				// if transferhandler is source => update the list (only if it is different,
				// otherwise this is endlessly recursive)
				list.setSelectedIndex(e.getFirstIndex());
			} else if(!ClipboardTransferHandler.class.equals(e.getSource())) {
				// if list is source => update the transferhandler
				ClipboardTransferHandler.setSelected(list.getSelectedIndex());
			}
		}
	}
}
