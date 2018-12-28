/**
 * 
 */
package be.witmoca.BEATs.connection.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import be.witmoca.BEATs.FileFilters.BEATsFileFilter;
import be.witmoca.BEATs.connection.ConnectionException;
import be.witmoca.BEATs.connection.SQLConnection;
import be.witmoca.BEATs.ui.ApplicationWindow;

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
* File: LoadFileAction.java
* Created: 2018
*/
public class LoadFileAction implements ActionListener {
	private final boolean showUi;
	private final File loadFile;

	private LoadFileAction(boolean showUi, File loadFile) {
		this.showUi = showUi;
		this.loadFile = loadFile;
	}

	public static ActionListener getNewFileAction() {
		return new LoadFileAction(false, null);
	}

	public static ActionListener getLoadFileActionWithUI() {
		return new LoadFileAction(true, null);
	}

	public static final ActionListener getLoadFileAction(File loadFile) {
		return new LoadFileAction(false, loadFile);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (!SwingUtilities.isEventDispatchThread())
			throw new RuntimeException("Cannot be called from outside the EDT!");

		// Check if data has changed
		CheckFileSavedAction check = new CheckFileSavedAction();
		check.actionPerformed(e);
		// check if action is cancelled or allowed to continue
		if (!check.hasSucceeded())
			return;

		File loadFile = this.loadFile;
		// Load new Database Connection
		if (showUi) {
			final JFileChooser fc = new JFileChooser();
			fc.setAcceptAllFileFilterUsed(false);
			fc.setFileFilter(new BEATsFileFilter());
			fc.setCurrentDirectory(SQLConnection.getDbConn().getCurrentFile());
			if (fc.showOpenDialog(ApplicationWindow.getAPP_WINDOW()) == JFileChooser.APPROVE_OPTION) {
				loadFile = fc.getSelectedFile();
			} else {
				return;
			}
		}

		try {
			SQLConnection.loadNewInternalDb(loadFile);
		} catch (ConnectionException e1) {
			e1.printStackTrace();
			String errorMessage = "";
			if(SQLConnection.isRecoveredDb()) {
				errorMessage += "Database recovered!\nBurning Ember detected an unusual shutdown.\nAn error occurred while recovering database.\n\n";
			}
			
			switch (e1.getState()) {
			case APP_ID_INVALID:
				errorMessage += "This file does not appear to be a valid file (Application Id is invalid).";
				break;
			case APP_OUTDATED:
				errorMessage += "This file was made with a newer version of Burning Ember.\nPlease update to a newer version.";
				break;
			case DB_ALREADY_LOCKED:
				errorMessage += "Another instance is already running.\nOnly one instance is allowed.";
				break;
			case DB_OUTDATED:
				errorMessage += "This file was made with an older version of Burning Ember.\nPlease try to import instead.";
				break;
			case FOREIGN_KEYS_CONSTRAINTS:
				errorMessage += "The loaded database contains errors.\n\n" + e1.getCause().getLocalizedMessage();
				break;
			case GENERAL_EXCEPTION:
			case INTEGRITY_FAILED:
				errorMessage += "An error occurred while loading.\n\n" + e1.getCause().getLocalizedMessage();
				break;
			case VACUUM_FAILED:
				errorMessage += "An error occurred during VACUUM.\n\n" + e1.getCause().getLocalizedMessage();
				break;
			}
			JOptionPane.showMessageDialog(ApplicationWindow.getAPP_WINDOW(), errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
			// Designated unrecoverable error
			System.exit(-1);
		}

		if (SQLConnection.isRecoveredDb()) {
			JOptionPane.showMessageDialog(ApplicationWindow.getAPP_WINDOW(),
					"Database recovered!\nBurning Ember detected an unusual shutdown.\nThe previously opened database has been recovered.",
					"Recovered", JOptionPane.WARNING_MESSAGE);
		}
	}
}
