/**
 * 
 */
package be.witmoca.BEATs.ui.components.SuggestCellEditor;

import java.util.List;

import javax.swing.JTable;

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
* File: IMatcher.java
* Created: 2018
*/
interface IMatcher {
	/**
	 * The table, row and col parameter are used for determining extra information.
	 * They should NEVER be used to edit the model of the jtable while an edit is in
	 * progress
	 * 
	 * @param search
	 *            The string to search for
	 * @param forwardOnly
	 *            True when {@code search} has to be the start of a match. False
	 *            when it may be anywhere within a match string
	 * @param table
	 *            The JTable that contains the cell being edited
	 * @param row
	 *            The row of the cell being edited
	 * @param col
	 *            The column of the cell being edited
	 * @return A list of matches for the given parameters
	 */
	public List<String> match(String search, boolean forwardOnly, JTable table, int row, int col);
}
