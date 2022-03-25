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

public class AppVersion implements Comparable<AppVersion>{
	// STATIC code: define the Internal APP version (as compiled)
	private static final AppVersion INTERNAL_APP_VERSION;
	static {
		Properties statics = new Properties();
		try {
			statics.load(AppVersion.class.getClassLoader().getResourceAsStream("Filtered/Version.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		String versionString = statics.getProperty("version").trim();
		INTERNAL_APP_VERSION = new AppVersion(versionString);
	}
	
	public static AppVersion getInternalAppVersion() {
		return INTERNAL_APP_VERSION;
	}
	
	private final String VERSION_STRING;
	private final int VERSION_MAJOR;
	private final int VERSION_MINOR;
	private final int VERSION_PATCH;
	
	/** 
	 * Each AppVersion object represents a version, initialised by a correctly formatted string
	 * This constructor initialises using a formatted version string.
	 * 
	 * @param versionString String in MMM.mmm.ppp-alfanum format
	 * [M = Major (1-3 chars)
	 * ;m = minor (1-3 chars)
	 * ;p = patch (1-3 chars)
	 * ;alfanum = special alfanum designation for test builds (e.g. rc1)
	 * ]
	 */
	
	public AppVersion(String versionString){
		Pattern regex = Pattern.compile("(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})(-([0-9A-Za-z-]*))?");
		Matcher matcher = regex.matcher(versionString);

		if (matcher.find()) {
			this.VERSION_MAJOR = Integer.parseInt(matcher.group(1));
			this.VERSION_MINOR = Integer.parseInt(matcher.group(2));
			this.VERSION_PATCH = Integer.parseInt(matcher.group(3));
			this.VERSION_STRING = (matcher.group(5) == null ? "" : matcher.group(5));
		} else {
			this.VERSION_MAJOR = 0; // For test purposes
			this.VERSION_MINOR = 0;
			this.VERSION_PATCH = 0;
			this.VERSION_STRING = "TestVersion";
		}
	}
	
	/**
	 * Each AppVersion object represents a version, initialised by a correctly formatted string
	 * This constructor initialises using manually specified parts
	 * 
	 * @param major Major version number (0-999)
	 * @param minor Minor version number (0-999)
	 * @param patch Patch version number (0-999)
	 * @param alphanum string designating special version (e.g. rc1)
	 */
	public AppVersion(int major, int minor, int patch, String alphanum) {
		if(major < 0 || minor < 0 || patch < 0 || major > 999 || minor > 999 || patch > 999)
			throw new IllegalArgumentException("Invalid Argument: version out of range");
		
		this.VERSION_MAJOR = major;
		this.VERSION_MINOR = minor;
		this.VERSION_PATCH = patch;
		this.VERSION_STRING = alphanum;
	}
	
	public AppVersion(int versionInt, String alphanum) {
		if(versionInt < 0 || versionInt > 999999999)
			throw new IllegalArgumentException("Invalid Argument: version out of range");
		
		this.VERSION_MAJOR = versionInt/1000000;
		this.VERSION_MINOR = (versionInt - this.VERSION_MAJOR * 1000000) / 1000;
		this.VERSION_PATCH = (versionInt - this.VERSION_MAJOR * 1000000 - this.VERSION_MINOR * 1000);
		this.VERSION_STRING = alphanum;
	}
	
	@Override
	public String toString() {
		return this.VERSION_MAJOR + "." + this.VERSION_MINOR + "." + this.VERSION_PATCH + (this.VERSION_STRING.isEmpty() ? "" : "-" + this.VERSION_STRING);
	}

	public int getAppVersionInt() {
		return this.VERSION_MAJOR * 1000000 + this.VERSION_MINOR * 1000
				+ this.VERSION_PATCH;
	}

	@Override
	public int compareTo(AppVersion o) {
		// The string part is does not influence version comparison (necessary for correct file version compares)
		// e.g. version 1.0.0 == 1.0.0-rc1 == 1.0.0-rc2
		return this.getAppVersionInt() - o.getAppVersionInt();
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof AppVersion))
			return false;
		return this.compareTo((AppVersion) obj) == 0;
	}

	public String getVERSION_STRING() {
		return VERSION_STRING;
	}

	public int getVERSION_MAJOR() {
		return VERSION_MAJOR;
	}

	public int getVERSION_MINOR() {
		return VERSION_MINOR;
	}

	public int getVERSION_PATCH() {
		return VERSION_PATCH;
	}
	
	
}
