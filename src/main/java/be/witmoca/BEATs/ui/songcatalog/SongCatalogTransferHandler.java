/**
 * 
 */
package be.witmoca.BEATs.ui.songcatalog;

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
class SongCatalogTransferHandler extends TransferHandler {
	private static final long serialVersionUID = 1L;

	@Override
	public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
		return false;
	}

	@Override
	public boolean canImport(TransferSupport support) {
		return false;
	}

	@Override
	public boolean importData(JComponent comp, Transferable t) {
		return false;
	}

	@Override
	public boolean importData(TransferSupport support) {
		return false;
	}

	@Override
	public int getSourceActions(JComponent c) {
		return TransferHandler.COPY;
	}

	@Override
	protected Transferable createTransferable(JComponent c) {
		if (!(c instanceof CatalogTable))
			return null;
		return ((CatalogTable) c).getSelectedSong();
	}

	@Override
	protected void exportDone(JComponent source, Transferable data, int action) {
		return;
	}

}
