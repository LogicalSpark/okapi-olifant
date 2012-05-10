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

import net.sf.okapi.lib.tmdb.IProgressCallback;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;

class LogPanel extends Composite {

	private Text edLog;
	private Button btStop;
	private Button btClear;
	private CLabel info;
	private long startTime;
	private boolean inProgress;
	private boolean isCanceled;
	private long errors;
	private long warnings;
	
	LogPanel (Composite p_Parent,
		int p_nFlags)
	{
		super(p_Parent, p_nFlags);
		inProgress = isCanceled = false;
		createContent();
	}
	
	private void createContent () {
		GridLayout layout = new GridLayout(3, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		setLayout(layout);

		btStop = new Button(this, SWT.PUSH);
		btStop.setText("Stop...");
		btStop.setEnabled(false);
		btStop.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				// Ask confirmation
				MessageBox dlg = new MessageBox(getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
				dlg.setMessage("Do you want to stop this process?");
				dlg.setText(MainForm.APPNAME);
				if ( dlg.open() != SWT.YES ) {
					return; // Cancel or no.
				}
				isCanceled = true;
            }
		});
		
		btClear = new Button(this, SWT.PUSH);
		btClear.setText("Clear");
		btClear.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				edLog.setText("");
            }
		});
		
		info = new CLabel(this, SWT.BORDER);
		GridData gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		info.setLayoutData(gdTmp);
		
		edLog = new Text(this, SWT.WRAP | SWT.V_SCROLL | SWT.BORDER);
		gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.horizontalSpan = 3;
		edLog.setLayoutData(gdTmp);
	}

	public boolean setInfo (String text) {
		info.setText((text == null) ? "" : text); //$NON-NLS-1$
		return isCanceled;
	}
	
	public boolean log (int type,
		String text)
	{
		if ( text != null ) {
			edLog.append(text+"\n");
			if ( type == IProgressCallback.MSGTYPE_WARNING ) warnings++;
			else if ( type == IProgressCallback.MSGTYPE_ERROR ) errors++;
		}
		return isCanceled;
	}
	
	public void startTask (String text) {
		isCanceled = false;
		inProgress = true;
		startTime = System.currentTimeMillis();
		edLog.setText(""); // Clear all
		log(IProgressCallback.MSGTYPE_INFO, text);
		btStop.setEnabled(true);
		warnings = errors = 0;
	}
	
	public void endTask (String text) {
		setInfo("");
		log(IProgressCallback.MSGTYPE_INFO, text);
		log(IProgressCallback.MSGTYPE_INFO,
			String.format("Duration: %s. Errors=%d Warnings=%d.",
				toHMSMS(System.currentTimeMillis()-startTime), errors, warnings));
		isCanceled = false;
		inProgress = false;
		btStop.setEnabled(false);
	}
	
	public boolean inProgress () {
		return inProgress;
	}

	private String toHMSMS (long millis) {
		long hours = millis/3600000;
		millis = millis - (hours*3600000);
		long minutes = millis/60000;
		millis = millis-(minutes*60000);
		long seconds = millis/1000;
		millis = millis-(seconds*1000);
		return String.format("%dh %dm %ds %dms", hours, minutes, seconds, millis);
	}
	
}
