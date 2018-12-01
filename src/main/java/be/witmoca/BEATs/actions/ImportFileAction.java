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
* File: ImportFileAction.java
* Created: 2018
*/
package be.witmoca.BEATs.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import be.witmoca.BEATs.Launch;
import be.witmoca.BEATs.model.DataChangedListener;
import be.witmoca.BEATs.model.SQLObjectTransformer;

public class ImportFileAction implements ActionListener {

	public ImportFileAction() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		// Start with new File
		(new NewFileAction()).actionPerformed(e);

		// Choose file to import (and method)
		final JFileChooser fc = new JFileChooser();
		fc.setAcceptAllFileFilterUsed(false);

		// WWDB v1.x
		fc.addChoosableFileFilter(new WWDB1FileFilter());

		// Check for Cancel/Error
		if (fc.showOpenDialog(Launch.getAPP_WINDOW()) != JFileChooser.APPROVE_OPTION) {
			return;
		}

		if (fc.getFileFilter().getClass() == WWDB1FileFilter.class) {
			// Import V1.x WWDB file
			try {
				importV1WWDBFile(fc.getSelectedFile());
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(Launch.getAPP_WINDOW(),
						"Could not open file:\n" + e1.getClass() + "\n" + e1.getLocalizedMessage(), "Import Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void importV1WWDBFile(File source) throws IOException, SAXException, ParserConfigurationException, SQLException {
		if (!source.isFile()) {
			throw new IOException("Not a file!");
		} else if (!source.canRead()) {
			throw new IOException("File marked as unreadable!");
		}

		// Validate file
		Source xmlFile = new StreamSource(source);
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = schemaFactory
				.newSchema(this.getClass().getClassLoader().getResource("Validators/WWDB1Validator.xsd"));
		Validator validator = schema.newValidator();
		validator.validate(xmlFile);

		// Read File
		DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document sourceTree = parser.parse(source);

		NodeList archiveList = sourceTree.getElementsByTagName("archief");
		NodeList playlistList = sourceTree.getElementsByTagName("playlist");
		

		// Archive
		// For every archive item
		for (int archiveIndex = 0; archiveIndex < archiveList.getLength(); archiveIndex++) {
			String aflCode = "";
			String artiest = "";
			String titel = "";
			int aflNr = 0;
			Date aflDatum = null;
			boolean belgisch = false;
			String commentaar = "";

			NodeList childNodes = archiveList.item(archiveIndex).getChildNodes();
			// For every child (property) of archive item
			for (int childNode = 0; childNode < childNodes.getLength(); childNode++) {
				Node item = childNodes.item(childNode);
				switch (item.getNodeName()) {
				case "aflCode":
					aflCode = item.getTextContent();
					break;
				case "artiest":
					artiest = item.getTextContent();
					break;
				case "titel":
					titel = item.getTextContent();
					break;
				case "aflNr":
					aflNr = Integer.parseInt(item.getTextContent());
					break;
				case "aflDatum":
					aflDatum = new Date(Long.parseLong(item.getTextContent()));
					break;
				case "belgisch":
					belgisch = item.getTextContent().startsWith("1");
					break;
				case "commentaar":
					commentaar = item.getTextContent();
					break;
				default:
					break;
				}
			}

			// Archive item -should- be fully read by this point (assumed, check validity of
			// XML beforehand)
			if (aflDatum == null) {
				throw new IOException("Archive item is missing a date. See items with aflNr: " + aflNr);
			}
			SQLObjectTransformer.addEpisode(aflNr, aflDatum);
			SQLObjectTransformer.addSection(aflCode);
			SQLObjectTransformer.addArtist(artiest, belgisch);
			int songId = SQLObjectTransformer.addSong(titel, artiest);
			SQLObjectTransformer.addSongInArchive(songId, aflNr, aflCode, commentaar);
		}
		
		// Playlists
		// For every playlistItem
		for (int playlistIndex = 0; playlistIndex < playlistList.getLength(); playlistIndex++) {
			String playlistName = "";
			String artist = "";
			String song = "";
			String comment = "";

			NodeList childNodes = playlistList.item(playlistIndex).getChildNodes();
			// For every child (property) of playlist item
			for (int childNode = 0; childNode < childNodes.getLength(); childNode++) {
				Node item = childNodes.item(childNode);
				switch (item.getNodeName()) {
				case "artiest":
					artist = item.getTextContent();
					break;
				case "titel":
					song = item.getTextContent();
					break;
				case "belgisch":
					// ignore
					break;
				case "commentaar":
					comment = item.getTextContent();
					break;
				default:
					break;
				}
			}
			// PlaylistName is an attribute
			playlistName = playlistList.item(playlistIndex).getAttributes().getNamedItem("id").getTextContent();

			// Playlist item -should- be fully read by this point (assumed, check validity
			// of XML beforehand)
			SQLObjectTransformer.addPlaylist(playlistName, -1);
			// Don't add empty playlistSongs
			if (!artist.trim().isEmpty() && !song.trim().isEmpty()) {
				SQLObjectTransformer.addSongInPlaylist(playlistName, artist, song, comment);
			}
		}
		
		Launch.getDB_CONN().commit(DataChangedListener.DataType.ALL_OPTS);
	}
}
