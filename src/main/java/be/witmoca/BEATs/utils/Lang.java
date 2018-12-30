/**
 * 
 */
package be.witmoca.BEATs.utils;

import java.util.HashSet;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

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
* File: Lang.java
* Created: 2018
*/
public class Lang {
	private static final String UI_BASE = "Langs.UserInterface";
	private static ResourceBundle UiBundle = ResourceBundle.getBundle(UI_BASE);

	public static String getUI(String msg) {
		return UiBundle.getString(msg);
	}

	public static Set<Locale> getPossibleLocales() {
		Set<Locale> locales = new HashSet<>();
		for (Locale lo : Locale.getAvailableLocales()) {
			Locale l = ResourceBundle.getBundle(UI_BASE, lo).getLocale();
			locales.add(l);
		}
		return locales;
	}
}
