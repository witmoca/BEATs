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
package be.witmoca.BEATs.ui.playlist;

import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.table.TableModel;

public class Utils {

	/**
	 * General purpose selectionmodel converter
	 * 
	 * @param viewSelection
	 *            the {@code int[]} holding indices from the view
	 * @return {@code int[]} holding corresponding indices from the model (returns
	 *         viewSelection if no rowsorter present)
	 */
	public static int[] convertSelectionToModel(int[] viewSelection, JTable table) {
		if (table.getRowSorter() == null)
			return viewSelection;
		else {
			RowSorter<? extends TableModel> rs = table.getRowSorter();
			int modelSel[] = new int[viewSelection.length];
			for (int i = 0; i < viewSelection.length; i++) {
				modelSel[i] = rs.convertRowIndexToModel(viewSelection[i]);
			}
			return modelSel;
		}
	}
}
