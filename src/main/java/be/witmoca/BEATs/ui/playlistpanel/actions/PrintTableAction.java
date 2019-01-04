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
* File: PrintTableAction.java
* Created: 2019
*/
package be.witmoca.BEATs.ui.playlistpanel.actions;

import java.awt.event.ActionEvent;
import java.awt.print.PrinterException;
import javax.swing.AbstractAction;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import be.witmoca.BEATs.utils.Lang;
import be.witmoca.BEATs.utils.UiIcon;

class PrintTableAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
	private final JTable table;

	PrintTableAction(JTable table) {
		super(Lang.getUI("action.print"), UiIcon.PRINT.getIcon());
		this.table = table;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			// Hide the last row
			TableColumnModel columnModel = table.getColumnModel();
			TableColumn col = columnModel.getColumn(3);
			columnModel.removeColumn(col);
			table.print();
			columnModel.addColumn(col);

		} catch (PrinterException e1) {
			e1.printStackTrace();
		}
	}

}
