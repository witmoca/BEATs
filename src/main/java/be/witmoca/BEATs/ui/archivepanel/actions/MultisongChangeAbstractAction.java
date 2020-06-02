/**
 * 
 */
package be.witmoca.BEATs.ui.archivepanel.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;

import be.witmoca.BEATs.ui.ApplicationWindow;
import be.witmoca.BEATs.ui.components.SongTable;
import be.witmoca.BEATs.utils.Lang;

/**
 * @author Witmoca
 *
 */
public abstract class MultisongChangeAbstractAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
	private SongTable connectedTable;

	protected MultisongChangeAbstractAction(SongTable table, String name) {
		super(name);
		connectedTable = table;
	}

	/**
	 * Performs action. Deals with 0 selects and multi-select "are you sure" dialog.
	 * Executes the (abstract) actionPerform function.
	 * Reselects every item after actionPerform completes.
	 * 
	 */
	@Override
	public final void actionPerformed(ActionEvent e) {
		int indices[] = connectedTable.getSelectedRows();
		if (indices.length == 0)
			return;
		
		// Create an "are you sure" dialog if more than one row is selected
		if(indices.length > 1) {
			if (JOptionPane.showConfirmDialog(ApplicationWindow.getAPP_WINDOW(), Lang.getUI("action.multirow"),(String) this.getValue(Action.NAME),
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
				return; // cancelled
			}
		}

		actionPerform(indices);
		
		// Reselect the selection that now changed
		ListSelectionModel lsl = connectedTable.getSelectionModel();
		RowSorter<?> rs = connectedTable.getRowSorter();
			
		for(int i : indices) {
			int index = i;
			if(rs != null)
				index = rs.convertRowIndexToView(index);
			lsl.addSelectionInterval(index, index);
		}
	}
	
	/**
	 * Perform the requested action on items with the given indices (model based)
	 * @param indices model based indices
	 */
	protected abstract void actionPerform(int[] indices);

	protected SongTable getConnectedTable() {
		return connectedTable;
	}

}
