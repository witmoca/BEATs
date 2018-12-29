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
* File: PlaylistManagerPanel.java
* Created: 2018
*/
package be.witmoca.BEATs.ui.playlistmanager;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import be.witmoca.BEATs.utils.Lang;

class PlaylistManagerPanel extends JPanel{
	private static final long serialVersionUID = 1L;
	private static final String PlaylistManagerDescription = Lang.getUI("playlistManager.descr");
	private final ReorderingListModel listModel;
	
	public PlaylistManagerPanel() {
		super(new BorderLayout(10,10));
		
		this.add(new JLabel(PlaylistManagerDescription), BorderLayout.NORTH);
		
		ReorderingList playlistOrderList = new ReorderingList();
		this.add(new JScrollPane(playlistOrderList,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
		listModel = playlistOrderList.getListModel();
		
		JPanel bPanel = new JPanel();
		JButton newPlaylist = new JButton(Lang.getUI("playlistManager.new"));
		newPlaylist.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String name = JOptionPane.showInputDialog((Component) e.getSource(), Lang.getUI("playlistManager.new.dialog") + ": ", Lang.getUI("playlistManager.new.dialogTitle"), JOptionPane.PLAIN_MESSAGE);
				playlistOrderList.getListModel().addElement(name);
			}
		});
		bPanel.add(newPlaylist);
		
		JButton delPlaylist = new JButton(Lang.getUI("playlistManager.delete"));
		delPlaylist.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				playlistOrderList.getListModel().removeElement(playlistOrderList.getSelectedIndex());
			}
		});
		bPanel.add(delPlaylist);
		
		this.add(bPanel, BorderLayout.SOUTH);
	}

	protected ReorderingListModel getListModel() {
		return listModel;
	}
	
}
