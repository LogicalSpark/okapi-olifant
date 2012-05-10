/*===========================================================================
  Copyright (C) 2011-2012 by the Okapi Framework contributors
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

import net.sf.okapi.common.observer.IObservable;

public interface IProgressCallback extends IObservable {

	public static final int MSGTYPE_WARNING = 1;
	public static final int MSGTYPE_ERROR = 2;
	public static final int MSGTYPE_INFO = 3;
	
	public void startProcess (String text);
	
	/**
	 * Notifies the end of the process.
	 * @param count number of entries processed, or -1 to get no message.
	 * @param updateRepositories true if the display of list of repositories needs to be updated,
	 * null or false otherwise.
	 */
	public void endProcess (long count,
		Boolean updateRepositories);

	public boolean updateProgress (long count);
	
	public boolean logMessage (int type,
		String text);
	
	public boolean isCanceled ();

}