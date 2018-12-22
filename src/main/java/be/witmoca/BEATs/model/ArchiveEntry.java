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

class ArchiveEntry {
	private final String ARTIST;
	private final String SONG;
	private final String EPISODESECTION;
	private final String COMMENT;	

	ArchiveEntry(String artist, String song, String episodeSection, String comment) {
		this.ARTIST =  artist;
		this.SONG = song;
		this.EPISODESECTION = episodeSection;
		this.COMMENT = comment;
	}
	
	String getColumn(int i) {
		switch(i) {
		case 0: return this.ARTIST;
		case 1: return this.SONG;
		case 2: return this.EPISODESECTION;
		case 3: return this.COMMENT;
		default: return null;
		}
	}
}
