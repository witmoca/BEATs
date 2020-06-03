/**
 * 
 */
package be.witmoca.BEATs.ui.archivepanel.actions;

import java.sql.SQLException;
import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import be.witmoca.BEATs.connection.CommonSQL;
import be.witmoca.BEATs.connection.DataChangedType;
import be.witmoca.BEATs.connection.SQLConnection;
import be.witmoca.BEATs.ui.ApplicationWindow;
import be.witmoca.BEATs.ui.archivepanel.ArchiveTableModel;
import be.witmoca.BEATs.ui.components.SongTable;
import be.witmoca.BEATs.utils.Lang;
import be.witmoca.BEATs.utils.UiIcon;

/**
 * @author Witmoca
 *
 */
public class ChangeGenreAction extends MultisongChangeAbstractAction {
	private static final long serialVersionUID = 1L;

	public ChangeGenreAction(SongTable table) {
		super(table, Lang.getUI("col.genre"));
		this.putValue(Action.SMALL_ICON, UiIcon.EDIT_W.getIcon());
	}

	@Override
	protected void actionPerform(int[] indices) {
		// PREPARE variables for user: first genre from list is always used as default
		String genre = (String) getConnectedTable().getModel().getValueAt(indices[0], 3);
		
		String[] genres = {};
		try {
			genres = CommonSQL.getGenres().toArray(new String[0]);
		} catch (SQLException e2) {
			
		}
		int startIndex = -1;
		for(int i = 0; i < genres.length; i++) {
			if(genres[i].equals(genre)) {
				startIndex = i;
				break;
			}
		}
		
		
		// USER UI interaction
		JPanel userPanel = new JPanel();
		
		JComboBox<String> newGenre = new JComboBox<String>(genres);
		// if the genre doesn't exist or there are no genres, don't select one. (This shouldn't happen)
		if(startIndex != -1)
			newGenre.setSelectedIndex(startIndex);
		newGenre.setEditable(false);
		userPanel.add(newGenre);

		if (JOptionPane.showConfirmDialog(ApplicationWindow.getAPP_WINDOW(), userPanel, Lang.getUI("col.genre"),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
			return; // cancelled
		}

		// MAKE CHANGES (String case is important here! No ignoreCase)
		String newString = (String) newGenre.getSelectedItem();
		if (genre.equals(newString))
			return;
		try {
			ArchiveTableModel atm = ((ArchiveTableModel) getConnectedTable().getModel());
			for(int i : indices) {
				CommonSQL.updateGenreInArchive(atm.getRowId(i), newString);
			}
			
			// Commit archive changes= Not exactly true, but true enough for our purposes.
			// We'll take the overhead as is.
			SQLConnection.getDbConn().commit(DataChangedType.ARCHIVE_DATA_OPTS);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

}
