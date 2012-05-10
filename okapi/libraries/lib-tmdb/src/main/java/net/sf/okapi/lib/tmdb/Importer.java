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

import java.util.LinkedHashMap;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.lib.tmdb.DbUtil;
import net.sf.okapi.lib.tmdb.IProgressCallback;
import net.sf.okapi.lib.tmdb.ITm;

public class Importer implements Runnable {

	private final IProgressCallback callback;
	private final ITm tm;
	private final RawDocument rd;
	private final IFilterConfigurationMapper fcMapper;
	private final DbUtil dbUtil = new DbUtil();
	
	public Importer (IProgressCallback progressCallback,
		ITm tm,
		RawDocument rd,
		IFilterConfigurationMapper fcMapper)
	{
		this.callback = progressCallback;
		this.tm = tm;
		this.rd = rd;
		this.fcMapper = fcMapper;
	}
	
	@Override
	public void run () {
		long count = 0;
		IFilter filter = null;
		boolean canceled = false;
		boolean flag;
		try {
			callback.startProcess("Importing "+rd.getInputURI().getPath()+"...");
			filter = fcMapper.createFilter(rd.getFilterConfigId());
			filter.open(rd);
	
			LinkedHashMap<String, Object> mapTUProp = new LinkedHashMap<String, Object>();
			LinkedHashMap<String, Object> mapSrcProp = new LinkedHashMap<String, Object>();
			LinkedHashMap<String, Object> mapTrgProp = new LinkedHashMap<String, Object>();
			LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
			String[] trgFields;
			String srcDbLang = DbUtil.toOlifantLocaleCode(rd.getSourceLocale());

//TODO: implement check for duplicates
//TODO: implement filter for fields
			tm.startImport();
			while ( filter.hasNext() && !canceled ) {
				Event event = filter.next();
				if ( !event.isTextUnit() ) continue;
				
				ITextUnit tu = event.getTextUnit();
				ISegments srcSegs = tu.getSourceSegments();
				
				// Get text-unit level properties
				mapTUProp.clear();
				flag = false;
				for ( String name : tu.getPropertyNames() ) {
					if ( name.equals(DbUtil.PROP_FLAG) ) {
						flag = "1".equals(tu.getProperty(name).getValue());
					}
					else {
						if ( DbUtil.isPreDefinedField(name) ) {
							name = name + "_Bis";
						}
						mapTUProp.put(DbUtil.checkFieldName(name), tu.getProperty(name).getValue());
					}
				}
				
				// Get source container properties
				mapSrcProp.clear();
				for ( String name : tu.getSourcePropertyNames() ) {
					if ( name.equals("lang") ) continue;
					mapSrcProp.put(DbUtil.checkFieldName(name)+DbUtil.LOC_SEP+srcDbLang, tu.getSourceProperty(name).getValue());
				}
	
				// For each source segment
				for ( net.sf.okapi.common.resource.Segment srcSeg : srcSegs ) {
	
					// Get the source fields
					String[] srcFields = dbUtil.fragmentToTmFields(srcSeg.getContent());
					map.clear();
					map.put(DbUtil.TEXT_PREFIX+srcDbLang, srcFields[0]);
					map.put(DbUtil.CODES_PREFIX+srcDbLang, srcFields[1]);
					long tuKey = -1;
					if ( flag ) {
						map.put(DbUtil.FLAG_NAME, (Boolean)flag);
					}
	
					// For each target
					for ( LocaleId locId : tu.getTargetLocales() ) {
						String trgDbLang = DbUtil.toOlifantLocaleCode(locId);
						
						mapTrgProp.clear();
						for ( String name : tu.getTargetPropertyNames(locId) ) {
							if ( name.equals("lang") ) continue;
							mapTrgProp.put(DbUtil.checkFieldName(name)+DbUtil.LOC_SEP+trgDbLang, tu.getTargetProperty(locId, name).getValue());
						}
						
						// Get the target segment
						net.sf.okapi.common.resource.Segment trgSeg = tu.getTargetSegments(locId).get(srcSeg.getId());
						if ( trgSeg != null ) {
							trgFields = dbUtil.fragmentToTmFields(trgSeg.getContent());
						}
						else {
							trgFields = new String[2];
						}
						map.put(DbUtil.TEXT_PREFIX+trgDbLang, trgFields[0]);
						map.put(DbUtil.CODES_PREFIX+trgDbLang, trgFields[1]);
					}
					// Add the record to the database
					map.putAll(mapSrcProp);
					map.putAll(mapTrgProp);
					tuKey = tm.addRecord(tuKey, mapTUProp, map);
					// Update UI from time to time
					if ( (++count % 452) == 0 ) {
						// And check for cancellation
						if ( !callback.updateProgress(count) ) {
							if ( !canceled ) {
								callback.logMessage(1, "Process interrupted by user.");
								canceled = true;
							}
						}
					}
				}
			}
		}
		catch ( Throwable e ) {
			callback.logMessage(IProgressCallback.MSGTYPE_ERROR, e.getMessage());
		}
		finally {
			// Final update (includes notifying the observers that we are done)
			tm.finishImport();
			if ( filter != null ) {
				filter.close();
			}
			callback.endProcess(count, false);
		}
	}

}
