/**
 * 
 */
package be.witmoca.BEATs.ui.liveview;

import javax.swing.JPanel;

import be.witmoca.BEATs.liveview.LiveViewDataClient;

/**
 * @author Witmoca
 *
 */
public class LiveViewPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private final LiveViewDataClient lvdc;

	public LiveViewPanel(LiveViewDataClient lvdc) {
		this.lvdc = lvdc;
	}
	
	public boolean isLvdcActive() {
		return this.lvdc.isActive();
	}
}
