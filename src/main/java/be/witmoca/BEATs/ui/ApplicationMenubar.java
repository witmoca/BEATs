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
* File: ApplicationMenubar.java
* Created: 2018
*/
package be.witmoca.BEATs.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import be.witmoca.BEATs.connection.SQLConnection;
import be.witmoca.BEATs.connection.actions.ImportFileAction;
import be.witmoca.BEATs.connection.actions.LoadFileAction;
import be.witmoca.BEATs.connection.actions.SaveFileAction;
import be.witmoca.BEATs.ui.actions.*;
import be.witmoca.BEATs.ui.playlistmanager.PlaylistManagerShowAction;
import be.witmoca.BEATs.utils.UiIcon;

class ApplicationMenubar extends JMenuBar {
	private static final long serialVersionUID = 1L;

	public ApplicationMenubar() {
		// FILE MENU
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);

		JMenuItem newFile = new JMenuItem("New", UiIcon.NEW.getIcon());
		newFile.setMnemonic(KeyEvent.VK_N);
		newFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		newFile.addActionListener(LoadFileAction.getNewFileAction());
		fileMenu.add(newFile);

		JMenuItem openFile = new JMenuItem("Open", UiIcon.OPEN.getIcon());
		openFile.setMnemonic(KeyEvent.VK_O);
		openFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		openFile.addActionListener(LoadFileAction.getLoadFileActionWithUI());
		fileMenu.add(openFile);

		JMenuItem saveFile = new JMenuItem("Save", UiIcon.SAVE.getIcon());
		saveFile.setMnemonic(KeyEvent.VK_S);
		saveFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		saveFile.addActionListener(new SaveFileAction());
		fileMenu.add(saveFile);

		fileMenu.addSeparator();

		JMenuItem importFile = new JMenuItem("Import", UiIcon.IMPORT.getIcon());
		importFile.setMnemonic(KeyEvent.VK_I);
		importFile.addActionListener(new ImportFileAction());
		fileMenu.add(importFile);

		fileMenu.addSeparator();
		JMenuItem exitApplication = new JMenuItem("Exit", UiIcon.CLOSE_APP.getIcon());
		exitApplication.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
		exitApplication.addActionListener(new ExitApplicationAction());
		fileMenu.add(exitApplication);

		this.add(fileMenu);
		
		// TOOLS MENU
		JMenu toolsMenu = new JMenu("Tools");
		toolsMenu.setMnemonic(KeyEvent.VK_T);
		
		JMenuItem playlistManager = new JMenuItem("Playlist Manager", UiIcon.PLAYLISTS.getIcon());
		playlistManager.setMnemonic(KeyEvent.VK_P);
		playlistManager.addActionListener(new PlaylistManagerShowAction());
		toolsMenu.add(playlistManager);
		
		toolsMenu.addSeparator();
		
		JMenuItem refreshScreen = new JMenuItem("Refresh Screen", UiIcon.SCREEN.getIcon());
		refreshScreen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SQLConnection.getDbConn().announceDataRefresh(); // Notify all listeners that the data is 'changed' => reloads said data
			}
		});
		toolsMenu.add(refreshScreen);
		
		JMenuItem episodeContinuityCheck = new JMenuItem("Episode Continuity Check", UiIcon.CHECKED.getIcon());
		episodeContinuityCheck.addActionListener(new EpisodeIdContinuityCheckAction());
		toolsMenu.add(episodeContinuityCheck);
		
		this.add(toolsMenu);
		
		// HELP
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic(KeyEvent.VK_H);
		
		JMenuItem about = new JMenuItem("About", UiIcon.INFO.getIcon());
		about.addActionListener(new ShowAboutDialogAction());
		helpMenu.add(about);
		
		this.add(helpMenu);
	}

}
