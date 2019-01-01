/**
 * 
 */
package be.witmoca.BEATs.ui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JOptionPane;

import be.witmoca.BEATs.ui.ApplicationWindow;
import be.witmoca.BEATs.utils.Lang;
import be.witmoca.BEATs.utils.ResourceLoader;
import be.witmoca.BEATs.utils.StaticSettings;

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
* File: ShowAboutDialog.java
* Created: 2018
*/
public class ShowAboutDialogAction implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent e) {
		List<String> lines = ResourceLoader.ReadResource("Text/About");
		JOptionPane.showMessageDialog(ApplicationWindow.getAPP_WINDOW(), String.join("\n", lines) + "\n\nVersion: " + StaticSettings.getAppVersionString(), Lang.getUI("menu.about"), JOptionPane.PLAIN_MESSAGE);
	}
}
