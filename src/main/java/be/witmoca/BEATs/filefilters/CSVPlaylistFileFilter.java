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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JTable;

import be.witmoca.BEATs.utils.Lang;

public class CSVPlaylistFileFilter extends ImportableExportableFileFilter{
	public static final char FIELD_SEPARATOR = ',';
	public static final char VALUE_SEPARATOR = '\"';
	
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

		int columns = this.playlistTable.getModel().getColumnCount()-1; // skip the last "play button" column
		int row = this.playlistTable.getModel().getRowCount()-1; // begin on the "empty" line

		try(FileReader fr = new FileReader(source)){
			try(BufferedReader br = new BufferedReader(fr)){
				while(br.ready()) {
					String line = br.readLine();
					String[] parts = line.split(String.valueOf(FIELD_SEPARATOR));
					for(int c = 0; c < columns; c++) {
						// insert value (or "" if no value available)
						String value = "";
						if(c < parts.length) {
							value = parts[c];
							if(value.charAt(0) == (VALUE_SEPARATOR)) {
								value = value.substring(1);
							}
							if(value.charAt(value.length()-1) == (VALUE_SEPARATOR)) {
								value = value.substring(0,value.length()-1);
							}
						}
						this.playlistTable.getModel().setValueAt(value , row, c);
					}
					row++;
				}
			}
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
				// VALUES
				String line = "";
				String emptyCheck = ""; // check if the line is empty
				for(int r = 0; r < rows; r++) {
					for(int c = 0; c < columns; c++) {
						line += String.valueOf(VALUE_SEPARATOR) + this.playlistTable.getValueAt(r, c) + String.valueOf(VALUE_SEPARATOR);
						emptyCheck += this.playlistTable.getValueAt(r, c);
						if (c+1 < columns)
							line += String.valueOf(FIELD_SEPARATOR);
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
