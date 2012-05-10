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

class RenameTMForm {
	
	private Shell shell;
	private String currentName;
	private List<String> existingNames;
	private Text edName;
	private String result = null;

	RenameTMForm (Shell parent,
		String currentName,
		List<String> existingNames)
	{
		this.existingNames = existingNames;
		this.currentName = currentName;
		
		shell = new Shell(parent, SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		shell.setText("Rename TM");
		UIUtil.inheritIcon(shell, parent);
		shell.setLayout(new GridLayout(1, false));

		Group group = new Group(shell, SWT.NONE);
		group.setLayout(new GridLayout());
		group.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label stTmp = new Label(group, SWT.NONE);
		stTmp.setText("&Current name:");
		
		Text edCurrent = new Text(group, SWT.BORDER);
		edCurrent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		edCurrent.setEditable(false);
		edCurrent.setText(currentName);

		Label stName = new Label(group, SWT.NONE);
		stName.setText("&New name:");
		
		edName = new Text(group, SWT.BORDER);
		edName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		edName.setFocus();
		edName.setText(currentName);
		
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
		String name = edName.getText();
		if ( name.isEmpty() ) {
			Dialogs.showError(shell, "You must specify a name.", null);
			edName.setFocus();
			return false;
		}
		if ( name.equalsIgnoreCase(currentName) ) {
			result = name; // Allow changing the cases
			return true;
		}

		for ( String tmName : existingNames ) {
			if ( name.equalsIgnoreCase(tmName) ) {
				Dialogs.showError(shell, "There is already a TM with this name, please enter another name.", null);
				edName.setFocus();
				return false;
			}
		}
		result = name;
		return true;
	}
}
