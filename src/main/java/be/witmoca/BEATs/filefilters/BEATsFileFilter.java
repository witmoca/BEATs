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
* File: BEATsFileFilter.java
* Created: 2018
*/
package be.witmoca.BEATs.filefilters;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ThreadLocalRandom;

import be.witmoca.BEATs.connection.ConnectionException;
import be.witmoca.BEATs.connection.SQLConnection;
import be.witmoca.BEATs.connection.actions.LoadFileAction;
import be.witmoca.BEATs.utils.AppVersion;
import be.witmoca.BEATs.utils.Lang;
import be.witmoca.BEATs.utils.ResourceLoader;

public class BEATsFileFilter extends ImportableFileFilter {
	private static final String UPDATE_1_2 = "Text/updateBEATs_1_2.sql";
	
	public BEATsFileFilter() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
	 */
	@Override
	public boolean accept(File f) {
		return (f.getName().endsWith(".beats") || f.isDirectory());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.filechooser.FileFilter#getDescription()
	 */
	@Override
	public String getDescription() {
		return Lang.getUI("fileFilters.BEATs.descr");
	}

	/**
	 * Import file is recursive for BEATs import: each version is update in one step, then import is called again until normal load is possible
	 */
	@Override
	public void importFile(File source) throws Exception {
		// Load file without checking versions, etc
		LoadFileAction.getLoadWithoutSanity(source).actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "import"));;
		SQLConnection db = SQLConnection.getDbConn();
		
		// Check application_id
		try (PreparedStatement appIdCheck = db.prepareStatement("PRAGMA application_id")) {
			// always returns a value! (0 as default)
			ResultSet appIdResult = appIdCheck.executeQuery();
			if (appIdResult.getInt(1) != SQLConnection.APPLICATION_ID)
				throw new ConnectionException(ConnectionException.ConnState.APP_ID_INVALID, null);
		}
		
		// Read the file version
		try (PreparedStatement versionCheck = db.prepareStatement("PRAGMA user_version")) {
			// always returns a value! (0 as default)
			ResultSet appversionCheckIdResult = versionCheck.executeQuery(); 
			AppVersion fileVersion = new AppVersion(appversionCheckIdResult.getInt(1), "");
			
			// Choose import action based on Major version
			switch (fileVersion.getVERSION_MAJOR()) {
			case 0:
				throw new ConnectionException(ConnectionException.ConnState.GENERAL_EXCEPTION, new IOException("Fileversion 0 loading not supported."));
			case 1:
				// update with sql file, save to backup dir & reload file correctly
				db.executeSQLFile(UPDATE_1_2);
				// Update current file connection settings (such as user_version)
				db.updateSettings();
				// Save this file, with random filename in the backup directory
				String importedFile = ResourceLoader.BACKUP_DIR + File.separator + "Import_1_" + ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE) + "_" + source.getName();
				db.saveDatabase(importedFile, false);
				// Recursively import again
				importFile(new File(importedFile).getAbsoluteFile());
				break;
			// Current major version should be 2
			case 2:
				// just load like normal
				LoadFileAction.getLoadFileAction(source).actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "load"));
				return;
			default:
				// App should be updated? 
				throw new ConnectionException(ConnectionException.ConnState.APP_OUTDATED, null);				
			}
		}
	}
}
