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

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import net.sf.okapi.lib.tmdb.DbUtil;
import net.sf.okapi.lib.tmdb.IIndexAccess;
import net.sf.okapi.lib.tmdb.IRepository;
import net.sf.okapi.lib.tmdb.ITm;

public class Repository implements IRepository {

	public static final String REPO_COLL = "REPO";
	public static final String TM_COLL = "TMLIST";
	
	public static final String TM_COL_NAME = "name";
	public static final String TM_COL_DESC = "desc";
	public static final String TM_COL_UUID = "uuid";
	public static final String TM_COL_TU_FIELDS = "tuFields";	
	public static final String TM_COL_SEG_FIELDS = "segFields";
	public static final String TM_COL_LOCALES = "locales";
	
	public static final String SEG_COL_SEGKEY = "_id";
	public static final String SEG_COL_FLAG = "Flag";
	
	private String name;			//Repository name
	Mongo connection = null;		//Working Connection
	DB repository = null;			//Working Repository
	DBCollection tm_coll= null;		//Working TM Collection
	
	//List<String> cachedTuFields;
	//List<String> cachedSegFields;
	//List<String> cachedLocales;
	//int cachedSegmentCount;
	
	/**
	 * Helper method to delete existing repository
	 * @param repoName
	 */
	static public void delete (String repoName) {
		try {
			Mongo mDel = new Mongo();
			mDel.dropDatabase(repoName);
			mDel.close();
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		} catch (MongoException e) {
			throw new RuntimeException(e);
		}
	}
	
	public Repository (String connStr){
		
		String host = "localhost";
		
		//--parse the url--
		String[] params = connStr.split("/");
		if(params.length == 1){
			name = params[0];
		}else if (params.length > 1){
			host = params[0];
			name = params[1];
		}

		//--verify--
		if(host == null || host.trim().length() == 0){
			return;
		} 
		if(name == null || name.trim().length() == 0){
			return;
		}
		
		try {
			connection = new Mongo(host);
			repository = connection.getDB(name);
			
			DBCollection repo = repository.getCollection(Repository.REPO_COLL);
			
			BasicDBObject query = new BasicDBObject();
	        query.put("name", name);

	        DBObject dbObj = repo.findOne(query);
			if(dbObj == null){
				BasicDBObject doc = new BasicDBObject();
			    doc.put("name", name);
			    doc.put("description", "Default Description");
		        repo.insert(doc);
		        //TODO: Unless description is not used this "REPO" table is not needed
			}
			
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		} catch (MongoException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void close () {
		if ( connection != null ) {
			connection.close();
			connection = null;
		}
	}

	@Override
	public ITm createTm (String tmName,
		String description,
		String localeId)
	{
		tm_coll = repository.getCollection(Repository.TM_COLL);

		BasicDBObject query = new BasicDBObject();
        query.put("name", tmName);

        DBObject dbObj = tm_coll.findOne(query);
		if ( dbObj == null ) {
			BasicDBObject doc = new BasicDBObject();
		    doc.put(Repository.TM_COL_NAME, tmName);
		    doc.put(Repository.TM_COL_UUID, UUID.randomUUID().toString());
		    doc.put(Repository.TM_COL_DESC, description);
		    doc.put(Repository.TM_COL_LOCALES, localeId);
		    doc.put(Repository.TM_COL_TU_FIELDS, "");
		    doc.put(Repository.TM_COL_SEG_FIELDS, DbUtil.TEXT_PREFIX+localeId+","+DbUtil.CODES_PREFIX+localeId);		    
		    tm_coll.insert(doc);
		    dbObj = tm_coll.findOne(query);
		}
		
		// Get the UUID
		String uuid = (String)dbObj.get(Repository.TM_COL_UUID);
		// Create the TM
		return new Tm(this, uuid, tmName);
	}

	@Override
	public void deleteTm (String tmName) {
		tm_coll = repository.getCollection(Repository.TM_COLL);
		
		BasicDBObject obj = new BasicDBObject();
        obj.put(Repository.TM_COL_NAME, tmName);
        
		tm_coll.remove(obj);
		
		DBCollection seg_col = repository.getCollection(tmName+"_SEG");
		seg_col.drop();		
	}

	@Override
	public String getName () {
		return name;
	}

	@Override
	public List<String> getTmLocales (String tmName) {
		return getCommaSeparatedValues(tmName, Repository.TM_COL_LOCALES);
	}

	@Override
	public List<String> getTmNames () {
		tm_coll = repository.getCollection(Repository.TM_COLL);
		
		List<String> list = new ArrayList<String>();

		DBCursor cur = tm_coll.find();
		
        while(cur.hasNext()) {
            DBObject obj = cur.next();
            list.add((String) obj.get(Repository.TM_COL_NAME));
        }
        cur.close();
   
        return list;
	}

	@Override
	public long getTotalSegmentCount (String tmName) {
		return repository.getCollection(tmName+"_SEG").count();
	}

	@Override
	public ITm openTm (String tmName) {
		tm_coll = repository.getCollection(Repository.TM_COLL);
		
		BasicDBObject query = new BasicDBObject();
        query.put(Repository.TM_COL_NAME, tmName);

        DBObject dbObj = tm_coll.findOne(query);
		if ( dbObj != null ) {
			// Get the UUID
			Object obj = dbObj.get(Repository.TM_COL_UUID);
			String uuid;
			if ( obj == null ) {
				// For now: if it wasn't there: create it to allow backward compatibility
				uuid = UUID.randomUUID().toString();
		        BasicDBObject set = new BasicDBObject("$set",
		        	new BasicDBObject(Repository.TM_COL_UUID, uuid));
		        tm_coll.update(dbObj, set);
			}
			else uuid = (String)obj;
			return new Tm(this, uuid, tmName);	
		}
		else {
			throw new RuntimeException(String.format("TM '%s' does not exists.", tmName));
		}
	}

	/**
	 * Return the Mongo DB representing the current Repository. Not to be confused with an instance of this class.
	 */
	public DB getDb(){
		return repository;
	}
	
	/**
	 * Get the TM description field. TODO: Is this method really necessary?
	 * @param tmName
	 * @return
	 */
	public String getTmDescription (String tmName){
		tm_coll = repository.getCollection(Repository.TM_COLL);

		BasicDBObject query = new BasicDBObject();
        query.put(Repository.TM_COL_NAME, tmName);

        DBObject obj = tm_coll.findOne(query);
        
        if(obj != null){
        	return (String) obj.get(Repository.TM_COL_DESC);
        }else{
        	throw new RuntimeException(String.format("TM '%s' does not exists.", tmName));        	
        }
	}

	List<String> getAvailableFields (String tmName) {
		tm_coll = repository.getCollection(Repository.TM_COLL);
		
		List<String> list = new ArrayList<String>();
		
		BasicDBObject query = new BasicDBObject();
        query.put(Repository.TM_COL_NAME, tmName);

        DBObject obj = tm_coll.findOne(query);

        if(obj == null){
        	throw new RuntimeException(String.format("TM '%s' does not exists.", tmName));
        }
        
        String tuFields = (String) obj.get(Repository.TM_COL_TU_FIELDS);
        String segFields = (String) obj.get(Repository.TM_COL_SEG_FIELDS);
        
        String[] items = tuFields.split(",");
        for (String item : items){
        	if(!item.trim().equals(""))
        		list.add(item);
        }
        
        list.add("TUREF");
        
        items = segFields.split(",");
        for (String item : items){
        	if(!item.trim().equals(""))
        		list.add(item);
        }
        return list;
	}

	/**
	 * Retrieves the list of TU level fields
	 * @param tmName
	 * @return
	 */
	List<String> getTuFields (String tmName) {
		return getCommaSeparatedValues(tmName, Repository.TM_COL_TU_FIELDS);
	}
	
	/**
	 * Retrieves the list of TU level fields
	 * @param tmName
	 * @return
	 */
	List<String> getSegFields (String tmName) {
		return getCommaSeparatedValues(tmName, Repository.TM_COL_SEG_FIELDS);
	}
	
	/**
	 * Retrieves commma separated values as a list
	 * @param tmName
	 * @param columnName
	 * @return
	 */
	List<String> getCommaSeparatedValues (String tmName, String columnName) {
		tm_coll = repository.getCollection(Repository.TM_COLL);
		
		List<String> list = new ArrayList<String>();
		
		BasicDBObject query = new BasicDBObject();
        query.put(Repository.TM_COL_NAME, tmName);

        DBObject obj = tm_coll.findOne(query);

        if(obj == null){
        	throw new RuntimeException(String.format("TM '%s' does not exists.", tmName));
        }
        
        String fields = (String) obj.get(columnName);
        
        String[] items = fields.split(",");
        for (String item : items){
        	if(!item.trim().equals(""))
        		list.add(item);
        }        
        return list;        
	}
	
	/**
	 * Renames TM
	 * @param currentName
	 * @param newName
	 */
	void renameTm (String currentName, String newName){
		tm_coll = repository.getCollection(Repository.TM_COLL);
		
		BasicDBObject query = new BasicDBObject();
        query.put(Repository.TM_COL_NAME, newName);

        DBObject obj = tm_coll.findOne(query);
        
        if(obj != null){
        	throw new RuntimeException(String.format("TM '%s' already exists. Cannot rename.", newName));
        }
        
		query = new BasicDBObject();
        query.put(Repository.TM_COL_NAME, currentName);

        obj = tm_coll.findOne(query);
        
        if(obj == null){
        	throw new RuntimeException(String.format("TM '%s' does not exists.", currentName));
        }
        
        BasicDBObject set = new BasicDBObject("$set", new BasicDBObject("name", newName));
        tm_coll.update(query, set);
		
		DBCollection segList = repository.getCollection(currentName+"_SEG");
		segList.rename(newName+"_SEG");
	}

	@Override
	public boolean isShared () {
		return true; // Always in shared mode
	}

	@Override
	public IIndexAccess getIndexAccess () {
		//TODO: TM index access
		return null;
	}

	@Override
	public boolean isServerMode () {
		// MongoDB is always access in server mode
		return true;
	}
	
}
