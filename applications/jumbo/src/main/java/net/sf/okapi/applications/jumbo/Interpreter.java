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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.okapi.applications.jumbo.Token.TYPE;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.lib.tmdb.DbUtil;
import net.sf.okapi.lib.tmdb.IRepository;
import net.sf.okapi.lib.tmdb.ITm;
import net.sf.okapi.lib.tmdb.h2.Repository;

public class Interpreter {
	
	private PrintStream ps;
	private IRepository repo;
	private ArrayList<ITm> tms = new ArrayList<ITm>();
	private ITm tm;
	
	public Interpreter (PrintStream ps) {
		this.ps = ps;
	}
	
	public boolean execute (String cmd) {
		try {
			ArrayList<Token> tokens = tokenize(cmd);
			Token token = tokens.get(0);
			if ( token.getType() == TYPE.OPERATOR ) {
				switch ( token.getOperator() ) {
				case QUIT:
					checkParameters(tokens);
					closeAll();
					return false; // Exit
				case SHOW:
					checkParameters(tokens, "O#S#all|fields");
					show(tokens, "all");
					break;
				case USE_REPOSITORY:
					checkParameters(tokens, "O#S#memory|default|$path");
					openRepository();
					break;
				case CREATE_TM:
					checkParameters(tokens, "M#S#$name", "O#S#$locale");
					createTm(tokens);
					break;
				}
			}
		}
		catch ( Throwable e ) {
			ps.println("Error when interpreting the command.\n"+e.getMessage());
		}
		return true;
	}

	private ArrayList<Token> tokenize (String cmd) {
		cmd = cmd.trim();
		String[] parts = cmd.split("[\\s;]", -1);
		ArrayList<Token> tokens = new ArrayList<Token>();

		for ( String part : parts ) {
			Token token = Token.getOperatorToken(part);
			if ( token != null ) {
				tokens.add(token);
			}
			else { // String
				tokens.add(new Token(part));
			}
		}
		
		return tokens;
	}
	
	void closeAll () {
		if ( repo != null ) {
			repo.close();
		}
		tms = new ArrayList<ITm>();
	}

	String getCurrentTmName () {
		if ( repo == null ) return "<No active repository>";
		if ( tm != null ) return tm.getName();
		return "<No active TM>";
	}
	
	private void show (ArrayList<Token> tokens,
		String defValue)
	{
		String what = defValue;
		if ( tokens.size() > 1 ) what = tokens.get(1).getString();
		
		if ( what.equals("all") ) showAll();
		else if ( what.equals("fields") ) showAvailableFields();
	}
	
	private void showAll () {
		if ( repo == null ) {
			ps.println("No repository open.");
			return;
		}
		ps.println("Repository: "+repo.getName());
		
		List<String> list = repo.getTmNames();
		if ( list.isEmpty() ) ps.println("There are no TMs in the repository");
		else {
			ps.println("TMs in the repository:");
			for ( String tmName : list ) {
				ps.println(tmName);
			}
		}
		
	}
	
	private void openRepository () {
		closeAll();
		repo = new Repository(null, false);
	}
	
	private void createTm (ArrayList<Token> tokens) {
		if ( repo == null ) return;
		String srcLoc = "en";
		if ( tokens.size() == 3 ) srcLoc = tokens.get(2).getString();
		String srcCode = DbUtil.toOlifantLocaleCode(LocaleId.fromString(srcLoc));
		ITm tmp = repo.createTm(tokens.get(1).getString(), null, srcCode);
		if ( tmp != null ) {
			tms.add(tmp);
			tm = tmp;
		}
	}

	private void showAvailableFields () {
		if ( tm == null ) return;
		boolean first = true;
		for ( String fn : tm.getAvailableFields() ) {
			if ( first ) first = false;
			else ps.print(", ");
			ps.print("\""+fn+"\"");
		}
		ps.println();
	}
	
	private void checkParameters (ArrayList<Token> tokens,
		String... expected)
	{
		int t=1;
		for ( String arg : expected ) {
			String[] parts = arg.split("#");
			Token token = null;
			
			// Check if mandatory or optional
			if ( parts[0].equals("M") ) { // Mandatory
				if ( tokens.size() <= t ) {
					throw new RuntimeException("Missing a string parameter.");
				}
				token = tokens.get(t);
			}
			else { // Optional
				if ( tokens.size() > t ) {
					token = tokens.get(t);
				}
			}
			
			if ( token != null ) {
				if ( parts[1].equals("S") ) { // String type
					if ( token.getType() != TYPE.STRING ) {
						throw new RuntimeException("Parameter must be a string.");
					}
					// Check value of the string
					String[] expValues = parts[2].split("\\|", 0);
					String paramValue = token.getString();
					for ( String expValue : expValues ) {
						if ( expValue.startsWith("$") ) {
							if ( expValue.equals("$any") ) return; // Any value is OK
							if ( expValue.equals("$path") ) return; // TODO
							if ( expValue.equals("$locale") ) return; // TODO
							if ( expValue.equals("$name") ) return; // TODO
						}
						else if ( paramValue.equals(expValue) ) return; // OK
					}
					throw new RuntimeException(String.format("Invalid parameter value '%s'.", paramValue));
				}
			}
			
			
			// Next
			t++;
		}
		
		if ( tokens.size() > expected.length+1 ) {
			throw new RuntimeException("Too many parameters.");
		}
	}
	
}