/*
*
+===============================================================================+
|    BEATs (Burning Ember Archival Tool suite)                                  |
|    Copyright 2018 Jente Heremans                                              |
|                                                                               |
|    Licensed under the Apache License, Version 2.0 (the "License");            |
|    you may not use this file except in compliance with the License.           |
|    You may obtain a copy of the License at                                    |
|                                                                               |
|    http://www.apache.org/licenses/LICENSE-2.0                                 |
|                                                                               |
|    Unless required by applicable law or agreed to in writing, software        |
|    distributed under the License is distributed on an "AS IS" BASIS,          |
|    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   |
|    See the License for the specific language governing permissions and        |
|    limitations under the License.                                             |
+===============================================================================+
*
* File: NotificationBar.java
* Created: 2018
*/
package be.witmoca.BEATs.ui;

import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import be.witmoca.BEATs.utils.Lang;
import be.witmoca.BEATs.utils.ResourceLoader;
import be.witmoca.BEATs.utils.StaticSettings;

class NotificationBar extends JPanel {
	private static final long serialVersionUID = 1L;
	private boolean holdsNotifications = false;

	public NotificationBar() {
		this.setBorder(BorderFactory.createEtchedBorder());

		JLabel errorLabel = null;

		if (ResourceLoader.bytesOfErrorData > 0) {
			errorLabel = new JLabel(
					Lang.getUI("notification.errorlogs") + " KBytes: " + ResourceLoader.bytesOfErrorData / 1000);
		}
		if (StaticSettings.getAppVersionInt() == 0) {
			errorLabel = new JLabel(Lang.getUI("notification.development"));
		}

		if (errorLabel != null) {
			errorLabel.setForeground(Color.red);
			this.add(errorLabel);
			holdsNotifications = true;
			return;
		}
	}

	boolean holdsNotifications() {
		return holdsNotifications;
	}
}
