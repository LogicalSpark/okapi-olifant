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

package net.sf.okapi.lib.tmdb.filter;

public class ValueNode extends FilterNode {

	public static enum TYPE {
		BOOLEAN,
		STRING,
		NUMBER,
		DATE
	}
	
	private final TYPE type;
	private final boolean isField;
	private Object value;
	
	public ValueNode (boolean isField,
		String value)
	{
		super();
		this.type = TYPE.STRING;
		this.isField = isField;
		this.value = value;
	}
	
	public ValueNode (boolean value) {
		super();
		this.type = TYPE.BOOLEAN;
		this.isField = false;
		this.value = value;
	}
	
	public TYPE getType () {
		return type;
	}
	
	public boolean isField () {
		return isField;
	}
	
	public String getStringValue () {
		if ( type == TYPE.STRING ) return (String)value;
		throw new RuntimeException("Not a string value.");
	}
	
	public boolean getBooleanValue () {
		if ( type == TYPE.BOOLEAN ) return (Boolean)value;
		throw new RuntimeException("Not a boolean value.");
	}

}
