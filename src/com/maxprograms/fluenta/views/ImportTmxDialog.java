/*******************************************************************************
 * Copyright (c) 2015-2021 Maxprograms.
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
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.maxprograms.tmengine.ILogger;
import com.maxprograms.fluenta.Fluenta;
import com.maxprograms.fluenta.MainView;
import com.maxprograms.fluenta.models.Memory;
import com.maxprograms.utils.Locator;
import com.maxprograms.widgets.AsyncLogger;
import com.maxprograms.widgets.LogPanel;

public class ImportTmxDialog extends Dialog  implements ILogger {

	protected Shell shell;
	private Display display;
	private LogPanel logger;
	protected Text tmxText;
	protected AsyncLogger alogger;
	protected boolean cancelled;
	protected Listener closeListener;
	protected Thread thread;

	public ImportTmxDialog(Shell parent, int style, Memory memory) {
		super(parent, style);
		
		alogger = new AsyncLogger(this);
		
		shell = new Shell(parent, style);
		shell.setImage(Fluenta.getResourceManager().getIcon());
		shell.setLayout(new GridLayout());
		shell.setText(Messages.getString("ImportTmxDialog.0")); //$NON-NLS-1$
		shell.addListener(SWT.Close, new Listener() {
			
			@Override
			public void handleEvent(Event arg0) {
				Locator.remember(shell, "ImportTmxDialog"); //$NON-NLS-1$
				if (thread != null) {
					thread = null;
				}
				System.gc();
			}
		});
		display = shell.getDisplay();
		
		Composite top = new Composite(shell, SWT.NONE);
		top.setLayout(new GridLayout(3, false));
		top.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label tmxLabel = new Label(top, SWT.NONE);
		tmxLabel.setText(Messages.getString("ImportTmxDialog.2")); //$NON-NLS-1$
		
		tmxText = new Text(top, SWT.BORDER);
		GridData tmxData = new GridData(GridData.FILL_HORIZONTAL);
		tmxData.widthHint = 250;
		tmxText.setLayoutData(tmxData);
		
		Button tmxBrowse = new Button(top, SWT.PUSH);
		tmxBrowse.setText(Messages.getString("ImportTmxDialog.3")); //$NON-NLS-1$
		tmxBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				
				FileDialog fd = new FileDialog(shell, SWT.OPEN|SWT.SINGLE);
				if (tmxText.getText() != null && !tmxText.getText().equals("")) { //$NON-NLS-1$
					File f = new File(tmxText.getText());
					fd.setFileName(f.getName());
					fd.setFilterPath(f.getAbsolutePath());
				}
				String file = fd.open();
				if (file != null) {
					tmxText.setText(file);
				}
			}
		});
		
		logger = new LogPanel(shell, SWT.BORDER);	
		logger.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Composite bottom = new Composite(shell, SWT.NONE);
		bottom.setLayout(new GridLayout(3, false));
		bottom.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label filler = new Label(bottom, SWT.NONE);
		filler.setText(""); //$NON-NLS-1$
		filler.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Button cancel = new Button(bottom, SWT.PUSH|SWT.CANCEL);
		cancel.setText(Messages.getString("ImportTmxDialog.6")); //$NON-NLS-1$
		cancel.setEnabled(false);
		cancel.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				cancelled = true;
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				cancelled = true;
			}
		});
		
		
		Button importMemory = new Button(bottom, SWT.PUSH);
		importMemory.setText(Messages.getString("ImportTmxDialog.7")); //$NON-NLS-1$
		importMemory.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (tmxText.getText() == null || tmxText.getText().trim().equals("")) { //$NON-NLS-1$
					MessageBox box = new MessageBox(shell, SWT.ICON_WARNING|SWT.OK);
					box.setMessage(Messages.getString("ImportTmxDialog.9")); //$NON-NLS-1$
					box.open();
					return;
				}
				String tmxFile = tmxText.getText();
				closeListener = new Listener() {
					
					@Override
					public void handleEvent(Event ev) {
						cancelled = true;
						ev.doit = false;
					}
				};
				shell.addListener(SWT.Close, closeListener);
				cancel.setEnabled(true);
				importMemory.setEnabled(false);
				thread = new Thread() {
					@Override
					public void run() {
						MainView.getController().importTMX(memory, tmxFile, alogger);
					}
				};
				thread.start();						
			}
		});
		
		shell.pack();
	}

	public void show() {
		Locator.setLocation(shell, "ImportTmxDialog"); //$NON-NLS-1$
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	@Override
	public void log(String message) {
		logger.log(message);
	}

	@Override
	public void setStage(String stage) {
		logger.setStage(stage);
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void logError(String error) {
		logger.logError(error);
	}

	@Override
	public Vector<String> getErrors() {
		return logger.getErrors();
	}

	@Override
	public void displayError(String string) {
		display.asyncExec(new Runnable() {

			@Override
			public void run() {
				shell.removeListener(SWT.Close, closeListener);
				MessageBox box = new MessageBox(shell, SWT.ICON_ERROR|SWT.OK);
				box.setMessage(string);
				box.open();
				shell.close();
			}
		});
	}

	@Override
	public void displaySuccess(String string) {
		display.asyncExec(new Runnable() {

			@Override
			public void run() {
				shell.removeListener(SWT.Close, closeListener);
				MainView.getProjectsView().loadProjects();
				MessageBox box = new MessageBox(shell, SWT.ICON_INFORMATION|SWT.OK);
				box.setMessage(string);
				box.open();
				MainView.getMemoriesView().loadMemories();
				Vector<String> errors = alogger.getErrors();
				if (errors != null) {
					try {
						File out = File.createTempFile("Errors", ".log"); //$NON-NLS-1$ //$NON-NLS-2$
						out.deleteOnExit();
						FileOutputStream output = new FileOutputStream(out);
						Iterator<String> it = errors.iterator();
						while (it.hasNext()) {
							String error = it.next() + "\r\n"; //$NON-NLS-1$
							output.write(error.getBytes("UTF-8")); //$NON-NLS-1$
						}
						output.close();						
						Program.launch(out.toURI().toURL().toString());
					} catch (Exception e) {
						e.printStackTrace();
						MessageBox box2 = new MessageBox(shell, SWT.ICON_ERROR|SWT.OK);
						box2.setMessage(Messages.getString("ImportTmxDialog.15")); //$NON-NLS-1$
						box2.open();
					}
				}
				
				shell.close();
			}
		});
		
	}

}