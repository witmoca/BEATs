/**
 * 
 */
package be.witmoca.BEATs.clipboard;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.swing.TransferHandler.TransferSupport;

import be.witmoca.BEATs.utils.Lang;
import be.witmoca.BEATs.utils.UiIcon;

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
* File: ActionFactory.java
* Created: 2018
*/
public class ClipboardActionFactory {
	public static Action getCutAction(JComponent source) {
		return new CutCopyAction(source, true);
	}

	public static Action getCopyAction(JComponent source) {
		return new CutCopyAction(source, false);
	}
	
	public static Action getPasteAction(JComponent source) {
		return new PasteAction(source);
	}

	private static final class CutCopyAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		private final int action;
		private final JComponent source;

		private CutCopyAction(JComponent source, boolean cutCopy) {
			super(cutCopy ? Lang.getUI("action.cut") : Lang.getUI("action.copy"));
			this.putValue(Action.SMALL_ICON, cutCopy ? UiIcon.CUT.getIcon() : UiIcon.COPY.getIcon());
			action = cutCopy ? TransferHandler.MOVE : TransferHandler.COPY;
			this.source = source;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			TransferHandler th = source.getTransferHandler();
			Clipboard clipboard = BEATsClipboard.getINT_CLIP();
			if ((clipboard == null) || (th == null)) {
				return;
			}

			try {
				th.exportToClipboard(source, clipboard, action);
			} catch (IllegalStateException ise) {
				// Unavailable clipboard
				return;
			}
		}
	}

	private static final class PasteAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		private final JComponent source;
		
		private PasteAction(JComponent source) {
			super(Lang.getUI("action.paste"));
			this.putValue(Action.SMALL_ICON, UiIcon.PASTE.getIcon());
			this.source = source;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			TransferHandler th = source.getTransferHandler();
			BEATsClipboard clipboard = BEATsClipboard.getINT_CLIP();

			if ((clipboard == null) || (th == null)) {
				return;
			}

			try {
				Transferable tr = clipboard.getContents(null);
				if (tr != null) {
					th.importData(new TransferSupport(source, tr));
					clipboard.pasteDone(tr);
				}
			} catch (IllegalStateException ise) {
				// Unavailable clipboard
				return;
			}
		}
	}
}
