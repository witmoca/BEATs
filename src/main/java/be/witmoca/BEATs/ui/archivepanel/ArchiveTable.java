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
* File: ArchiveTable.java
* Created: 2018
*/
package be.witmoca.BEATs.ui.archivepanel;

import javax.swing.JTable;
import be.witmoca.BEATs.model.ArchiveTableModel;
import be.witmoca.BEATs.ui.t4j.MultisortTableHeaderCellRenderer;
import be.witmoca.BEATs.ui.t4j.TableColumnManager;

public class ArchiveTable extends JTable {
	private static final long serialVersionUID = 1L;

	public ArchiveTable() {
		super(new ArchiveTableModel());
		this.getTableHeader().setReorderingAllowed(false);

		// Table Column Manager (choose the available columns)
		new TableColumnManager(this);

		// Add a rowsorter and render icons at the top to indicate sorting order
		this.setRowSorter(new ArchiveTableRowSorter<>(this.getModel()));
		this.getTableHeader().setDefaultRenderer(new MultisortTableHeaderCellRenderer());
	}
}
