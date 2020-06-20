/**
 * 
 */
package be.witmoca.BEATs.ui.discovery;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.Timer;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import be.witmoca.BEATs.discovery.DiscoveryListEntry;
import be.witmoca.BEATs.discovery.DiscoveryServer;
import be.witmoca.BEATs.ui.ApplicationWindow;
import be.witmoca.BEATs.utils.Lang;

/*
*
+===============================================================================+
|    BEATs (Burning Ember Archival Tool suite)                                  |
|    Copyright 2020 Jente Heremans                                              |
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
* File: AddConnectionAction.java
* Created: 2020
*/
public class AddConnectionAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
	private static final int LIST_UPDATE_DELAY_MS = 100;
	private final CCListModel ccl;
	
	public AddConnectionAction(CCListModel ccl) {
		super("+");
		this.ccl = ccl;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		DiscoveryListModel dlm = new DiscoveryListModel();
		JList<String> discoveryList = new JList<String>(dlm);
		
		Timer REFRESH_TIMER = new Timer(LIST_UPDATE_DELAY_MS, new UpdateAction(dlm));
		
		// start broadcasting
		DiscoveryServer.startBroadcaster();
		// start gui updating
		REFRESH_TIMER.start();
		
		String options[] = {Lang.getUI("action.ok"), Lang.getUI("action.cancel")};
		int result = JOptionPane.showOptionDialog(ApplicationWindow.getAPP_WINDOW(), new JScrollPane(discoveryList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),
				Lang.getUI("menu.liveshare.clientconnections"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[1]);	
		
		// stop gui updating
		REFRESH_TIMER.stop();
		// stop broadcasting
		DiscoveryServer.stopBroadcaster();
		
		if(result == 0) {
			// save selected item in dlm
			
			ccl.UpdateContent();
		}
	}
	
	private static class DiscoveryListModel implements ListModel<String>{
		private final List<DiscoveryListEntry> content = new ArrayList<DiscoveryListEntry>();
		private final List<ListDataListener> ldl = new ArrayList<ListDataListener>();
		
		@Override
		public int getSize() {
			return content.size();
		}

		@Override
		public String getElementAt(int index) {
			DiscoveryListEntry d = content.get(index);
			return d.getHostname() + " " + d.getIp() + " " + d.getPort();
		}

		@Override
		public void addListDataListener(ListDataListener l) {
			ldl.add(l);
		}

		@Override
		public void removeListDataListener(ListDataListener l) {
			ldl.remove(l);
		}
		
		private void fireContentChanged() {
			for(ListDataListener l : ldl) {
				l.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, content.size()));
			}
		}
		
		private void setContent(List<DiscoveryListEntry> newcontent) {
			content.clear();
			content.addAll(newcontent);
			fireContentChanged();			
		}
	}
	
	/**
	 * 
	*
	+===============================================================================+
	|    BEATs (Burning Ember Archival Tool suite)                                  |
	|    Copyright 2020 Jente Heremans                                              |
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
	* Action responsible for updating the list
	*
	* File: AddConnectionAction.java
	* Created: 2020
	 */
	private static class UpdateAction implements ActionListener {
		private final DiscoveryListModel dlm;
		
		private UpdateAction(DiscoveryListModel dlm) {
			this.dlm = dlm;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			this.dlm.setContent(DiscoveryServer.getDiscovered());
		}
	}
}
