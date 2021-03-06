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

package net.sf.okapi.tm.olifant.common;

import java.util.HashMap;
import java.util.List;

import net.sf.okapi.tm.olifant.common.lucene.TmHit;
import net.sf.okapi.tm.olifant.common.lucene.Writer;

/**
 * Provides implementation-agnostic access to an Olifant TM index.
 */
public interface IIndexAccess {
	
	public void close ();
	
	public int search (String codedText,
		String codesAsString,
		String tmUUID,
		String locale,
		int maxHits,
		int threshold,
		HashMap<String, String> attributes);
	
	public List<TmHit> getHits ();
	
	public Writer getWriter ();

	public void deleteTMIndex (String uuid);
}
