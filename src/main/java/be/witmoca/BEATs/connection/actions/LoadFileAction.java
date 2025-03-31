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
	private final boolean skipSanity;

	private LoadFileAction(boolean showUi, File loadFile, boolean skipSanity) {
		this.showUi = showUi;
		this.loadFile = loadFile;
		this.skipSanity = skipSanity;
	}

	public static ActionListener getNewFileAction() {
		return new LoadFileAction(false, null, false);
	}

	public static ActionListener getLoadFileActionWithUI() {
		return new LoadFileAction(true, null, false);
	}

	public static ActionListener getLoadFileActionWithUI(File suggestLocation) {
		return new LoadFileAction(true, suggestLocation, false);
	}

	public static final ActionListener getLoadFileAction(File loadFile) {
		return new LoadFileAction(false, loadFile, false);
	}
	
	public static final ActionListener getLoadWithoutSanity(File loadFile) {
		return new LoadFileAction(false, loadFile, true);
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
			SQLConnection.loadNewInternalDb(loadFile, this.skipSanity);
		} catch (ConnectionException e1) {
			String errorMessage = "";
			boolean isRecoveredDb = SQLConnection.isRecoveredDb();
			if (isRecoveredDb) {
				errorMessage += "Database recovered!\nBurning Ember detected an unusual shutdown.\nAn error occurred while recovering database.\n\n";
			}
			
			// Flag to set if an error would NOT inhibit an empty file from opening instead
			boolean loadEmptyFile = false;
			
			switch (e1.getState()) {
			case APP_ID_INVALID:
				errorMessage += "This file does not appear to be a valid file (Application Id is invalid).";
				loadEmptyFile = true;
				break;
			case APP_OUTDATED:
				errorMessage += "This file was made with a newer version of Burning Ember.\nPlease update to a newer version.";
				loadEmptyFile = true;
				break;
			case DB_ALREADY_LOCKED:
				errorMessage += "Another instance is already running.\nOnly one instance is allowed.";
				break;
			case DB_MAJOR_OUTDATED:
				JOptionPane.showMessageDialog(ApplicationWindow.getAPP_WINDOW(), Lang.getUI("loadFileAction.importOlderVersion"), "Update",
						JOptionPane.INFORMATION_MESSAGE);
				try {
					(new BEATsFileFilter()).importFile(loadFile);
				} catch (Exception e2) {
					e2.printStackTrace();
					JOptionPane.showMessageDialog(ApplicationWindow.getAPP_WINDOW(), e2.getLocalizedMessage(), "Error",
							JOptionPane.ERROR_MESSAGE);
				}
				return;
			case FOREIGN_KEYS_CONSTRAINTS:
				errorMessage += "The loaded database contains errors.\n\n" + e1.getCause().getLocalizedMessage();
				loadEmptyFile = true;
				break;
			case GENERAL_EXCEPTION:
			case INTEGRITY_FAILED:
				errorMessage += "An error occurred while loading.\n\n" + e1.getCause().getLocalizedMessage();
				break;
			case VACUUM_FAILED:
				errorMessage += "An error occurred during VACUUM.\n\n" + e1.getCause().getLocalizedMessage();
				loadEmptyFile = true;
				break;
			default:
				break;
			}
			
			JOptionPane.showMessageDialog(ApplicationWindow.getAPP_WINDOW(), errorMessage, "Error",
						JOptionPane.ERROR_MESSAGE);
			e1.printStackTrace();
			
			// If flag is set, load empty file. If not, crash the application due to critical error
			if(loadEmptyFile) {
				getNewFileAction().actionPerformed(e);
			} else {
				System.exit(-1);
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
