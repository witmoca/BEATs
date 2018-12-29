/**
 * 
 */
package be.witmoca.BEATs.ui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import be.witmoca.BEATs.connection.CommonSQL;
import be.witmoca.BEATs.ui.ApplicationWindow;
import be.witmoca.BEATs.utils.Lang;

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
* File: EpisodeIdContinuityCheckAction.java
* Created: 2018
*/
public class EpisodeIdContinuityCheckAction implements ActionListener {

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		JTextArea result = new JTextArea(10,80);
		result.setEditable(false);
		result.setLineWrap(true);
		result.setWrapStyleWord(true);
		
		try {
			List<Integer> missing = new ArrayList<Integer>();
			Integer lastCorrectIndex = null;
			for(int newIndex : CommonSQL.getEpisodes()) {				
				// lastCorrect null and we are not at the start of the episodeId List?
				if(lastCorrectIndex == null && newIndex > 1) {
					
					for(int i = 1; i < newIndex; i++)
						missing.add(i);
				} else if(lastCorrectIndex != null && lastCorrectIndex + 1 != newIndex) {
					// the newIndex is not the next one after LastCorrectIndex?
					for(int i = lastCorrectIndex + 1; i < newIndex; i++)
						missing.add(i);
				}
				lastCorrectIndex = newIndex;
			}
			if(lastCorrectIndex == null)
				lastCorrectIndex = 0;
			
			// Compiled missing list
			String message = Lang.getUI("continuity.start") + "\n";
			message += Lang.getUI("continuity.recentEp") + ": " + lastCorrectIndex.intValue() + "\n";
			message += Lang.getUI("continuity.missingEp") + ": " + missing.size() + "\n";
			if(missing.size() == 0) {
				message += "\n" + Lang.getUI("continuity.perfect");
			} else {
				message += "\n" + Lang.getUI("continuity.missingEp") + ":\n";
				for(Integer missed : missing)
					message += missed + "\n";
			}
			result.setText(message);
		} catch (SQLException e1) {
			String error = "Error while checking for continuity.\n";
			error += "SQL Error code: " + e1.getErrorCode() + "\n";
			error += e1.getLocalizedMessage() + "\n";
			error += "Stack trace:\n";
			for(StackTraceElement el : e1.getStackTrace()) {
				error += el.toString() + "\n";
			}
			result.setText(error);
		}
		
		JOptionPane.showMessageDialog(ApplicationWindow.getAPP_WINDOW(), new JScrollPane(result, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), Lang.getUI("menu.tools.continuity"), JOptionPane.PLAIN_MESSAGE);
	}

}
