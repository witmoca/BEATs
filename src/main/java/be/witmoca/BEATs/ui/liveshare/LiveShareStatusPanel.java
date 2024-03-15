/**
 *
 */
package be.witmoca.BEATs.ui.liveshare;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import be.witmoca.BEATs.liveshare.LiveShareClient;
import be.witmoca.BEATs.utils.Lang;

/**
 *
 */
public class LiveShareStatusPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	private static final int TIMER_DELAY_MS = 1000; // update the info screen every second
	private static final long THRESHOLD_MIN_MS = LiveShareClient.TIMER_UPDATE_PERIOD_MS * 3; // Do not show a seconds counter below this threshold
	private static final long THRESHOLD_MAX_MS = LiveShareClient.TIMER_UPDATE_PERIOD_MS * 60; // Do not show a seconds counter above this threshold

	private final LiveShareClient lsc;
	private final Timer updateTimer;
	private final String serverName;
	private final JLabel connectionHealthLabel;
	private final Color defaultLabelBackground; // default color of the text in a label

	public LiveShareStatusPanel(LiveShareClient lsc, String serverName) {
		super(new GridLayout());

		this.lsc = lsc;
		this.serverName = serverName;

		this.updateTimer = new Timer(TIMER_DELAY_MS, this);
		this.updateTimer.setRepeats(true);
		this.updateTimer.start();

		this.connectionHealthLabel = new JLabel("");
		this.connectionHealthLabel.setOpaque(true);
		this.add(this.connectionHealthLabel);
		this.defaultLabelBackground = this.connectionHealthLabel.getBackground();
	}

	/**
	 * Updates the Panel on a regular timer
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		// Check if the application isn't exiting
		if (lsc.isStopping()) {
			this.updateTimer.stop();
			return;
		}

		// Update the connection healthy label
		Instant t = this.lsc.getLastSuccessfullReceipt(serverName);
		if(t == null) {
			this.connectionHealthLabel.setText(Lang.getUI("LiveShareStatusPanel.connectionHealth.noConnection"));
		} else {
			// calculate the amount of seconds that have passed since the last data packet
			long secondsPast = ChronoUnit.SECONDS.between(t,Instant.now());
			if(secondsPast * 1000 <= THRESHOLD_MIN_MS) {
				// If connection healthy, show message
				this.connectionHealthLabel.setText(Lang.getUI("LiveShareStatusPanel.connectionHealth.belowThreshold"));
				this.connectionHealthLabel.setBackground(this.defaultLabelBackground);
			} else if (secondsPast * 1000 >= THRESHOLD_MAX_MS) {
				// If connection stale, show message
				this.connectionHealthLabel.setText(Lang.getUI("LiveShareStatusPanel.connectionHealth.overThreshold"));
				this.connectionHealthLabel.setBackground(Color.RED);
			} else {
				this.connectionHealthLabel.setText(Lang.getUI("LiveShareStatusPanel.connectionHealth.secondsCounter") + ": " + secondsPast);
				// Flash the color slowly, to get attention
				if(this.connectionHealthLabel.getBackground().equals(Color.RED)) {
					this.connectionHealthLabel.setBackground(this.defaultLabelBackground);
				} else {
					this.connectionHealthLabel.setBackground(Color.RED);
				}
			}
		}
	}
}
