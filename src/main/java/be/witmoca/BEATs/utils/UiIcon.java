/**
 * 
 */
package be.witmoca.BEATs.utils;

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
	NEW("BasicUi/document"),
	OPEN("BasicUi/folder"),
	SAVE("BasicUi/save"),
	IMPORT(UIManager.getIcon("Table.ascendingSortIcon")),
	EXPORT(UIManager.getIcon("Table.descendingSortIcon")),
	CLOSE_APP("BasicUi/home"),

	// Tool icons
	LIST("BasicUi/list-mark"),
	SEARCH("BasicUi/search"),
	CALENDAR("BasicUi/calendar"),
	CHECKED("BasicUi/check-mark"),
	SCREEN("BasicUi/desktop"),
	EDIT_W("BasicUi/edit-write"),
	FOLDER_OPEN("BasicUi/folder-open"),
	SETTINGS("BasicUi/gear-setting"),

	// Item operations
	DELETE("BasicUi/garbage"),
	REVERT("BasicUi/arrow-backward"),
	PROCEED("BasicUi/arrow-forward"),
	INFO("BasicUi/information-mark"),
	PLAY("BasicUi/play"),
	CUT("Icons8/cut"),
	COPY("Icons8/copy"),
	PASTE("Icons8/paste"),
	EDIT("BasicUi/edit"),
	PRINT("BasicUi/printer"),
	UP(UIManager.getIcon("Table.ascendingSortIcon")),
	DOWN(UIManager.getIcon("Table.descendingSortIcon")),

	// Logo's
	LOGO_256("Logo/logo-0"),
	LOGO_128("Logo/logo-1"),
	LOGO_64("Logo/logo-2"),
	LOGO_48("Logo/logo-3"),
	LOGO_32("Logo/logo-4"),
	LOGO_16("Logo/logo-5");

	// Unused
	// status
	// WARNING_RND("exclamation-mark-round-sign"),
	// WARNING_TRI("exclamation-mark-triangle-sign"),
	//
	// // tools
	// OPTIONS("option-menu"),
	// CHART("fold-chart"),
	//
	// // item operations
	// LOCKED("lock-locked"),
	// UNLOCKED("lock-unlocked"),
	// STICKY("pricker"),
	// BACK("sign-backward"),
	// FORWARD("sign-forward"),
	// ZOOM_IN("zoom-in"),
	// ZOOM_OUT("zoom-out");

	private static final String FOLDER = "Icons/";
	private static final String EXT = ".png";
	private final ImageIcon icon;

	private UiIcon(String name) {
		icon = new ImageIcon(getClass().getClassLoader().getResource(FOLDER + name + EXT));
	}

	private UiIcon(Icon icon) {
		if (icon instanceof ImageIcon) {
			this.icon = (ImageIcon) icon;
			return;
		}
		this.icon = null; // Icon not supported
	}

	public final ImageIcon getIcon() {
		return icon;
	}
}
