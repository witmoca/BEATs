/**
 * 
 */
package be.witmoca.BEATs.ui.liveshare.actions;

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
public class LiveShareKeyBindings {
	public static void RegisterKeyBindings(SongTable table) {
		InputMap im = table.getInputMap();
		ActionMap am = table.getActionMap();
		
		// CCP
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK), "Playlist_Copy");
		am.put("Playlist_Copy", ClipboardActionFactory.getCopyAction(table));
	}
}
