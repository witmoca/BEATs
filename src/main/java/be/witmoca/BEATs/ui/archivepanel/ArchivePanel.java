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
package be.witmoca.BEATs.ui.archivepanel;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;

import be.witmoca.BEATs.ui.archivepanel.actions.ArchiveToolbar;
import be.witmoca.BEATs.ui.southpanel.SouthPanel;
import be.witmoca.BEATs.ui.t4j.RowNumberTable;

public class ArchivePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	public static final String TITLE = "Archive"; 
	
	private final ArchiveTable archiveTable = new ArchiveTable();
	private final JScrollPane archiveScrollPane = new JScrollPane(archiveTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	private final JPanel southPanel = new SouthPanel(archiveTable,0,1);
	
	public ArchivePanel() {
		super(new BorderLayout());
		
		this.add(new ArchiveToolbar(archiveTable), BorderLayout.NORTH);
		this.add(archiveScrollPane, BorderLayout.CENTER);
		this.add(southPanel, BorderLayout.SOUTH);
		
		List<SortKey> defaultSort = new ArrayList<SortKey>();
		defaultSort.add(new SortKey(0, SortOrder.ASCENDING));
		defaultSort.add(new SortKey(2, SortOrder.ASCENDING));
		JTable rowTable = new RowNumberTable(archiveTable, defaultSort);
		archiveScrollPane.setRowHeaderView(rowTable);
		archiveScrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, rowTable.getTableHeader());
	}
}
