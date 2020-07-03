/**
 * 
 */
package be.witmoca.BEATs.ui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetSocketAddress;
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
import be.witmoca.BEATs.liveshare.LiveShareClient;
import be.witmoca.BEATs.liveshare.LiveShareServer;
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
		DiscoveryServer.startBroadcaster(); // start discovering, otherwise "discovered" will always be "no"
		
		JOptionPane.showMessageDialog(ApplicationWindow.getAPP_WINDOW(),
				new JScrollPane(monitor, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
						JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
				Lang.getUI("menu.liveshare.clientmonitor"), JOptionPane.PLAIN_MESSAGE);
		
		DiscoveryServer.stopBroadcaster();
		ccm.stopTimer();
	}
	
	private static final String COLUMN_NAME[] = { "Server","Discovered by protocol", "Connected", "Resolved" };
	
	private class ClientMonitorModel extends AbstractTableModel implements ActionListener {
		private static final long serialVersionUID = 1L;
		private final Timer UPDATE_TIMER = new Timer((int) TimeUnit.SECONDS.toMillis(1), this);
		private final Map<String, String> discovered = new HashMap<String, String>();
		private List<String> watchServers = Collections.emptyList();
		private List<String> connectedServers = Collections.emptyList(); 
		
		private ClientMonitorModel() {
			UPDATE_TIMER.setInitialDelay(300);
			UPDATE_TIMER.start();
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
					return discovered.get(watchServers.get(row));
				case 2:
					return connectedServers.contains(watchServers.get(row)) ? Lang.getUI("action.yes") : Lang.getUI("action.no");
				case 3:
					return (new InetSocketAddress(watchServers.get(row), LiveShareServer.SERVER_PORT).isUnresolved() ? Lang.getUI("action.no") : Lang.getUI("action.yes"));
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
			Set<String> delete = discovered.keySet();
			delete.removeAll(watchServers);
			for(String del : delete)
				discovered.remove(del);
			
			// start from "not found"
			for(String s : watchServers)
				discovered.put(s, Lang.getUI("action.no"));
			// add if "discovered"
			DiscoveryServer.getDiscoveredSorted().forEach(d -> discovered.put(d, Lang.getUI("action.yes")));
			
			// Updated Connected list
			this.connectedServers = LiveShareClient.getLvc() == null ? Collections.emptyList() : LiveShareClient.getLvc().getConnectedServerNames();
			
			this.fireTableDataChanged();
		}
		
		public void stopTimer() {
			UPDATE_TIMER.stop();
		}
	}
}
