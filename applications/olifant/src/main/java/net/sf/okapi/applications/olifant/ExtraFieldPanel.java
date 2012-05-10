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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

public class ExtraFieldPanel extends SashForm {

	private boolean modified;
	private List lbFields;
	private SegmentEditor edExtra;

	ExtraFieldPanel (Composite parent,
		int flags,
		ISegmentEditorUser caller)
	{
		super(parent, flags);
		setLayout(new GridLayout(1, false));
		setLayoutData(new GridData(GridData.FILL_BOTH));
		setOrientation(SWT.HORIZONTAL);
		
		edExtra = new SegmentEditor(this, -1, caller);
		
		lbFields = new List(this, SWT.BORDER);
		lbFields.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		setWeights(new int[] {4, 1});
		setSashWidth(4);
	}

	public boolean setFocus () {
		return edExtra.setFocus();
	}
	
	public void setEnabled (boolean enabled) {
		edExtra.setEnabled(enabled);
	}
	
	public void setEditable (boolean editable) {
		edExtra.setEditable(editable);
	}

	public void clear () {
		edExtra.setText("", null, -2);
		modified = false;
	}
	
	public boolean isModified () {
		return modified;
	}

	public void setText (String text,
		String codesAsText,
		int column)
	{
		edExtra.setEnabled(text != null);
		if ( text == null ) {
			edExtra.setText("", null, column);
		}
		else {
			edExtra.setText(text, codesAsText, column);
		}
		modified = false;
	}

	public String getText () {
		return edExtra.getText();
	}
	
	public void toggleFieldList () {
		if ( getWeights()[1] > 0 ) {
			setWeights(new int[]{1, 0});
			setSashWidth(0);
		}
		else {
			setWeights(new int[]{4, 1});
			setSashWidth(4);
		}
	}

}
