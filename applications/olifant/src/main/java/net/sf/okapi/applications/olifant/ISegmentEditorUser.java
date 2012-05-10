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

package net.sf.okapi.applications.olifant;

interface ISegmentEditorUser {

	/**
	 * Informs the caller of the segment editor that the edit is done
	 * and it can reclaim the focus.
	 * @param save true if the edit is to be saved, false to cancel it.
	 * @return true if the caller has done its task successfully, false if an error
	 * occurred. This can be used for example to tell the editor if
	 * the edit is really done or not. 
	 */
	public boolean returnFromEdit (boolean save);
	
	/**
	 * Informs the caller that one of the edit fields has just gain focus.
	 * @param field feild that just gain the focus: 0=source, 1=target, 2=extra.
	 */
	public void notifyOfFocus (int field);

}
