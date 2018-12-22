/**
 * 
 */
package be.witmoca.BEATs.ui.playlistpanel;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.TransferHandler;
import be.witmoca.BEATs.ApplicationManager;
import be.witmoca.BEATs.model.DataChangedListener;
import be.witmoca.BEATs.model.PlaylistEntry;
import be.witmoca.BEATs.model.SQLObjectTransformer;
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
		if(!(support.getComponent() instanceof PlaylistTable))
			return false;
		
		DataFlavor[] transferFlavors = support.getDataFlavors();
		
		for(DataFlavor df : transferFlavors) {
			if(df.isMimeTypeEqual(TransferableSongs.MIME_TYPE))
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
		if(!this.canImport(support))
			return false;
		
		String pName = ((PlaylistTable) support.getComponent()).getPlaylistName();
		
		try {
			Object o =  support.getTransferable().getTransferData(new DataFlavor(TransferableSongs.MIME_TYPE));
			if (!(o instanceof List<?>) ) {
				throw new ClassCastException("Cannot cast " + o.getClass() + " to List<?>");
			}
			List<?> lpe = (List<?>) o;
			for(Object songO : lpe) {
				PlaylistEntry pe = (PlaylistEntry) songO;
				SQLObjectTransformer.addSongInPlaylist(pName, pe.getColumn(0), pe.getColumn(1), pe.getColumn(2));
			}
			ApplicationManager.getDB_CONN().commit(EnumSet.of(DataChangedListener.DataType.SONGS_IN_PLAYLIST));			
		} catch (ClassNotFoundException | UnsupportedFlavorException | IOException | SQLException e) {
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
		if(!(c instanceof PlaylistTable))
			return null;
		return ((PlaylistTable) c).getSelectedSongs();
	}

	@Override
	protected void exportDone(JComponent source, Transferable data, int action) {		
		System.out.println("Move: " + (action == TransferHandler.MOVE));
		if(action == TransferHandler.MOVE) {			
			try {
				Object o = data.getTransferData(new DataFlavor(TransferableSongs.MIME_TYPE));
				if (!(o instanceof List<?>) ) {
					throw new ClassCastException("Cannot cast " + o.getClass() + " to List<?>");
				}
				List<?> lpe = (List<?>) o;
				
				try (PreparedStatement delRow = ApplicationManager.getDB_CONN().prepareStatement("DELETE FROM SongsInPlaylist WHERE rowid = ?")) {
				for(Object songO : lpe) {
					PlaylistEntry pe = (PlaylistEntry) songO;
						delRow.setInt(1, pe.getROWID());
						delRow.executeUpdate();
					}
				}
				
				ApplicationManager.getDB_CONN().commit(EnumSet.of(DataChangedListener.DataType.SONGS_IN_PLAYLIST));
			} catch (SQLException | ClassNotFoundException | UnsupportedFlavorException | IOException e) {
				e.printStackTrace();
			}		
		}
	}
	
	
}
