/*******************************************************************************
 * Copyright (c) 2015-2022 Maxprograms.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-v10.html
 *
 * Contributors:
 *     Maxprograms - initial API and implementation
 *******************************************************************************/

package com.maxprograms.fluenta.views;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import com.maxprograms.languages.Language;
import com.maxprograms.languages.LanguageUtils;
import com.maxprograms.utils.Preferences;
import com.maxprograms.utils.TextUtils;
import com.maxprograms.utils.PreferencesUtil;

public class ProjectPreferences extends Composite {

	protected List<Language> defaultTargets;
	private Language defaultSource;
	protected Combo sourceLangCombo;
	protected Text srxText;

	public ProjectPreferences(Composite parent, int style) {
		super(parent, style);

		setLayout(new GridLayout());
		setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite sourceComposite = new Composite(this, SWT.NONE);
		sourceComposite.setLayout(new GridLayout(2, false));
		sourceComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label sourceLabel = new Label(sourceComposite, SWT.NONE);
		sourceLabel.setText(Messages.getString("ProjectPreferences.0")); 

		sourceLangCombo = new Combo(sourceComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		sourceLangCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		try {
			sourceLangCombo.setItems(LanguageUtils.getLanguageNames());
		} catch (SAXException | IOException | ParserConfigurationException e) {
			e.printStackTrace();
			MessageBox box = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
			box.setMessage(Messages.getString("ProjectPreferences.1")); 
			box.open();
			getShell().close();
		}

		try {
			defaultSource = PreferencesUtil.getDefaultSource();
			if (defaultSource != null) {
				sourceLangCombo.select(TextUtils.geIndex(sourceLangCombo.getItems(),
						LanguageUtils.getLanguage(defaultSource.getCode()).getDescription()));
			}
		} catch (IOException e) {
			e.printStackTrace();
			MessageBox box = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
			box.setMessage(Messages.getString("ProjectPreferences.2")); 
			box.open();
			getShell().close();
		}

		Group targetLanguages = new Group(this, SWT.NONE);
		targetLanguages.setText(Messages.getString("ProjectPreferences.3")); 
		targetLanguages.setLayoutData(new GridData(GridData.FILL_BOTH));
		targetLanguages.setLayout(new GridLayout());

		Table langsTable = new Table(targetLanguages, SWT.V_SCROLL | SWT.H_SCROLL | SWT.CHECK);
		langsTable.setLinesVisible(true);
		langsTable.setHeaderVisible(false);
		GridData langData = new GridData(GridData.FILL_BOTH);
		langData.heightHint = langsTable.getItemHeight() * 8;
		langsTable.setLayoutData(langData);

		TableColumn langDescColumn = new TableColumn(langsTable, SWT.FILL);
		langDescColumn.setText(Messages.getString("ProjectPreferences.4")); 

		langsTable.addListener(SWT.Resize, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				int width = langsTable.getClientArea().width;
				int scroll = langsTable.getVerticalBar().isVisible() ? langsTable.getVerticalBar().getSize().x : 0;
				langDescColumn.setWidth(width - scroll);
			}
		});

		try {
			defaultTargets = PreferencesUtil.getDefaultTargets();
			for (int i = 0; i < defaultTargets.size(); i++) {
				Language l = defaultTargets.get(i);
				TableItem item = new TableItem(langsTable, SWT.NONE);
				item.setText(LanguageUtils.getLanguage(l.getCode()).getDescription());
				item.setData("language", l); 
			}
		} catch (IOException e) {
			e.printStackTrace();
			MessageBox box = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
			box.setMessage(Messages.getString("ProjectPreferences.5")); 
			box.open();
			getShell().close();
		}

		Composite targetButtons = new Composite(targetLanguages, SWT.NONE);
		targetButtons.setLayout(new GridLayout(3, false));
		targetButtons.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label filler = new Label(targetButtons, SWT.NONE);
		filler.setText(""); 
		filler.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button addLang = new Button(targetButtons, SWT.PUSH);
		addLang.setText(Messages.getString("ProjectPreferences.8")); 
		addLang.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					LanguageAddDialog dialog = new LanguageAddDialog(getShell(), SWT.DIALOG_TRIM);
					dialog.show();
					if (!dialog.wasCancelled()) {
						Language l = dialog.getLanguage();
						TableItem item = new TableItem(langsTable, SWT.NONE);
						item.setText(LanguageUtils.getLanguage(l.getCode()).getDescription());
						item.setData("language", l); 
						defaultTargets.add(l);
					}
				} catch (IOException e) {
					e.printStackTrace();
					MessageBox box = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
					box.setMessage(Messages.getString("ProjectPreferences.6")); 
					box.open();
				}

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing
			}
		});

		Button removeLang = new Button(targetButtons, SWT.PUSH);
		removeLang.setText(Messages.getString("ProjectPreferences.10")); 
		removeLang.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					TableItem[] oldItems = langsTable.getItems();
					defaultTargets.clear();
					for (int i = 0; i < oldItems.length; i++) {
						if (!oldItems[i].getChecked()) {
							defaultTargets.add((Language) oldItems[i].getData("language")); 
						}
					}
					langsTable.removeAll();
					for (int i = 0; i < defaultTargets.size(); i++) {
						Language l = defaultTargets.get(i);
						TableItem item = new TableItem(langsTable, SWT.NONE);
						item.setText(LanguageUtils.getLanguage(l.getCode()).getDescription());
						item.setData("language", l); 
					}
				} catch (IOException e) {
					e.printStackTrace();
					MessageBox box = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
					box.setMessage(Messages.getString("ProjectPreferences.7")); 
					box.open();
				}

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing
			}
		});

		Composite srxComposite = new Composite(this, SWT.NONE);
		srxComposite.setLayout(new GridLayout(3, false));
		srxComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label srxLabel = new Label(srxComposite, SWT.NONE);
		srxLabel.setText(Messages.getString("ProjectPreferences.13")); 

		srxText = new Text(srxComposite, SWT.BORDER);
		srxText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		try {
			srxText.setText(PreferencesUtil.getDefaultSRX());
		} catch (IOException e) {
			e.printStackTrace();
			MessageBox box = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
			box.setMessage(Messages.getString("ProjectPreferences.14")); 
			box.open();
			getShell().close();
		}

		Button browse = new Button(srxComposite, SWT.PUSH);
		browse.setText(Messages.getString("ProjectPreferences.15")); 
		browse.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				FileDialog fd = new FileDialog(getShell(), SWT.OPEN | SWT.SINGLE);
				fd.setFilterExtensions(new String[] { "*.srx", "*.*" });  
				fd.setFilterNames(new String[] { Messages.getString("ProjectPreferences.18"), 
						Messages.getString("ProjectPreferences.19") }); 
				if (!srxText.getText().isEmpty()) { 
					File f = new File(srxText.getText());
					if (f.exists()) {
						fd.setFilterPath(f.getParentFile().getAbsolutePath());
						fd.setFileName(f.getName());
					}
				}
				String map = fd.open();
				if (map != null) {
					srxText.setText(map);
				}
			}

		});

		Composite bottom = new Composite(this, SWT.NONE);
		bottom.setLayout(new GridLayout(2, false));
		bottom.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label filler2 = new Label(bottom, SWT.NONE);
		filler2.setText(""); 
		filler2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button save = new Button(bottom, SWT.PUSH);
		save.setText(Messages.getString("ProjectPreferences.22")); 
		save.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (srxText.getText().isEmpty()) { 
					MessageBox box = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.OK);
					box.setMessage(Messages.getString("ProjectPreferences.24")); 
					box.open();
					return;
				}
				try {
					File srx = new File(srxText.getText());
					if (!srx.exists()) {
						MessageBox box = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.OK);
						box.setMessage(Messages.getString("ProjectPreferences.25")); 
						box.open();
						return;
					}
					Preferences prefs = Preferences.getInstance();
					JSONObject targetLangs = new JSONObject();
					Iterator<Language> it = defaultTargets.iterator();
					while (it.hasNext()) {
						Language l = it.next();
						targetLangs.put(l.getCode(), LanguageUtils.getLanguage(l.getCode()).getDescription());
					}
					prefs.save("DefaultTargetLanguages", targetLangs); 
					if (sourceLangCombo.getSelectionIndex() != -1) {
						JSONObject sourceLangs = new JSONObject();
						Language l = LanguageUtils
								.languageFromName(sourceLangCombo.getItem(sourceLangCombo.getSelectionIndex()));
						sourceLangs.put("default", l.getCode()); 
						prefs.save("DefaultSourceLanguages", sourceLangs); 
					}
					JSONObject srxTable = new JSONObject();
					srxTable.put("srx", srxText.getText()); 
					prefs.save("DefaultSRX", srxTable); 
				} catch (IOException | SAXException | ParserConfigurationException e) {
					e.printStackTrace();
					MessageBox box = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
					box.setMessage(Messages.getString("ProjectPreferences.30")); 
					box.open();
					getShell().close();
				}
				getShell().close();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing
			}
		});
		save.setFocus();
	}


}
