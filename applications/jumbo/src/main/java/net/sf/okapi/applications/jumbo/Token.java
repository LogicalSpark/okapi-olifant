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

package net.sf.okapi.applications.jumbo;

import java.util.HashMap;

public class Token {
	
	static public enum TYPE {
		STRING,
		VALUE,
		OPERATOR
	}
	
	static enum OPERATOR {
		SHOW_CONTEXT,
		USE_REPOSITORY,
		CREATE_TM,
		SHOW,
		QUIT
	}
	
	static HashMap<String, OPERATOR> LOOKUP = new HashMap<String, OPERATOR>();
	static {
		LOOKUP.put("quit", OPERATOR.QUIT);
		LOOKUP.put("exit", OPERATOR.QUIT);
		LOOKUP.put("context", OPERATOR.SHOW_CONTEXT);
		LOOKUP.put("ctx", OPERATOR.SHOW_CONTEXT);
		LOOKUP.put("use", OPERATOR.USE_REPOSITORY);
		LOOKUP.put("create", OPERATOR.CREATE_TM);
		LOOKUP.put("show", OPERATOR.SHOW);
		LOOKUP.put("sh", OPERATOR.SHOW);
	}
	
	private TYPE type;
	private String string;
	private OPERATOR operator;

	static Token getOperatorToken (String text) {
		if (( text == null ) || ( text.isEmpty() )) return null;
		OPERATOR op = LOOKUP.get(text.toLowerCase());
		if ( op != null ) {
			return new Token(op);
		}
		return null;
	}
	
	public  Token (OPERATOR operator) {
		this.type = TYPE.OPERATOR;
		this.operator = operator;
	}

	public  Token (String string) {
		this.type = TYPE.STRING;
		this.string = string;
	}

	public TYPE getType () {
		return type;
	}
	
	public OPERATOR getOperator () {
		return operator;
	}
	
	public String getString () {
		return string;
	}

}
