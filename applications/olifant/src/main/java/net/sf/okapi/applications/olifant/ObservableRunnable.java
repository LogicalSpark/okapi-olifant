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

import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.observer.IObservable;
import net.sf.okapi.common.observer.IObserver;

public abstract class ObservableRunnable implements IObservable, Runnable {

	ArrayList<IObserver> observers;
	
	public ObservableRunnable () {
		observers = new ArrayList<IObserver>();
	}
	
	@Override
	public void addObserver (IObserver observer) {
		observers.add(observer);
	}

	@Override
	public int countObservers () {
		return observers.size();
	}

	@Override
	public void deleteObserver (IObserver observer) {
		observers.remove(observer);
	}

	@Override
	public void deleteObservers () {
		observers.clear();
	}

	@Override
	public List<IObserver> getObservers () {
		return observers;
	}

	@Override
	public void notifyObservers () {
		notifyObservers(null);
	}

	@Override
	public void notifyObservers (Object arg) {
		for ( IObserver observer : observers ) {
			observer.update(this, arg);
		}
	}
	
}
