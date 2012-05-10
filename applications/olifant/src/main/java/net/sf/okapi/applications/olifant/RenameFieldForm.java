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

import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.OKCancelPanel;
import net.sf.okapi.common.ui.UIUtil;
import net.sf.okapi.lib.tmdb.DbUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

class RenameFieldForm {
	
	private Shell shell;
	private String currentName;
	private List<String> existingNames;
	private List<String> existingCodes;
	private Text edName;
	private String result = null;

	RenameFieldForm (Shell parent,
		String currentName,
		List<String> existingNames,
		List<String> existingCodes)
	{
		this.existingNames = existingNames;
		this.currentName = currentName;
		this.existingCodes = existingCodes;
		
		shell = new Shell(parent, SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		shell.setText("Rename Field");
		UIUtil.inheritIcon(shell, parent);
		shell.setLayout(new GridLayout(1, false));

		Group group = new Group(shell, SWT.NONE);
		group.setLayout(new GridLayout());
		group.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label stTmp = new Label(group, SWT.NONE);
		stTmp.setText("&Current field name:");
		
		Text edCurrent = new Text(group, SWT.BORDER);
		edCurrent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		edCurrent.setEditable(false);
		edCurrent.setText(currentName);

		Label stCode = new Label(group, SWT.NONE);
		stCode.setText("&New name:");
		
		edName = new Text(group, SWT.BORDER);
		edName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		edName.setText(currentName);
		edName.setFocus();
		
		SelectionAdapter OKCancelActions = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if ( e.widget.getData().equals("o") ) { //$NON-NLS-1$
					if ( !saveData() ) return;
				}
				shell.close();
			};
		};
		OKCancelPanel pnlActions = new OKCancelPanel(shell, SWT.NONE, OKCancelActions, false);
		GridData gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalSpan = 3;
		pnlActions.setLayoutData(gdTmp);
		shell.setDefaultButton(pnlActions.btOK);

		shell.pack();
		Rectangle Rect = shell.getBounds();
		shell.setMinimumSize(Rect.width, Rect.height);
		Dialogs.centerWindow(shell, parent);
	}

	String showDialog () {
		shell.open();
		while ( !shell.isDisposed() ) {
			if ( !shell.getDisplay().readAndDispatch() )
				shell.getDisplay().sleep();
		}
		return result;
	}

	private boolean saveData () {
		// Check the name
		String fullName = edName.getText();
		try {
			String loc = DbUtil.getFieldLocale(fullName);
			String tmp = DbUtil.getFieldRoot(fullName);
			fullName = DbUtil.checkFieldName(tmp);
			if ( loc != null ) {
				fullName = fullName + DbUtil.LOC_SEP + loc;
				// Check the code
				if ( !existingCodes.contains(loc) ) {
					Dialogs.showError(shell, String.format("The locale '%s' does not exists.", loc), null);
					edName.setFocus();
					return false;
				}
			}
			edName.setText(fullName);
		}
		catch ( Throwable e ) {
			Dialogs.showError(shell, e.getMessage(), null);
			edName.setFocus();
			return false;
		}
		if ( fullName.equals(currentName) ) {
			result = null; // No changes
			return true;
		}

		if ( DbUtil.isPreDefinedField(fullName) ) {
			Dialogs.showError(shell, "You canot use a special field name.", null);
			edName.setFocus();
			return false;
		}
		if ( existingNames.contains(fullName) ) {
			Dialogs.showError(shell, "There is already a field with this name.", null);
			edName.setFocus();
			return false;
		}
		result = fullName;
		return true;
	}

}
