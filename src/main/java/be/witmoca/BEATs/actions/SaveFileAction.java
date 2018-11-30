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
package be.witmoca.BEATs.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import be.witmoca.BEATs.Launch;

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
		final JFileChooser fc = new JFileChooser();
		fc.setAcceptAllFileFilterUsed(false);
		fc.setFileFilter(new BEATsFileFilter());
		if (fc.showSaveDialog(Launch.getAPP_WINDOW()) == JFileChooser.APPROVE_OPTION) {
			String pathToFile = fc.getSelectedFile().getAbsolutePath();
			// Only 1 ".beats" extension!
			while (pathToFile.endsWith(".beats")) {
				pathToFile = pathToFile.substring(0, pathToFile.length() - 6);
			}
			pathToFile += ".beats";
			try {
				Launch.getDB_CONN().saveDatabase(pathToFile);
				hasSucceeded = true;
			} catch (SQLException e1) {
				JOptionPane.showMessageDialog(Launch.getAPP_WINDOW(),
						"Error during saving:\n" + e1.getLocalizedMessage(), "Oops!",
						javax.swing.JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public boolean hasSucceeded() {
		return hasSucceeded;
	}
}
