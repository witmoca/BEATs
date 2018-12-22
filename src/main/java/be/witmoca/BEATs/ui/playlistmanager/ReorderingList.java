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
* File: ReorderingList.java
* Created: 2018
*/
package be.witmoca.BEATs.ui.playlistmanager;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;

class ReorderingList extends JList<String> {
	private static final long serialVersionUID = 1L;
	private final ReorderingListModel model;

	public ReorderingList() {
		super(new ReorderingListModel());
		model = (ReorderingListModel) this.getModel();

		this.setDragEnabled(true);
		this.setDropMode(DropMode.INSERT);

		this.setTransferHandler(new ReorderTransfer(this));
		
		// GUI
		this.setVisibleRowCount(10);
		this.setPrototypeCellValue(String.format("%30s", ""));
	}
	
	public ReorderingListModel getListModel() {
		return model;
	}

	private class ReorderTransfer extends TransferHandler {
		private static final long serialVersionUID = 1L;
		private final JList<String> src;

		private ReorderTransfer(JList<String> source) {
			src = source;
		}

		@Override
		public int getSourceActions(JComponent c) {
			if (c == src)
				return TransferHandler.MOVE;
			return TransferHandler.NONE;
		}

		@Override
		protected Transferable createTransferable(JComponent c) {
			if (c == src)
				return new StringSelection(src.getSelectedValue());
			return null;
		}

		@Override
		protected void exportDone(JComponent source, Transferable data, int action) {
			return;
		}

		@Override
		public boolean canImport(TransferSupport support) {
			if (!support.isDataFlavorSupported(DataFlavor.stringFlavor))
				return false;
			// check if the droplocation is a valid list index
			return ((JList.DropLocation) support.getDropLocation()).getIndex() != -1;
		}

		@Override
		public boolean importData(TransferSupport support) {
			if (!canImport(support))
				return false;

			String rt;
			try {
				rt = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
			} catch (UnsupportedFlavorException | IOException e) {
				return false;
			}
			if (rt == null || rt.isEmpty())
				return false;

			int index = ((JList.DropLocation) support.getDropLocation()).getIndex();
			// compensate for removed duplicate if drop location is under original
			if(index > model.findKey(rt))
				index--;				
			
			model.insertElement(rt, index);

			return true;
		}
	}
}
