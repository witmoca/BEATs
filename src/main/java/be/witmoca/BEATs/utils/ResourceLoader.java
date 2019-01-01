/**
 * 
 */
package be.witmoca.BEATs.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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
* File: ResourceLoader.java
* Created: 2018
*/
public class ResourceLoader {
	// Directories
	private static final String APP_DIR = System.getProperty("user.home") + File.separator + "BEATs";
	public static final String LOG_DIR = APP_DIR + File.separator + "log";	
	public static final String BACKUP_DIR = APP_DIR + File.separator + "backups";
	private static final String[] DIRECTORIES = { APP_DIR, LOG_DIR, BACKUP_DIR};	
	
	// Files
	public static final String DB_LOC = APP_DIR + File.separator + "currentDocument.beats";
	static final String USER_SETTINGS_LOC = APP_DIR + File.separator + "UserPreferences.properties";

	// Extensions
	private static final String ERR_LOG_EXT = ".err";
	
	// Metadata
	public static int bytesOfErrorData = 0;

	/**
	 * Initialises the File/Folder tree needed for operation.
	 * 
	 * @throws IOException if the necessary tree could not be created.
	 */
	public static void initFileTree() throws IOException {
		try {
			// Create the folder structure
			for (String dir : DIRECTORIES) {
				File f = new File(dir);
				f.mkdirs();
				if (!f.exists() || !f.isDirectory())
					throw new IOException("Could not create directory " +  dir);
			}
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	public static void registerStandardErrorLog() throws IOException {
		File[] errorLogs = listErrFiles();
		// Delete empty log files
		for(int i = 0; i < errorLogs.length; i++) {
			if(errorLogs[i].length() == 0) {
				errorLogs[i].delete();	// ignore if they couldn't be delete (might be in use by another instance)
			}
		}
		// Reload list
		errorLogs = listErrFiles();
		// Delete oldest if necessary
		while(errorLogs.length >= 20) {
			// find oldest
			File oldest = errorLogs[0];
			for(int i = 1; i < errorLogs.length; i++) {
				if(oldest.getName().compareTo(errorLogs[i].getName()) > 0) {
					oldest = errorLogs[i];
				}
			}
			if(!oldest.delete())
				throw new IOException("Could not delete error log!\n" + oldest.getAbsolutePath());
			errorLogs = listErrFiles();
		}
		
		// Calculate total amount of error data 
		for(File f : errorLogs) {
			bytesOfErrorData += f.length();
		}
		
		// Calculate new name
		File errLog = null;
		while(errLog == null || errLog.exists())
			errLog = new File(LOG_DIR + File.separator + (LocalDateTime.now().format(DateTimeFormatter.ofPattern("uuuu_MM_dd-HH_mm_ss"))) + ERR_LOG_EXT);
		
		// Register new error log (to both standard error and the log folder)
		System.setErr(new PrintStream(new DuplicateOutputStream(new FileOutputStream(errLog), System.err),true));
	}
	
	private static File[] listErrFiles() {
		return (new File(LOG_DIR)).listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(ERR_LOG_EXT);
			}
		});
	}

	/**
	 * Reads in a text based file and returns a list containing the lines inside it
	 * 
	 * @param resource the resource to load
	 * @return the list containing the resulting lines.
	 *         returned. May return {@code null} if the resource was not found.
	 */
	public static List<String> ReadResource(String resource) {
		try (BufferedReader in = new BufferedReader(new InputStreamReader(ResourceLoader.class.getClassLoader().getResourceAsStream(resource))) ) {
			List<String> result = new ArrayList<String>();
			String line = in.readLine();
			while(line != null) {
			    result.add(line.trim());
			    line = in.readLine();
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
