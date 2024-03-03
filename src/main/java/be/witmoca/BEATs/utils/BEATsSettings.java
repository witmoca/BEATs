/**
 * 
 */
package be.witmoca.BEATs.utils;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.swing.UIDefaults;
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
* File: Settings.java
* Created: 2018
*/
public enum BEATsSettings {
	LANGUAGE,
	COUNTRY,
	LAST_FILE_PATH,
	BACKUPS_ENABLED,
	BACKUPS_TIMEBETWEEN,
	BACKUPS_MAXAMOUNT,
	BACKUPS_MAXSIZE,	
	LIVESHARE_SERVER_ENABLED,
	LIVESHARE_SERVER_HOSTNAME,
	LIVESHARE_SERVER_MAXCONNECTIONS,
	LIVESHARE_CLIENT_ENABLED,
	LIVESHARE_CLIENT_ALLOWEDFAILS,
	LIVESHARE_CLIENT_HOSTLIST;
	

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
	
	public List<String> getListValue(){
		String v = userSettings.getProperty(this.name(),"").trim();
		if(v.isEmpty()) {
			return new ArrayList<String>();
		} else {
			return Arrays.asList(v.split(";"));
		}
	}
	
	public void setListValue(List<String> val) {
		userSettings.setProperty(this.name(), String.join(";", val));
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

		UIDefaults uiDefaults= UIManager.getLookAndFeelDefaults();
		// Alternating color for table rows
		uiDefaults.put("Table.alternateRowColor", new Color(240,240,255));
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
		// locale
		defaultSettings.setProperty(LANGUAGE.name(), Locale.getDefault().getLanguage());
		defaultSettings.setProperty(COUNTRY.name(), Locale.getDefault().getCountry());
		// hostname
		try
		{
		    InetAddress addr;
		    addr = InetAddress.getLocalHost();
		    defaultSettings.setProperty(LIVESHARE_SERVER_HOSTNAME.name(), addr.getHostName());
		}
		catch (UnknownHostException ex)
		{
			 defaultSettings.setProperty(LIVESHARE_SERVER_HOSTNAME.name(), "BEATsHost-" + (Math.random() * 9000 + 1000 )); // pasta a number (1000-9999) at the end of the hostname 
		}
		
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
