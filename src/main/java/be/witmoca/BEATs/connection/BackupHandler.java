/*
*
+===============================================================================+
|    BEATs (Burning Ember Archival Tool suite)                                  |
|    Copyright 2019 Jente Heremans                                              |
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
* File: BackupHandler.java
* Created: 2019
*/
package be.witmoca.BEATs.connection;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.Timer;

import be.witmoca.BEATs.utils.BEATsSettings;
import be.witmoca.BEATs.utils.ResourceLoader;

class BackupHandler implements ActionListener {
	private static final Timer BACKUP_TIMER = new Timer(1000, new BackupHandler()); // (default delay is overriden on every timer start)
	private static final String BACKUP_DIR = ResourceLoader.BACKUP_DIR;
	
	private static int MAX_BACKUP_COUNT = 10; // max X backups (default is overriden on every timer start)
	private static int MAX_BACKUP_SIZE = 1024 * 1024 * 50; // max Mb size of backups folder (default is overriden on every timer start)

	@Override
	public void actionPerformed(ActionEvent e) {
		// Safety, should the backup timer still be running with an invalid connection
		if (SQLConnection.getDbConn() == null) {
			StopBackups();
			return;
		}

		// Check if there have been changes since last backup
		if (!SQLConnection.getDbConn().isBackupNeeded())
			return;

		try {
			// Do a save to the backup directory
			String dateString = LocalDateTime.now().format(DateTimeFormatter.ofPattern("uuuu_MM_dd-HH_mm_ss"));
			File currentFile = SQLConnection.getDbConn().getCurrentFile();
			String fileName = currentFile == null ? "New" : currentFile.getName();

			SQLConnection.getDbConn().saveDatabase(BACKUP_DIR + File.separator + dateString + "-" + fileName, true);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		// Do a cleanup of the existing backups if necessary
		List<File> files = new ArrayList<File>(Arrays.asList((new File(BACKUP_DIR)).listFiles()));
		// Sort all files in order of filename (as the filenames start with the date,
		// this should order according to date)
		files.sort(new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				return o1.getName().compareToIgnoreCase(o2.getName());
			}
		});

		for (int i = 0; countSize(files) > MAX_BACKUP_SIZE || files.size() - i > MAX_BACKUP_COUNT; i++) {
			// Leave at least 1 backup
			if (i == files.size() - 1)
				break;
			files.get(i).delete();
		}
	}

	static void StartBackups() {
		// reload values
		int delay = (int) TimeUnit.MINUTES.toMillis(BEATsSettings.BACKUPS_TIMEBETWEEN.getIntValue());
		BACKUP_TIMER.setDelay(delay);
		BACKUP_TIMER.setInitialDelay(delay);
		
		MAX_BACKUP_COUNT = BEATsSettings.BACKUPS_MAXAMOUNT.getIntValue(); // max X backups
		MAX_BACKUP_SIZE = 1024 * 1024 * BEATsSettings.BACKUPS_MAXSIZE.getIntValue(); // max Mb size of backups folder
		
		// Only start if enabled
		if(BEATsSettings.BACKUPS_ENABLED.getBoolValue())
			BACKUP_TIMER.start();
	}

	static void StopBackups() {
		BACKUP_TIMER.stop();
	}

	private static long countSize(List<File> files) {
		int i = 0;
		for (File f : files) {
			i += f.length();
		}
		return i;
	}
}
