/**
 * 
 */
package be.witmoca.BEATs.ui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.table.AbstractTableModel;

import be.witmoca.BEATs.discovery.DiscoveryServer;
import be.witmoca.BEATs.liveshare.ConnectionsSetChangedListener;
import be.witmoca.BEATs.liveshare.LiveShareClient;
import be.witmoca.BEATs.ui.ApplicationWindow;
import be.witmoca.BEATs.utils.BEATsSettings;
import be.witmoca.BEATs.utils.Lang;

/**
 * @author Jente
 *
 */
public class ShowLiveShareClientMonitor implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent arg0) {
		ClientMonitorModel ccm = new ClientMonitorModel();
		JTable monitor = new JTable(ccm);

		JOptionPane.showMessageDialog(ApplicationWindow.getAPP_WINDOW(),
				new JScrollPane(monitor, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
						JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
				Lang.getUI("menu.liveshare.clientmonitor"), JOptionPane.PLAIN_MESSAGE);
		
		ccm.stopTimer();
	}
	
	private static final String COLUMN_NAME[] = { "Server","Ip", "Connected" };
	
	private class ClientMonitorModel extends AbstractTableModel implements ActionListener, ConnectionsSetChangedListener {
		private static final long serialVersionUID = 1L;
		private final Timer UPDATE_TIMER = new Timer((int) TimeUnit.SECONDS.toMillis(1), this);
		private final Map<String, String> status = new HashMap<String, String>();
		private List<String> watchServers = Collections.emptyList();
		private List<String> connectedServers = Collections.emptyList(); 
		
		private ClientMonitorModel() {
			UPDATE_TIMER.setInitialDelay(300);
			UPDATE_TIMER.start();
			LiveShareClient.addConnectionsSetChangedListener(this);
		}
		
		@Override
		public String getColumnName(int column) {
			return COLUMN_NAME[column];
		}

		@Override
		public int getColumnCount() {
			return COLUMN_NAME.length;
		}

		@Override
		public int getRowCount() {
			return watchServers.size();
		}

		@Override
		public Object getValueAt(int row, int column) {
			switch (column) {
				case 0: 
					return watchServers.get(row);
				case 1: 
					return status.get(watchServers.get(row));
				case 2:
					return connectedServers.contains(watchServers.get(row)) ? Lang.getUI("action.yes") : Lang.getUI("action.no");
				default:
					return null;
			}
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			// get all watchservers
			watchServers = BEATsSettings.LIVESHARE_CLIENT_HOSTLIST.getListValue();
			watchServers.sort((c1, c2) -> c1.compareTo(c2));
			
			// prune all status leftovers
			Set<String> delete = status.keySet();
			delete.removeAll(watchServers);
			for(String del : delete)
				status.remove(del);
			
			// build status
			// start from "not found"
			for(String s : watchServers)
				status.put(s, Lang.getUI("action.no"));
			// add "discovered"
			List<String> discovered = DiscoveryServer.getDiscoveredSorted();
			for(String dle : discovered)
				status.put(dle, Lang.getUI("action.yes"));
			
			this.fireTableDataChanged();
		}
		
		public void stopTimer() {
			UPDATE_TIMER.stop();
		}

		@Override
		public void connectionsSetChanged(LiveShareClient lsc) {
			this.connectedServers = lsc.getConnectedServerNames();
		}
	}
}
