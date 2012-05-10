/*===========================================================================
  Copyright (C) 2011 by the Okapi Framework contributors
-----------------------------------------------------------------------------
  This library is free software; you can redistribute it and/or modify it 
  under the terms of the GNU Lesser General Public License as published by 
  the Free Software Foundation; either version 2.1 of the License, or (at 
  your option) any later version.

  This library is distributed in the hope that it will be useful, but 
  WITHOUT ANY WARRANTY; without even the implied warranty of 
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
  General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License 
  along with this library; if not, write to the Free Software Foundation, 
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

  See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
===========================================================================*/

package net.sf.okapi.applications.olifant;

import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.OKCancelPanel;
import net.sf.okapi.common.ui.UIUtil;
import net.sf.okapi.lib.tmdb.IRepository;
import net.sf.okapi.lib.tmdb.SplitterOptions;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

class SplitTMForm {
	
	private final long totalEntries;
	private final String sourceLocale;
	
	private Shell shell;
	private Text edNumberOfParts;
	private Text edNumberOfEntries;
	private SplitterOptions options = null;
	private boolean doUpdate = true;

	SplitTMForm (Shell parent,
		IRepository repo,
		String tmName,
		String sourceLocale)
	{
		totalEntries = repo.getTotalSegmentCount(tmName);
		this.sourceLocale = sourceLocale;
		
		shell = new Shell(parent, SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		shell.setText("Split the TM into several TMs");
		UIUtil.inheritIcon(shell, parent);
		shell.setLayout(new GridLayout(1, false));

		Group group = new Group(shell, SWT.NONE);
		group.setLayout(new GridLayout(2, false));
		group.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label stTmp = new Label(group, SWT.NONE);
		stTmp.setText(String.format("Total number of segments = %d", totalEntries));
		GridData gdTmp = new GridData();
		gdTmp.horizontalSpan = 2;
		stTmp.setLayoutData(gdTmp);

		stTmp = new Label(group, SWT.NONE);
		stTmp.setText("Number of TM to create:");
		
		edNumberOfParts = new Text(group, SWT.BORDER);
		gdTmp = new GridData();
		gdTmp.widthHint = 100;
		edNumberOfParts.setLayoutData(gdTmp);
		edNumberOfParts.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText (ModifyEvent event) {
				if ( doUpdate ) updateValues(true);
			}
		});
		
		stTmp = new Label(group, SWT.NONE);
		stTmp.setText("Number of entries per new TM:");
		
		edNumberOfEntries = new Text(group, SWT.BORDER);
		gdTmp = new GridData();
		gdTmp.widthHint = 100;
		edNumberOfEntries.setLayoutData(gdTmp);
		edNumberOfEntries.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText (ModifyEvent event) {
				if ( doUpdate ) updateValues(false);
			}
		});
		
		SelectionAdapter OKCancelActions = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if ( e.widget.getData().equals("o") ) { //$NON-NLS-1$
					if ( !saveData() ) return;
				}
				shell.close();
			};
		};
		OKCancelPanel pnlActions = new OKCancelPanel(shell, SWT.NONE, OKCancelActions, false);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalSpan = 3;
		pnlActions.setLayoutData(gdTmp);
		shell.setDefaultButton(pnlActions.btOK);

		edNumberOfParts.setText("2");

		shell.pack();
		Rectangle Rect = shell.getBounds();
		shell.setMinimumSize(Rect.width, Rect.height);
		Dialogs.centerWindow(shell, parent);
	}

	SplitterOptions showDialog () {
		shell.open();
		while ( !shell.isDisposed() ) {
			if ( !shell.getDisplay().readAndDispatch() )
				shell.getDisplay().sleep();
		}
		return options;
	}
	
	private void updateValues (boolean byNumberOfParts) {
		doUpdate = false;
		long n;
		if ( byNumberOfParts ) {
			try {
				n = Long.parseLong(edNumberOfParts.getText().trim());
				if ( n < 1 ) n = 2;
			}
			catch ( NumberFormatException e ) {
				// Fix automatically
				n = 2;
			}
			edNumberOfParts.setText(String.valueOf(n));
			edNumberOfParts.setSelection(edNumberOfParts.getText().length());
			long left = totalEntries % n;
			long entries = totalEntries / (left>0 ? n+1 : n);
			edNumberOfEntries.setText(String.valueOf(entries));
		}
		else {
			try {
				n = Long.parseLong(edNumberOfEntries.getText().trim());
				if ( n < 1 ) n = 1;
				if ( n > totalEntries ) n = totalEntries / 2;
			}
			catch ( NumberFormatException e ) {
				// Fix automatically
				n = totalEntries / 2;
			}
			edNumberOfEntries.setText(String.valueOf(n));
			edNumberOfEntries.setSelection(edNumberOfEntries.getText().length());
			long left = totalEntries % n;
			long parts = (totalEntries / n) + (left>0 ? 1 : 0);
			edNumberOfParts.setText(String.valueOf(parts));
		}
		doUpdate = true;
	}

	private boolean saveData () {
		long n;
		try {
			n = Long.parseLong(edNumberOfEntries.getText().trim());
		}
		catch ( NumberFormatException e ) {
			return false;
		}
		
		options = new SplitterOptions(sourceLocale);
		options.setEntriesPerPart(n);
		return true;
	}
}
