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
* File: EpisodeColumnRenderer.java
* Created: 2019
*/
package be.witmoca.BEATs.ui.components;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class EpisodeColumnRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = 1L;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		if (table == null || !(table.getModel() instanceof ContainsEpisodeColumn))
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		// there is a difference between UI Row that needs to be rendered & the row that contains the actual value
		// This is only necessary for the Date of the episode. The episode number is already correctly contained in 'Value'
		int dateRow = row;
		if (table.getRowSorter() != null)
			dateRow = table.getRowSorter().convertRowIndexToModel(row);

		return super.getTableCellRendererComponent(table,
				value + " (" + ((ContainsEpisodeColumn) table.getModel()).getEpisodeDate(dateRow) + ")", isSelected,
				hasFocus, row, column);
	}
}
