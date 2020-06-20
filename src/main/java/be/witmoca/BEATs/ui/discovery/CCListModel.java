/**
 * 
 */
package be.witmoca.BEATs.ui.discovery;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import be.witmoca.BEATs.utils.BEATsSettings;

/*
*
+===============================================================================+
|    BEATs (Burning Ember Archival Tool suite)                                  |
|    Copyright 2020 Jente Heremans                                              |
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
* File: CCListModel.java
* Created: 2020
*/
public class CCListModel implements ListModel<String> {
	private List<String> content= new ArrayList<String>();
	private List<ListDataListener> ldl = new ArrayList<ListDataListener>();
	
	public CCListModel() {
		UpdateContent();
	}
	
	@Override
	public int getSize() {
		return content.size();
	}

	@Override
	public String getElementAt(int index) {
		return content.get(index);
	}

	@Override
	public void addListDataListener(ListDataListener l) {
		ldl.add(l);
	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		ldl.remove(l);
	}
	
	public void UpdateContent() {
		content = BEATsSettings.LIVESHARE_CLIENT_HOSTLIST.getListValue();
		// update listeners
		for(ListDataListener l : ldl) {
			l.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, this.getSize()));
		}
	}
}
