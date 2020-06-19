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
	COUNTRY,
	LAST_FILE_PATH,
	LOCAL_PORT,
	BACKUPS_ENABLED,
	BACKUPS_TIMEBETWEEN,
	BACKUPS_MAXAMOUNT,
	BACKUPS_MAXSIZE,	
	LIVESHARE_SERVER_ENABLED,
	LIVESHARE_SERVER_PORT,
	LIVESHARE_SERVER_MAXCONNECTIONS,
	LIVESHARE_CLIENT_ENABLED,
	LIVESHARE_CLIENT_IP_LIST,
	LIVESHARE_CLIENT_PORT_LIST;
	

	public String getStringValue() {
		return userSettings.getProperty(this.name());
	}

	public void setStringValue(String val) {
		userSettings.setProperty(this.name(), val);
	}
	
	public int getIntValue() {
		return Integer.parseInt(userSettings.getProperty(this.name()));
	}
	
	public void setIntValue(int val) {
		userSettings.setProperty(this.name(), Integer.toString(val));
	}
	
	/**
	 * Warning: defaults to false if unreadable/not a boolean
	 * @return boolean value of property
	 */
	public boolean getBoolValue() {
		return Boolean.parseBoolean(userSettings.getProperty(this.name()));
	}
	
	public void setBoolValue(boolean val) {
		userSettings.setProperty(this.name(), Boolean.toString(val));
	}
	
	

	// Statics

	private static Properties userSettings = null;

	public static void loadPreferences() throws IOException {
		// Create user setting defaults (defaults are not saved)
		userSettings = new Properties(loadDefaultPreferences());
		// User preferences exist? Load
		File userFile = new File(ResourceLoader.USER_SETTINGS_LOC);
		if (userFile.exists()) {
			userSettings.load(new FileInputStream(userFile));
		}

		// Install settings
		Locale l = new Locale(LANGUAGE.getStringValue(), COUNTRY.getStringValue());
		Locale.setDefault(l);
		Lang.setNewLocale(l);
	}

	public static void savePreferences() {
		try {
			userSettings.store(new FileOutputStream(new File(ResourceLoader.USER_SETTINGS_LOC)), null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Reads the default properties from the DefaultSettings file.
	 * Overwrites the default Locale with the operating system locale
	 * 
	 * @return DefaultProperties
	 * @throws IOException
	 */
	private static Properties loadDefaultPreferences() throws IOException {
		// Load default settings
		Properties defaultSettings = new Properties();
		defaultSettings.load(BEATsSettings.class.getClassLoader().getResourceAsStream("Text/DefaultSettings.properties"));

		// Overwrite default settings with system defaults
		defaultSettings.setProperty(LANGUAGE.name(), Locale.getDefault().getLanguage());
		defaultSettings.setProperty(COUNTRY.name(), Locale.getDefault().getCountry());
		return defaultSettings;
	}
	
	/**
	 * Resets properties to the default state
	 * 
	 * @return True if operation succeeded
	 */
	public static boolean resetDefaultPreferences() {
		try {
			userSettings = new Properties(loadDefaultPreferences());
			savePreferences();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
}
