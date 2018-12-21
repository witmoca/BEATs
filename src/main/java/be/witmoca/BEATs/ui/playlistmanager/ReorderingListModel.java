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
* File: ReorderingListModel.java
* Created: 2018
*/
package be.witmoca.BEATs.ui.playlistmanager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractListModel;

import be.witmoca.BEATs.ApplicationManager;

public class ReorderingListModel extends AbstractListModel<String> {
	private static final long serialVersionUID = 1L;
	private final Map<Integer, String> contents = new HashMap<Integer, String>();
	
	public ReorderingListModel() {
		try (PreparedStatement selPlaylist = ApplicationManager.getDB_CONN().prepareStatement("SELECT PlaylistName FROM playlist ORDER BY TabOrder ASC")) {
			ResultSet rs = selPlaylist.executeQuery();
			int i = 0;
			while (rs.next())
				contents.put(i++, rs.getString(1));
		} catch (SQLException e1) {
			e1.printStackTrace();
			return;
		}
	}
	
	@Override
	public int getSize() {
		return contents.size();
	}

	@Override
	public String getElementAt(int index) {
		if(index >= this.getSize())
			return null;
		return contents.get(index);
	}

	public void addElement(String name) {
		name = name.trim();
		if(name.isEmpty())
			return;
		this.insertElement(name, this.getSize());
	}
	
	public void insertElement(String name, int index) {
		if(index < 0)
			return;
		// remove an element if it is already in the list
		if(contents.containsValue(name)) {
			int key = this.findKey(name);
			if(key != -1)
				this.removeElement(key);
		}

		if(index >= this.getSize())
			contents.put(this.getSize(), name);
		else {
			// Move contents to the right (starting at the back, until index is freed)
			for(int i = this.getSize(); i > index ; i--) {
				contents.put(i, contents.get(i-1));
			}
			contents.put(index, name);
		}

		this.fireContentsChanged(this, index, contents.size()-1);
	}
	
	public void removeElement(int index) {
		if(index < 0 || index >= this.getSize())
			return;

		// move all values to the left (overwriting index)
		for(int i = index ; i < this.getSize()-1; i++) {
			contents.put(i, contents.get(i+1));
		}
		contents.remove(this.getSize()-1);
		this.fireContentsChanged(this, index, contents.size()-1);
	}
	
	/**
	 *  Loops through the contents to find the specified string
	* @param item String to find index of
	* @return index of String (or -1 if none found)
	 */
	public int findKey(String item) {
		for(int i = 0; i < this.getSize(); i++) {
			if(item.equals(contents.get(i))) {
				return i;
			}
		}
		return -1;
	}
	
	public List<String> getNewPlaylists(){
		ArrayList<String> al = new ArrayList<String>();
		for(int i = 0; i < this.getSize(); i ++) {
			al.add(contents.get(i));
		}
		return al;
	}
}
