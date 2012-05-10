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

package net.sf.okapi.lib.tmdb.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class MongoHelper {
	
	/**
	 * Find a specific coll entry
	 * @return
	 */
	static DBObject findCollEntry(DB db, String collName, String key, Object value){
		DBCollection coll = db.getCollection(collName);
		return findCollEntry(coll, key, value);
	}
	
	/**
	 * Find a specific coll entry
	 * @return
	 */
	static DBObject findCollEntry(DBCollection coll, String key, Object value){
		BasicDBObject query = new BasicDBObject();
	    query.put(key, value);
	    return coll.findOne(query);
	}
	
	/**
	 * Find a specific coll entry value
	 * @return
	 */
	static String findCollEntryValue(DB db, String collName, String key, Object value, String field){
		DBCollection coll = db.getCollection(collName);
		return findCollEntryValue(coll, key, value, field);
	}
	
	/**
	 * Find a specific coll entry value
	 * @return
	 */
	static String findCollEntryValue(DBCollection coll, String key, Object value, String field){
		BasicDBObject query = new BasicDBObject();
	    query.put(key, value);
	    
	    DBObject obj = coll.findOne(query); 
	    
	    if(obj != null){
	    	return (String)obj.get(field);
	    }else{
	    	return null;
	    }
	}	
}
