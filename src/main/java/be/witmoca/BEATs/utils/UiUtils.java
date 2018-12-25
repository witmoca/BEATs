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
* File: Utils.java
* Created: 2018
*/
package be.witmoca.BEATs.utils;

import java.awt.Dimension;

import javax.swing.JSeparator;
import javax.swing.SwingConstants;

public class UiUtils {
	public static JSeparator SingleLineSeparator() {
		JSeparator s = new JSeparator(SwingConstants.VERTICAL);
		s.setMaximumSize(new Dimension(3, 50));
		return s;
	}
}
