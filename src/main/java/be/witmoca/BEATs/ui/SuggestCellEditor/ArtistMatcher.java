/**
 * 
 */
package be.witmoca.BEATs.ui.SuggestCellEditor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import be.witmoca.BEATs.Launch;

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
* File: ArtistMatcher.java
* Created: 2018
*/
public class ArtistMatcher implements IMatcher {

	/* (non-Javadoc)
	 * @see be.witmoca.BEATs.ui.SuggestCellEditor.IMatcher#match(java.lang.String)
	 */
	@Override
	public List<String> match(String search, boolean forwardOnly) {
		// Does not support % or _ characters (special characters from the SQLite LIKE function)
		if(search.contains("%") || search.contains("_"))
			return new ArrayList<String>();;
		
		try (PreparedStatement selMatches = Launch.getDB_CONN().prepareStatement("SELECT ArtistName FROM Artist WHERE ArtistName LIKE ? ORDER BY ArtistName ASC")) {
			selMatches.setString(1, (forwardOnly ? "" : "%" )+search+"%");
			List<String> result = new ArrayList<String>();	
			ResultSet rs = selMatches.executeQuery();
			
			while(rs.next()) {
				result.add(rs.getString(1));
			}
			return result;
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return new ArrayList<String>();
	}

}
