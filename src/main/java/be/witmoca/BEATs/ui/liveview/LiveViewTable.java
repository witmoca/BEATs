/**
 * 
 */
package be.witmoca.BEATs.ui.liveview;

import javax.swing.table.TableRowSorter;

import be.witmoca.BEATs.clipboard.TransferableSongList;
import be.witmoca.BEATs.liveview.LiveViewDataClient;
import be.witmoca.BEATs.ui.components.SongTable;
import be.witmoca.BEATs.ui.components.SongTableCopyOnlyTransferHandler;
import be.witmoca.BEATs.ui.liveview.actions.LiveViewKeyBindings;
import be.witmoca.BEATs.ui.liveview.actions.LiveViewPopupMenu;

/**
 * @author Witmoca
 *
 */
public class LiveViewTable extends SongTable {
	private static final long serialVersionUID = 1L;
	
	public LiveViewTable(String playlistName, LiveViewDataClient lvdc) {
		super(new LiveViewTableModel(playlistName, lvdc));
		
		// Add standard single column rowsorter
		TableRowSorter<LiveViewTableModel> srt = new TableRowSorter<LiveViewTableModel>(
				(LiveViewTableModel) this.getModel());
		srt.setMaxSortKeys(1);
		this.setRowSorter(srt);

		// right click menu
		this.setComponentPopupMenu(new LiveViewPopupMenu(this));

		// Drag and drop logic (no drag and drop, just Cut/Cop/Paste)
		this.setTransferHandler(new SongTableCopyOnlyTransferHandler());

		// Register all keyboard shortcuts to be used on the table
		LiveViewKeyBindings.RegisterKeyBindings(this);
	}



	@Override
	public TransferableSongList getSelectedSongs() {
		int indices[] = this.getSelectedRows();
		if (indices.length == 0)
			return null;

		if (!(this.getModel() instanceof LiveViewTableModel))
			return null;
		LiveViewTableModel model = (LiveViewTableModel) this.getModel();
		
		TransferableSongList list = new TransferableSongList();
		for (int i : indices) {
			// Since this is a copy only table, the RowID can be anything (0 here)
			list.addSong((String) model.getValueAt(i, 0), (String) model.getValueAt(i, 1), 0);
		}
		return list;
	}

}
