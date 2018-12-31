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
import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import be.witmoca.BEATs.connection.DataChangedListener;
import be.witmoca.BEATs.connection.DataChangedType;
import be.witmoca.BEATs.connection.SQLConnection;
import be.witmoca.BEATs.ui.actions.ExitApplicationAction;
import be.witmoca.BEATs.ui.currentqueue.CurrentQueuePanel;
import be.witmoca.BEATs.utils.Lang;
import be.witmoca.BEATs.utils.UiIcon;

public class ApplicationWindow extends JFrame implements WindowListener, DataChangedListener{
	private static final long serialVersionUID = 1L;
	
	public static ApplicationWindow APP_WINDOW = null;
	
	private final JComponent eastPanel = new CurrentQueuePanel();
	private final JComponent centerPanel = new CenterTabbedPane();

	public static void createAndShowUi() {
		APP_WINDOW = new ApplicationWindow();
	}
	
	private ApplicationWindow() {
		// Initialise frame
		super(Lang.getUI("shortname"));
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.setIconImages();
		this.addWindowListener(this);
		this.setLayout(new BorderLayout());
		
		// Components
		this.add(eastPanel, BorderLayout.EAST);
		this.add(centerPanel, BorderLayout.CENTER);
		NotificationBar notify = new NotificationBar();
		if(notify.holdsNotifications())
			this.add(notify, BorderLayout.SOUTH);
		
		// Menu
		this.setJMenuBar(new ApplicationMenubar());
		
		// Draw frame
		this.setExtendedState(MAXIMIZED_BOTH);
		this.pack();
		this.setVisible(true);
		SQLConnection.getDbConn().addDataChangedListener(this, EnumSet.of(DataChangedType.META_DATA));
		tableChanged();
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

	public static ApplicationWindow getAPP_WINDOW() {
		return APP_WINDOW;
	}

	@Override
	public void tableChanged() {
		// META_DATA has changed => reload title
		String title = Lang.getUI("shortname");
		
		File currentFile = SQLConnection.getDbConn().getCurrentFile();
		if(currentFile != null)
			title = currentFile.getAbsolutePath() + " - " + title;
		
		this.setTitle(title);
	}
}