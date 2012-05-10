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

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import net.sf.okapi.lib.tmdb.IProgressCallback;

class LogHandler extends Handler {
	
	private IProgressCallback pfb;

	public void setCallback (IProgressCallback pfb) {
		this.pfb = pfb;
	}
	
	@Override
	public void close ()
		throws SecurityException
	{
		// Do nothing
	}

	@Override
	public void flush () {
		// Do nothing
	}

	@Override
	public void publish (LogRecord record) {
		if ( pfb ==  null ) return;
		if ( record.getLevel() == Level.SEVERE ) {
			pfb.logMessage(IProgressCallback.MSGTYPE_ERROR, record.getMessage());
			Throwable e = record.getThrown();
			if ( e != null ) {
				String tmp = e.getMessage() + "\n@ "+e.toString(); //$NON-NLS-1$
				pfb.logMessage(IProgressCallback.MSGTYPE_INFO, tmp);
			}
		}
		else if ( record.getLevel() == Level.WARNING ) {
			// Otherwise print
			pfb.logMessage(IProgressCallback.MSGTYPE_WARNING, record.getMessage());
		}
		else if ( record.getLevel() == Level.INFO ) {
			pfb.logMessage(IProgressCallback.MSGTYPE_INFO, record.getMessage());
		}
	}

}
