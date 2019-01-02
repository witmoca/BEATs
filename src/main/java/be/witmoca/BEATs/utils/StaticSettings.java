/*
*
+===============================================================================+
|    BEATs (Burning Ember Archival Tool suite)                                  |
|    Copyright 2019 Jente Heremans                                              |
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
* File: StaticSettings.java
* Created: 2019
*/
package be.witmoca.BEATs.utils;

import java.io.IOException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StaticSettings {
	private static final String APP_VERSION_STRING;
	private static final int APP_VERSION_MAJOR;
	private static final int APP_VERSION_MINOR;
	private static final int APP_VERSION_PATCH;

	static {
		Properties statics = new Properties();
		try {
			statics.load(StaticSettings.class.getClassLoader().getResourceAsStream("Filtered/Version.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		String versionString = statics.getProperty("version").trim();
		Pattern regex = Pattern.compile("(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})(-([0-9A-Za-z-]*))?");
		Matcher matcher = regex.matcher(versionString);

		if (matcher.find()) {
			APP_VERSION_MAJOR = Integer.parseInt(matcher.group(1));
			APP_VERSION_MINOR = Integer.parseInt(matcher.group(2));
			APP_VERSION_PATCH = Integer.parseInt(matcher.group(3));
			APP_VERSION_STRING = versionString;
		} else {
			APP_VERSION_MAJOR = 0; // For test purposes
			APP_VERSION_MINOR = 0;
			APP_VERSION_PATCH = 0;
			APP_VERSION_STRING = "0.0.0-TestVersion";
		}
	}

	private static final int APP_VERSION_INT = APP_VERSION_MAJOR * 1000000 + APP_VERSION_MINOR * 1000
			+ APP_VERSION_PATCH;

	// GETTERS

	public static String getAppVersionString() {
		return APP_VERSION_STRING;
	}

	public static int getAppVersionInt() {
		return APP_VERSION_INT;
	}

	public static int getAppVersionMajor() {
		return APP_VERSION_MAJOR;
	}
}
