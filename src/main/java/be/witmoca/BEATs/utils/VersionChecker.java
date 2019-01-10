/*
*
+===============================================================================+
|    BEATs (Burning Ember Archival Tool suite)                                  |
|    Copyright 2019 Jente Heremans                                              |
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
* File: VersionChecker.java
* Created: 2019
*/
package be.witmoca.BEATs.utils;

import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import be.witmoca.BEATs.ui.ApplicationWindow;

public class VersionChecker implements Runnable {
	private String versionTag = "";
	private URL downloadURL = null;

	public static void Run() {
		(new Thread(new VersionChecker())).start();
	}

	@Override
	public void run() {
		try {
			URLConnection con = (new URL("https://api.github.com/repos/witmoca/BEATs/releases/latest"))
					.openConnection();
			con.setDoInput(true);
			con.connect();
			readResults(con.getInputStream());
		} catch (Exception e) {
			// Fail gracefully, version checking is just optional
		}
		if (versionTag.isEmpty())
			return; // Fail gracefully once more

		// Extract the version
		Matcher matcher = Pattern.compile("(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})").matcher(versionTag);
		if (!matcher.find())
			return; // Fail... you know the drill by now
		int latestVersion = (Integer.parseInt(matcher.group(1)) * 1000000) + (Integer.parseInt(matcher.group(2)) * 1000)
				+ Integer.parseInt(matcher.group(3));
		if (StaticSettings.getAppVersionInt() < latestVersion) {
			NotifyUserToUpdate();
		}
	}

	private void readResults(InputStream in) throws IOException {
		try (JsonReader reader = new JsonReader(new InputStreamReader(in))) {
			for (JsonToken p = reader.peek(); !JsonToken.END_DOCUMENT.equals(p); p = reader.peek()) {
				switch (p) {
				case BEGIN_ARRAY:
					reader.beginArray();
					break;
				case BEGIN_OBJECT:
					reader.beginObject();
					break;
				case END_ARRAY:
					reader.endArray();
					break;
				case END_DOCUMENT:
					break;
				case END_OBJECT:
					reader.endObject();
					break;
				case NAME:
					String name = reader.nextName();
					if ("tag_name".equals(name))
						versionTag = reader.nextString();
					else if ("html_url".equals(name)) {
						if (downloadURL == null)
							downloadURL = new URL(reader.nextString());
					}
					break;
				case NULL:
				case NUMBER:
				case BOOLEAN:
				case STRING:
					reader.skipValue();
					break;
				}
			}
		}
	}

	private void NotifyUserToUpdate() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				String[] options = { Lang.getUI("versionChecker.download"), Lang.getUI("versionChecker.skip") };
				int result = JOptionPane.showOptionDialog(ApplicationWindow.getAPP_WINDOW(),
						Lang.getUI("versionChecker.descr"), "", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
						null, options, null);
				if (result == 0 && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
					try {
						Desktop.getDesktop().browse(downloadURL.toURI());
					} catch (URISyntaxException | IOException e) {
						JOptionPane.showMessageDialog(ApplicationWindow.getAPP_WINDOW(),
								Lang.getUI("versionChecker.noDesktop") + "\n" + downloadURL.toString(), "",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
	}
}
