/**
 * 
 */
package be.witmoca.BEATs.ui.playlistpanel.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;

import be.witmoca.BEATs.filefilters.CSVPlaylistFileFilter;
import be.witmoca.BEATs.ui.ApplicationWindow;
import be.witmoca.BEATs.utils.Lang;
import be.witmoca.BEATs.utils.UiIcon;

/**
 * 
 */
public class ImportPlaylistAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
	private final JTable playlistTable;
	
	ImportPlaylistAction(JTable table){
		super(Lang.getUI("action.import"), UiIcon.IMPORT.getIcon());
		this.playlistTable = table;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		final JFileChooser fc = new JFileChooser();		
		fc.setAcceptAllFileFilterUsed(false);
		fc.setFileFilter(new CSVPlaylistFileFilter(this.playlistTable));
		
		// Check for Cancel/Error
		if (fc.showOpenDialog(ApplicationWindow.getAPP_WINDOW()) != JFileChooser.APPROVE_OPTION) {
			return;
		}
		
		try {
			// execute the import contained in the filefilter with the given file
			((CSVPlaylistFileFilter) fc.getFileFilter()).importFile(fc.getSelectedFile());
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(ApplicationWindow.getAPP_WINDOW(),
					"Could not open file:\n" + e1.getClass() + "\n" + e1.getLocalizedMessage(), "Import Error",
					JOptionPane.ERROR_MESSAGE);
			e1.printStackTrace();
		}
	}

}
