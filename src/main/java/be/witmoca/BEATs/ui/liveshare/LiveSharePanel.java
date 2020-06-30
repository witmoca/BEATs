/**
 * 
 */
package be.witmoca.BEATs.ui.liveshare;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import be.witmoca.BEATs.liveshare.LiveShareClient;
import be.witmoca.BEATs.ui.liveshare.actions.LiveShareToolbar;
import be.witmoca.BEATs.ui.southpanel.SouthPanel;
import be.witmoca.BEATs.ui.t4j.RowNumberTable;

/**
 * @author Witmoca
 *
 */
public class LiveSharePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private final LiveShareTable lvTable;
	private final JScrollPane scrollPane;
	private final JPanel southPanel;
	
	public LiveSharePanel(String playlistName, LiveShareClient lsc, String serverName) {
		super(new BorderLayout());
		
		lvTable = new LiveShareTable(playlistName, lsc, serverName);
		southPanel = new SouthPanel(lvTable, 0, 1);
		scrollPane = new JScrollPane(lvTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		this.add(new LiveShareToolbar(lvTable), BorderLayout.NORTH);
		this.add(scrollPane, BorderLayout.CENTER);
		this.add(southPanel, BorderLayout.SOUTH);

		// Row numbers
		JTable rowTable = new RowNumberTable(lvTable, null);
		scrollPane.setRowHeaderView(rowTable);
		scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, rowTable.getTableHeader());
	}
}
