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

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import net.sf.okapi.common.Util;
import net.sf.okapi.lib.tmdb.DbUtil;
import net.sf.okapi.lib.tmdb.filter.FilterNode;
import net.sf.okapi.lib.tmdb.filter.Operator;
import net.sf.okapi.lib.tmdb.filter.OperatorNode;
import net.sf.okapi.lib.tmdb.filter.ValueNode;

public class FilterExpressionPanel extends Composite {

	private final Combo cbField;
	private final Combo cbOperator;
	private final Text edValue;
	private final Button btOK;
	private final Button btCancel;
	
	public FilterExpressionPanel (Composite parent,
		int flags)
	{
		super(parent, flags);
		setLayout(new GridLayout(7, false));
		setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		cbField = new Combo(this, SWT.READ_ONLY | SWT.BORDER);
		cbField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		cbOperator = new Combo(this, SWT.READ_ONLY | SWT.BORDER);
		cbOperator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		edValue = new Text(this, SWT.BORDER);
		edValue.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		btOK = new Button(this, SWT.PUSH);
		btOK.setText("Apply");
		
		btCancel = new Button(this, SWT.PUSH);
		btCancel.setText("Discard");
	}

	public void updateAvailableFields (List<String> fields) {
		String current = cbField.getText();
		cbField.removeAll();
		if ( fields != null ) {
			for ( String fn : fields ) {
				cbField.add(fn);
			}
			int n = 0;
			if ( !Util.isEmpty(current) ) {
				n = cbField.indexOf(current);
				if ( n == -1 ) n = 0;
			}
			cbField.select(n);
		}
		updateOperators();
	}
	
	private void updateOperators () {
		// Get the type of field
		int scope = Operator.SCOPE_BOOLEAN;
		
		String current = cbOperator.getText();
		cbOperator.removeAll();
		for ( Operator.TYPE opType : Operator.OPERATORS.keySet() ) {
			Operator op = Operator.OPERATORS.get(opType);
			if ( (op.getScope() & scope) == scope ) {
				cbOperator.add(op.getName());
			}
		}
	}
	
	public void setNode (FilterNode node) {
		
	}
}
