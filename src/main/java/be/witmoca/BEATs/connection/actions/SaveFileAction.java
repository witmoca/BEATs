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
* File: SaveFileAction.java
* Created: 2018
*/
package be.witmoca.BEATs.connection.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.SQLException;
import java.util.EnumSet;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import be.witmoca.BEATs.connection.DataChangedType;
import be.witmoca.BEATs.connection.SQLConnection;
import be.witmoca.BEATs.filefilters.BEATsFileFilter;
import be.witmoca.BEATs.ui.ApplicationWindow;
import be.witmoca.BEATs.utils.BEATsSettings;
import be.witmoca.BEATs.utils.Lang;

public class SaveFileAction implements ActionListener {
	private boolean hasSucceeded = false;

	public SaveFileAction() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		final JFileChooser fc = new JFileChooser() {
			private static final long serialVersionUID = 1L;

			@Override
			public void approveSelection() {
				// if file already exists => show confirm dialog
				if (getSelectedFile().exists() && JOptionPane.showConfirmDialog(this,
						Lang.getUI("savedFileAction.overwrite"), Lang.getUI("savedFileAction.overwriteTitle"),
						JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
					return;
				}
				super.approveSelection();
			}
		};

		fc.setAcceptAllFileFilterUsed(false);
		fc.setFileFilter(new BEATsFileFilter());
		
		// Choose default directory based on currentFile or LastFilePath
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

		if (fc.showSaveDialog(ApplicationWindow.getAPP_WINDOW()) == JFileChooser.APPROVE_OPTION) {
			String pathToFile = fc.getSelectedFile().getAbsolutePath();
			// Only 1 ".beats" extension!
			while (pathToFile.endsWith(".beats")) {
				pathToFile = pathToFile.substring(0, pathToFile.length() - 6);
			}
			pathToFile += ".beats";
			try {
				SQLConnection.getDbConn().commit(EnumSet.noneOf(DataChangedType.class)); // An extra commit to catch any (by accident) uncommitted changes
				SQLConnection.getDbConn().saveDatabase(pathToFile, false);
				BEATsSettings.LAST_FILE_PATH.setStringValue(pathToFile);
				BEATsSettings.savePreferences();
				hasSucceeded = true;
			} catch (SQLException e1) {
				JOptionPane.showMessageDialog(ApplicationWindow.getAPP_WINDOW(),
						"Error during saving:\nError code: " + e1.getErrorCode() + "\n" + e1.getLocalizedMessage(), "Oops!",
						javax.swing.JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	boolean hasSucceeded() {
		return hasSucceeded;
	}
}
