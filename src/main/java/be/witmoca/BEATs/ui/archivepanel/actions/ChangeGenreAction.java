/**
 * 
 */
package be.witmoca.BEATs.ui.archivepanel.actions;

import java.awt.event.ActionEvent;
import java.sql.SQLException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import be.witmoca.BEATs.connection.CommonSQL;
import be.witmoca.BEATs.connection.DataChangedType;
import be.witmoca.BEATs.connection.SQLConnection;
import be.witmoca.BEATs.ui.ApplicationWindow;
import be.witmoca.BEATs.ui.archivepanel.ArchiveTableModel;
import be.witmoca.BEATs.utils.Lang;
import be.witmoca.BEATs.utils.StringUtils;
import be.witmoca.BEATs.utils.UiIcon;

/**
 * @author Witmoca
 *
 */
public class ChangeGenreAction extends AbstractAction {
	private static final long serialVersionUID = 1L;

	private final JTable archive;

	public ChangeGenreAction(JTable table) {
		super(Lang.getUI("col.genre"));
		this.putValue(Action.SMALL_ICON, UiIcon.EDIT_W.getIcon());
		archive = table;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		int index = archive.getSelectedRow();
		int originalIndex = index;
		if (index < 0)
			return;
		if (archive.getRowSorter() != null)
			index = archive.getRowSorter().convertRowIndexToModel(index);

		// PREPARE variables for user
		String genre = (String) archive.getModel().getValueAt(index, 3);
		int rowid = ((ArchiveTableModel) archive.getModel()).getRowId(index);
		
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

		// MAKE CHANGES
		String newString = StringUtils.ToUpperCamelCase((String) newGenre.getSelectedItem());
		if (genre.equalsIgnoreCase(newString))
			return;
		try {
			CommonSQL.updateGenreInArchive(rowid, newString);
			// Commit archive changes= Not exactly true, but true enough for our purposes.
			// We'll take the overhead as is.
			SQLConnection.getDbConn().commit(DataChangedType.ARCHIVE_DATA_OPTS);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		// Reselect the selection that now changed
		archive.setRowSelectionInterval(originalIndex, originalIndex);

	}

}
