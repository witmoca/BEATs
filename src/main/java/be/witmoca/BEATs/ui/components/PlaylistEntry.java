/**
 * 
 */
package be.witmoca.BEATs.ui.components;

import java.io.Serializable;

/**
 * @author Witmoca
 *
 */
public class PlaylistEntry implements Serializable{
	private static final long serialVersionUID = 1L;
	private final String ARTIST;
	private final String SONG;
	private final String COMMENT;
	private final int ROWID;

	public PlaylistEntry(int rowid, String aRTIST, String sONG, String cOMMENT) {
		super();
		ARTIST = aRTIST;
		SONG = sONG;
		COMMENT = cOMMENT;
		ROWID = rowid;
	}

	public String getColumn(int i) {
		switch (i) {
		case 0:
			return this.ARTIST;
		case 1:
			return this.SONG;
		case 2:
			return this.COMMENT;
		default:
			return null;
		}
	}

	public int getROWID() {
		return ROWID;
	}
}
