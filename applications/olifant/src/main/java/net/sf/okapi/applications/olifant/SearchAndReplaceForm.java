/*===========================================================================
  Copyright (C) 2011-2012 by the Okapi Framework contributors
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

import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.UIUtil;
import net.sf.okapi.lib.tmdb.SearchAndReplaceOptions;
import net.sf.okapi.lib.tmdb.SearchAndReplaceOptions.ACTION;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

class SearchAndReplaceForm {
	
	private final SearchAndReplaceOptions options;
	private final Shell shell;
	private final Text edSearch;
	private final Button chkReplace;
	private final Text edReplace;
	private final Button btFindNext;
	private final Button btReplaceOne;
	private final Button btFlagAll;
	private final Button btReplaceAll;
	private final Table tblFields;

	SearchAndReplaceForm (Shell parent,
		SearchAndReplaceOptions p_options,
		ArrayList<String> displayFields)
	{
		shell = new Shell(parent, SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		shell.setText("Search and Replace");
		UIUtil.inheritIcon(shell, parent);
		shell.setLayout(new GridLayout(3, false));

		this.options = p_options;
		options.setAction(ACTION.CLOSE);
		
		Label label = new Label(shell, SWT.NONE);
		label.setText("Search for:");
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_BEGINNING));
		
		edSearch = new Text(shell, SWT.BORDER);
		edSearch.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING));
		
		Composite buttons = new Composite(shell, SWT.NONE);
		GridData gdTmp = new GridData();
		gdTmp.verticalSpan = 3;
		buttons.setLayoutData(gdTmp);
		buttons.setLayout(new GridLayout(1, false));
		
		SelectionAdapter adapter = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if ( e.widget == btFindNext ) { //$NON-NLS-1$
					if ( !saveData() ) return;
					options.setAction(ACTION.FINDNEXT);
				}
				else if ( e.widget == btReplaceOne ) { //$NON-NLS-1$
					if ( !saveData() ) return;
					options.setAction(ACTION.REPLACE);
				}
				else if ( e.widget == btReplaceAll ) { //$NON-NLS-1$
					if ( !saveData() ) return;
					options.setAction(ACTION.REPLACEALL);
				}
				else if ( e.widget == btFlagAll ) { //$NON-NLS-1$
					if ( !saveData() ) return;
					options.setAction(ACTION.FLAGALL);
				}
				shell.close();
			};
		};
		
		btFindNext = new Button(buttons, SWT.PUSH);
		btFindNext.setText("Find Next");
		btFindNext.addSelectionListener(adapter);
		
		btReplaceOne = new Button(buttons, SWT.PUSH);
		btReplaceOne.setText("Replace");
		btReplaceOne.addSelectionListener(adapter);
		
		btFlagAll = new Button(buttons, SWT.PUSH);
		btFlagAll.setText("Flag All");
		btFlagAll.addSelectionListener(adapter);
		
		btReplaceAll = new Button(buttons, SWT.PUSH);
		btReplaceAll.setText("Replace All");
		btReplaceAll.addSelectionListener(adapter);
		
		UIUtil.setSameWidth(10, btFindNext, btReplaceOne, btFlagAll, btReplaceAll);
		
		chkReplace = new Button(shell, SWT.CHECK); 
		chkReplace.setText("Replace with:");
		chkReplace.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_BEGINNING));
		chkReplace.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateMode();
			}
		});
		
		edReplace = new Text(shell, SWT.BORDER);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
		gdTmp.widthHint = 350;
		edReplace.setLayoutData(gdTmp);
		
		tblFields = new Table(shell, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalSpan = 2;
		tblFields.setLayoutData(gdTmp);
		
		shell.pack();
		Rectangle Rect = shell.getBounds();
		shell.setMinimumSize(Rect.width, Rect.height);
		
		updateData(displayFields);
		Dialogs.placeWindowInSECorner(shell, parent);
	}

	private void updateMode () {
		boolean enabled = chkReplace.getSelection();
		edReplace.setEnabled(enabled);
		btReplaceOne.setEnabled(enabled);
		btReplaceAll.setEnabled(enabled);
	}
	
	private void updateData (ArrayList<String> displayFields) {
		edSearch.setText(options.getSearch());
		edReplace.setText(options.getReplace());
		
		ArrayList<String> fields = options.getFields();
		for ( String fn : displayFields ) {
			TableItem ti = new TableItem(tblFields, SWT.NONE);
			ti.setText(fn);
			ti.setChecked(fields.contains(fn));
		}

		chkReplace.setSelection(options.getReplaceMode());
		updateMode();
	}
	
	ACTION showDialog () {
		shell.open();
		while ( !shell.isDisposed() ) {
			if ( !shell.getDisplay().readAndDispatch() )
				shell.getDisplay().sleep();
		}
		return options.getAction();
	}

	private boolean saveData () {
		String tmp = edSearch.getText();
		if ( tmp.isEmpty() ) {
			Dialogs.showError(shell, "You must specify the text to searchfor.", null);
			edSearch.setFocus();
			return false;
		}
		
		ArrayList<String> fields = new ArrayList<String>();
		for ( TableItem ti : tblFields.getItems() ) {
			if ( ti.getChecked() ) fields.add(ti.getText());
		}
		if ( fields.isEmpty() ) {
			Dialogs.showError(shell, "You must select at least one field to search on.", null);
			tblFields.setFocus();
			return false;
		}

		options.setSearch(tmp);
		options.setReplace(edReplace.getText());
		options.setFields(fields);
		options.setReplaceMode(chkReplace.getSelection());
		return true;
	}
}
