/**
 * 
 */
package be.witmoca.BEATs.ui.playlistpanel;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.EnumSet;
import javax.swing.JComponent;
import javax.swing.TransferHandler;
import be.witmoca.BEATs.clipboard.TransferableSong;
import be.witmoca.BEATs.connection.SQLConnection;
import be.witmoca.BEATs.connection.CommonSQL;
import be.witmoca.BEATs.connection.DataChangedType;

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
* File: PlaylistTransferHandler.java
* Created: 2018
*/
class PlaylistTransferHandler extends TransferHandler {
	private static final long serialVersionUID = 1L;

	@Override
	public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
		return false;
	}

	@Override
	public boolean canImport(TransferSupport support) {
		if (!(support.getComponent() instanceof PlaylistTable))
			return false;

		DataFlavor[] transferFlavors = support.getDataFlavors();

		for (DataFlavor df : transferFlavors) {
			if (df.equals(TransferableSong.FLAVOR))
				return true;
		}
		return false;
	}

	@Override
	public boolean importData(JComponent comp, Transferable t) {
		return false;
	}

	@Override
	public boolean importData(TransferSupport support) {
		if (!this.canImport(support))
			return false;

		String pName = ((PlaylistTable) support.getComponent()).getPlaylistName();

		try {
			Object o = support.getTransferable().getTransferData(TransferableSong.FLAVOR);
			if (!(o instanceof TransferableSong)) {
				throw new ClassCastException("TransferableSong");
			}
			TransferableSong ts = (TransferableSong) o;

			CommonSQL.addSongInPlaylist(pName, ts.getARTIST(), ts.getSONG(), "");

			SQLConnection.getDbConn().commit(EnumSet.of(DataChangedType.SONGS_IN_PLAYLIST));
		} catch (UnsupportedFlavorException | IOException | SQLException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	@Override
	public int getSourceActions(JComponent c) {
		return TransferHandler.COPY_OR_MOVE;
	}

	@Override
	protected Transferable createTransferable(JComponent c) {
		if (!(c instanceof PlaylistTable))
			return null;
		return ((PlaylistTable) c).getSelectedSong();
	}

	@Override
	protected void exportDone(JComponent source, Transferable data, int action) {
		if (action == TransferHandler.MOVE) {
			try {
				Object o = data.getTransferData(TransferableSong.FLAVOR);
				if (!(o instanceof TransferableSong)) {
					throw new ClassCastException("TransferableSong");
				}
				TransferableSong ts = (TransferableSong) o;

				CommonSQL.removeFromSongsInPlaylist(ts.getROWID());

				SQLConnection.getDbConn().commit(EnumSet.of(DataChangedType.SONGS_IN_PLAYLIST));
			} catch (SQLException | UnsupportedFlavorException | IOException e) {
				e.printStackTrace();
			}
		}
	}

}
