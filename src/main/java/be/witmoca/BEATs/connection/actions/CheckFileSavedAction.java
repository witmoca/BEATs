/**
 * 
 */
package be.witmoca.BEATs.connection.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import be.witmoca.BEATs.connection.SQLConnection;
import be.witmoca.BEATs.ui.ApplicationWindow;
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
* File: CheckFileSavedAction.java
* Created: 2018
*/
public class CheckFileSavedAction implements ActionListener {
	private boolean hasSucceeded = false;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (SQLConnection.getDbConn() != null && SQLConnection.getDbConn().isChanged()) {
			String options[] = { Lang.getUI("action.save"), Lang.getUI("action.quit_no_save"),
					Lang.getUI("action.cancel") };
			int response = JOptionPane.showOptionDialog(ApplicationWindow.getAPP_WINDOW(),
					Lang.getUI("checkFileSavedAction.confirm"), Lang.getUI("checkFileSavedAction.dialogTitle"),
					JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, options, null);
			if (response == JOptionPane.YES_OPTION) {
				// Save first
				SaveFileAction save = new SaveFileAction();
				save.actionPerformed(e);
				if (!save.hasSucceeded()) {
					// Cancelled or Error
					return;
				}
			} else if (response != JOptionPane.NO_OPTION) {
				// Only yes (save) and no (close without saving) are accepted options
				return;
			}
		}
		hasSucceeded = true;
	}

	public boolean hasSucceeded() {
		return hasSucceeded;
	}
}
