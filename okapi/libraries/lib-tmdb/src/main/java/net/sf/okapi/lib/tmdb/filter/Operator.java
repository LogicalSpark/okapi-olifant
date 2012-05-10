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

import java.util.LinkedHashMap;

public class Operator {

	public static enum TYPE {
		OR,
		AND,
		NOT,
		EQUALS,
		CONTAINS
	}
	
	public static final int SCOPE_BOOLEAN = 0x01;
	public static final int SCOPE_STRING = 0x02;
	public static final int SCOPE_NUMBER = 0x04;
	public static final int SCOPE_DATE = 0x08;
	public static final int SCOPE_ALL = 0xFF;

	public static final Operator OP_OR = new Operator(TYPE.OR, SCOPE_BOOLEAN, "or");
	public static final Operator OP_AND = new Operator(TYPE.AND, SCOPE_BOOLEAN, "and");
	public static final Operator OP_NOT = new Operator(TYPE.NOT, SCOPE_BOOLEAN, "not");
	public static final Operator OP_EQUALS = new Operator(TYPE.EQUALS, SCOPE_ALL, "equals");
	public static final Operator OP_CONTAINS = new Operator(TYPE.CONTAINS, SCOPE_STRING, "contains");
	
	public static final LinkedHashMap<TYPE, Operator> OPERATORS;
	static {
		OPERATORS = new LinkedHashMap<Operator.TYPE, Operator>();
		OPERATORS.put(TYPE.OR, OP_OR);
		OPERATORS.put(TYPE.AND, OP_AND);
		OPERATORS.put(TYPE.NOT, OP_NOT);
		OPERATORS.put(TYPE.EQUALS, OP_EQUALS);
		OPERATORS.put(TYPE.CONTAINS, OP_CONTAINS);
	}
	
	private final TYPE type;
	private final int scope;
	private final String name;
	
	public Operator (TYPE type,
		int scope,
		String name)
	{
		this.type = type;
		this.scope = scope;
		this.name = name;
	}
	
	public String getName () {
		return name;
	}
	
	public TYPE getType () {
		return type;
	}
	
	public int getScope () {
		return scope;
	}

}
