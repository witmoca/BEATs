/**
 * 
 */
package be.witmoca.BEATs;

import java.io.File;
import java.io.IOException;

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
* File: FileManager.java
* Created: 2018
*/
final public class FileManager {
	private static final String APP_FOLDER = System.getProperty("user.home") + File.separator + "BEATs";
	public static final String DB_LOC = APP_FOLDER + File.separator + "currentDocument.beats";

	/**
	 * Initialises the File/Folder tree needed for operation.
	 * 
	 
	 * @throws IOException if the necessary tree could not be created.
	 */
	static void initFileTree() throws IOException {
		try {
			// create root
			File root = new File(APP_FOLDER);
			root.mkdirs();
			if (!root.exists() || !root.isDirectory())
				throw new IOException("Root folder " + APP_FOLDER + " doesn't exist or is not a directory");
		} catch (Exception e) {
			throw new IOException(e);
		}
	}
}
