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
* File: PlaylistToolbar.java
* Created: 2018
*/
package be.witmoca.BEATs.ui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.RowSorter;
import javax.swing.table.TableModel;

import be.witmoca.BEATs.Launch;

public class PlaylistToolbar extends JToolBar {
	private static final long serialVersionUID = 1L;

	private final JButton deleteButton;

	private final JTable playlistTable;

	public PlaylistToolbar(JTable table) {
		super("Playlist Toolbar", JToolBar.HORIZONTAL);
		playlistTable = table;

		this.setFloatable(false);

		// Add delete button
		deleteAction delAction = new deleteAction(playlistTable);
		deleteButton = new JButton(delAction);

		this.add(deleteButton);
	}

	/**
	 * General purpose selectionmodel converter
	 * 
	 * @param viewSelection
	 *            the {@code int[]} holding indices from the view
	 * @return {@code int[]} holding corresponding indices from the model (returns
	 *         viewSelection if no rowsorter present)
	 */
	private int[] convertSelectionToModel(int[] viewSelection) {
		if (playlistTable.getRowSorter() == null)
			return viewSelection;
		else {
			RowSorter<? extends TableModel> rs = playlistTable.getRowSorter();
			int modelSel[] = new int[viewSelection.length];
			for (int i = 0; i < viewSelection.length; i++) {
				modelSel[i] = rs.convertRowIndexToModel(viewSelection[i]);
			}
			return modelSel;
		}
	}

	private class deleteAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		private final JTable connectedTable;

		public deleteAction(JTable table) {
			super("Delete");
			this.putValue(Action.ACTION_COMMAND_KEY, "Playlist: Delete Song Selection");
			
			// Attach global hotkey
			playlistTable.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
					this.getValue(Action.ACTION_COMMAND_KEY));
			playlistTable.getActionMap().put(this.getValue(Action.ACTION_COMMAND_KEY),this);
			
			connectedTable = table;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (JOptionPane.showConfirmDialog(Launch.getAPP_WINDOW(),
					"Are you sure you want to delete " + connectedTable.getSelectedRowCount() + " row(s)?", "Delete?",
					 JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION)
				return;

			int rowIndices[] = convertSelectionToModel(connectedTable.getSelectedRows());
			// Make sure that we start deleting at the end of the list
			Arrays.sort(rowIndices);

			// Clearing all cells removes the row
			for (int i = rowIndices.length - 1; i >= 0; i--) {
				for (int column = 0; column < connectedTable.getColumnCount(); column++) {
					connectedTable.getModel().setValueAt("", rowIndices[i], column);
				}
			}
		}
	}
}
