/**
 * 
 */
package be.witmoca.BEATs.ui.archivepanel.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.JTextComponent;

import be.witmoca.BEATs.utils.StringUtils;

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
* File: SortAction.java
* Created: 2018
*/
class SortAction implements ActionListener {
	private final JTextComponent searchTerm;
	private final JTable table;

	/**
	 * Sorts a table when action is performed
	 * @param table the table to sort
	 * @param inputProvider the text component providing the (String) input to search for
	 */
	SortAction(JTable table, JTextComponent inputProvider) {
		this.table = table;
		this.searchTerm = inputProvider;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String sTerm = searchTerm.getText().trim();
		if (!sTerm.isEmpty())
			((TableRowSorter<?>) table.getRowSorter()).setRowFilter(new SearchRowFilter(sTerm));
		else
			((TableRowSorter<?>) table.getRowSorter()).setRowFilter(null);
	}

	private static class SearchRowFilter extends RowFilter<TableModel, Integer> {
		private final String searchString;

		public SearchRowFilter(String searchString) {
			this.searchString = StringUtils.filterPrefix(searchString).toLowerCase();
		}

		@Override
		public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
			TableModel model = entry.getModel();
			int cCount = model.getColumnCount();

			for (int i = 0; i < cCount; i++) {
				if (model.getValueAt(entry.getIdentifier(), i).toString().toLowerCase().contains(searchString))
					return true;
			}
			return false;
		}
	}
}
