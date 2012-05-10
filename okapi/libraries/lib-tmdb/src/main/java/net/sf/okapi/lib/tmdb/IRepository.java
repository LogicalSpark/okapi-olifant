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

import java.util.List;

/**
 * provides an implementation-agnostic access to an Olifant repository.
 */
public interface IRepository {

	/**
	 * Gets the name of this repository.
	 * @return the name of this repository.
	 */
	public String getName ();
	
	/**
	 * Closes and free any resources allocated by this repository.
	 * This method can be called even when the repository is not open.
	 */
	public void close ();

	/**
	 * Gets the list of all TMs in this repository.
	 * @return the list of all TMs in this repository.
	 */
	public List<String> getTmNames ();
	
	/**
	 * Creates a new TM.
	 * If a TM with the same name already exists, no new TM is created and the existing TM
	 * is used. 
	 * @param name the name of the new tM to create.
	 * @param description the description for the new TM.
	 * @param localeCode the locale code of the initial language.
	 * @return the ITm object for the newly created TM or the already existing TM,
	 * or null if the TM was not created.
	 */
	public ITm createTm (String tmName,
		String description,
		String localeCode);
	
	/**
	 * Deletes a given TM from this repository.
	 * If there is no TM with such name in the repository, nothing happens.
	 * @param name the name of the TM to remove.
	 */
	public void deleteTm (String tmName);

	/**
	 * Create a new object that gives access to the TM of the given name.
	 * Each call returns a new object!
	 * @param name the name of the TM to access.
	 * @return the ITm object for the given TM name, or null if the name is not the one
	 * of an existing TM.
	 * @throws InvalidParameterException if the TM does not exists.
	 */
	public ITm openTm (String tmName);

	/**
	 * Gets the list of the locales present in a given TM.
	 * @param tmName the name of the TM to look up.
	 * @return the list of the locales present in the given TM.
	 */
	public List<String> getTmLocales (String tmName);

	/**
	 * Gets the total number of segments in a given TM.
	 * @param tmName the name of the TM to look up.
	 * @return the total number of segments in the given TM.
	 */
	public long getTotalSegmentCount (String tmName);

	/**
	 * Indicates if this repository is currently used in shared mode.
	 * That is other users may make modification in the repository such
	 * as add or remove TMs, etc. 
	 * @return true if this repository is shared.
	 */
	public boolean isShared ();
	
	/**
	 * Indicates if this repository is accessed in server mode.
	 * @return true if this repository is accessed in server mode.
	 */
	public boolean isServerMode ();

	/**
	 * Get a unique instance of the object allowing to access the TM index for this repository.
	 * @return an interface to the TM index for this repository.
	 */
	public IIndexAccess getIndexAccess ();
	
}
