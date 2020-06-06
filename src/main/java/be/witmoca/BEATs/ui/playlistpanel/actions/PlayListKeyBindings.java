/**
 * 
 */
package be.witmoca.BEATs.ui.playlistpanel.actions;

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
public class PlayListKeyBindings {
	public static void RegisterKeyBindings(SongTable table) {
		InputMap im = table.getInputMap();
		ActionMap am = table.getActionMap();
		
		// Delete
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,0), "Playlist_Delete");
		am.put("Playlist_Delete", new DeleteAction(table));
		
		// CCP
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK), "Playlist_Cut");
		am.put("Playlist_Cut", ClipboardActionFactory.getCutAction(table));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK), "Playlist_Copy");
		am.put("Playlist_Copy", ClipboardActionFactory.getCopyAction(table));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK), "Playlist_Paste");
		am.put("Playlist_Paste", ClipboardActionFactory.getPasteAction(table));
	}
}
