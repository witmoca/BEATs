/**
 * 
 */
package be.witmoca.BEATs.ui.artistcatalog.actions;

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
public class ArtistCatalogKeyBindings {
	public static void RegisterKeyBindings(SongTable table) {
		InputMap im = table.getInputMap();
		ActionMap am = table.getActionMap();
		
		// CCP
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK), "ArtistCatalog_Copy");
		am.put("ArtistCatalog_Copy", ClipboardActionFactory.getCopyAction(table));
	}
}
