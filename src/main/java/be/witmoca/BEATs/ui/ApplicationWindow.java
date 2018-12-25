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
* File: ApplicationWindow.java
* Created: 2018
*/
package be.witmoca.BEATs.ui;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import be.witmoca.BEATs.actions.ExitApplicationAction;
import be.witmoca.BEATs.ui.currentqueue.CurrentQueuePanel;

public class ApplicationWindow extends JFrame implements WindowListener{
	private static final long serialVersionUID = 1L;
	private static final String mainTitleBase = "Burning Ember";
	
	private final JComponent eastPanel = new CurrentQueuePanel();
	private final JComponent centerPanel = new CenterTabbedPane();

	public ApplicationWindow() {
		// Initialise frame
		super(mainTitleBase);
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.setIconImages();
		this.addWindowListener(this);
		this.setLayout(new BorderLayout());
		
		// Components
		this.add(eastPanel, BorderLayout.EAST);
		this.add(centerPanel, BorderLayout.CENTER);
		
		// Menu
		this.setJMenuBar(new ApplicationMenubar());
		
		// Draw frame
		this.setExtendedState(MAXIMIZED_BOTH);
		this.pack();
		this.setVisible(true);
	}

	private void setIconImages() {
		List<Image> icons = new ArrayList<Image>();
		icons.add(UiIcon.LOGO_256.getIcon().getImage());
		icons.add(UiIcon.LOGO_128.getIcon().getImage());
		icons.add(UiIcon.LOGO_64.getIcon().getImage());
		icons.add(UiIcon.LOGO_48.getIcon().getImage());
		icons.add(UiIcon.LOGO_32.getIcon().getImage());
		icons.add(UiIcon.LOGO_16.getIcon().getImage());
		this.setIconImages(icons);
	}
	
	@Override
	public void windowOpened(WindowEvent e) {	
	}

	@Override
	public void windowClosing(WindowEvent e) {
		(new ExitApplicationAction()).actionPerformed(new ActionEvent(e.getSource(), ActionEvent.ACTION_PERFORMED, e.paramString()));
	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}
}