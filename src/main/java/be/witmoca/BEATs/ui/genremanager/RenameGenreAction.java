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
* File: RenameGenreAction.java
* Created: 2019
*/
package be.witmoca.BEATs.ui.genremanager;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.EnumSet;

import javax.swing.AbstractAction;
import javax.swing.JList;
import javax.swing.JOptionPane;

import be.witmoca.BEATs.connection.CommonSQL;
import be.witmoca.BEATs.connection.DataChangedType;
import be.witmoca.BEATs.connection.SQLConnection;
import be.witmoca.BEATs.utils.Lang;
import be.witmoca.BEATs.utils.UiIcon;

class RenameGenreAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
	private final JList<String> model;
	
	RenameGenreAction(JList<String> model) {
		super(null, UiIcon.EDIT.getIcon());
		this.model = model;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		String gName = model.getSelectedValue();
		if(gName == null)
			return;
		
		String newName = JOptionPane.showInputDialog((Component) e.getSource(), Lang.getUI("genreManager.rename.dialog") + ": ").trim();
		if(newName == null || newName.isEmpty())
			return;
		
		
		try {
			CommonSQL.addGenre(newName);
			CommonSQL.updateAllGenreReferences(gName, newName);
			CommonSQL.removeGenre(gName);	
			SQLConnection.getDbConn().commit(EnumSet.of(DataChangedType.GENRE));
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}
}
