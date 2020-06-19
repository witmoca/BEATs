/**
 * 
 */
package be.witmoca.BEATs.ui.liveshare;

import javax.swing.table.TableRowSorter;

import be.witmoca.BEATs.clipboard.TransferableSongList;
import be.witmoca.BEATs.liveshare.LiveShareDataClient;
import be.witmoca.BEATs.ui.components.SongTable;
import be.witmoca.BEATs.ui.components.SongTableCopyOnlyTransferHandler;
import be.witmoca.BEATs.ui.liveshare.actions.LiveShareKeyBindings;
import be.witmoca.BEATs.ui.liveshare.actions.LiveSharePopupMenu;

/**
 * @author Witmoca
 *
 */
public class LiveShareTable extends SongTable {
	private static final long serialVersionUID = 1L;
	
	public LiveShareTable(String playlistName, LiveShareDataClient lvdc) {
		super(new LiveShareTableModel(playlistName, lvdc));
		
		// Add standard single column rowsorter
		TableRowSorter<LiveShareTableModel> srt = new TableRowSorter<LiveShareTableModel>(
				(LiveShareTableModel) this.getModel());
		srt.setMaxSortKeys(1);
		this.setRowSorter(srt);

		// right click menu
		this.setComponentPopupMenu(new LiveSharePopupMenu(this));

		// Drag and drop logic (no drag and drop, just Cut/Cop/Paste)
		this.setTransferHandler(new SongTableCopyOnlyTransferHandler());

		// Register all keyboard shortcuts to be used on the table
		LiveShareKeyBindings.RegisterKeyBindings(this);
	}



	@Override
	public TransferableSongList getSelectedSongs() {
		int indices[] = this.getSelectedRows();
		if (indices.length == 0)
			return null;

		if (!(this.getModel() instanceof LiveShareTableModel))
			return null;
		LiveShareTableModel model = (LiveShareTableModel) this.getModel();
		
		TransferableSongList list = new TransferableSongList();
		for (int i : indices) {
			// Since this is a copy only table, the RowID can be anything (0 here)
			list.addSong((String) model.getValueAt(i, 0), (String) model.getValueAt(i, 1), 0);
		}
		return list;
	}

}
