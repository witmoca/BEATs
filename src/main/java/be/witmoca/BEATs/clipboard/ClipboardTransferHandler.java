/**
 * 
 */
package be.witmoca.BEATs.clipboard;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import be.witmoca.BEATs.connection.DataChangedType;
import be.witmoca.BEATs.connection.SQLConnection;

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
public class ClipboardTransferHandler extends TransferHandler {
	private static final long serialVersionUID = 1L;
	private static final List<ListSelectionListener> listeners = new ArrayList<ListSelectionListener>();

	private static int selected = -1;

	@Override
	public boolean canImport(TransferSupport support) {
		DataFlavor[] transferFlavors = support.getDataFlavors();

		for (DataFlavor df : transferFlavors) {
			if (df.equals(TransferableSong.FLAVOR))
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
			Object o = support.getTransferable().getTransferData(TransferableSong.FLAVOR);
			if (!(o instanceof TransferableSong)) {
				throw new ClassCastException("TransferableSong");
			}
			TransferableSong ts = (TransferableSong) o;
			try (PreparedStatement insertCCP = SQLConnection.getDbConn()
					.prepareStatement("INSERT INTO ccp (Artist, Song) VALUES (?,?)")) {
				insertCCP.setString(1, ts.getARTIST());
				insertCCP.setString(2, ts.getSONG());
				insertCCP.executeUpdate();
			}
			SQLConnection.getDbConn().commit(EnumSet.of(DataChangedType.CCP));
			// If nothing is selected in CCP window, select the top one
			if(ClipboardTransferHandler.selected < 0)
				setSelected(0);
		} catch (UnsupportedFlavorException | IOException | SQLException e) {
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
		if (selected < 0)
			return null;
		try (PreparedStatement selRow = SQLConnection.getDbConn()
				.prepareStatement("SELECT rowid, artist, song FROM CCP ORDER BY rowid ASC")) {
			ResultSet rs = selRow.executeQuery();
			for (int i = 0; i <= selected; i++) {
				if (!rs.next())
					return null;
			}
			return new TransferableSong(rs.getString(2), rs.getString(3), rs.getInt(1));
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	protected void exportDone(JComponent source, Transferable data, int action) {
		if (action != TransferHandler.MOVE)
			return;

		try {
			Object o = data.getTransferData(TransferableSong.FLAVOR);
			if (!(o instanceof TransferableSong)) {
				throw new ClassCastException("TransferableSong");
			}
			TransferableSong ts = (TransferableSong) o;

			try (PreparedStatement delRow = SQLConnection.getDbConn()
					.prepareStatement("DELETE FROM CCP WHERE rowid = ?")) {
				delRow.setInt(1, ts.getROWID());
				delRow.executeUpdate();
			}

			SQLConnection.getDbConn().commit(EnumSet.of(DataChangedType.CCP));
			setSelected(ClipboardTransferHandler.selected > 1 ? ClipboardTransferHandler.selected - 1 : 0);
		} catch (SQLException | UnsupportedFlavorException | IOException e) {
			e.printStackTrace();
		}
	}

	public static void setSelected(int selected) {
		if (ClipboardTransferHandler.selected == selected)
			return;

		ClipboardTransferHandler.selected = selected;

		for (ListSelectionListener l : listeners) {
			l.valueChanged(new ListSelectionEvent(ClipboardTransferHandler.class, selected, selected, false));
		}
	}

	public static void addListSelectionListener(ListSelectionListener l) {
		listeners.add(l);
	}
}
