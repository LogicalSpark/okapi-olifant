package okapi.tmserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletContext;

import okapi.shared.Record;
import okapi.shared.RestServices;

import net.sf.okapi.lib.tmdb.IRecordSet;
import net.sf.okapi.lib.tmdb.IRepository;
import net.sf.okapi.lib.tmdb.ITm;
import net.sf.okapi.lib.tmdb.DbUtil.PageMode;

public class RestServicesImpl implements RestServices
{
	@Override
	public String getUUID(ServletContext ctx, String tmName) {
		IRepository repo = (IRepository) ctx.getAttribute("repo");
		ITm tm = repo.openTm(tmName);
		String uuid = tm.getUUID();
		tm = null;
		return uuid;
	}
  
	@Override
	public String getDescription(ServletContext ctx, String tmName) {
		IRepository repo = (IRepository) ctx.getAttribute("repo");
		ITm tm = repo.openTm(tmName);
		String desc = tm.getDescription();
		tm = null;
		return desc;
	}
	
	@Override
	public void rename(ServletContext ctx, String tmName, String newName) {
		IRepository repo = (IRepository) ctx.getAttribute("repo");
		ITm tm = repo.openTm(tmName);
		tm.rename(newName);
		tm = null;	
	}
	
	@Override
	public long addRecord(ServletContext ctx, String tmName, Record record) {
		IRepository repo = (IRepository) ctx.getAttribute("repo");
		ITm tm = repo.openTm(tmName);
		tm.startImport();
		long key = tm.addRecord(-1, record.tuFields, record.segFields);
		tm.finishImport();
		tm = null;
		return key;
	}
	
	@Override
	public HashMap<String, String> getRecord(ServletContext ctx, String tmName, long segKey) {
		//TO IMPLEMENT IN ITM
		HashMap<String, String> mockRecord = new HashMap<String, String>();
		mockRecord.put("segKey", "1");
		mockRecord.put("flag", "true");
		mockRecord.put("tuId", "a");
		mockRecord.put("Text~EN", "Mock Text EN");
		mockRecord.put("Field~EN", "Mock Field EN");
		return mockRecord;
	}
	
	@Override
	public void updateRecord(ServletContext ctx, String tmName, long segKey, Record record) {
		IRepository repo = (IRepository) ctx.getAttribute("repo");
		ITm tm = repo.openTm(tmName);
		tm.startImport();
		tm.updateRecord(segKey, record.tuFields, record.segFields);
		tm.finishImport();
		tm = null;
	}
	
	@Override
	public void deleteRecord(ServletContext ctx, String tmName, long segKey){
		IRepository repo = (IRepository) ctx.getAttribute("repo");
		ITm tm = repo.openTm(tmName);
		List<Long> segKeys = new ArrayList<Long>();
		segKeys.add(segKey);
		tm.deleteSegments(segKeys);
		tm = null;
	}
	
	
	
	
	@Override
	public void addLocale(ServletContext ctx, String tmName, String locale) {
		IRepository repo = (IRepository) ctx.getAttribute("repo");
		ITm tm = repo.openTm(tmName);
		tm.addLocale(locale);
		tm = null;
	}

	@Override
	public void deleteLocale(ServletContext ctx, String tmName,
			String locale) {
		IRepository repo = (IRepository) ctx.getAttribute("repo");
		ITm tm = repo.openTm(tmName);
		tm.deleteLocale(locale);
		tm = null;
	}

	@Override
	public void renameLocale(ServletContext ctx, String tmName,
			String locale, String newLocale) {
		IRepository repo = (IRepository) ctx.getAttribute("repo");
		ITm tm = repo.openTm(tmName);
		tm.renameLocale(locale, newLocale);
		tm = null;
	}

	@Override
	public List<String> getLocales(ServletContext ctx, String tmName) {
		IRepository repo = (IRepository) ctx.getAttribute("repo");
		ITm tm = repo.openTm(tmName);
		List<String> locales = tm.getLocales();
		tm = null;
		return locales; 
	}
	
	
	
	
	@Override
	public void addField(ServletContext ctx, String tmName, String field) {
		IRepository repo = (IRepository) ctx.getAttribute("repo");
		ITm tm = repo.openTm(tmName);
		tm.addField(field);
		tm = null;
	}

	@Override
	public void deleteField(ServletContext ctx, String tmName, String field) {
		IRepository repo = (IRepository) ctx.getAttribute("repo");
		ITm tm = repo.openTm(tmName);
		tm.deleteField(field);
		tm = null;
	}

	@Override
	public void renameField(ServletContext ctx, String tmName, String field,
			String newField) {
		IRepository repo = (IRepository) ctx.getAttribute("repo");
		ITm tm = repo.openTm(tmName);
		tm.renameField(field, newField);
		tm = null;	
	}

	@Override
	public List<String> getFields(ServletContext ctx, String tmName) {
		IRepository repo = (IRepository) ctx.getAttribute("repo");
		ITm tm = repo.openTm(tmName);
		List<String> fields = tm.getAvailableFields();
		tm = null;
		return fields; 
	}


	
	
	@Override
	public List<HashMap<String, String>> getPage(ServletContext ctx,
			String tmName, long page, long size, String mode) {
		IRepository repo = (IRepository) ctx.getAttribute("repo");
		ITm tm = repo.openTm(tmName);
		
		//--SET PAGE SIZE--
		if(size != 0){
			tm.setPageSize(size);
		}
		
		//--SET PAGE MODE--
		if(mode != null && mode.equals("editor")){
			tm.setPageMode(PageMode.EDITOR);
		}else{
			tm.setPageMode(PageMode.ITERATOR);
		}
		
		//--SET FIELDS--
		tm.setRecordFields(tm.getAvailableFields());
		
		IRecordSet rs = tm.getPage(page);		
		return convertRecordSet(rs);
	} 
	
	@Override
	public long getPageCount(ServletContext ctx, String tmName) {
		IRepository repo = (IRepository) ctx.getAttribute("repo");
		ITm tm = repo.openTm(tmName);
		long count = tm.getPageCount();
		tm = null;
		return count;
	}
	
	@Override
	public long getSegmentCount(ServletContext ctx, String tmName) {
		IRepository repo = (IRepository) ctx.getAttribute("repo");
		ITm tm = repo.openTm(tmName);
		long count = tm.getTotalSegmentCount();
		tm = null;
		return count;
	}



	
	
	
	/**
	 * Generate a serializable list from RecordSet
	 * @param rs
	 * @return
	 */
	public ArrayList<HashMap<String,String>> convertRecordSet(IRecordSet rs){
		
		int fieldCount = rs.getFieldCount();
		
		ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();
		HashMap<String, String> rec;
		
		while ( rs.next() ) {
			rec = new HashMap<String, String>();
			for(int i = 1; i <= fieldCount; i++){
				rec.put(rs.getFieldName(i), rs.getString(i));
			}
			list.add(rec);
		}
		return list;
	}




	
}