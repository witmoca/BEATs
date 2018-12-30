/**
 * 
 */
package be.witmoca.BEATs.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

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
* File: Settings.java
* Created: 2018
*/
public enum BEATsSettings {
	LANGUAGE,
	COUNTRY;
	
	public String getValue() {
		return userSettings.getProperty(this.name());
	}
	
	public void setValue(String val) {
		userSettings.setProperty(this.name(), val);
	}
	
	// Statics
	
	private static Properties userSettings = null;
	public static void loadPreferences() throws IOException {
		// Load default settings
		Properties defaultSettings = new Properties();
		defaultSettings.load(BEATsSettings.class.getClassLoader().getResourceAsStream("Text/DefaultSettings.properties"));
		
		// Overwrite default settings with system defaults
		defaultSettings.setProperty(LANGUAGE.name(), Locale.getDefault().getLanguage());
		defaultSettings.setProperty(COUNTRY.name(), Locale.getDefault().getCountry());
		
		// Create user settings
		userSettings = new Properties(defaultSettings);
		// User preferences exist? Load
		File userFile = new File(ResourceLoader.USER_SETTINGS_LOC);
		if(userFile.exists()) {
			userSettings.load(new FileInputStream(userFile));
		}
		
		// Install settings
		Locale.setDefault(new Locale(LANGUAGE.getValue(), COUNTRY.getValue()));
	}
	
	public static void savePreferences() {
		try {
			userSettings.store(new FileOutputStream(new File(ResourceLoader.USER_SETTINGS_LOC)),null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}