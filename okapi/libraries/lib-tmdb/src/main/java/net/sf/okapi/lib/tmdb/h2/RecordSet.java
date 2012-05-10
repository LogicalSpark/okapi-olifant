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

package net.sf.okapi.lib.tmdb.h2;

import java.sql.ResultSet;
import java.sql.SQLException;

import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.lib.tmdb.DbUtil;
import net.sf.okapi.lib.tmdb.IRecordSet;

public class RecordSet implements IRecordSet {

	private ResultSet rs;
	
	public RecordSet (ResultSet rs) {
		this.rs = rs;
	}
	
	@Override
	public boolean next () {
		try {
			return rs.next();
		}
		catch ( SQLException e ) {
			throw new OkapiIOException(e.getMessage(), e);
		}
	}

	@Override
	public boolean last () {
		try {
			return rs.last();
		}
		catch ( SQLException e ) {
			throw new OkapiIOException(e.getMessage(), e);
		}
	}

	@Override
	public boolean getBoolean (int index) {
		try {
			return rs.getBoolean(index);
		}
		catch ( SQLException e ) {
			throw new OkapiIOException(e.getMessage(), e);
		}
	}

	@Override
	public String getString (int index) {
		try {
			return rs.getString(index);
		}
		catch ( SQLException e ) {
			throw new OkapiIOException(e.getMessage(), e);
		}
	}

	@Override
	public String getString (String name) {
		try {
			return rs.getString(name);
		}
		catch ( SQLException e ) {
			throw new OkapiIOException(e.getMessage(), e);
		}
	}

	@Override
	public long getLong (int index) {
		try {
			return rs.getLong(index);
		}
		catch ( SQLException e ) {
			throw new OkapiIOException(e.getMessage(), e);
		}
	}

	@Override
	public boolean getFlag () {
		try {
			return rs.getBoolean(DbUtil.FLAG_NAME);
		}
		catch ( SQLException e ) {
			throw new OkapiIOException(e.getMessage(), e);
		}
	}

	@Override
	public long getSegKey () {
		try {
			return rs.getLong(DbUtil.SEGKEY_NAME);
		}
		catch ( SQLException e ) {
			throw new OkapiIOException(e.getMessage(), e);
		}
	}

	@Override
	public long getTuRef () {
		try {
			return rs.getLong(DbUtil.TUREF_NAME);
		}
		catch ( SQLException e ) {
			throw new OkapiIOException(e.getMessage(), e);
		}
	}

	@Override
	public Object getObject (String name) {
		try {
			return rs.getObject(name);
		}
		catch ( SQLException e ) {
			throw new OkapiIOException(e.getMessage(), e);
		}
	}

	@Override
	public int getFieldCount () {
		try {
			return rs.getMetaData().getColumnCount();
		}
		catch ( SQLException e ) {
			throw new OkapiIOException(e.getMessage(), e);
		}
	}

	@Override
	public String getFieldName (int index) {
		try {
			return rs.getMetaData().getColumnName(index);
		}
		catch ( SQLException e ) {
			throw new OkapiIOException(e.getMessage(), e);
		}
	}
	
}