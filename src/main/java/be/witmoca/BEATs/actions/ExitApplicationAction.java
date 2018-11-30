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
* File: ExitApplicationAction.java
* Created: 2018
*/
package be.witmoca.BEATs.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

import javax.swing.JOptionPane;

import be.witmoca.BEATs.Launch;

public class ExitApplicationAction implements ActionListener {
	private boolean hasSucceeded = false;

	public ExitApplicationAction() {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		// Check if data has changed
		if(Launch.getDB_CONN().isChanged()) {
			String options[] = {"Save", "Close without saving", "Cancel"};
			int response = JOptionPane.showOptionDialog(Launch.getAPP_WINDOW(), "You have not saved this file.\nYour changes will be discarded if you continue wihout saving.", "Confirm", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, options, null);
			if(response == 2 || response == -1) {
				// Cancelled
				return;
			} else if (response == 0) {
				// Save first
				SaveFileAction save = new SaveFileAction();
				save.actionPerformed(e);
				if(!save.hasSucceeded()) {
					// Cancelled or Error
					return;
				}
			}
		}
		
		// Exit Application
		try {
			Launch.getDB_CONN().close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		// Kill GUI
		Launch.getAPP_WINDOW().dispose();
		hasSucceeded = true;
	}

	public boolean hasSucceeded() {
		return hasSucceeded;
	}
}
