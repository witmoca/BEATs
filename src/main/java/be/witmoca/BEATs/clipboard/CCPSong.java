/**
 * 
 */
package be.witmoca.BEATs.clipboard;

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
* File: CCPSong.java
* Created: 2018
*/
public class CCPSong {
	private final String ARTIST;
	private final String SONG;
	private final int ROWID;

	protected CCPSong(String aRTIST, String sONG, int rOWID) {
		ARTIST = aRTIST;
		SONG = sONG;
		ROWID = rOWID;
	}

	public String getARTIST() {
		return ARTIST;
	}

	public String getSONG() {
		return SONG;
	}

	public int getROWID() {
		return ROWID;
	}
	
	@Override
	public String toString() {
		return ARTIST + " - " + SONG;
	}
}
