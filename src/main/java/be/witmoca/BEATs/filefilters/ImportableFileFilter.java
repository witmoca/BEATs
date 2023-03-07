package be.witmoca.BEATs.filefilters;

import java.io.File;
import javax.swing.filechooser.FileFilter;

public abstract class ImportableFileFilter extends FileFilter {
	
	/**
	 * Imports the file according to the rules of the class
	 * @param source File to import
	 * @throws Exception - Implementation dependent
	 */
	public abstract void importFile(File source) throws Exception;
}
