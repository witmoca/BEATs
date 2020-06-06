/**
 * 
 */
package be.witmoca.BEATs.ui.archivepanel.actions;

import java.awt.event.KeyEvent;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;

import be.witmoca.BEATs.clipboard.ClipboardActionFactory;
import be.witmoca.BEATs.ui.components.SongTable;

/**
 * @author Witmoca
 *
 */
public class ArchiveKeyBindings {
	public static void RegisterKeyBindings(SongTable table) {
		InputMap im = table.getInputMap();
		ActionMap am = table.getActionMap();
		
		// Delete
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,0), "Archive_Delete");
		am.put("Archive_Delete", new DeleteEntryAction(table));
		
		// CCP
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK), "Archive_Copy");
		am.put("Archive_Copy", ClipboardActionFactory.getCopyAction(table));
	}
}
