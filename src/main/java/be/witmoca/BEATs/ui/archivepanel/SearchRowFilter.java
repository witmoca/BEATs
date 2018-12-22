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
* File: SearchRowFilter.java
* Created: 2018
*/
package be.witmoca.BEATs.ui.archivepanel;

import javax.swing.RowFilter;
import javax.swing.table.TableModel;

import be.witmoca.BEATs.utils.StringUtils;

class SearchRowFilter extends RowFilter<TableModel, Integer> {
	private final String searchString;
	
	
	public SearchRowFilter(String searchString) {
		this.searchString = StringUtils.filterPrefix(searchString).toLowerCase();
	}
	
	@Override
	public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
		TableModel model = entry.getModel();
		int cCount = model.getColumnCount();
		
		for(int i = 0; i < cCount; i ++) {
			if (model.getValueAt(entry.getIdentifier(), i).toString().toLowerCase().contains(searchString))
				return true;
		}
		return false;
	}
}
