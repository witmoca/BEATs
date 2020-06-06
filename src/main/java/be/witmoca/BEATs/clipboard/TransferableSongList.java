/**
 * 
 */
package be.witmoca.BEATs.clipboard;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Witmoca
 *
 */
public class TransferableSongList implements Transferable, Iterable<CCPSong> {
	public static final DataFlavor FLAVOR = new DataFlavor(TransferableSongList.class, "CCP Song List");
	private List<CCPSong> content = new ArrayList<CCPSong>();
	
	public TransferableSongList() {
	}
	
	public void addSong(String artist, String song, int rowid) {
		content.add(new CCPSong(artist,song,rowid));
	}
	
	public int size() {
		return content.size();
	}
	
	public String getHumanReadable(int index) {
		return content.get(index).toString();
	}
	
	public void clear() {
		content.clear();
	}
	
	@Override
	public Iterator<CCPSong> iterator() {
		return content.iterator();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
	 */
	@Override
	public DataFlavor[] getTransferDataFlavors() {
		DataFlavor[] df = new DataFlavor[1];
		df[0] = FLAVOR;
		return df;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.
	 * datatransfer.DataFlavor)
	 */
	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavor.equals(FLAVOR);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.
	 * DataFlavor)
	 */
	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (!this.isDataFlavorSupported(flavor))
			throw new UnsupportedFlavorException(flavor);
		return this;
	}
}
