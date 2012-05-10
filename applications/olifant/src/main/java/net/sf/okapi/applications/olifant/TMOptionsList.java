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

import java.util.Iterator;

import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.ui.UserConfiguration;

public class TMOptionsList extends UserConfiguration {
	
	private static final long serialVersionUID = 1L;

	TMOptions getItem (String uuid,
		boolean createIfNeeded)
	{
		String data = getProperty(uuid);
		IParameters prm = new TMOptions();
		
		if ( data != null ) {
			prm.fromString(data);
			// Update the last-usage information
			((TMOptions)prm).setLastUsage(System.currentTimeMillis());
			return (TMOptions)prm;
		}
		else if ( createIfNeeded ) {
			setProperty(uuid, prm.toString());
			return (TMOptions)prm;
		}
		// Does not exists and was not created
		return null;
	}

	void setItem (TmPanel tp) {
		setProperty(tp.getTm().getUUID(), tp.getTmOptions().toString());
	}

	/**
	 * Removes from the list any entry that has its last-usage time older than 30 days.
	 */
	void purgeOldItems () {
		final long limit = System.currentTimeMillis() - (((1000L*60L)*24)*30);
		Iterator<Object> iter = keySet().iterator();
		while ( iter.hasNext() ) {
			String uuid = (String)iter.next();
			String data = getProperty(uuid);
			if ( data == null )  continue;
			TMOptions opt = new TMOptions();
			((IParameters)opt).fromString(data);
			if ( opt.getLastUsage() < limit ) {
				iter.remove();
			}
		}
	}
}
