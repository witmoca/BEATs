/**
 * 
 */
package be.witmoca.BEATs.ui.actions;

import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import be.witmoca.BEATs.ui.ApplicationWindow;
import be.witmoca.BEATs.utils.BEATsSettings;
import be.witmoca.BEATs.utils.Lang;

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
* File: ShowSettingsDialogAction.java
* Created: 2018
*/
public class ShowSettingsDialogAction implements ActionListener {

	private JComboBox<LocaleWrapper> localePicker;
	private JCheckBox backupEnabled;
	private JFormattedTextField backupAmount;
	private JFormattedTextField backupSize;
	private JFormattedTextField backupFrequency;
	private JCheckBox liveShareServerEnabled;
	private JFormattedTextField liveShareServerMaxConnections;
	private JCheckBox liveShareClientEnabled;
	
	public ShowSettingsDialogAction() {
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		JPanel content = constructGUI();
		// Show
		String[] options = {Lang.getUI("action.save"), Lang.getUI("action.cancel"), Lang.getUI("settings.reset")};
		int answer = JOptionPane.showOptionDialog(ApplicationWindow.getAPP_WINDOW(), content, Lang.getUI("menu.tools.settings"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
		if(answer == 0) {
			// Save
			saveSettings();
			// Warning label
			JOptionPane.showMessageDialog(ApplicationWindow.getAPP_WINDOW(), Lang.getUI("settings.restartrequired"));
			// Force restart
			(new ExitApplicationAction(true)).actionPerformed(new ActionEvent(e.getSource(), ActionEvent.ACTION_PERFORMED, e.paramString()));
		} else if (answer == 2) {
			// reset
			if(BEATsSettings.resetDefaultPreferences()) {
				JOptionPane.showMessageDialog(content, Lang.getUI("settings.resetSucceeded"));
			} else {
				JOptionPane.showMessageDialog(content, Lang.getUI("settings.resetFailed"));
			}
			// Force restart
			(new ExitApplicationAction(true)).actionPerformed(new ActionEvent(e.getSource(), ActionEvent.ACTION_PERFORMED, e.paramString()));
		}
	}
	
	private JPanel constructGUI() {
		JPanel content = new JPanel(new GridLayout(0, 2, 10, 0));
		localePicker = new JComboBox<LocaleWrapper>();
		backupEnabled = new JCheckBox("", BEATsSettings.BACKUPS_ENABLED.getBoolValue());
		backupAmount = new JFormattedTextField(BEATsSettings.BACKUPS_MAXAMOUNT.getIntValue());
		backupSize = new JFormattedTextField(BEATsSettings.BACKUPS_MAXSIZE.getIntValue());
		backupFrequency = new JFormattedTextField(BEATsSettings.BACKUPS_TIMEBETWEEN.getIntValue());		
		liveShareServerEnabled = new JCheckBox("", BEATsSettings.LIVESHARE_SERVER_ENABLED.getBoolValue());
		liveShareServerMaxConnections = new JFormattedTextField(BEATsSettings.LIVESHARE_SERVER_MAXCONNECTIONS.getIntValue());
		liveShareClientEnabled = new JCheckBox("", BEATsSettings.LIVESHARE_CLIENT_ENABLED.getBoolValue());
		
		JLabel languageTitle = new JLabel(Lang.getUI("settings.label.lang.title"));
		Font titleFont = languageTitle.getFont();
		Map<TextAttribute, Object> attributes = new HashMap<>(titleFont.getAttributes());
		attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
		titleFont = titleFont.deriveFont(attributes);
		
		languageTitle.setFont(titleFont);
		content.add(languageTitle);
		content.add(new JLabel(""));
		
		content.add(new JLabel(Lang.getUI("settings.label.lang")));
		
		// Construct localePicker
		List<LocaleWrapper> locales = new ArrayList<LocaleWrapper>();
		for (Locale l : Lang.getPossibleLocales()) {
			if (!l.getLanguage().isEmpty()) {
				locales.add(new LocaleWrapper(l));
			}
		}
		localePicker.setModel(new DefaultComboBoxModel<LocaleWrapper>(locales.toArray(new LocaleWrapper[0])));
		
		// Set current as selected
		Locale currentL = new Locale(BEATsSettings.LANGUAGE.getStringValue(), BEATsSettings.COUNTRY.getStringValue());
		for (LocaleWrapper lw : locales) {
			if (lw.getLocale().equals(currentL))
				localePicker.getModel().setSelectedItem(lw);
		}
		content.add(localePicker);
		
		// BACKUPS
		JLabel backupTitle = new JLabel(Lang.getUI("settings.label.backup.title"));
		backupTitle.setFont(titleFont);
		content.add(backupTitle);
		content.add(new JLabel(""));
		content.add(new JLabel(Lang.getUI("settings.label.backup.enabled")));
		content.add(backupEnabled);
		backupEnabled.addActionListener(new AbstractAction() {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void actionPerformed(ActionEvent e) {
				backupFrequency.setEnabled(backupEnabled.isSelected());
				backupAmount.setEnabled(backupEnabled.isSelected());
				backupSize.setEnabled(backupEnabled.isSelected());
			}
		});
		backupFrequency.setEnabled(backupEnabled.isSelected());
		backupAmount.setEnabled(backupEnabled.isSelected());
		backupSize.setEnabled(backupEnabled.isSelected());
		
		content.add(new JLabel(Lang.getUI("settings.label.backup.time")));
		content.add(backupFrequency);
		content.add(new JLabel(Lang.getUI("settings.label.backup.amount")));
		content.add(backupAmount);
		content.add(new JLabel(Lang.getUI("settings.label.backup.totalsize")));
		content.add(backupSize);
		
		// LiveShare SERVER
		JLabel liveShareServerTitle = new JLabel(Lang.getUI("settings.label.liveshare.server.title"));
		liveShareServerTitle.setFont(titleFont);
		content.add(liveShareServerTitle);
		content.add(new JLabel(""));
		
		content.add(new JLabel(Lang.getUI("settings.label.liveshare.server.enabled")));
		content.add(liveShareServerEnabled);
		liveShareServerEnabled.addActionListener(new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {	
				liveShareServerMaxConnections.setEnabled(liveShareServerEnabled.isSelected());	
			}
			
		});
		liveShareServerMaxConnections.setEnabled(liveShareServerEnabled.isSelected());	
		
		content.add(new JLabel(Lang.getUI("settings.label.liveshare.server.maxcon")));
		content.add(liveShareServerMaxConnections);
		
		// LiveShare CLIENT
		JLabel liveShareClientTitle = new JLabel(Lang.getUI("settings.label.liveshare.client.title"));
		liveShareClientTitle.setFont(titleFont);
		content.add(liveShareClientTitle);
		content.add(new JLabel(""));
		
		content.add(new JLabel(Lang.getUI("settings.label.liveshare.client.enabled")));
		content.add(liveShareClientEnabled);
		return content;
	}
	
	private void saveSettings() {
		Locale l = ((LocaleWrapper) localePicker.getSelectedItem()).getLocale();
		BEATsSettings.LANGUAGE.setStringValue(l.getLanguage());
		BEATsSettings.COUNTRY.setStringValue(l.getCountry());
		
		BEATsSettings.BACKUPS_ENABLED.setBoolValue(backupEnabled.isSelected());
		int freq = (int) backupFrequency.getValue();
		freq = (freq < 1 ? 1 : (freq > 99 ? 99 : freq));
		BEATsSettings.BACKUPS_TIMEBETWEEN.setIntValue(freq);
		int amount = (int) backupAmount.getValue();
		amount = (amount < 1 ? 1 : (amount > 99 ? 99 : amount));
		BEATsSettings.BACKUPS_MAXAMOUNT.setIntValue(amount);
		int size = (int) backupSize.getValue();
		size = (size < 1 ? 1 : (size > 999 ? 999 : size));
		BEATsSettings.BACKUPS_MAXSIZE.setIntValue(size);
		
		BEATsSettings.LIVESHARE_SERVER_ENABLED.setBoolValue(liveShareServerEnabled.isSelected());
		int maxcon = (int) liveShareServerMaxConnections.getValue();
		maxcon = (maxcon < 1 ? 1 : (maxcon > 99 ? 99 : maxcon));
		BEATsSettings.LIVESHARE_SERVER_MAXCONNECTIONS.setIntValue(maxcon);
		
		BEATsSettings.LIVESHARE_CLIENT_ENABLED.setBoolValue(liveShareClientEnabled.isSelected());
		
		BEATsSettings.savePreferences();
	}

	private static class LocaleWrapper {
		private final Locale locale; // Locale is a final class (wrapper for convenience)

		private LocaleWrapper(Locale l) {
			locale = l;
		}

		@Override
		public String toString() {
			// Display the language and country in the locale belonging to them (so they are
			// always readable)
			String s = locale.getDisplayLanguage(locale);
			if (!locale.getCountry().isEmpty()) {
				s += " (" + locale.getDisplayCountry(locale) + ")";
			}
			return s;
		}

		public Locale getLocale() {
			return locale;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof LocaleWrapper) {
				return (((LocaleWrapper) obj).getLocale().equals(locale));
			}
			return super.equals(obj);
		}
	}
}
