/**
 * 
 */
package be.witmoca.BEATs.connection;

import java.util.EnumSet;

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
* File: DataChangedType.java
* Created: 2018
*/
public enum DataChangedType {
	ARTIST,
	SONG,
	PLAYLIST,
	EPISODE,
	GENRE,
	SONGS_IN_PLAYLIST,
	CURRENT_QUEUE,
	SONGS_IN_ARCHIVE,
	CCP,
	META_DATA;
	public static final EnumSet<DataChangedType> ALL_OPTS = EnumSet.allOf(DataChangedType.class);
	public static final EnumSet<DataChangedType> ARCHIVE_DATA_OPTS = EnumSet.of(ARTIST, SONG, EPISODE, GENRE,
			SONGS_IN_ARCHIVE);
	public static final EnumSet<DataChangedType> PLAYLIST_DATA_OPTS = EnumSet.of(PLAYLIST, SONGS_IN_PLAYLIST);
}
