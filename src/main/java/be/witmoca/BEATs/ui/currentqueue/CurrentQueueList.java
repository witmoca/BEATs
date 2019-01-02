/**
 * 
 */
package be.witmoca.BEATs.ui.currentqueue;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;

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
* File: CurrentQueueList.java
* Created: 2018
*/
class CurrentQueueList extends JList<String> {
	private static final long serialVersionUID = 1L;

	public CurrentQueueList(ListModel<String> dataModel) {
		super(dataModel);

		// add mouselistener: Rightclick (popupmenu open) also adjusts selector
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					JList<?> list = (JList<?>) e.getSource();
					int index = list.locationToIndex(e.getPoint());
					if (index < 0)
						return;
					list.setSelectedIndex(index);
				}
			}
		});
	}

	@Override
	public Point getPopupLocation(MouseEvent event) {
		// calculate bottomleft corner of the selected item
		int index = this.getSelectedIndex();
		if (index < 0)
			return null;
		Point t = this.indexToLocation(index);
		Rectangle bounds = this.getCellBounds(index, index);
		if (bounds == null)
			return null;
		return new Point(t.x, t.y + bounds.height);
	}

}
