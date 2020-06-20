/**
 * 
 */
package be.witmoca.BEATs.discovery;

import java.time.LocalTime;

/*
*
+===============================================================================+
|    BEATs (Burning Ember Archival Tool suite)                                  |
|    Copyright 2020 Jente Heremans                                              |
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
* File: DiscoveryListEntry.java
* Created: 2020
*/
public class DiscoveryListEntry {
	private final String hostname;
	private final int port;
	private final String ip;
	private final LocalTime timestamp;
	
	public DiscoveryListEntry(String hostname, int port, String ip){
		this.ip = ip;
		this.hostname = hostname;
		this.port = port;
		this.timestamp = LocalTime.now();
	}

	public String getHostname() {
		return hostname;
	}

	public int getPort() {
		return port;
	}

	public LocalTime getTimestamp() {
		return timestamp;
	}

	public String getIp() {
		return ip;
	}
}
