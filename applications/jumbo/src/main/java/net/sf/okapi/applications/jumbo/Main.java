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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.util.ArrayList;

public class Main {

	private static PrintStream ps;
	private Context ctx;
	
	static public void main (String[] args) {
		try {
			(new Main()).run(args);
		}
		catch ( Throwable e ) {
			e.printStackTrace();
		}
	}

	/**
	 * Try the guess the encoding of the console.
	 * @return the guessed name of the console's encoding.
	 */
	private static String getConsoleEncodingName () {
		String osName = System.getProperty("os.name");
		if ( osName.startsWith("Mac OS")) {
			return "UTF-8"; // Apparently the default for bash on Mac
		}
		if ( osName.startsWith("Windows") ) {
			//TODO: Get DOS code-pages per locale
			return "cp850"; // Not perfect, but covers many languages
		}
		// Default: Assumes unique encoding overall 
		return Charset.defaultCharset().name();
	}

	public void run (String[] args) {
		BufferedReader br = null;
		ps = null;
		try {
			ctx = new Context();
			br = new BufferedReader(new InputStreamReader(System.in));
			// Create an encoding-aware output for the console
			// System.out uses the default system encoding that
			// may not be the right one (e.g. windows-1252 vs cp850)
			ps = new PrintStream(System.out, true, getConsoleEncodingName());
			
			showBanner();
			
			// Process the command-line parameters
			if ( !processParameters(args) ) return;
			
			// Main loop
			while ( true ) {
				showPrompt();
				String cmd = br.readLine(); // in.nextLine();
				execute(cmd);
			}
		}
		catch ( Throwable e ) {
			e.printStackTrace();
		}
		finally {
			if ( ps != null ) ps.close();
			if ( br != null ) {
				try {
					br.close();
				} catch (IOException e) {
					// Ignore this one
				}
			}
		}
	}

	private void showPrompt () {
		ps.print("J>");
	}
	
	private void showContext () {
		ps.println(String.format("Source: %s", ctx.srcLoc.toString()));
	}

	private void showBanner () {
		ps.println("-------------------------------------------------------------------------------"); //$NON-NLS-1$
		ps.println("Okapi Jumbo - Olifant Console");
		// The version will show as 'null' until the code is build as a JAR.
		ps.println(String.format("Version: %s", getClass().getPackage().getImplementationVersion()));
		ps.println("-------------------------------------------------------------------------------"); //$NON-NLS-1$
	}

	private void showInfo () {
		Runtime rt = Runtime.getRuntime();
		rt.runFinalization();
		rt.gc();
		ps.println("Java version: " + System.getProperty("java.version")); //$NON-NLS-1$
		ps.println(String.format("Platform: %s, %s, %s",
			System.getProperty("os.name"), //$NON-NLS-1$ 
			System.getProperty("os.arch"), //$NON-NLS-1$
			System.getProperty("os.version"))); //$NON-NLS-1$
		NumberFormat nf = NumberFormat.getInstance();
		ps.println(String.format("Java VM memory: free=%s KB, total=%s KB", //$NON-NLS-1$
			nf.format(rt.freeMemory()/1024),
			nf.format(rt.totalMemory()/1024)));
	}

	private boolean processParameters (String[] originalArgs) {
		
		// Remove all empty arguments
		// This is to work around the "$1" issue in bash
		ArrayList<String> args = new ArrayList<String>();
		for ( String tmp : originalArgs ) {
			if ( tmp.length() > 0 ) args.add(tmp);
		}
		
		for ( int i=0; i<args.size(); i++ ) {
			String arg = args.get(i);
			if ( arg.equals("-info") ) {
				showInfo();
			}
		}
		
		return true;
	}
	
	private void execute (String cmd) {
		cmd = cmd.trim();
		
		if ( cmd.equals("exit") ) {
			ps.println("Session closed");
			System.exit(0);
		}
		else if ( cmd.equals("info") ) {
			showInfo();
		}
		else if ( cmd.equals("ctx") || cmd.equals("context") ) {
			showContext();
		}
		else {
			ps.println("Syntax error or unknown command: \""+cmd+"\"");
		}
	}

}
