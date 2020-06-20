/**
 * 
 */
package be.witmoca.BEATs.ui.discovery;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JList;

import be.witmoca.BEATs.utils.BEATsSettings;
import be.witmoca.BEATs.utils.UiIcon;

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
* File: DeleteConnectionAction.java
* Created: 2020
*/
public class DeleteConnectionAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
	private final JList<String> model;
	
	DeleteConnectionAction(JList<String> model){
		super(null, UiIcon.DELETE.getIcon());
		this.model = model;
	}
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (!(e.getSource() instanceof Component) || model.getSelectedIndex() == -1 || model.getModel().getSize() == 0)
			return;
		
		List<String> hosts = new ArrayList<String>(BEATsSettings.LIVESHARE_CLIENT_HOSTLIST.getListValue());
		hosts.remove(model.getSelectedValue());
		BEATsSettings.LIVESHARE_CLIENT_HOSTLIST.setListValue(hosts);
		BEATsSettings.savePreferences();
		
		((CCListModel) model.getModel()).UpdateContent();
	}

}
