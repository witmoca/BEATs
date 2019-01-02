/**
 * 
 */
package be.witmoca.BEATs.utils;

import java.io.IOException;
import java.io.OutputStream;

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
* File: DuplicateOutputStream.java
* Created: 2018
*/
class DuplicateOutputStream extends OutputStream {
	private final OutputStream out1;
	private final OutputStream out2;

	DuplicateOutputStream(OutputStream o1, OutputStream o2) {
		if (o1 == null || o2 == null)
			throw new NullPointerException();
		out1 = o1;
		out2 = o2;
	}

	@Override
	public void write(int b) throws IOException {
		out1.write(b);
		out2.write(b);
	}

	@Override
	public void write(byte[] b) throws IOException {
		out1.write(b);
		out2.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		out1.write(b, off, len);
		out2.write(b, off, len);
	}

	@Override
	public void flush() throws IOException {
		out1.flush();
		out2.flush();
	}

	@Override
	public void close() throws IOException {
		IOException e = null;
		try {
			out1.close();
		} catch (IOException e1) {
			e = e1;
		}
		out2.close();
		if (e != null)
			throw e;
	}

}
