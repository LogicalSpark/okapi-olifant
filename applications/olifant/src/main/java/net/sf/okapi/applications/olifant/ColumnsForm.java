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
import java.util.Arrays;

import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.OKCancelPanel;
import net.sf.okapi.common.ui.UIUtil;
import net.sf.okapi.lib.tmdb.DbUtil;
import net.sf.okapi.lib.tmdb.ITm;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

class ColumnsForm {
	
	private Shell shell;
	private final List lbAvailableFields;
	private final List lbDisplayFields;
	private final Button btShow;
	private final Button btHide;
	private final Button btShowAll;
	private final Button btShowAllTexts;
	private final Button btMoveUp;
	private final Button btMoveDown;
	private final Button btAdd;
	private final Button btDelete;
	private final Button btRename;
	private boolean changedFields = false;
	private Object results[] = new Object[2];
	private ITm tm;
	private boolean inProcessMode = false;

	ColumnsForm (Shell parent,
		ITm tm,
		ArrayList<String> visibleFields)
	{
		this.tm = tm;
		shell = new Shell(parent, SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		shell.setText("Columns and Fields");
		UIUtil.inheritIcon(shell, parent);
		shell.setLayout(new GridLayout(3, false));

		// Handling of the closing event
		shell.addShellListener(new ShellListener() {
			public void shellActivated(ShellEvent event) {}
			public void shellClosed(ShellEvent event) {
				results[1] = changedFields;
			}
			public void shellDeactivated(ShellEvent event) {}
			public void shellDeiconified(ShellEvent event) {}
			public void shellIconified(ShellEvent event) {}
		});
		
		Group group = new Group(shell, SWT.NONE);
		group.setLayout(new GridLayout());
		group.setLayoutData(new GridData(GridData.FILL_BOTH));
		group.setText("Available fields:");
		
		lbAvailableFields = new List(group, SWT.BORDER | SWT.V_SCROLL);
		GridData gdTmp = new GridData(GridData.FILL_BOTH);
		int minListWidth = 150;
		int minListHeight = 250;
		gdTmp.widthHint = minListWidth;
		gdTmp.heightHint = minListHeight;
		lbAvailableFields.setLayoutData(gdTmp);
		
		// Buttons for the "available fields" list
		
		int minButtonWidth = 100;
		btAdd = UIUtil.createGridButton(group, SWT.PUSH, "Add Fields...", minButtonWidth, 1);
		btAdd.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		btAdd.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				addFields();
			}
		});
		
		btDelete = UIUtil.createGridButton(group, SWT.PUSH, "Delete Field...", minButtonWidth, 1);
		btDelete.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		btDelete.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				deleteField();
			}
		});

		btRename = UIUtil.createGridButton(group, SWT.PUSH, "Rename Field...", minButtonWidth, 1);
		btRename.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		btRename.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				renameField();
			}
		});

		UIUtil.setSameWidth(minButtonWidth, btAdd, btDelete, btRename);

		
		//--- Middle buttons
		Composite cmp = new Composite(shell, SWT.NONE);
		cmp.setLayout(new GridLayout());
		
		btShow = UIUtil.createGridButton(cmp, SWT.PUSH, "Show >>", minButtonWidth, 1);
		btShow.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
		btShow.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				showField();
			}
		});
		
		btHide = UIUtil.createGridButton(cmp, SWT.PUSH, "<< Hide", minButtonWidth, 1);
		btHide.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
		btHide.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				hideField();
			}
		});
		
		btShowAll = UIUtil.createGridButton(cmp, SWT.PUSH, "Show All", minButtonWidth, 1);
		btShowAll.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
		btShowAll.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				showAllFields();
			}
		});
		
		btShowAllTexts = UIUtil.createGridButton(cmp, SWT.PUSH, "Show All Texts", minButtonWidth, 1);
		btShowAllTexts.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
		btShowAllTexts.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				showAllTextFields();
			}
		});
		
		
		UIUtil.setSameWidth(minButtonWidth, cmp.getChildren());
		
		group = new Group(shell, SWT.NONE);
		group.setLayout(new GridLayout());
		group.setLayoutData(new GridData(GridData.FILL_BOTH));
		group.setText("Fields to display (Columns):");

		lbDisplayFields = new List(group, SWT.BORDER | SWT.V_SCROLL);
		gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.widthHint = minListWidth;
		gdTmp.heightHint = minListHeight;
		lbDisplayFields.setLayoutData(gdTmp);
		lbDisplayFields.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateMoveCommands();
			};
		});

		btMoveUp = UIUtil.createGridButton(group, SWT.PUSH, "Move Up", minButtonWidth, 1);
		btMoveUp.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		btMoveUp.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				moveUp();
			}
		});
		
		btMoveDown = UIUtil.createGridButton(group, SWT.PUSH, "Move Down", minButtonWidth, 1);
		btMoveDown.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		btMoveDown.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				moveDown();
			}
		});

		UIUtil.setSameWidth(minButtonWidth, btMoveUp, btMoveDown);

		updateLists(visibleFields, 0, 1);
		
		SelectionAdapter OKCancelActions = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if ( e.widget.getData().equals("o") ) { //$NON-NLS-1$
					ArrayList<String> newList = new ArrayList<String>();
					newList = new ArrayList<String>(Arrays.asList(lbDisplayFields.getItems()));
					newList.remove(0);
					results[0] = newList;
				}
				// In both case: the second returned object is a boolean saying if fields
				// have been added or deleted
				results[1] = changedFields;
				shell.close();
			};
		};
		OKCancelPanel pnlActions = new OKCancelPanel(shell, SWT.NONE, OKCancelActions, false);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalSpan = 3;
		pnlActions.setLayoutData(gdTmp);
		shell.setDefaultButton(pnlActions.btOK);

		shell.pack();
		Rectangle Rect = shell.getBounds();
		shell.setMinimumSize(Rect.width, Rect.height);
		Dialogs.centerWindow(shell, parent);
	}

//	private boolean saveData () {
//		ArrayList<String> newList = new ArrayList<String>();
//		newList = new ArrayList<String>(Arrays.asList(lbDisplayFields.getItems()));
//		newList.remove(0);
//		boolean hasText = false;
//		for ( String fn : lbDisplayFields.getItems() ) {
//			if ( fn.startsWith(DbUtil.TEXT_PREFIX) ) {
//				hasText = true;
//				break;
//			}
//		}
//		if ( !hasText ) {
//			Dialogs.showError(shell, "You must select at least one text field to display.", null);
//			return false;
//		}
//		results[0] = newList;
//		
//		return true;
//	}
	
	private void updateLists (ArrayList<String> visibleFields,
		int availableSelection,
		int displaySelection)
	{
		// Get the current list of available fields
		ArrayList<String> list = new ArrayList<String>(tm.getAvailableFields());
		// Remove from the display list fields not available
		visibleFields.retainAll(list);
		// Remove the fields that are already in the display list
		list.removeAll(visibleFields);
		
		// Reset the list of available fields
		lbAvailableFields.removeAll();
		for ( String fn :  list ) {
			lbAvailableFields.add(fn);
		}
		// Set the selection
		if (( availableSelection < 0 )
			|| ( availableSelection >= lbAvailableFields.getItemCount() )) {
			availableSelection = lbAvailableFields.getItemCount()-1;
		}
		if ( lbAvailableFields.getItemCount() > availableSelection ) {
			lbAvailableFields.setSelection(availableSelection);
		}

		// Reset the list of display fields
		lbDisplayFields.removeAll();
		lbDisplayFields.add("Flag/SegKey (always)");
		for ( String fn : visibleFields ) {
			lbDisplayFields.add(fn);
		}
		// Set the selection
		if (( displaySelection < 0 ) 
			|| ( displaySelection >= lbDisplayFields.getItemCount() )) {
			displaySelection = lbDisplayFields.getItemCount()-1;
		}
		if ( lbDisplayFields.getItemCount() > displaySelection ) {
			lbDisplayFields.setSelection(displaySelection);
		}
		
		// Update the buttons
		updateCommands();
	}
	
	private void updateCommands () {
		boolean hasAvailableFields = (( lbAvailableFields.getItemCount() > 0 ) && !inProcessMode );
		btAdd.setEnabled(!inProcessMode);
		btShow.setEnabled(hasAvailableFields);
		btShowAll.setEnabled(hasAvailableFields);
		btShowAllTexts.setEnabled(hasAvailableFields);
		updateMoveCommands();
		btDelete.setEnabled(hasAvailableFields);
		btRename.setEnabled(hasAvailableFields);
	}
	
	private void updateMoveCommands () {
		int n = (inProcessMode ? -1 : lbDisplayFields.getSelectionIndex());
		btHide.setEnabled(lbDisplayFields.getSelectionCount()>0 && lbDisplayFields.getItemCount()>1 && n>0);
		btMoveUp.setEnabled(n>1);
		btMoveDown.setEnabled(n<lbDisplayFields.getItemCount()-1 && n>0);
	}
	
	private void ensureSelection (int last,
		List list)
	{
		if ( list.getItemCount() > 0) {
			if ( last > list.getItemCount()-1 ) {
				list.setSelection(list.getItemCount()-1);
			}
			else {
				list.setSelection(last < 0 ? list.getItemCount()-1 : last);
			}
		}
	}
	
	private void showAllTextFields () {
		try {
			int n = lbAvailableFields.getSelectionIndex();
			for ( String fn : lbAvailableFields.getItems() ) {
				if ( fn.startsWith(DbUtil.TEXT_PREFIX) ) {
					lbAvailableFields.remove(fn);
					lbDisplayFields.add(fn);
				}
			}
			ensureSelection(-1, lbDisplayFields);
			ensureSelection(n, lbAvailableFields);
			updateCommands();
		}
		catch ( Throwable e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
	}
	
	private void showAllFields () {
		try {
			for ( String fn : lbAvailableFields.getItems() ) {
				lbAvailableFields.remove(fn);
				lbDisplayFields.add(fn);
			}
			ensureSelection(-1, lbDisplayFields);
			updateCommands();
		}
		catch ( Throwable e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
	}
	
	private void showField () {
		try {
			int n = lbAvailableFields.getSelectionIndex();
			if ( n < 0 ) return;
			String names[] = lbAvailableFields.getSelection();
			for ( String fn : names ) {
				lbDisplayFields.add(fn);
				lbAvailableFields.remove(fn);
			}
			ensureSelection(-1, lbDisplayFields);
			ensureSelection(n, lbAvailableFields);
			updateCommands();
		}
		catch ( Throwable e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
	}
	
	private void hideField () {
		try {
			int n = lbDisplayFields.getSelectionIndex();
			if ( n < 0 ) return;
			String names[] = lbDisplayFields.getSelection();
			for ( String fn : names ) {
				lbAvailableFields.add(fn);
				lbDisplayFields.remove(fn);
			}
			ensureSelection(-1, lbAvailableFields);
			ensureSelection(n, lbDisplayFields);
			updateCommands();
		}
		catch ( Throwable e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
	}
	
	private void moveUp () {
		try {
			int n = lbDisplayFields.getSelectionIndex();
			if ( n < 2 ) return;
//todo			
			Dialogs.showWarning(shell, "Not implemented yet.", null);
		}
		catch ( Throwable e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
	}
	
	private void moveDown () {
		try {
			int n = lbDisplayFields.getSelectionIndex();
			if ( n < 0 ) return;
//todo			
			Dialogs.showWarning(shell, "Not implemented yet.", null);
		}
		catch ( Throwable e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
	}
	
	private void setInProcess (boolean inProcess) {
		this.inProcessMode = inProcess;
		updateCommands();
	}
	
	private void addFields () {
		try {
			ArrayList<String> toDisplay = new ArrayList<String>(Arrays.asList(lbDisplayFields.getItems()));
			toDisplay.remove(0);

			AddFieldsForm dlg = new AddFieldsForm(shell, tm);
			setInProcess(true);
			if ( !dlg.showDialog() ) return; // Nothing changed
			// Else: update the lists
			updateLists(toDisplay, lbAvailableFields.getSelectionIndex(), lbDisplayFields.getSelectionIndex());
			changedFields = true;
		}
		catch ( Throwable e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
		finally {
			setInProcess(false);
		}
	}
	
	private void deleteField () {
		try {
			int n = lbAvailableFields.getSelectionIndex();
			if ( n < 0 ) return;
			setInProcess(true);
			String fn = lbAvailableFields.getItem(n);
			if ( DbUtil.isPreDefinedField(fn) ) {
				Dialogs.showError(shell, "You cannot delete a special field.", null);
				return;
			}
			
			// Ask confirmation
			MessageBox dlg = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO | SWT.CANCEL);
			dlg.setMessage(String.format(
				"This command will delete the field '%s' in the TM '%s'.\n" +
				"This operation is done immediately and cannot be undone.\n" +
				"Do you want to proceed?",
				fn, tm.getName()));
			if ( dlg.open() != SWT.YES ) {
				return; // Cancel or no.
			}
			tm.deleteField(fn);
			changedFields = true;
			updateLists(new ArrayList<String>(Arrays.asList(lbDisplayFields.getItems())),
				lbAvailableFields.getSelectionIndex(), lbDisplayFields.getSelectionIndex());
		}
		catch ( Throwable e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
		finally {
			setInProcess(false);
		}
	}
	
	private void renameField () {
		try {
			int n = lbAvailableFields.getSelectionIndex();
			if ( n < 0 ) return;
			String currentName = lbAvailableFields.getItem(n);

			ArrayList<String> existingNames = new ArrayList<String>(tm.getAvailableFields());
			java.util.List<String> existingCodes = tm.getLocales();
			RenameFieldForm dlg = new RenameFieldForm(shell, currentName, existingNames, existingCodes);
			setInProcess(true);
			String newName = dlg.showDialog();
			if ( newName == null ) return; // Cancel

			// Rename the field
			tm.renameField(currentName, newName);
			
			changedFields = true;
			updateLists(new ArrayList<String>(Arrays.asList(lbDisplayFields.getItems())),
				lbAvailableFields.getSelectionIndex(), lbDisplayFields.getSelectionIndex());
		}
		catch ( Throwable e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
		finally {
			setInProcess(false);
		}
	}
	
	/* Return 2 objects:
	 * 0 = an ArrayList<String> of the new fields to display or null if cancel
	 * 1 = a boolean true if fields have been modified (added, deleted, renamed)
	 */
	Object[] showDialog () {
		shell.open();
		while ( !shell.isDisposed() ) {
			if ( !shell.getDisplay().readAndDispatch() )
				shell.getDisplay().sleep();
		}
		return results;
	}

}
