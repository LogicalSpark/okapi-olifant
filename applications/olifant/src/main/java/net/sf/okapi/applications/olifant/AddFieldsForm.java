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

import java.util.List;

import net.sf.okapi.common.ui.ClosePanel;
import net.sf.okapi.common.ui.Dialogs;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

class AddFieldsForm {
	
	private Shell shell;
	private final Combo cbLocales;
	private final Text edName;
	private final Button btAdd;
	private final List<String> existingFields;
	private boolean changeWasMade = false;
	private ITm tm;

	AddFieldsForm (Shell parent,
		ITm tm)
	{
		this.tm = tm;
		shell = new Shell(parent, SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		shell.setText("Add Field");
		UIUtil.inheritIcon(shell, parent);
		shell.setLayout(new GridLayout());

		Group group = new Group(shell, SWT.NONE);
		group.setLayout(new GridLayout());
		group.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label stName = new Label(group, SWT.NONE);
		stName.setText("Name of the field to add:");
		
		edName = new Text(group, SWT.BORDER);
		edName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label stLocales = new Label(group, SWT.NONE);
		stLocales.setText("Locale to which the field applies:");

		cbLocales = new Combo(group, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
		GridData gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.widthHint = 200;
		cbLocales.setLayoutData(gdTmp);
		
		existingFields = tm.getAvailableFields();
		List<String> list = tm.getLocales();
		
		cbLocales.add("<All> (text unit level)");
		for ( String loc : list ) {
			cbLocales.add(loc);
		}
		if ( cbLocales.getItemCount() > 0 ) {
			cbLocales.select(0);
		}
		
		btAdd = UIUtil.createGridButton(group, SWT.PUSH, "Add Now", UIUtil.BUTTON_DEFAULT_WIDTH, 1);
		btAdd.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				addField();
			}
		});
		
		Label stNote = new Label(group, SWT.WRAP);
		stNote.setText("Note that adding or removing fields on a large translation memory may take time.");
		gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.widthHint = 200;
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
	
	private void addField () {
		try {
			// Check the field
			int n = cbLocales.getSelectionIndex();
			if ( n < 0 ) return;
			// Get and check the base name
			String fn = edName.getText().trim();
			if ( fn.isEmpty() ) {
				Dialogs.showError(shell, "You must provide a field name.", null);
				edName.setFocus();
				edName.selectAll();
				return;
			}
			fn = DbUtil.checkFieldName(fn);

			// Add the locale if needed
			if ( n > 0 ) {
				fn = fn + DbUtil.LOC_SEP + cbLocales.getItem(n);
			}
			if ( existingFields.contains(fn) 
				|| fn.equalsIgnoreCase(DbUtil.FLAG_NAME)
				|| fn.equalsIgnoreCase(DbUtil.SEGKEY_NAME)
				|| fn.equalsIgnoreCase(DbUtil.TUREF_NAME)
			) {
				Dialogs.showError(shell, String.format("The field '%s' exists already.", fn), null);
				edName.setFocus();
				edName.selectAll();
				return;
			}

			// Do the addition
			tm.addField(fn);
			
			// Update the list
			changeWasMade = true;
			existingFields.add(fn);
			edName.setText(""); // Prepare for next name
		}
		catch ( Throwable e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
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
