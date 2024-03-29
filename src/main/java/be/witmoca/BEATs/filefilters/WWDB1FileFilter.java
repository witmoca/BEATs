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
* File: WWDB1FileFilter.java
* Created: 2018
*/
package be.witmoca.BEATs.filefilters;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

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

import be.witmoca.BEATs.connection.CommonSQL;
import be.witmoca.BEATs.connection.DataChangedType;
import be.witmoca.BEATs.connection.SQLConnection;
import be.witmoca.BEATs.utils.Lang;

public class WWDB1FileFilter extends ImportableFileFilter {

	public WWDB1FileFilter() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
	 */
	@Override
	public boolean accept(File f) {
		return (f.getName().endsWith(".wwdb") || f.isDirectory());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.filechooser.FileFilter#getDescription()
	 */
	@Override
	public String getDescription() {
		return Lang.getUI("fileFilters.WWDB1.descr");
	}
	
	/**
	 * 
	 */
	@Override
	public void importFile(File source) throws IOException, SAXException, ParserConfigurationException, SQLException{
		importV1WWDBFile(source);
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
			LocalDate aflDatum = null;
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
					Calendar cdar = Calendar.getInstance();
					cdar.setTime(new Date(Long.parseLong(item.getTextContent())));
					aflDatum = LocalDate.of(cdar.get(Calendar.YEAR), cdar.get(Calendar.MONTH) + 1,
							cdar.get(Calendar.DAY_OF_MONTH));
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
			CommonSQL.addEpisode(aflNr, aflDatum);
			CommonSQL.addGenre(aflCode);
			artiest = CommonSQL.addArtist(artiest, belgisch ? "BE" : "");
			int songId = CommonSQL.addSong(titel, artiest);
			CommonSQL.addSongInArchive(songId, aflNr, aflCode, commentaar);
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
			CommonSQL.addPlaylist(playlistName, -1);
			// Don't add empty playlistSongs
			if (!artist.trim().isEmpty() && !song.trim().isEmpty()) {
				CommonSQL.addSongInPlaylist(playlistName, artist, song, comment);
			}
		}

		SQLConnection.getDbConn().commit(DataChangedType.ALL_OPTS);
	}
}
