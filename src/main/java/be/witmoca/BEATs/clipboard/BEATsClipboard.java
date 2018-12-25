/**
 * 
 */
package be.witmoca.BEATs.clipboard;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.TransferHandler;
import javax.swing.TransferHandler.TransferSupport;

/**
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
* File: BEATsClipboard.java
* Created: 2018
*/
public class BEATsClipboard extends Clipboard {
	private static final ClipboardTransferHandler handler = new ClipboardTransferHandler();
	private static final JComponent COMPONENT_REPRESENTATION = new JLabel("Internal Clipboard");
	
	public BEATsClipboard(String name) {
		super(name);
	}

	@Override
	public synchronized void setContents(Transferable contents, ClipboardOwner owner) {
		if(!(contents.isDataFlavorSupported(TransferableSong.FLAVOR)))
			throw new IllegalArgumentException("Dataflavor is not acceptable");	
		
		if (owner != null)
			owner.lostOwnership(this, contents);
		
		handler.importData(new TransferSupport(COMPONENT_REPRESENTATION, contents));
	}

	@Override
	public synchronized Transferable getContents(Object requestor) {
		return handler.createTransferable(COMPONENT_REPRESENTATION);
	}	
	
	synchronized void pasteDone(Transferable t) {
		handler.exportDone(COMPONENT_REPRESENTATION, t, TransferHandler.MOVE);
	}
}
