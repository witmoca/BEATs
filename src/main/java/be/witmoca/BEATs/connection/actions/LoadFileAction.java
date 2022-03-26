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

import be.witmoca.BEATs.connection.ConnectionException;
import be.witmoca.BEATs.connection.SQLConnection;
import be.witmoca.BEATs.filefilters.BEATsFileFilter;
import be.witmoca.BEATs.ui.ApplicationWindow;
import be.witmoca.BEATs.ui.actions.ExitApplicationAction;
import be.witmoca.BEATs.utils.BEATsSettings;
import be.witmoca.BEATs.utils.Lang;

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

	public static ActionListener getLoadFileActionWithUI(File suggestLocation) {
		return new LoadFileAction(true, suggestLocation);
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
			if (this.loadFile == null) {
				// If no specific file/folder given, use the current opened file/dir
				File currentFile = SQLConnection.getDbConn().getCurrentFile();
				if(currentFile != null) {
					fc.setCurrentDirectory(currentFile);
				} else {
					// No current directory? Is there a saved setting from the last time BEATs was open?
					String lastPath = BEATsSettings.LAST_FILE_PATH.getStringValue();
					if(lastPath != null) {
						fc.setCurrentDirectory(new File(lastPath));
					} else {
						fc.setCurrentDirectory(null);
					}
				}
			}
			else
				fc.setCurrentDirectory(this.loadFile);
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
			boolean isRecoveredDb = SQLConnection.isRecoveredDb();
			if (isRecoveredDb) {
				errorMessage += "Database recovered!\nBurning Ember detected an unusual shutdown.\nAn error occurred while recovering database.\n\n";
			}

			// end state flags
			boolean quitApplication = false; // deletes the local db file as well
			boolean crashApplication = false;
			
			switch (e1.getState()) {
			case APP_ID_INVALID:
				errorMessage += "This file does not appear to be a valid file (Application Id is invalid).";
				quitApplication = true;
				break;
			case APP_OUTDATED:
				errorMessage += "This file was made with a newer version of Burning Ember.\nPlease update to a newer version.";
				quitApplication = true;
				break;
			case DB_ALREADY_LOCKED:
				errorMessage += "Another instance is already running.\nOnly one instance is allowed.";
				quitApplication = true;
				break;
			case DB_MAJOR_OUTDATED:
				errorMessage += "This file was made with an older version of Burning Ember.\nPlease try to import instead.";
				quitApplication = true;
				break;
			case FOREIGN_KEYS_CONSTRAINTS:
				errorMessage += "The loaded database contains errors.\n\n" + e1.getCause().getLocalizedMessage();
				quitApplication = false;
				crashApplication = true;
				break;
			case GENERAL_EXCEPTION:
			case INTEGRITY_FAILED:
				errorMessage += "An error occurred while loading.\n\n" + e1.getCause().getLocalizedMessage();
				quitApplication = false;
				crashApplication = true;
				break;
			case VACUUM_FAILED:
				errorMessage += "An error occurred during VACUUM.\n\n" + e1.getCause().getLocalizedMessage();
				quitApplication = true;
				break;
			default:
				break;
			}
			
			// if the database was recovered, and closeApplication is true -> convert into crash (and don't delete local DB file)
			if (quitApplication && isRecoveredDb) {
				quitApplication = false;
				crashApplication = true;
			}
			
			JOptionPane.showMessageDialog(ApplicationWindow.getAPP_WINDOW(), errorMessage, "Error",
					JOptionPane.ERROR_MESSAGE);


			// handle flags as previously set
			if (crashApplication) {
				System.exit(-1);
			} else if (quitApplication) {
				(new ExitApplicationAction(false))
				.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, errorMessage));
			}
		}

		if (SQLConnection.isRecoveredDb()) {
			JOptionPane.showMessageDialog(ApplicationWindow.getAPP_WINDOW(), Lang.getUI("loadFileAction.recoveredMsg"),
					Lang.getUI("loadFileAction.recovered"), JOptionPane.WARNING_MESSAGE);
		} else {
			if(loadFile != null) {
				BEATsSettings.LAST_FILE_PATH.setStringValue(loadFile.getAbsolutePath());
				BEATsSettings.savePreferences();
			}
		}
	}
}
