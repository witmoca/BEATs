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
* File: StringUtils.java
* Created: 2018
*/
package be.witmoca.BEATs.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StringUtils {
	public static String prefixes[] = {"The","De"}; // Prefixes from the different languages
	
	public static String filterPrefix(String artist) {
		String result = artist;
		for(String prefix : prefixes) {
			if(artist.startsWith(prefix)) {
				try {
					artist = artist.substring(prefix.length());
				} catch (IndexOutOfBoundsException e) {
					artist = "";
				}
			}
		}
		return result;
	}
	
	/**
	 * Transforms a string into a sanitized Upper CamelCase string
	* @param s String to transform
	* @return Sanitized and transformed string
	 */
	public static String ToUpperCamelCase(String s) {
		List<String> words = Arrays.asList(s.trim().split(" ")); //immutable list
		List<String> returnWords = new ArrayList<String>();
		
		for (String word : words) {
			word = word.trim().toLowerCase();
			
			if(word.isEmpty())
				continue;
			else if(word.length() == 1) {
				returnWords.add(word.toUpperCase());
			} else {
				returnWords.add(word.substring(0, 1).toUpperCase() + word.substring(1));
			}
		}
		try {
			return String.join(" ", returnWords);
		} catch (NullPointerException e) {
			return "";
		}
	}

}
