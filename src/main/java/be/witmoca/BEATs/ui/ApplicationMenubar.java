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

public class ApplicationMenubar extends JMenuBar {
	private static final long serialVersionUID = 1L;

	public ApplicationMenubar() {
		// FILE MENU
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		
		JMenuItem newFile = new JMenuItem("New", new ImageIcon(getClass().getClassLoader().getResource("fileIcons/file.png")));
		newFile.setMnemonic(KeyEvent.VK_N);
		newFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		newFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("New!");
			}
		});
		fileMenu.add(newFile);
		
		JMenuItem openFile = new JMenuItem("Open", new ImageIcon(getClass().getClassLoader().getResource("fileIcons/folder.png")));
		openFile.setMnemonic(KeyEvent.VK_O);
		openFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		openFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Open!");
			}
		});
		fileMenu.add(openFile);
		
		JMenuItem saveFile = new JMenuItem("Save");
		saveFile.setMnemonic(KeyEvent.VK_S);
		saveFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		saveFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Save!");
			}
		});
		fileMenu.add(saveFile);
		
		fileMenu.addSeparator();
		
		JMenuItem importFile = new JMenuItem("Import");
		importFile.setMnemonic(KeyEvent.VK_I);
		importFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Import!");
			}
		});
		fileMenu.add(importFile);
		
		JMenuItem exportFile = new JMenuItem("Export");
		exportFile.setMnemonic(KeyEvent.VK_E);
		exportFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Export!");
			}
		});
		fileMenu.add(exportFile);
		
		
		this.add(fileMenu);
	}

}
