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

import java.util.ArrayList;
import java.util.LinkedHashMap;

import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.OKCancelPanel;
import net.sf.okapi.common.ui.UIUtil;
import net.sf.okapi.lib.tmdb.DbUtil;
import net.sf.okapi.lib.tmdb.ITm;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

class ExportForm {
	
	private final Shell shell;
	private final LinkedHashMap<String, Boolean> fields;
	private final Table tblLangs;
	private final Table tblLangProps;
	private final Table tblUnitProps;
	private final Label stLangProps;
	
	private Object[] result;

	ExportForm (Shell parent,
		ITm tm)
	{
		shell = new Shell(parent, SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		shell.setText("Export");
		UIUtil.inheritIcon(shell, parent);
		shell.setLayout(new GridLayout(1, true));

		// Set up the list
		fields = new LinkedHashMap<String, Boolean>();
		java.util.List<String> list = tm.getAvailableFields();
		for ( String fn : list ) {
			if ( fn.startsWith(DbUtil.TEXT_PREFIX) || fn.startsWith(DbUtil.CODES_PREFIX) ) continue;
			fields.put(fn, true);
		}
		
		Group group = new Group(shell, SWT.NONE);
		group.setLayout(new GridLayout(3, true));
		group.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label label = new Label(group, SWT.NONE);
		label.setText("Locales:");
		
		stLangProps = new Label(group, SWT.NONE);
		stLangProps.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		label = new Label(group, SWT.NONE);
		label.setText("Unit fields:");

		tblLangs = new Table(group, SWT.BORDER | SWT.V_SCROLL | SWT.CHECK | SWT.FULL_SELECTION);
		GridData gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.widthHint = 200;
		gdTmp.heightHint = 300;
		tblLangs.setLayoutData(gdTmp);
		tblLangs.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent (Event e) {
				updateSegmentFields();
			}
		});
		new TableColumn(tblLangs, SWT.NONE);
		tblLangs.addControlListener(new ControlAdapter() {
		    public void controlResized(ControlEvent e) {
		    	tblLangs.getColumn(0).setWidth(tblLangs.getClientArea().width);
		    }
		});
		for ( String loc : tm.getLocales() ) {
			TableItem ti = new TableItem(tblLangs, SWT.NONE);
			ti.setText(loc);
			ti.setChecked(true);
		}
		
		tblLangProps = new Table(group, SWT.BORDER | SWT.V_SCROLL | SWT.CHECK | SWT.FULL_SELECTION);
		tblLangProps.setLayoutData(new GridData(GridData.FILL_BOTH));
		new TableColumn(tblLangProps, SWT.NONE);
		tblLangProps.addControlListener(new ControlAdapter() {
		    public void controlResized(ControlEvent e) {
		    	tblLangProps.getColumn(0).setWidth(tblLangProps.getClientArea().width);
		    }
		});
		
		tblUnitProps = new Table(group, SWT.BORDER | SWT.V_SCROLL | SWT.CHECK | SWT.FULL_SELECTION);
		tblUnitProps.setLayoutData(new GridData(GridData.FILL_BOTH));
		TableItem ti;
		for ( String fn : fields.keySet() ) {
			if ( !DbUtil.isSegmentField(fn) ) {
				ti = new TableItem(tblUnitProps, SWT.NONE);
				ti.setText(fn);
				ti.setChecked(fields.get(fn));
			}
		}
		ti = new TableItem(tblUnitProps, SWT.NONE); ti.setText(DbUtil.FLAG_NAME);
		ti = new TableItem(tblUnitProps, SWT.NONE); ti.setText(DbUtil.TUREF_NAME);
		new TableColumn(tblUnitProps, SWT.NONE);
		tblUnitProps.addControlListener(new ControlAdapter() {
		    public void controlResized(ControlEvent e) {
		    	tblUnitProps.getColumn(0).setWidth(tblUnitProps.getClientArea().width);
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
		
		tblLangs.setSelection(0);
		updateSegmentFields();
		
		shell.pack();
		shell.setMinimumSize(500, 200);
		Dialogs.centerWindow(shell, parent);
	}

	/**
	 * Opens the dialog box.
	 * @return An array of objects: 0=list of the locales to export,
	 * 1=list of the fields to export.
	 * or null if the user cancelled the operation.
	 */
	Object[] showDialog () {
		shell.open();
		while ( !shell.isDisposed() ) {
			if ( !shell.getDisplay().readAndDispatch() )
				shell.getDisplay().sleep();
		}
		return result;
	}

	private boolean saveData () {
		updateSegmentFields(); // Get the latest changes
		
		// Check the locales
		ArrayList<String> langs = new ArrayList<String>();
		for ( int i=0; i<tblLangs.getItemCount(); i++ ) {
			if ( tblLangs.getItem(i).getChecked() ) {
				langs.add(tblLangs.getItem(i).getText());
			}
		}
		if ( langs.size() < 1 ) {
			Dialogs.showError(shell, "You must select at list one locale to export.", null);
			tblLangs.setFocus();
			return false;
		}
		
		// Gather the fields
		ArrayList<String> list = new ArrayList<String>();
		for ( String fn : fields.keySet() ) {
			// Eliminate any field that in in a locale not for output
			String loc = DbUtil.getFieldLocale(fn);
			if ( loc != null ) {
				if ( !langs.contains(loc) ) continue;
			}
			// Else: add only the fields that are checked
			if ( fields.get(fn) ) {
				list.add(fn);
			}
		}
		
		result = new Object[2];
		result[0] = langs;
		result[1] = list;
		
		return true;
	}

	private void updateSegmentFields () {
		// Save previous selection
		for ( int i=0; i<tblLangProps.getItemCount(); i++ ) {
			String fn = tblLangProps.getItem(i).getText();
			fields.put(fn, tblLangProps.getItem(i).getChecked());
		}
		// Reset the list and fill it according the new locale
		tblLangProps.removeAll();
		String loc = tblLangs.getSelection()[0].getText();
		stLangProps.setText(String.format("Fields for %s:", loc));
		for ( String fn : fields.keySet() ) {
			String tmp = DbUtil.getFieldLocale(fn);
			if (( tmp != null ) && tmp.equals(loc) ) {
				TableItem ti = new TableItem(tblLangProps, SWT.NONE);
				ti.setText(fn);
				ti.setChecked(fields.get(fn));
			}
		}
	}

}
