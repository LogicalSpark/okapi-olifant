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
import java.util.Iterator;
import java.util.List;

import net.sf.okapi.common.BaseParameters;
import net.sf.okapi.common.ListUtil;
import net.sf.okapi.lib.tmdb.ITm;

public class TMOptions extends BaseParameters {

	private static final String LASTUSAGE = "lastUsage";
	private static final String PAGESIZE = "pageSize";
	private static final String VISIBLEFIELDS = "visibleFields";
	private static final String SOURCELOCALE = "sourceLocale";
	private static final String TARGETLOCALE = "targetLocale";
	
	private long lastUsage;
	private long pageSize;
	private ArrayList<String> visibleFields;
	private String sourceLocale;
	private String targetLocale;
	
	public TMOptions () {
		reset();
		toString(); // fill the list
	}
	
	public long getPageSize () {
		return pageSize;
	}
	
	public void setPageSize (long pageSize) {
		this.pageSize = pageSize;
	}
	
	public ArrayList<String> getVisibleFields () {
		return visibleFields;
	}
	
	public void setVisibleFields (ArrayList<String> visibleFields) {
		this.visibleFields = visibleFields;
	}
	
	public String getSourceLocale () {
		return sourceLocale;
	}
	
	public void setSourceLocale (String sourceLocale) {
		this.sourceLocale = sourceLocale;
	}
	
	public String getTargetLocale () {
		return targetLocale;
	}

	public void setTargetLocale (String targetLocale) {
		this.targetLocale = targetLocale;
	}
	
	public long getLastUsage () {
		return lastUsage;
	}

	public void setLastUsage (long lastUsage) {
		this.lastUsage = lastUsage;
	}

	@Override
	public void reset () {
		lastUsage = System.currentTimeMillis();
		pageSize = 500;
		visibleFields = new ArrayList<String>();
		sourceLocale = "";
		targetLocale = "";
	}

	@Override
	public void fromString (String data) {
		reset();
		buffer.fromString(data);
		String tmp = buffer.getString(PAGESIZE, String.valueOf(pageSize));
		pageSize = Long.valueOf(tmp);
		tmp = buffer.getString(VISIBLEFIELDS, null);
		visibleFields = new ArrayList<String>(ListUtil.stringAsList(tmp));
		sourceLocale = buffer.getString(SOURCELOCALE, sourceLocale);
		targetLocale = buffer.getString(TARGETLOCALE, targetLocale);
		tmp = buffer.getString(LASTUSAGE, String.valueOf(lastUsage));
		lastUsage = Long.valueOf(tmp);
	}
	
	@Override
	public String toString () {
		buffer.reset();
		buffer.setString(PAGESIZE, String.valueOf(pageSize));
		buffer.setString(VISIBLEFIELDS, ListUtil.listAsString(visibleFields));
		buffer.setString(SOURCELOCALE, sourceLocale);
		buffer.setString(TARGETLOCALE, targetLocale);
		buffer.setString(LASTUSAGE, String.valueOf(lastUsage));
		return buffer.toString();
	}
	
	public void ajustOptions (ITm tm) {
		// Checks that all visible fields exist
		List<String> available = tm.getAvailableFields();
		Iterator<String> iter = visibleFields.iterator();
		while ( iter.hasNext() ) {
			String fn = iter.next();
			if ( available.indexOf(fn) == -1 ) {
				iter.remove();
			}
		}
	}

}
