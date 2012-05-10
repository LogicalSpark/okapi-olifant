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

public class SplitterOptions {

	private boolean splitLocales;
	private boolean splitEntries = true;
	private long entriesPerPart;
	private String sourceLocale;
	
	public SplitterOptions (String sourceLocale) {
		this.sourceLocale = sourceLocale;
	}

	public boolean getSplitLocales () {
		return splitLocales;
	}

	public void setSplitLocales (boolean splitLocales) {
		this.splitLocales = splitLocales;
	}

	public boolean getSplitEntries () {
		return splitEntries;
	}

	public void setSplitEntries (boolean splitEntries) {
		this.splitEntries = splitEntries;
	}

	public long getEntriesPerPart () {
		return entriesPerPart;
	}

	public void setEntriesPerPart (long entriesPerPart) {
		this.entriesPerPart = entriesPerPart;
	}

	public String getSourceLocale () {
		return sourceLocale;
	}
	
	public void setSourceLocale (String sourceLocale) {
		this.sourceLocale = sourceLocale;
	}

}
