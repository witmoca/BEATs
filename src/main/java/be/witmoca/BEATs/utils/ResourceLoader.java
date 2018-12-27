/**
 * 
 */
package be.witmoca.BEATs.utils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
* File: ResourceLoader.java
* Created: 2018
*/
public class ResourceLoader {
	/**
	 * Reads in a text based file and returns a list containing the lines inside it
	 * 
	 * @param resource the resource to load
	 * @return the list containing the resulting lines. There are no guarantees on
	 *         the type, mutability,serializability, or thread-safety of the List
	 *         returned. May return {@code null} if the resource was not found.
	 */
	public static List<String> ReadResource(String resource) {
		try (Stream<String> lines = Files
				.lines(Paths.get(ResourceLoader.class.getClassLoader().getResource(resource).toURI()));) {
			List<String> result = lines.collect(Collectors.toList());
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}