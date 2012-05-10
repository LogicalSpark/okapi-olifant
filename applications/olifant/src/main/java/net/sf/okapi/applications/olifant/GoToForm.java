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

import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.UIUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

class GoToForm {
	
	private final Shell shell;
	private final Text edValue;
	private final long pageCount;
	
	private Object[] result;

	GoToForm (Shell parent,
		long pageCount)
	{
		shell = new Shell(parent, SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		shell.setText("Go To");
		UIUtil.inheritIcon(shell, parent);
		shell.setLayout(new GridLayout(1, false));

		this.pageCount = pageCount;
		
		Group group = new Group(shell, SWT.NONE);
		group.setLayout(new GridLayout());
		group.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label stValue = new Label(group, SWT.NONE);
		stValue.setText("Enter the SegKey value of a segment or a page number:");
		
		edValue = new Text(group, SWT.BORDER);
		edValue.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		final SelectionAdapter buttonsActions = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				char id = (Character)e.widget.getData();
				if (( id == 's' ) || ( id == 'p' )) { //$NON-NLS-1$
					if ( !saveData(id) ) return;
				}
				shell.close();
			};
		};
		
		Composite panel = new Composite(shell, SWT.NONE);
		panel.setLayout(new GridLayout(3, true));
		panel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		final Button btSegment = UIUtil.createGridButton(panel, SWT.PUSH, "Go To &Segment", UIUtil.BUTTON_DEFAULT_WIDTH, 1);
		btSegment.setData('s');
		btSegment.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btSegment.addSelectionListener(buttonsActions);
		
		final Button btPage = UIUtil.createGridButton(panel, SWT.PUSH, "Go To &Page", UIUtil.BUTTON_DEFAULT_WIDTH, 1);
		btPage.setData('p');
		btPage.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btPage.addSelectionListener(buttonsActions);
		
		final Button btCancel = UIUtil.createGridButton(panel, SWT.PUSH, "Cancel", UIUtil.BUTTON_DEFAULT_WIDTH, 1);
		btCancel.setData('c');
		btCancel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btCancel.addSelectionListener(buttonsActions);
		
		shell.setDefaultButton(btSegment);
		
		shell.pack();
		Rectangle Rect = shell.getBounds();
		shell.setMinimumSize(Rect.width, Rect.height);
		Dialogs.centerWindow(shell, parent);
	}

	Object[] showDialog () {
		shell.open();
		while ( !shell.isDisposed() ) {
			if ( !shell.getDisplay().readAndDispatch() )
				shell.getDisplay().sleep();
		}
		return result;
	}

	private boolean saveData (char buttonId) {
		long value = -1;
		String tmp = edValue.getText().trim();
		if ( tmp.isEmpty() ) {
			Dialogs.showError(shell, "You must specify a value.", null);
			edValue.setFocus();
			return false;
		}
		try {
			value = Long.parseLong(tmp);
		}
		catch ( NumberFormatException e ) {
			Dialogs.showError(shell, "Invalid numeric value.", null);
			edValue.setFocus();
			return false;
		}
		
		if ( buttonId == 'p' ) {
			if (( value < 1 ) || ( value > pageCount )) {
				Dialogs.showError(shell, String.format("The page must be beetween 1 and %d.", pageCount), null);
				edValue.setFocus();
				return false;
			}
			value--; // Pages are actually zero-based
		}
		
		result = new Object[2];
		result[0] = buttonId;
		result[1] = value;
		return true;
	}

}
