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
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import be.witmoca.BEATs.actions.ExitApplicationAction;
import be.witmoca.BEATs.actions.ImportFileAction;
import be.witmoca.BEATs.actions.NewFileAction;
import be.witmoca.BEATs.actions.OpenFileAction;
import be.witmoca.BEATs.actions.SaveFileAction;

public class ApplicationMenubar extends JMenuBar {
	private static final long serialVersionUID = 1L;

	public ApplicationMenubar() {
		// FILE MENU
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);

		JMenuItem newFile = new JMenuItem("New",UIManager.getIcon("FileView.fileIcon"));
		newFile.setMnemonic(KeyEvent.VK_N);
		newFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		newFile.addActionListener(new NewFileAction());
		fileMenu.add(newFile);

		JMenuItem openFile = new JMenuItem("Open",UIManager.getIcon("FileView.directoryIcon"));
		openFile.setMnemonic(KeyEvent.VK_O);
		openFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		openFile.addActionListener(new OpenFileAction());
		fileMenu.add(openFile);

		JMenuItem saveFile = new JMenuItem("Save", UIManager.getIcon("FileView.floppyDriveIcon"));
		saveFile.setMnemonic(KeyEvent.VK_S);
		saveFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		saveFile.addActionListener(new SaveFileAction());
		fileMenu.add(saveFile);

		fileMenu.addSeparator();

		JMenuItem importFile = new JMenuItem("Import",UIManager.getIcon("Table.ascendingSortIcon"));
		importFile.setMnemonic(KeyEvent.VK_I);
		importFile.addActionListener(new ImportFileAction());
		fileMenu.add(importFile);

		JMenuItem exportFile = new JMenuItem("Export",UIManager.getIcon("Table.descendingSortIcon"));
		exportFile.setMnemonic(KeyEvent.VK_E);
		exportFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Export!");
			}
		});
		fileMenu.add(exportFile);

		fileMenu.addSeparator();
		JMenuItem exitApplication = new JMenuItem("Exit",UIManager.getIcon("InternalFrame.closeIcon"));
		saveFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
		exitApplication.addActionListener(new ExitApplicationAction());
		fileMenu.add(exitApplication);

		this.add(fileMenu);
	}

}