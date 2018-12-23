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
* File: ArchiveEntry.java
* Created: 2018
*/
package be.witmoca.BEATs.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

class ArchiveEntry {
	private final int ROWID;
	private final String ARTIST;
	private final String SONG;
	private final int EPISODE;
	private final String EPISODE_DATE;
	private final String SECTION;
	private final String COMMENT;	

	ArchiveEntry(int rowid, String artist, String song, int episode, int epDate, String section, String comment) {
		this.ROWID = rowid;
		this.ARTIST =  artist;
		this.SONG = song;
		this.EPISODE = episode;
		this.EPISODE_DATE =  DateTimeFormatter.ofPattern("dd/MM/uu").format(LocalDate.ofEpochDay(epDate));
		this.SECTION = section;
		this.COMMENT = comment;
	}
	
	Object getColumn(int i) {
		switch(i) {
		case 0: return this.ARTIST;
		case 1: return this.SONG;
		case 2: return this.EPISODE;
		case 3: return this.SECTION;
		case 4: return this.COMMENT;
		default: return null;
		}
	}
	
	static Class<?> getColumnType(int column) {
		switch(column) {
		case 2: return Integer.class;
		default: return String.class;		
		}
	}

	public int getROWID() {
		return ROWID;
	}
	
	public String getDate() {
		return EPISODE_DATE;
	}
}
