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

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.applications.jumbo.Token.TYPE;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
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
					checkParameters(tokens, "O#S#all|fields|locales|tms|columns");
					show(tokens, "all");
					break;
				case USE_REPOSITORY:
					checkParameters(tokens, "O#S#memory|default|$path");
					openRepository(tokens);
					break;
				case CREATE_TM:
					checkParameters(tokens, "M#S#$name", "O#S#$locale");
					createTm(tokens);
					break;
				case OPEN_TM:
					checkParameters(tokens, "M#S#$name");
					openTm(tokens);
					break;
				case CLOSE_TM:
					checkParameters(tokens, "O#S#all|$name");
					closeTm(tokens);
					break;
				case HELP:
					showUsage();
					break;
				case ADD:
					checkParameters(tokens, "M#S#locale|field", "M#S#$any");
					add(tokens);
					break;
				case DELETE:
					checkParameters(tokens, "M#S#tm|locale|field", "M#S#$any");
					delete(tokens);
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
			repo = null;
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
		else if ( what.equals("locales") ) showLocales();
		else if ( what.equals("tms") ) showTMs();
		else if ( what.equals("columns") ) showColumns();
	}
	
	private void showAll () {
		if ( repo == null ) {
			ps.println("No repository open.");
			return;
		}
		ps.println("Repository: "+repo.getName());
		showTMs();
	}
	
	private void openRepository (ArrayList<Token> tokens) {
		closeAll();
		String param = "default";
		if ( tokens.size() > 1 ) param = tokens.get(1).getString();
		
		if ( param.equals("memory") ) {
			repo = new Repository(null, false);
		}
		else if ( param.equals("default") ) {
			param = Util.ensureSeparator(System.getProperty("user.home"), false)
				+ "Olifant" + File.separator + "defaultOlifantTMRepository";
			repo = new Repository(param, false);
		}
		showAll();
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

	private void openTm (ArrayList<Token> tokens) {
		if ( repo == null ) return;
		ITm tmp = repo.openTm(tokens.get(1).getString());
		if ( tmp != null ) {
			tms.add(tmp);
			tm = tmp;
		}
	}

	private void closeTm (ArrayList<Token> tokens) {
		if ( repo == null ) return;
		String tmName = null;
		if ( tokens.size() > 1 ) tmName = tokens.get(1).getString();
		else if ( tm != null ) tmName = tm.getName();
		
		if ( tmName == null ) return; // Nothing to do

		if ( tmName.equals("all") ) {
			tms.clear();
			tm = null;
			return;
		}
		
		for ( ITm tmp : tms ) {
			if ( tmp.getName().equals(tmName) ) {
				tms.remove(tmp);
				if (( tm != null ) && tm.getName().equals(tmName) ) {
					tm = null;
				}
				return;
			}
		}
		ps.println(String.format("There is no open TM named '%s'.", tmName));
	}

	private void showTMs () {
		if ( repo == null ) return;
		List<String> list = repo.getTmNames();
		if ( list.isEmpty() ) ps.println("There are no TMs in the repository");
		else {
			ps.println("TMs in the repository:");
			for ( String tmName : list ) {
				ps.println("- "+tmName);
			}
		}
	}
	
	private void showLocales () {
		if ( tm == null ) return;
		boolean first = true;
		for ( String loc : tm.getLocales() ) {
			if ( first ) first = false;
			else ps.print(", ");
			ps.print("\""+loc+"\"");
		}
		ps.println();
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
	
	private void showColumns () {
		if ( tm == null ) return;
		boolean first = true;
		
		for ( String fn : tm.getRecordFields() ) {
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

	private void add (List<Token> tokens) {
		if ( tm == null ) return;
		String what = tokens.get(1).getString();
		if ( what.equals("locale") ) {
			String locCode = DbUtil.toOlifantLocaleCode(new LocaleId(tokens.get(2).getString()));
			tm.addLocale(locCode);
		}
		else if ( what.equals("field") ) {
			String fn = DbUtil.checkFieldName(tokens.get(2).getString());
			tm.addField(fn);
		}
	}

	private void delete (List<Token> tokens) {
		if ( repo == null ) return;
		String what = tokens.get(1).getString();
		
		if ( what.equals("tm") ) {
			// Get the name
			String tmName = tokens.get(2).getString();
			// Check if it exists
			List<String> list = repo.getTmNames();
			if ( list.indexOf(tmName) == -1 ) {
				ps.println(String.format("There are no TMs '%s' in the repository", tmName));
				return;
			}
			// Check if it's the active one
			boolean isCurrent = (( tm != null ) && tm.getName().equals(tmName));
			// Delete it from the repository
			repo.deleteTm(tmName);
			// reset the current TM if it was the one we just deleted
			if ( isCurrent ) tm = null;
			// We are done
			return;
		}
		
		if ( tm == null ) return;
		if ( what.equals("locale") ) {
			String locCode = DbUtil.toOlifantLocaleCode(new LocaleId(tokens.get(2).getString()));
			tm.deleteLocale(locCode);
		}
		else if ( what.equals("field") ) {
			String fn = DbUtil.checkFieldName(tokens.get(2).getString());
			tm.deleteField(fn);
		}
		
	}
	
	private void showUsage () {
		ps.println(" help|? : Shows this screen");
		ps.println(" quit|exit : Exits jumbo");
		ps.println(" use[ default] : Opens the default local repository");
		ps.println(" use memory : Opens a in-memory repository");
		ps.println(" open <tmName> : Opens a TM");
		ps.println(" create <tmName>[ <srcLocale>] : Creates a TM");
		ps.println(" close[ <tmName>] : Closes an open TM");
		ps.println(" close all] : Closes all open TMs");
		ps.println(" show tms : Lists all TMS in the repository");
		ps.println(" show locales : Lists all locales in the current TM");
		ps.println(" show fields : Lists all fields in the current TM");
		ps.println(" show columns : Lists all fields displayed");
		ps.println(" add locale <locale> : Adds a locale to the current TM");
		ps.println(" add field <name> : Adds a field to the current TM");
		ps.println(" delete tm <tmName>: Delete the given TM");
		ps.println(" delete locale <locale>: Delete the given locale from the current TM");
		ps.println(" delete field <name>: Delete the given field from the current TM");
	}

}
