/**
 * 
 */
package be.witmoca.BEATs.ui;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIManager;

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
* File: UiIcon.java
* Created: 2018
*/
public enum UiIcon {	
	// File icons
	NEW("document"),
	OPEN("folder"),
	SAVE("save"),
	IMPORT(UIManager.getIcon("Table.ascendingSortIcon")),
	CLOSE_APP("home"),
	
	// Tool icons
	PLAYLISTS("list-mark"),
	SEARCH("search"),
	CALENDAR("calendar"),
	CHECKED("check-mark"),
	SCREEN("desktop"),
	
	// Item operations
	DELETE("garbage"),
	REVERT("arrow-backward"),
	PROCEED("arrow-forward"),
	INFO("information-mark"),
	PLAY("play");
	
	// Unused
	// status
//	WARNING_RND("exclamation-mark-round-sign"),
//	WARNING_TRI("exclamation-mark-triangle-sign"),
//	
//	// tools
//	FOLDER_OPEN("folder-open"),
//	OPTIONS("option-menu"),
//	SETTINGS("gear-setting"),
//	CHART("fold-chart"),
//	PRINT("printer"),
//	
//	// item operations
//	EDIT("edit"),
//	EDIT_W("edit-write"),
//	LOCKED("lock-locked"),
//	UNLOCKED("lock-unlocked"),
//	STICKY("pricker"),
//	BACK("sign-backward"),
//	FORWARD("sign-forward"),
//	ZOOM_IN("zoom-in"),
//	ZOOM_OUT("zoom-out");
	
	
	private static final String FOLDER = "Icons/";
	private static final String EXT = ".png";
	private final Icon icon;
	
	private UiIcon(String name) {
		icon = new ImageIcon(getClass().getClassLoader().getResource(FOLDER + name + EXT));
	}
	
	private UiIcon(Icon icon) {
		this.icon = icon;
	}
	
	public final Icon getIcon() {
		return icon;
	}
}
