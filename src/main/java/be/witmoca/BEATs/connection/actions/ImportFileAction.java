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
* File: ImportFileAction.java
* Created: 2018
*/
package be.witmoca.BEATs.connection.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import be.witmoca.BEATs.filefilters.BEATsFileFilter;
import be.witmoca.BEATs.filefilters.ImportableFileFilter;
import be.witmoca.BEATs.filefilters.WWDB1FileFilter;
import be.witmoca.BEATs.ui.ApplicationWindow;

public class ImportFileAction implements ActionListener {

	public ImportFileAction() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		// Start with new File
		LoadFileAction.getNewFileAction().actionPerformed(e);

		// Choose file to import (and method)
		final JFileChooser fc = new JFileChooser();
		fc.setAcceptAllFileFilterUsed(false);

		// WWDB v1.x
		fc.addChoosableFileFilter(new WWDB1FileFilter());
		fc.addChoosableFileFilter(new BEATsFileFilter());

		// Check for Cancel/Error
		if (fc.showOpenDialog(ApplicationWindow.getAPP_WINDOW()) != JFileChooser.APPROVE_OPTION) {
			return;
		}

		
		try {
			// execute the import contained in the filefilter with the given file
			((ImportableFileFilter) fc.getFileFilter()).importFile(fc.getSelectedFile());
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(ApplicationWindow.getAPP_WINDOW(),
					"Could not open file:\n" + e1.getClass() + "\n" + e1.getLocalizedMessage(), "Import Error",
					JOptionPane.ERROR_MESSAGE);
			e1.printStackTrace();
		}
	}
}
