/**
 * 
 */
package be.witmoca.BEATs.ui.actions;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import be.witmoca.BEATs.ui.ApplicationWindow;
import be.witmoca.BEATs.ui.actions.clientconnections.AddConnectionAction;
import be.witmoca.BEATs.ui.actions.clientconnections.CCListModel;
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
* File: ShowLiveServerClientConnections.java
* Created: 2020
*/
public class ShowLiveShareClientConnections implements ActionListener {
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		JPanel content = new JPanel(new BorderLayout(10, 10));
		
		JList<String> CCList = new JList<String>(new CCListModel());
		content.add(new JScrollPane(CCList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),
				BorderLayout.CENTER);
		
		// button panel
		JPanel buttonPanel = new JPanel(new GridLayout(0, 1));
		buttonPanel.setBorder(BorderFactory.createEtchedBorder());
		buttonPanel.add(wrapAction(new AddConnectionAction()));
		
		content.add(buttonPanel, BorderLayout.EAST);
		
		JOptionPane.showMessageDialog(ApplicationWindow.getAPP_WINDOW(),
				content, Lang.getUI("menu.liveshare.clientconnections"), JOptionPane.PLAIN_MESSAGE);
	}

	private JComponent wrapAction(Action a) {
		JPanel p = new JPanel();
		JButton b = new JButton(a);
		b.setPreferredSize(new Dimension(60, 25));
		p.add(b);
		return p;
	}
}
