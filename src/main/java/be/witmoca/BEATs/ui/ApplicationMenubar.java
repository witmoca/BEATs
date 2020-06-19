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

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import be.witmoca.BEATs.connection.SQLConnection;
import be.witmoca.BEATs.connection.actions.ImportFileAction;
import be.witmoca.BEATs.connection.actions.LoadFileAction;
import be.witmoca.BEATs.connection.actions.SaveFileAction;
import be.witmoca.BEATs.ui.actions.*;
import be.witmoca.BEATs.ui.genremanager.GenreManagerShowAction;
import be.witmoca.BEATs.ui.playlistmanager.PlaylistManagerShowAction;
import be.witmoca.BEATs.utils.BEATsSettings;
import be.witmoca.BEATs.utils.Lang;
import be.witmoca.BEATs.utils.ResourceLoader;
import be.witmoca.BEATs.utils.UiIcon;

class ApplicationMenubar extends JMenuBar {
	private static final long serialVersionUID = 1L;

	public ApplicationMenubar() {
		// FILE MENU
		JMenu fileMenu = new JMenu(Lang.getUI("menu.file"));
		fileMenu.setMnemonic(KeyEvent.VK_F);

		JMenuItem newFile = new JMenuItem(Lang.getUI("menu.file.new"), UiIcon.NEW.getIcon());
		newFile.setMnemonic(KeyEvent.VK_N);
		newFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		newFile.addActionListener(LoadFileAction.getNewFileAction());
		fileMenu.add(newFile);

		JMenuItem openFile = new JMenuItem(Lang.getUI("menu.file.open"), UiIcon.OPEN.getIcon());
		openFile.setMnemonic(KeyEvent.VK_O);
		openFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		openFile.addActionListener(LoadFileAction.getLoadFileActionWithUI());
		fileMenu.add(openFile);

		JMenuItem saveFile = new JMenuItem(Lang.getUI("menu.file.save"), UiIcon.SAVE.getIcon());
		saveFile.setMnemonic(KeyEvent.VK_S);
		saveFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		saveFile.addActionListener(new SaveFileAction());
		fileMenu.add(saveFile);

		fileMenu.addSeparator();

		JMenuItem importFile = new JMenuItem(Lang.getUI("menu.file.import"), UiIcon.IMPORT.getIcon());
		importFile.setMnemonic(KeyEvent.VK_I);
		importFile.addActionListener(new ImportFileAction());
		fileMenu.add(importFile);

		fileMenu.addSeparator();
		JMenuItem exitApplication = new JMenuItem(Lang.getUI("menu.file.exit"), UiIcon.CLOSE_APP.getIcon());
		exitApplication.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
		exitApplication.addActionListener(new ExitApplicationAction(false));
		fileMenu.add(exitApplication);

		this.add(fileMenu);

		// TOOLS MENU
		JMenu toolsMenu = new JMenu(Lang.getUI("menu.tools"));
		toolsMenu.setMnemonic(KeyEvent.VK_T);

		JMenuItem playlistManager = new JMenuItem(Lang.getUI("menu.tools.playlistManager"), UiIcon.LIST.getIcon());
		playlistManager.setMnemonic(KeyEvent.VK_P);
		playlistManager.addActionListener(new PlaylistManagerShowAction());
		toolsMenu.add(playlistManager);

		JMenuItem genreManager = new JMenuItem(Lang.getUI("menu.tools.genreManager"), UiIcon.LIST.getIcon());
		genreManager.setMnemonic(KeyEvent.VK_G);
		genreManager.addActionListener(new GenreManagerShowAction());
		toolsMenu.add(genreManager);

		toolsMenu.addSeparator();

		JMenuItem episodeContinuityCheck = new JMenuItem(Lang.getUI("menu.tools.continuity"), UiIcon.CHECKED.getIcon());
		episodeContinuityCheck.addActionListener(new EpisodeIdContinuityCheckAction());
		toolsMenu.add(episodeContinuityCheck);

		toolsMenu.addSeparator();

		JMenuItem settings = new JMenuItem(Lang.getUI("menu.tools.settings"), UiIcon.SETTINGS.getIcon());
		settings.addActionListener(new ShowSettingsDialogAction());
		toolsMenu.add(settings);

		this.add(toolsMenu);
		
		// LIVE VIEW
		JMenu liveViewMenu = new JMenu(Lang.getUI("menu.liveview"));
		fileMenu.setMnemonic(KeyEvent.VK_L);
		
		JMenuItem serverMonitor = new JMenuItem(Lang.getUI("menu.liveview.servermonitor"));
		serverMonitor.setMnemonic(KeyEvent.VK_S);
		serverMonitor.addActionListener(new ShowLiveViewServerMonitor());
		serverMonitor.setEnabled(BEATsSettings.LIVESHARE_SERVER_ENABLED.getBoolValue());
		liveViewMenu.add(serverMonitor);
		
		this.add(liveViewMenu);

		// HELP
		JMenu helpMenu = new JMenu(Lang.getUI("menu.help"));
		helpMenu.setMnemonic(KeyEvent.VK_H);

		JMenuItem openLocal = new JMenuItem(Lang.getUI("menu.logdir"), UiIcon.FOLDER_OPEN.getIcon());
		openLocal.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
						openLocal.setEnabled(false);
						return;
					}
					Desktop.getDesktop().open(new File(ResourceLoader.LOG_DIR));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		helpMenu.add(openLocal);

		JMenuItem loadBackup = new JMenuItem(Lang.getUI("menu.loadbackup"), UiIcon.FOLDER_OPEN.getIcon());
		loadBackup.addActionListener(LoadFileAction.getLoadFileActionWithUI(new File(ResourceLoader.BACKUP_DIR)));
		helpMenu.add(loadBackup);

		JMenuItem refreshScreen = new JMenuItem(Lang.getUI("menu.refresh"), UiIcon.SCREEN.getIcon());
		refreshScreen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SQLConnection.getDbConn().announceDataRefresh(); // Notify all listeners that the data is 'changed' =>
																	// reloads said data
			}
		});
		helpMenu.add(refreshScreen);

		helpMenu.addSeparator();

		JMenuItem checkUpdates = new JMenuItem(Lang.getUI("menu.update"), UiIcon.SEARCH.getIcon());
		checkUpdates.addActionListener(new ShowUpdateCheckDialogAction());
		helpMenu.add(checkUpdates);
		
		JMenuItem about = new JMenuItem(Lang.getUI("menu.about"), UiIcon.INFO.getIcon());
		about.addActionListener(new ShowAboutDialogAction());
		helpMenu.add(about);

		this.add(helpMenu);
	}

}
