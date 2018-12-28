/**
 * 
 */
package be.witmoca.BEATs.connection;

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
* File: ConnectionException.java
* Created: 2018
*/
public class ConnectionException extends Exception {
	private static final long serialVersionUID = 1L;
	
	private final ConnState state;
	
	public static enum ConnState {
		DB_ALREADY_LOCKED, 			// Another instance has locked the db
		GENERAL_EXCEPTION, 			// General statement exception (usually sql syntax error)
		APP_ID_INVALID, 			// Db Application id does not match application
		APP_OUTDATED, 				// Db version is higher than the application version
		DB_OUTDATED,				// Major version of the DB is lower then Major of the application (suggest import instead)
		FOREIGN_KEYS_CONSTRAINTS,	// Foreign key constraints failed
		INTEGRITY_FAILED,			// Db failed the integrity check
		VACUUM_FAILED;				// Db VACUUM failed
	}
	
	
	ConnectionException(ConnState state, Throwable t) {
		super(t);
		if(state == null)
			throw new IllegalArgumentException("state can't be null");
		this.state = state;
	}

	public ConnState getState() {
		return state;
	}

	@Override
	public String getLocalizedMessage() {
		return super.getLocalizedMessage() +  "{" + this.getState().name() + "}";
	}
	
	
}
