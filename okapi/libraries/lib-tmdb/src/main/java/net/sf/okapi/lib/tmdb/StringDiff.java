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

class StringDiff  {

	private static final int m_nNorth = 0;
	private static final int m_nNorthWest = 1;
	private static final int m_nWest = 2;
	
	private int[][] m_Len;
	private int[][] m_Prv;
	private String m_sOld;
	private String m_sNew;
	
	public String calculate (String p_sOld,
		String p_sNew)
	{
		fillMatrix(p_sOld, p_sNew);
		return printOutput();
	}

	private void fillMatrix (String p_sOld,
		String p_sNew)
	{
		int nNorthLen;
		int nWestLen;
		int i;
		int j;

		m_sOld = p_sOld;
		m_sNew = p_sNew;
		m_Len = new int[m_sOld.length()+1][m_sNew.length()+1];
		m_Prv = new int[m_sOld.length()+1][m_sNew.length()+1];

		for ( i=0; i<m_sOld.length()+1; i++ ) {
			m_Len[i][0] = 0;
			m_Prv[i][0] = m_nNorth;
		}

		for ( j=0; j<m_sNew.length()+1; j++ ) {
			m_Len[0][j] = 0;
			m_Prv[0][j] = m_nWest;
		}

		for ( i=1; i<m_sOld.length()+1; i++ ) {
			for ( j=1; j<m_sNew.length()+1; j++ ) {
				if ( p_sOld.charAt(i-1) == p_sNew.charAt(j-1) ) {
					m_Prv[i][j] = m_nNorthWest;
					m_Len[i][j] = m_Len[i-1][j-1]+1;
				}
				else {
					nNorthLen = m_Len[i-1][j];
					nWestLen = m_Len[i][j-1];
					if ( nNorthLen > nWestLen ) {
						m_Prv[i][j] = m_nNorth;
						m_Len[i][j] = nNorthLen;
					}
					else {
						m_Prv[i][j] = m_nWest;
						m_Len[i][j] = nWestLen;
					}
				}
			}
		}
	}

	private String printOutput () {
		StringBuilder sbTmp = new StringBuilder(255);
		StringBuilder sbState = new StringBuilder(255);
		StringBuilder sbOut = new StringBuilder(255);
		char chCurrent = '=';
		int i;

		/* \x1=<ins> \x2=</ins> \x3=<del> \x4=</del> */
		getChar(sbTmp, sbState, m_sOld.length(), m_sNew.length());

		for ( i=0; i<sbState.length(); i++ ) {
			if ( sbState.charAt(i) != chCurrent ) {
				switch ( chCurrent ) {
					case '=':
						if ( sbState.charAt(i) == '-' ) sbOut.append('\u0003');
						else  sbOut.append('\u0001');
						break;
					case '-':
						if ( sbState.charAt(i) == '=' ) sbOut.append('\u0004');
						else sbOut.append("\u0004\u0001");
						break;
					case '+':
						if ( sbState.charAt(i) == '=' ) sbOut.append('\u0002');
						else sbOut.append("\u0002\u0003");
						break;
				}
				chCurrent = sbState.charAt(i);
			}
			sbOut.append(sbTmp.charAt(i));
		}
		if ( chCurrent != '=' ) {
			if ( sbState.charAt(i-1) == '-' ) sbOut.append('\u0004');
			else sbOut.append('\u0002');
		}
		return sbOut.toString();
	}

	private void getChar (StringBuilder p_sbOut, 
		StringBuilder p_sbState,
		int p_nOld,
		int p_nNew)
	{
		if (( p_nOld == 0 ) && ( p_nNew == 0 )) return;
     
		if ( p_nOld == 0 ) {
			getChar(p_sbOut, p_sbState, p_nOld, p_nNew-1);
			p_sbState.append('+');
			p_sbOut.append(m_sNew.charAt(p_nNew-1));
		}
		else if ( p_nNew == 0 ) {
			getChar(p_sbOut, p_sbState, p_nOld-1, p_nNew);
			p_sbState.append('-');
			p_sbOut.append(m_sOld.charAt(p_nOld-1));
		}
		else {
			switch ( m_Prv[p_nOld][p_nNew] ) {
				case m_nNorth:
					getChar(p_sbOut, p_sbState, p_nOld-1, p_nNew);
					p_sbState.append('-');
					p_sbOut.append(m_sOld.charAt(p_nOld-1));
					break;
				case m_nNorthWest:
					getChar(p_sbOut, p_sbState, p_nOld-1, p_nNew-1);
					p_sbState.append('=');
					p_sbOut.append(m_sOld.charAt(p_nOld-1));
					break;
				case m_nWest:
					getChar(p_sbOut, p_sbState, p_nOld, p_nNew-1);
					p_sbState.append('+');
					p_sbOut.append(m_sNew.charAt(p_nNew-1));
					break;
			}
		}

	}

	public String convert (String codedString) {
		String tmp = codedString.replace("\u0001", "<ins>");
		tmp = tmp.replace("\u0002", "</ins>");
		tmp = tmp.replace("\u0003", "<del>");
		return tmp.replace("\u0004", "</del>");
	}
}
