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
import java.sql.SQLException;
import java.util.concurrent.ThreadLocalRandom;

import be.witmoca.BEATs.connection.ConnectionException;
import be.witmoca.BEATs.connection.DataChangedType;
import be.witmoca.BEATs.connection.SQLConnection;
import be.witmoca.BEATs.connection.actions.LoadFileAction;
import be.witmoca.BEATs.utils.AppVersion;
import be.witmoca.BEATs.utils.Lang;
import be.witmoca.BEATs.utils.ResourceLoader;

public class BEATsFileFilter extends ImportableFileFilter {
	private static final String UPDATE_1_2 = "Text/updateBEATs_1_2.sql";
	private static final String UPDATE_2_3 = "Text/updateBEATs_2_3.sql";
	
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
		File tempSource = source;
		// Load file without checking versions, etc
		LoadFileAction.getLoadWithoutSanity(tempSource).actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "import"));
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
			case 2:	
				// Update current file connection settings (such as user_version)
				db.updateSettings();

				// Generate random int used as part of all filenames during import
				int randomId = ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE);
				File startFile = new File(ResourceLoader.BACKUP_DIR + File.separator + randomId + "_" + fileVersion.getVERSION_MAJOR() + "_" + fileVersion.getVERSION_MAJOR() + "_Start_" + tempSource.getName());
				db.saveDatabase(startFile.getAbsolutePath(), false);
				db.close();
				
				// Update database contents in steps until current version
				File updatedFile = updateFile(startFile.getAbsoluteFile(), fileVersion.getVERSION_MAJOR(), randomId, tempSource.getName());
				// Set new source
				if(updatedFile.exists())
					tempSource = updatedFile.getAbsoluteFile();


				
				// Do not return, just let the file get imported like normal by going to the next case
			// Current major version should be 3
			case 3:
				// just load like normal
				db.close();
				LoadFileAction.getLoadFileAction(tempSource).actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "load"));
				return;
			default:
				// App should be updated? 
				throw new ConnectionException(ConnectionException.ConnState.APP_OUTDATED, null);				
			}
		}
	}
	
	/**
	 *  Recursively updates the source file contents (correct config, such as user_version should already be set)
	 * @param source
	 * @param fromVersion
	 * @param Id
	 * @return
	 * @throws SQLException 
	 * @throws ConnectionException 
	 */
	private File updateFile(File source, int fromVersion, int Id, String origFileName) throws SQLException, ConnectionException {
		System.out.println("From " + fromVersion);
		// Is fromVersion one that we can update?
		if(fromVersion >= AppVersion.getInternalAppVersion().getVERSION_MAJOR() || fromVersion <= 0)
			return source;
		
		// Load source file
		LoadFileAction.getLoadFileAction(source).actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "load"));
		SQLConnection db = SQLConnection.getDbConn();
		
		File dest = new File(ResourceLoader.BACKUP_DIR + File.separator + Id + "_" + fromVersion + "_" + (fromVersion+1) + "_" + "_Import_" + origFileName);
		
		switch(fromVersion) {
		case 1:
			db.executeSQLFile(UPDATE_1_2);
			break;
		case 2:
			db.executeSQLFile(UPDATE_2_3);
			break;
		default:
			return source;
		}
		
		db.commit(DataChangedType.ALL_OPTS);
		db.saveDatabase(dest.getAbsolutePath(), false);
		db.close();
		
		// Recursively update if not yet to current
		if(fromVersion + 1 < AppVersion.getInternalAppVersion().getVERSION_MAJOR()) {
			return updateFile(dest.getAbsoluteFile(), fromVersion + 1, Id, origFileName);
		}	
		return dest;
	}
}
