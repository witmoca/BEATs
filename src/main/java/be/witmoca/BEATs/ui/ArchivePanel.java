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
* File: ArchivePanel.java
* Created: 2018
*/
package be.witmoca.BEATs.ui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import be.witmoca.BEATs.ui.t4j.MultisortTableHeaderCellRenderer;
import be.witmoca.BEATs.ui.t4j.RowNumberTable;
import be.witmoca.BEATs.ui.t4j.TableColumnManager;

public class ArchivePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	public static final String TITLE = "Archive"; 
	
	private final JTable archiveTable = new ArchiveTable();
	private final JScrollPane archiveScrollPane = new JScrollPane(archiveTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	
	public ArchivePanel() {
		super(new BorderLayout());
		
		this.add(new ArchiveToolbar(archiveTable), BorderLayout.NORTH);
		this.add(archiveScrollPane, BorderLayout.CENTER);
		
		// Table Column Manager (choose the available columns)
		new TableColumnManager(archiveTable);
		
		// Add a rowsorter and render icons at the top to indicate sorting order
		archiveTable.setRowSorter(new ArchiveTableRowSorter<>(archiveTable.getModel()));
		archiveTable.getTableHeader().setDefaultRenderer(new MultisortTableHeaderCellRenderer());
		
		// Add a rownumbers (has to go AFTER the rowsorter)
		JTable rowTable = new RowNumberTable(archiveTable);
		archiveScrollPane.setRowHeaderView(rowTable);
		archiveScrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, rowTable.getTableHeader());
	}
}
