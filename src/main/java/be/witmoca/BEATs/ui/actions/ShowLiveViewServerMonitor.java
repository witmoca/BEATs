/**
 * 
 */
package be.witmoca.BEATs.ui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.table.AbstractTableModel;

import be.witmoca.BEATs.liveview.LiveViewDataServer;
import be.witmoca.BEATs.ui.ApplicationWindow;
import be.witmoca.BEATs.utils.BEATsSettings;
import be.witmoca.BEATs.utils.Lang;

/**
 * @author Witmoca
 *
 */
public class ShowLiveViewServerMonitor implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {
		JTable monitor = new JTable(new ServerMonitorModel());

		JOptionPane.showMessageDialog(ApplicationWindow.getAPP_WINDOW(),
				new JScrollPane(monitor, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
						JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
				Lang.getUI("menu.liveview.servermonitor"), JOptionPane.PLAIN_MESSAGE);
	}
	
	
	private static final String COLUMN_NAME[] = { "Host","IP" };
	
	private class ServerMonitorModel extends AbstractTableModel implements ActionListener {
		private static final long serialVersionUID = 1L;
		private final Timer UPDATE_TIMER = new Timer((int) TimeUnit.SECONDS.toMillis(2), this);
		
		List<String[]> content = new ArrayList<String[]>();
		
		private ServerMonitorModel() {
			UPDATE_TIMER.setInitialDelay(300);
			UPDATE_TIMER.start();
		}
		
		// Called on timer
		@Override
		public void actionPerformed(ActionEvent e) {
			updateData();
		}
		
		private void updateData() {
			content.clear();
			LiveViewDataServer[] cons;
			synchronized(LiveViewDataServer.getConnections()) {
				cons = LiveViewDataServer.getConnections().toArray(new LiveViewDataServer[0]);
			}

			for(LiveViewDataServer lvds : cons) {
				content.add(new String[]{lvds.getClientHostName(), lvds.getClientIp()});
			}
			this.fireTableDataChanged();
		}
		
		
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}

		@Override
		public int getRowCount() {
			return content.size();
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return content.get(rowIndex)[columnIndex];
		}

		@Override
		public String getColumnName(int column) {
			if(column == 1)
				return COLUMN_NAME[column] + " (Max " + BEATsSettings.LIVESHARE_SERVER_MAXCONNECTIONS.getIntValue() + ")";
			return COLUMN_NAME[column];
		}
		
		
	}
}
