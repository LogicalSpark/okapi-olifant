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

package net.sf.okapi.lib.tmdb;

import java.util.ArrayList;

public class SearchAndReplaceOptions {

	public static enum ACTION {
		CLOSE,
		FINDNEXT,
		REPLACE,
		REPLACEALL,
		FLAGALL,
	}
	
	private String search;
	private String replace;
	private boolean replaceMode;
	private ArrayList<String> fields;
	private ACTION action;
	
	private int startPos;
	private int currentPos;
	private String startField;
	private String currentField;
	private long startSegKey;
	private long currentSegKey;
	
	public SearchAndReplaceOptions () {
		setSearch(null);
		setReplace(null);
	}

	public ACTION getAction () {
		return action;
	}

	public void setAction (ACTION action) {
		this.action = action;
	}
	
	public boolean getReplaceMode () {
		return replaceMode;
	}
	
	public void setReplaceMode (boolean replaceMode) {
		this.replaceMode = replaceMode;
	}

	public String getSearch () {
		return search;
	}
	
	public void setSearch (String search) {
		this.search = (search == null ? "" : search);
	}
	
	public String getReplace () {
		return replace;
	}
	
	public void setReplace (String replace) {
		this.replace = (replace == null ? "" : replace);
	}

	public ArrayList<String> getFields () {
		if ( fields == null ) fields = new ArrayList<String>();
		return fields;
	}
	
	public void setFields (ArrayList<String> newFields) {
		this.fields = newFields;
	}
	
}
