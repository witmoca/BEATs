/**
 * 
 */
package be.witmoca.BEATs.ui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;
import be.witmoca.BEATs.ui.ApplicationWindow;
import be.witmoca.BEATs.utils.Lang;
import be.witmoca.BEATs.utils.VersionChecker;

/**
 * @author Witmoca
 *
 */
public class ShowUpdateCheckDialogAction implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {		
		JOptionPane.showMessageDialog(ApplicationWindow.getAPP_WINDOW(),
				VersionChecker.CheckVersion(),
				Lang.getUI("menu.update"), JOptionPane.PLAIN_MESSAGE);
	}
}
