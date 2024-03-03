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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JTable;

import be.witmoca.BEATs.utils.Lang;

public class CSVPlaylistFileFilter extends ImportableExportableFileFilter{
	public static final String LINE_SEPARATOR = ",";
	public static final String FIELD_SEPARATOR = "\"";
	
	public final JTable playlistTable;
	
	public CSVPlaylistFileFilter(JTable table) {
		this.playlistTable = table;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
	 */
	@Override
	public boolean accept(File f) {
		return (f.getName().endsWith(".csv") || f.isDirectory());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.filechooser.FileFilter#getDescription()
	 */
	@Override
	public String getDescription() {
		return Lang.getUI("fileFilters.CSV.descr");
	}

	
	@Override
	public void importFile(File source) throws Exception {
		if(source.isDirectory()) {
			throw new IOException("File is a directory, not a filename");
		}
		if(!source.exists() || !source.canRead()) {
			throw new IOException("File does not exist or could not be read");
		}
	}

	@Override
	public void exportFile(File destination) throws Exception {
		if(destination.isDirectory()) {
			throw new IOException("File is a directory, not a filename");
		}
		if(!destination.exists()) {
			destination.createNewFile();
		}
		
		if(!destination.exists() || !destination.canWrite()) {
			throw new IOException("File is not writable or could not be created.");
		}
		
		int columns = this.playlistTable.getColumnCount()-1; // skip the last "play button" column
		int rows = this.playlistTable.getRowCount();
		
		try(FileWriter fw = new FileWriter(destination, false)){
			try(BufferedWriter bw = new BufferedWriter(fw)){
				// HEADER
				String header = "";
				for(int i = 0; i < columns; i++) {
					header += FIELD_SEPARATOR + this.playlistTable.getColumnName(i) + FIELD_SEPARATOR;
					if (i+1 < columns)
						header += LINE_SEPARATOR;
				}
				bw.write(header);
				bw.newLine();
				
				// VALUES
				String line = "";
				String emptyCheck = ""; // check if the line is empty
				for(int r = 0; r < rows; r++) {
					for(int c = 0; c < columns; c++) {
						line += FIELD_SEPARATOR + this.playlistTable.getValueAt(r, c) + FIELD_SEPARATOR;
						emptyCheck += this.playlistTable.getValueAt(r, c);
						if (c+1 < columns)
							line += LINE_SEPARATOR;
					}
					if(emptyCheck.trim() != "") {
						bw.write(line);
						bw.newLine();
					}
					line = "";
					emptyCheck = "";
				}
				bw.flush();
			}
		}

	}
}
