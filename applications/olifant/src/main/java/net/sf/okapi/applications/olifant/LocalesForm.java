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

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.ui.ClosePanel;
import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.InputDialog;
import net.sf.okapi.common.ui.UIUtil;
import net.sf.okapi.lib.tmdb.DbUtil;
import net.sf.okapi.lib.tmdb.ITm;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

class LocalesForm {
	
	private Shell shell;
	private final List lbLocales;
	private final Button btAdd;
	private final Button btDelete;
	private final Button btRename;
	private boolean changeWasMade = false;
	private ITm tm;

	LocalesForm (Shell parent,
		ITm tm)
	{
		this.tm = tm;
		shell = new Shell(parent, SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		shell.setText("Locales");
		UIUtil.inheritIcon(shell, parent);
		shell.setLayout(new GridLayout());

		Group group = new Group(shell, SWT.NONE);
		group.setLayout(new GridLayout(2, false));
		group.setLayoutData(new GridData(GridData.FILL_BOTH));
		group.setText("Current locales in the TM:");
		
		lbLocales = new List(group, SWT.BORDER | SWT.V_SCROLL);
		GridData gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.verticalSpan = 3;
		gdTmp.widthHint = 150;
		gdTmp.heightHint = 200;
		lbLocales.setLayoutData(gdTmp);
		
		btAdd = UIUtil.createGridButton(group, SWT.PUSH, "Add...", UIUtil.BUTTON_DEFAULT_WIDTH, 1);
		btAdd.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		btAdd.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				addLocale();
			}
		});
		
		btDelete = UIUtil.createGridButton(group, SWT.PUSH, "Delete...", UIUtil.BUTTON_DEFAULT_WIDTH, 1);
		btDelete.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		btDelete.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				deleteLocale();
			}
		});
		
		btRename = UIUtil.createGridButton(group, SWT.PUSH, "Rename...", UIUtil.BUTTON_DEFAULT_WIDTH, 1);
		btRename.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		btRename.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				renameLocale();
			}
		});
		
		UIUtil.setSameWidth(UIUtil.BUTTON_DEFAULT_WIDTH, btAdd, btDelete, btRename);
		updateList(0);
		
		Label stNote = new Label(group, SWT.WRAP);
		stNote.setText("Note that adding or removing locales on a large translation memory may take time.");
		gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.horizontalSpan = 2;
		gdTmp.widthHint = 150;
		stNote.setLayoutData(gdTmp);
		
		SelectionAdapter CloseActions = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				shell.close();
			};
		};
		
		ClosePanel pnlActions = new ClosePanel(shell, SWT.NONE, CloseActions, false);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		pnlActions.setLayoutData(gdTmp);

		shell.pack();
		Rectangle Rect = shell.getBounds();
		shell.setMinimumSize(Rect.width, Rect.height);
		Dialogs.centerWindow(shell, parent);
	}
	
	private void updateList (int selection) {
		java.util.List<String> list = tm.getLocales();
		// Reset the list of available fields
		lbLocales.removeAll();
		for ( String loc :  list ) {
			lbLocales.add(loc);
		}
		
		// Set the selection
		if ( selection < 0 ) {
			selection = lbLocales.getItemCount()-1;
		}
		else if ( selection >= lbLocales.getItemCount() ) {
			selection = 0;
		}
		if ( lbLocales.getItemCount() > 0 ) {
			lbLocales.setSelection(selection);
		}
		
		// Update delete button (must have at least one locale)
		btDelete.setEnabled(lbLocales.getItemCount()>1);
	}
	
	private void addLocale () {
		try {
			java.util.List<String> existing = tm.getLocales();
			String res = "";
			
			while ( true ) {
				InputDialog dlg = new InputDialog(shell, "Add Locale",
					"Code of the locale to add (e.g. EN for English, etc.):", res, null, 0, -1, -1);
				res = dlg.showDialog();
				if ( Util.isEmpty(res) ) return; // Cancel
				// Convert to a LocaleId to check the syntax and convert to Olifant locale
				try {
					res = DbUtil.toOlifantLocaleCode(LocaleId.fromString(res));
				}
				catch ( Throwable e ) {
					Dialogs.showError(shell, e.getMessage(), null);
				}
				if ( !existing.contains(res) ) break;
				// Else: error message: locale exists already
				Dialogs.showError(shell, String.format("The locale '%s' exists already", res), null);
			}
			
			tm.addLocale(res);
			updateList(-1);
		}
		catch ( Throwable e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
		changeWasMade = true;
	}

	private void deleteLocale () {
		try {
			int n = lbLocales.getSelectionIndex();
			if ( n < 0 ) return;
			String loc = lbLocales.getItem(n);
			
			// Ask confirmation
			MessageBox dlg = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO | SWT.CANCEL);
			dlg.setMessage(String.format(
				"This command will delete ALL FIELDS for the locale '%s'.\nThis operation cannot be undone.\nDo you want to proceed?", loc));
			if ( dlg.open() != SWT.YES ) {
				return; // Cancel or no.
			}
			// Delete the locale
			tm.deleteLocale(loc);
			updateList(n);
		}
		catch ( Throwable e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
		changeWasMade = true;
	}

	private void renameLocale () {
		try {
			int n = lbLocales.getSelectionIndex();
			if ( n < 0 ) return;
			String currentCode = lbLocales.getItem(n);
			java.util.List<String> existingCodes = tm.getLocales();
			RenameLocaleForm dlg = new RenameLocaleForm(shell, currentCode, existingCodes);
			String newCode = dlg.showDialog();
			if ( newCode == null ) return; // Cancel
			// Rename the locale
			tm.renameLocale(currentCode, newCode);
			updateList(n);
		}
		catch ( Throwable e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
		changeWasMade = true;
	}

	boolean showDialog () {
		shell.open();
		while ( !shell.isDisposed() ) {
			if ( !shell.getDisplay().readAndDispatch() )
				shell.getDisplay().sleep();
		}
		return changeWasMade;
	}

}
