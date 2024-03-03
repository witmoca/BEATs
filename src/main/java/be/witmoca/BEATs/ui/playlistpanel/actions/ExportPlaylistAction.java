/**
 * 
 */
package be.witmoca.BEATs.ui.playlistpanel.actions;

import java.awt.event.ActionEvent;
import java.io.File;

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
public class ExportPlaylistAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
	private final JTable playlistTable;
	
	ExportPlaylistAction(JTable table){
		super(Lang.getUI("action.export"), UiIcon.EXPORT.getIcon());
		this.playlistTable = table;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		final JFileChooser fc = new JFileChooser() {
			private static final long serialVersionUID = 1L;

			@Override
			public void approveSelection() {
				// if file already exists => show confirm dialog
				if (getSelectedFile().exists() && JOptionPane.showConfirmDialog(this,
						Lang.getUI("savedFileAction.overwrite"), Lang.getUI("savedFileAction.overwriteTitle"),
						JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
					return;
				}
				super.approveSelection();
			}
		};
		
		fc.setAcceptAllFileFilterUsed(false);
		fc.setFileFilter(new CSVPlaylistFileFilter(this.playlistTable));
		
		// Check for Cancel/Error
		if (fc.showSaveDialog(ApplicationWindow.getAPP_WINDOW()) != JFileChooser.APPROVE_OPTION) {
			return;
		}
		
		String pathToFile = fc.getSelectedFile().getAbsolutePath();
		// Only 1 ".csv" extension!
		while (pathToFile.endsWith(".csv")) {
			pathToFile = pathToFile.substring(0, pathToFile.length() - 4);
		}
		pathToFile += ".csv";
		
		try {
			// execute the import contained in the filefilter with the given file
			((CSVPlaylistFileFilter) fc.getFileFilter()).exportFile(new File(pathToFile));
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(ApplicationWindow.getAPP_WINDOW(),
					"Error during saving:\n" + e1.getLocalizedMessage(), "Export Error!",
					javax.swing.JOptionPane.ERROR_MESSAGE);
		}
	}

}
