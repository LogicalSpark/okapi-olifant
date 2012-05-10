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

package net.sf.okapi.lib.tmdb.mongodb;

import java.util.List;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.lib.tmdb.DbUtil;
import net.sf.okapi.lib.tmdb.IRecordSet;

public class RecordSet implements IRecordSet {

	private DBCursor dbCursor;
	private DBObject dbObject;
	private List<String> fields;
	private int limit;

	public RecordSet (DBCursor dbCursor,
		List<String> fields,
		int limit)
	{
		this.dbCursor = dbCursor;
		this.fields = fields;
		this.limit = limit;
	}
	
	@Override
	public boolean next () {
		if ( dbCursor.hasNext() ) {
			dbObject = dbCursor.next();
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public boolean last () {
		int skip = limit-dbCursor.numSeen(); 
		for ( int i=0 ; i<skip; i++ ) {
			// This if statement is needed in cases of the last pages 
			// where the actual items is smaller than the limit
			if ( dbCursor.hasNext() ) {
				dbObject = dbCursor.next();				
			}
		}
		return true;
	}

	@Override
	public boolean getBoolean (int index) {
		String columnName = getFieldName(index-1);
		Object dbObj = dbObject.get(columnName);
		if ( dbObj != null ) {
			return (Boolean)dbObj;
		}
		else {
			throw new OkapiIOException(String.format("Can't read column '%d'.", index));
		}
	}

	@Override
	public String getString (int index) {
		Object dbObj = dbObject.get(getFieldName(index-1));
		if ( dbObj instanceof String ) {
			return (String)dbObj;
		}
		else if ( dbObj instanceof Integer ) {
			return ((Integer)dbObj).toString();
		}
		else if ( dbObj instanceof Boolean ) {
			return ((Boolean)dbObj).toString();
		}
		else{
			return null;
		}
	}

	@Override
	public long getLong (int index) {
		Object obj = dbObject.get(getFieldName(index-1));
		if ( obj != null ) {
			return (Integer)obj;
		}
		else { 
			throw new OkapiIOException(String.format("Can't read column '%d'.", index));
		}
	}

	@Override
	public boolean getFlag () {
		Object dbObj = dbObject.get(DbUtil.FLAG_NAME);
		if ( dbObj != null ) {
			return (Boolean)dbObj;
		}
		else {
			throw new OkapiIOException("Can't read flag.");
		}
	}

	@Override
	public long getSegKey () {
		Object dbObj = dbObject.get(DbUtil.SEGKEY_NAME);
		if ( dbObj != null ) {
			return (Long)dbObj;
		}
		else {
			throw new OkapiIOException("Can't read flag.");
		}
	}

	@Override
	public long getTuRef () {
		Object dbObj = dbObject.get(DbUtil.TUREF_NAME);
		if ( dbObj != null ) {
			return (Long)dbObj;
		}
		else {
			throw new OkapiIOException("Can't read flag.");
		}
	}

	@Override
	public String getString (String name) {
		Object dbObj = dbObject.get(name);
		if ( dbObj instanceof String ) {
			return (String)dbObj;
		}
		else if ( dbObj instanceof Integer ) {
			return ((Integer)dbObj).toString();
		}
		else if ( dbObj instanceof Boolean ) {
			return ((Boolean)dbObj).toString();
		}
		else{
			return null;
		}
	}

	@Override
	public Object getObject (String name) {
		return dbObject.get(name);
	}

	@Override
	public int getFieldCount () {
		return fields.size();
	}

	@Override
	public String getFieldName (int index) {
		return fields.get(index);
	}
	
}