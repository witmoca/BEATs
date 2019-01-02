/**
 * 
 */
package be.witmoca.BEATs.ui.components.SuggestCellEditor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;

import be.witmoca.BEATs.connection.SQLConnection;

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
* File: SongMatcher.java
* Created: 2018
*/

class SongMatcher implements IMatcher {
	private final int artistColumn;

	/**
	 * 
	 * @param artistColumn
	 *            The column containing the matching ArtistName values of the
	 *            SongTitle being edited in String format.
	 */
	SongMatcher(int artistColumn) {
		this.artistColumn = artistColumn;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see be.witmoca.BEATs.ui.SuggestCellEditor.IMatcher#match(java.lang.String)
	 */
	@Override
	public List<String> match(String search, boolean forwardOnly, JTable table, int row, int col) {
		// Does not support % or _ characters (special characters from the SQLite LIKE
		// function)
		if (search.contains("%") || search.contains("_"))
			return null;

		// To find a song, we need to know the artist first
		String artist = (String) table.getModel().getValueAt(row, this.artistColumn);
		if (artist.isEmpty())
			return null;

		try (PreparedStatement selMatches = SQLConnection.getDbConn()
				.prepareStatement("SELECT Title FROM Song WHERE ArtistName = ? AND Title LIKE ? ORDER BY Title ASC")) {
			selMatches.setString(1, artist);
			selMatches.setString(2, (forwardOnly ? "" : "%") + search + "%");
			List<String> result = new ArrayList<String>();
			ResultSet rs = selMatches.executeQuery();

			while (rs.next()) {
				result.add(rs.getString(1));
			}
			return result;
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return null;
	}

}
