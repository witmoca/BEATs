/**
 * 
 */
package be.witmoca.BEATs.ui.actions;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

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
	@Override
	public void actionPerformed(ActionEvent e) {
		// Construct and show dialog
		JPanel content = new JPanel(new GridLayout(0, 1));

		// Construct localePicker
		JComboBox<LocaleWrapper> localePicker = new JComboBox<LocaleWrapper>();
		List<LocaleWrapper> locales = new ArrayList<LocaleWrapper>();
		for (Locale l : Lang.getPossibleLocales()) {
			if (!l.getLanguage().isEmpty()) {
				locales.add(new LocaleWrapper(l));
			}
		}
		localePicker.setModel(new DefaultComboBoxModel<LocaleWrapper>(locales.toArray(new LocaleWrapper[0])));
		// Set current as selected
		Locale currentL = new Locale(BEATsSettings.LANGUAGE.getValue(), BEATsSettings.COUNTRY.getValue());
		for (LocaleWrapper lw : locales) {
			if (lw.getLocale().equals(currentL))
				localePicker.getModel().setSelectedItem(lw);
		}
		localePicker.getModel().addListDataListener(new ListDataListener() {
			@Override
			public void intervalAdded(ListDataEvent e) {
			}

			@Override
			public void intervalRemoved(ListDataEvent e) {
			}

			@Override
			public void contentsChanged(ListDataEvent e) {
				Locale l = ((LocaleWrapper) localePicker.getSelectedItem()).getLocale();
				BEATsSettings.LANGUAGE.setValue(l.getLanguage());
				BEATsSettings.COUNTRY.setValue(l.getCountry());
				BEATsSettings.savePreferences();
			}
		});
		content.add(localePicker);
		
		// Reset Settings Button
		JButton resetDefault = new JButton(new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				if(BEATsSettings.resetDefaultPreferences()) {
					JOptionPane.showMessageDialog(content, Lang.getUI("settings.resetSucceeded"));
				} else {
					JOptionPane.showMessageDialog(content, Lang.getUI("settings.resetFailed"));
				}
			}
		});
		resetDefault.setText(Lang.getUI("settings.reset"));
		content.add(resetDefault);

		// Warning label
		JPanel descr = new JPanel();
		descr.setBorder(BorderFactory.createLineBorder(Color.red));
		descr.add(new JLabel(Lang.getUI("settings.descr")));
		content.add(descr);

		JOptionPane.showMessageDialog(ApplicationWindow.getAPP_WINDOW(), content, Lang.getUI("menu.tools.settings"),
				JOptionPane.PLAIN_MESSAGE);
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
