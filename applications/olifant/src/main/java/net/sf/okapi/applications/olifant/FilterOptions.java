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

import net.sf.okapi.lib.tmdb.DbUtil;
import net.sf.okapi.lib.tmdb.filter.FilterNode;
import net.sf.okapi.lib.tmdb.filter.Operator;
import net.sf.okapi.lib.tmdb.filter.OperatorNode;

public class FilterOptions {

	private boolean active;
	private boolean simpleFilterFlaggedOnly;
	private boolean simpleFilterExpression;
	private FilterNode simpleFilter;
	private FilterNode fullFilter;
	
	public FilterOptions () {
	}

	public FilterNode getCurrentFilter () {
		//TODO: complex filter
		if ( simpleFilterFlaggedOnly ) {
			return new OperatorNode(Operator.OP_EQUALS, DbUtil.FLAG_NAME, true);
		}

		return null;
	}
	
	public FilterNode getSimpleFilter () {
		return simpleFilter;
	}
	
	public void setSimpleFilter (FilterNode root) {
		simpleFilter = root;
	}

	public boolean getSimpleFilterFlaggedOnly () {
		return simpleFilterFlaggedOnly;
	}
	
	public void setSimpleFilterFlaggedOnly (boolean simpleFilterFlaggedOnly) {
		this.simpleFilterFlaggedOnly = simpleFilterFlaggedOnly;
	}
}
