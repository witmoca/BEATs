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
import java.sql.SQLException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import be.witmoca.BEATs.FileFilters.BEATsFileFilter;
import be.witmoca.BEATs.connection.SQLConnection;
import be.witmoca.BEATs.ui.ApplicationWindow;

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
		        if(getSelectedFile().exists() && JOptionPane.showConfirmDialog(this,"Do you want to overwrite this file?","File Exists",JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION){
		        	return;
		        }
				super.approveSelection();
			}
		};
		
		fc.setAcceptAllFileFilterUsed(false);
		fc.setFileFilter(new BEATsFileFilter());
		
		if (fc.showSaveDialog(ApplicationWindow.getAPP_WINDOW()) == JFileChooser.APPROVE_OPTION) {
			String pathToFile = fc.getSelectedFile().getAbsolutePath();
			// Only 1 ".beats" extension!
			while (pathToFile.endsWith(".beats")) {
				pathToFile = pathToFile.substring(0, pathToFile.length() - 6);
			}
			pathToFile += ".beats";
			try {
				SQLConnection.getDbConn().saveDatabase(pathToFile);
				hasSucceeded = true;
			} catch (SQLException e1) {
				JOptionPane.showMessageDialog(ApplicationWindow.getAPP_WINDOW(),
						"Error during saving:\n" + e1.getLocalizedMessage(), "Oops!",
						javax.swing.JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	
	public boolean hasSucceeded() {
		return hasSucceeded;
	}
}
