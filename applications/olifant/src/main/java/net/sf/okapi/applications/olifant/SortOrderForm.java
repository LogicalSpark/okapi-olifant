/*===========================================================================
  Copyright (C) 2012 by the Okapi Framework contributors
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.okapi.common.Util;
import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.OKCancelPanel;
import net.sf.okapi.common.ui.UIUtil;
import net.sf.okapi.lib.tmdb.DbUtil;
import net.sf.okapi.lib.tmdb.ITm;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

class SortOrderForm {
	
	private final Shell shell;
	private final Text edExpression;
	private final ArrayList<String> fields;
	
	private Listener menuItemAction;
	
	private LinkedHashMap<String, Boolean> result = null;

	SortOrderForm (Shell parent,
		ITm tm,
		String source,
		String target,
		LinkedHashMap<String, Boolean> currentOrder)
	{
		shell = new Shell(parent, SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		shell.setText("Sort Order");
		UIUtil.inheritIcon(shell, parent);
		shell.setLayout(new GridLayout(1, true));

		// Set up the list
		fields = new ArrayList<String>(tm.getAvailableFields());
		fields.add(DbUtil.SEGKEY_NAME);
		fields.add(DbUtil.FLAG_NAME);

		Group group = new Group(shell, SWT.NONE);
		group.setLayout(new GridLayout(3, false));
		group.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label label = new Label(group, SWT.NONE);
		label.setText("Fields to sort on:");
		GridData gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalSpan = 3;
		label.setLayoutData(gdTmp);
		
		edExpression = new Text(group, SWT.BORDER);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalSpan = 3;
		edExpression.setLayoutData(gdTmp);

		createDropDownButton(group, "Fields...", UIUtil.BUTTON_DEFAULT_WIDTH, 1, fields, null, edExpression);

		ArrayList<String> directions = new ArrayList<String>();
		directions.add("ASC");
		directions.add("DESC");
		createDropDownButton(group, "Direction...", UIUtil.BUTTON_DEFAULT_WIDTH, 1, directions, null, edExpression);
		
		ArrayList<String> sampleText = new ArrayList<String>();
		sampleText.add("Sort by source text, and for the same source by segment key");
		sampleText.add("Sort by segment key in descending order");
		sampleText.add("Sort by target text, and for the same target by source text");
		ArrayList<String> sampleData = new ArrayList<String>();
		if ( Util.isEmpty(source) ) source = DbUtil.TEXT_PREFIX+"??";
		else source = DbUtil.TEXT_PREFIX+source;
		if ( Util.isEmpty(target) ) target = DbUtil.TEXT_PREFIX+"??";
		else target = DbUtil.TEXT_PREFIX+target;
		sampleData.add(source+", SegKey");
		sampleData.add("SegKey DESC");
		sampleData.add(target+", "+source);
		createDropDownButton(group, "Examples...", UIUtil.BUTTON_DEFAULT_WIDTH, 1, sampleText, sampleData, edExpression);
		
		SelectionAdapter OKCancelActions = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				result = null;
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
		
		shell.pack();
		shell.setMinimumSize(shell.getBounds().width, shell.getBounds().height);

		edExpression.setText(convertToString(currentOrder));
		
		Dialogs.centerWindow(shell, parent);
	}

	LinkedHashMap<String, Boolean> showDialog () {
		shell.open();
		while ( !shell.isDisposed() ) {
			if ( !shell.getDisplay().readAndDispatch() )
				shell.getDisplay().sleep();
		}
		return result;
	}

	private boolean saveData () {
		try {
			// Syntax: fieldname1 desc, fieldName2 asc, etc.
			// or fieldName1, fieldName2, etc.
			// or "file name"; 'filename2' desc, etc.
			String expr = edExpression.getText().trim();
			// Split by comma
			String[] tmp = expr.split("[,;]", -1);
			result = new LinkedHashMap<String, Boolean>();
			Pattern pat = Pattern.compile("(((['\"](.+)['\"]))|([^\\s]+))(\\s+(\\w+))?");
			for ( String part : tmp ) {
				part = part.trim();
				if ( part.isEmpty() ) continue;
				Matcher m = pat.matcher(part);
				if ( !m.find() ) {
					throw new RuntimeException(String.format("Invalid expression '%s'.", part));
				}
				
				// Parse field name
				String fn = m.group(4); // Check quoted first
				if ( Util.isEmpty(fn) ) {
					fn = m.group(5); // Un-quoted
					if ( Util.isEmpty(fn) ) {
						throw new RuntimeException(String.format("Invalid field name in '%s'.", part));
					}
				}
				if ( !fields.contains(fn) ) {
					throw new RuntimeException(String.format("Unkown field name '%s'.", fn));
				}
				
				// Parse order
				String order = m.group(7);
				if ( Util.isEmpty(order) ) order = "asc"; // Default
				else order = order.toLowerCase();
				if ( order.equals("asc") || order.equals("desc") ) {
					result.put(fn, order.equals("asc"));
				}
				else {
					throw new RuntimeException(String.format("Invalid direction '%s'. It must be 'ASC' or 'DESC'.", order));
				}
			}
		}
		catch ( Throwable e ) {
			Dialogs.showError(shell, e.getMessage(), null);
			edExpression.setFocus();
			return false;
		}
		return true;
	}

	Listener getMenuItemAction (final Text target) {
		if ( menuItemAction == null ) {
			menuItemAction = new Listener() {
				@Override
				public void handleEvent (Event event) {
					MenuItem mi = (MenuItem)event.widget;
					// Get the value to insert
					String tmp = (String)mi.getData();
//					// Make sure it's quoted if it has a space
//					if ( tmp.indexOf(' ') > -1 ) tmp = "\""+tmp+"\"";
					// Insert the value
					target.insert(tmp);
					target.setFocus();
				}
			};
		}
		return menuItemAction;
	}
	
	Button createDropDownButton (Composite parent,
		String label,
		int width,
		int horizontalSpan,
		java.util.List<String> displayItems,
		java.util.List<String> dataItems,
		Text target)
	{
		final Button button = UIUtil.createGridButton(parent, SWT.PUSH, label, width, horizontalSpan);
		final Menu menu = new Menu(button);
		for ( int i=0; i<displayItems.size(); i++ ) {
			MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
			menuItem.setText(displayItems.get(i));
			if ( dataItems != null ) menuItem.setData(dataItems.get(i));
			else menuItem.setData(displayItems.get(i));
			menuItem.addListener(SWT.Selection, getMenuItemAction(target));
		}
		button.setMenu(menu);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected (SelectionEvent event) {
				button.getMenu().setVisible(true);
            }
		});
		return button;
	}
	
	private String convertToString (LinkedHashMap<String, Boolean> order) {
		if ( Util.isEmpty(order) ) return "";
		StringBuilder tmp = new StringBuilder();
		for ( String fn : order.keySet() ) {
			Boolean asc = order.get(fn);
			if ( fn.indexOf(' ') > -1 ) fn = "\""+fn+"\"";
			if ( tmp.length() > 0 ) tmp.append(", ");
			tmp.append(fn+" ");
			tmp.append(asc ? "ASC" : "DESC");
		}
		return tmp.toString();
	}
}
