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
* File: ArtistCatalog.java
* Created: 2019
*/
package be.witmoca.BEATs.ui.artistcatalog;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SortOrder;
import javax.swing.RowSorter.SortKey;

import be.witmoca.BEATs.ui.t4j.RowNumberTable;

public class ArtistCatalog extends JPanel {
	private static final long serialVersionUID = 1L;

	private final CatalogTable catalogTable = new CatalogTable();
	private final JScrollPane scrollPane = new JScrollPane(catalogTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	
	public ArtistCatalog() {
		super(new BorderLayout());
		
		//this.add(new ArchiveToolbar(archiveTable), BorderLayout.NORTH);
		this.add(scrollPane, BorderLayout.CENTER);
		
		List<SortKey> defaultSort = new ArrayList<SortKey>();
		defaultSort.add(new SortKey(0, SortOrder.DESCENDING));
		defaultSort.add(new SortKey(1, SortOrder.ASCENDING));
		JTable rowTable = new RowNumberTable(catalogTable, defaultSort);
		scrollPane.setRowHeaderView(rowTable);
		scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, rowTable.getTableHeader());
	}
}
