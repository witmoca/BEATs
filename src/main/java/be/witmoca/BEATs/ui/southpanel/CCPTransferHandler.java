/**
 * 
 */
package be.witmoca.BEATs.ui.southpanel;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;

import be.witmoca.BEATs.ApplicationManager;
import be.witmoca.BEATs.model.DataChangedListener;
import be.witmoca.BEATs.model.PlaylistEntry;
import be.witmoca.BEATs.model.TransferableSongs;

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
* File: CCPTransferHandler.java
* Created: 2018
*/
public class CCPTransferHandler extends TransferHandler {
	private static final long serialVersionUID = 1L;

	@Override
	public boolean canImport(TransferSupport support) {
		DataFlavor[] transferFlavors = support.getDataFlavors();

		for (DataFlavor df : transferFlavors) {
			if (df.isMimeTypeEqual(TransferableSongs.MIME_TYPE))
				return true;
		}
		return false;
	}

	@Override
	public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
		return false;
	}

	@Override
	public boolean importData(TransferSupport support) {
		if (!this.canImport(support))
			return false;

		try {
			Object o = support.getTransferable().getTransferData(new DataFlavor(TransferableSongs.MIME_TYPE));
			if (!(o instanceof List<?>)) {
				throw new ClassCastException("Cannot cast " + o.getClass() + " to List<?>");
			}
			List<?> lpe = (List<?>) o;
			try (PreparedStatement insertCCP = ApplicationManager.getDB_CONN().prepareStatement("INSERT INTO ccp VALUES (?,?,?)")) {
				for (Object songO : lpe) {
					PlaylistEntry pe = (PlaylistEntry) songO;
					for (int i = 0; i < 3; i++)
						insertCCP.setString(i + 1, pe.getColumn(i));
					insertCCP.executeUpdate();
				}
			}
			ApplicationManager.getDB_CONN().commit(EnumSet.of(DataChangedListener.DataType.CCP));
		} catch (ClassNotFoundException | UnsupportedFlavorException | IOException | SQLException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	@Override
	public boolean importData(JComponent comp, Transferable t) {
		return false;
	}

	@Override
	public int getSourceActions(JComponent c) {
		return TransferHandler.MOVE;
	}

	@Override
	protected Transferable createTransferable(JComponent c) {
		if (!(c instanceof JList))
			return null;

		JList<?> list = ((JList<?>) c);
		if (!(list.getModel() instanceof CCPListModel))
			return null;
		CCPListModel model = (CCPListModel) list.getModel();

		int indices[] = list.getSelectedIndices();
		List<PlaylistEntry> peList = new ArrayList<PlaylistEntry>();
		for (int i = 0; i < indices.length; i++) {
			peList.add(model.getEntry(indices[i]));
		}
		
		return new TransferableSongs(peList);
	}

	@Override
	protected void exportDone(JComponent source, Transferable data, int action) {
		if (action != TransferHandler.MOVE)
			return;

		try {
			Object o = data.getTransferData(new DataFlavor(TransferableSongs.MIME_TYPE));
			if (!(o instanceof List<?>)) {
				throw new ClassCastException("Cannot cast " + o.getClass() + " to List<?>");
			}
			List<?> lpe = (List<?>) o;

			try (PreparedStatement delRow = ApplicationManager.getDB_CONN().prepareStatement("DELETE FROM CCP WHERE Artist = ? AND Song = ? AND Comment = ?")) {
				for (Object songO : lpe) {
					PlaylistEntry pe = (PlaylistEntry) songO;
					delRow.setString(1, pe.getColumn(0));
					delRow.setString(2, pe.getColumn(1));
					delRow.setString(3, pe.getColumn(2));
					delRow.executeUpdate();
				}
			}

			ApplicationManager.getDB_CONN().commit(EnumSet.of(DataChangedListener.DataType.CCP));
		} catch (SQLException | ClassNotFoundException | UnsupportedFlavorException | IOException e) {
			e.printStackTrace();
		}
	}
}
