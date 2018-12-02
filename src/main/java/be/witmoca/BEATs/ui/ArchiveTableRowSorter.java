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
* File: ArchiveTableRowSorter.java
* Created: 2018
*/
package be.witmoca.BEATs.ui;

import java.util.ArrayList;
import java.util.Comparator;

import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public class ArchiveTableRowSorter<M extends TableModel> extends TableRowSorter<M> implements RowSorterListener{
	public ArchiveTableRowSorter(M model) {
		super(model);	
		this.setSortsOnUpdates(true);
		this.addRowSorterListener(this);
		this.setMaxSortKeys(2);
		this.setComparator(2, new EpisodeComparator());
	}

	@Override
	public void sorterChanged(RowSorterEvent e) {
		if(e.getType() == RowSorterEvent.Type.SORTED || e.getType() == RowSorterEvent.Type.SORT_ORDER_CHANGED) {
			ArrayList<SortKey> keys = new ArrayList<SortKey>();
			keys.add(this.getSortKeys().get(0));
			
			switch (keys.get(0).getColumn()) {
			case 0: 
				keys.add(new SortKey(1, keys.get(0).getSortOrder()) );
				break;
			case 1:
				keys.add(new SortKey(2, keys.get(0).getSortOrder()) );
				break;
			case 2:
				keys.add(new SortKey(0, keys.get(0).getSortOrder()) );
				break;
			case 3:
				keys.add(new SortKey(2, keys.get(0).getSortOrder()) );
				break;
			} 
			this.setSortKeys(keys);
		}
	}
	
    private static class EpisodeComparator implements Comparator<String> {
		@Override
		public int compare(String o1, String o2) {
			char c1[] = o1.toCharArray();
			char c2[] = o2.toCharArray();
			
			// Parse into numeric values
			int i1 = 0;
			for(int i = 0; Character.isDigit(c1[i]); i++) {
				i1 = (i1*10) + Character.getNumericValue(c1[i]);
			}
			int i2 = 0;
			for(int i = 0; Character.isDigit(c2[i]); i++) {
				i2 = (i2*10) + Character.getNumericValue(c2[i]);
			}
			// compare numeric values
			if(i1 != i2)
				return i1 - i2;
			
			// if numeric values are the same => compare OLDSKEWL
			return o1.compareTo(o2);		
		}
    }
}
