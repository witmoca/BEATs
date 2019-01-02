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
* File: GenreManagerShowAction.java
* Created: 2019
*/
package be.witmoca.BEATs.ui.genremanager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import be.witmoca.BEATs.ui.ApplicationWindow;
import be.witmoca.BEATs.utils.Lang;

public class GenreManagerShowAction implements ActionListener {
	private final JList<String> gList = new JList<String>(new GenreListModel());

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		JPanel contentPanel = new JPanel(new BorderLayout(10, 10));

		// listing of genres
		contentPanel.add(
				new JScrollPane(gList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),
				BorderLayout.CENTER);
		gList.setVisibleRowCount(5);

		// button panel
		JPanel buttonPanel = new JPanel(new GridLayout(0, 1));
		buttonPanel.setBorder(BorderFactory.createEtchedBorder());
		buttonPanel.add(wrapAction(new AddGenreAction()));
		buttonPanel.add(wrapAction(new RenameGenreAction(gList)));
		buttonPanel.add(wrapAction(new DeleteGenreAction(gList)));
		contentPanel.add(buttonPanel, BorderLayout.EAST);

		JOptionPane.showMessageDialog(ApplicationWindow.APP_WINDOW, contentPanel, Lang.getUI("menu.tools.genreManager"),
				JOptionPane.PLAIN_MESSAGE, null);
	}

	private JComponent wrapAction(Action a) {
		JPanel p = new JPanel();
		JButton b = new JButton(a);
		b.setPreferredSize(new Dimension(60, 25));
		p.add(b);
		return p;
	}
}
