package net.sf.okapi.lib.tmdb.mongodb;

import static org.junit.Assert.assertEquals;

import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.lib.tmdb.DbUtil;
import net.sf.okapi.lib.tmdb.IRepository;
import net.sf.okapi.lib.tmdb.ITm;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class RepositoryTest {
	
	String testRepo = "testTMRepository";
	
	Mongo mongo = null;
	DB db = null;
	DBCollection tmListColl;
	IRepository repo;
	
	boolean skipTests = true;
	
	@Before
	public void setUp() throws UnknownHostException, MongoException {
		
		if (skipTests)
			return;
		
		mongo = new Mongo();
		db = mongo.getDB(testRepo);
		tmListColl = db.getCollection(Repository.TM_COLL);
		
		// Make sure the repository is not there
		Repository.delete(testRepo);

		// Create repository (step 1)
		repo = new Repository(testRepo);
	}
	
	@After
	public void destroy() {
		if (skipTests)
			return;
		
		repo.close();
		mongo.close();
	}
	
	@Test
	public void testAddUpdateDeleteRecord ()
		throws SQLException, InterruptedException
	{
		if (skipTests)
			return;
		
		// There should be no TM
		List<String> list = repo.getTmNames();
		assertEquals(0, list.size());

		// Create a first TM
		String tmName = "first";
		String localeCode = DbUtil.toOlifantLocaleCode(LocaleId.ENGLISH);
		ITm tm = repo.createTm(tmName, null, localeCode);
		assertEquals(tmName, tm.getName());
		list = repo.getTmNames();
		assertEquals(1, list.size());
		assertEquals(tmName, list.get(0));

		DBCollection segColl = db.getCollection(tmName+"_SEG");
		assertEquals(0, segColl.count());
		
		// Add some records
		tm.startImport();
		LinkedHashMap<String, Object> segMap = new LinkedHashMap<String, Object>();
		segMap.put(DbUtil.TEXT_PREFIX+"EN", "Text EN 1");
		tm.addRecord(-1, null, segMap);
		tm.finishImport();
		Thread.sleep(100);	
		assertEquals(1, segColl.count());
		
		// Add another row
		tm.startImport();
		segMap.clear();
		segMap.put(DbUtil.TEXT_PREFIX+"EN", "Text EN 2");
		segMap.put("Quality"+DbUtil.LOC_SEP+"EN", 80);

		LinkedHashMap<String, Object> tuMap = new LinkedHashMap<String, Object>();
		tuMap.put("TUInfo", "TU-level info");
		tm.addRecord(-1, tuMap, segMap);
		tm.finishImport();
		Thread.sleep(100);
		assertEquals(2, segColl.count());

		tm.setRecordFields(Arrays.asList(new String[]{DbUtil.TEXT_PREFIX+"EN","Quality"+DbUtil.LOC_SEP+"EN","TUInfo"}));
		DBObject entry = MongoHelper.findCollEntry(db, tmName+"_SEG", Repository.SEG_COL_SEGKEY, 1);
		assertEquals("Text EN 1", entry.get(DbUtil.TEXT_PREFIX+"EN"));

		entry = MongoHelper.findCollEntry(db, tmName+"_SEG", Repository.SEG_COL_SEGKEY, 2);
		assertEquals("Text EN 2", entry.get(DbUtil.TEXT_PREFIX+"EN"));
		assertEquals(80, entry.get("Quality"+DbUtil.LOC_SEP+"EN"));
		assertEquals("TU-level info", entry.get("TUInfo"));
		
		// Add another row, this time create new fields
		tm.startImport();
		segMap.clear();
		segMap.put(DbUtil.TEXT_PREFIX+"EN", "Text EN 3");
		segMap.put(DbUtil.TEXT_PREFIX+"FR", "Text FR 3");
		segMap.put("Quality"+DbUtil.LOC_SEP+"FR", 33);
		tm.addRecord(-1, null, segMap);
		segMap.clear();
		segMap.put(DbUtil.TEXT_PREFIX+"EN", "Text EN 4");
		segMap.put(DbUtil.TEXT_PREFIX+"FR", "Text FR 4");
		tm.addRecord(-1, null, segMap);
		tm.finishImport();
		Thread.sleep(100);	
		assertEquals(4, segColl.count());
		
		tm.setRecordFields(Arrays.asList(new String[]{DbUtil.TEXT_PREFIX+"EN",
				"Quality"+DbUtil.LOC_SEP+"EN",
				"Quality"+DbUtil.LOC_SEP+"FR",
				DbUtil.TEXT_PREFIX+"FR"}));
		entry = MongoHelper.findCollEntry(db, tmName+"_SEG", Repository.SEG_COL_SEGKEY, 3);
		assertEquals("Text EN 3", entry.get(DbUtil.TEXT_PREFIX+"EN"));
		assertEquals("Text FR 3", entry.get(DbUtil.TEXT_PREFIX+"FR"));
		assertEquals(33, entry.get("Quality"+DbUtil.LOC_SEP+"FR"));

		entry = MongoHelper.findCollEntry(db, tmName+"_SEG", Repository.SEG_COL_SEGKEY, 4);
		assertEquals("Text EN 4", entry.get(DbUtil.TEXT_PREFIX+"EN"));
		assertEquals("Text FR 4", entry.get(DbUtil.TEXT_PREFIX+"FR"));
		
		List<Long> deleteEntries = new ArrayList<Long>();
		deleteEntries.add((long) 2);
		deleteEntries.add((long) 4);
		tm.deleteSegments(deleteEntries);
		
		Thread.sleep(100);	
		assertEquals(2, segColl.count());
		
		
		/*tm.addLocale(DbUtil.toOlifantLocaleCode(LocaleId.FRENCH));
		Thread.sleep(100);
		
		assertEquals("EN,FR", MongoHelper.findCollEntryValue(db, Repository.TM_COLL, Repository.TM_COL_NAME, tmName, Repository.TM_COL_LOCALES));
		assertEquals("Text~EN,Codes~EN,Text~FR,Codes~FR",MongoHelper.findCollEntryValue(db, Repository.TM_COLL, Repository.TM_COL_NAME, tmName, Repository.TM_COL_SEG_FIELDS));

		tm.renameLocale(DbUtil.toOlifantLocaleCode(LocaleId.FRENCH), DbUtil.toOlifantLocaleCode(LocaleId.GERMAN));
		Thread.sleep(100);
		
		assertEquals("EN,DE", MongoHelper.findCollEntryValue(db, Repository.TM_COLL, Repository.TM_COL_NAME, tmName, Repository.TM_COL_LOCALES));
		assertEquals("Text~EN,Codes~EN,Text~DE,Codes~DE",MongoHelper.findCollEntryValue(db, Repository.TM_COLL, Repository.TM_COL_NAME, tmName, Repository.TM_COL_SEG_FIELDS));

		tm.deleteLocale(DbUtil.toOlifantLocaleCode(LocaleId.GERMAN));
		Thread.sleep(100);
		
		assertEquals("EN", MongoHelper.findCollEntryValue(db, Repository.TM_COLL, Repository.TM_COL_NAME, tmName, Repository.TM_COL_LOCALES));
		assertEquals("Text~EN,Codes~EN",MongoHelper.findCollEntryValue(db, Repository.TM_COLL, Repository.TM_COL_NAME, tmName, Repository.TM_COL_SEG_FIELDS));*/
	}
	
	@Test
	public void testAddRenameDeleteLocalesTest ()
		throws SQLException, InterruptedException
	{
		if (skipTests)
			return;
		
		// There should be no TM
		List<String> list = repo.getTmNames();
		assertEquals(0, list.size());

		// Create a first TM
		String tmName = "first";
		String localeCode = DbUtil.toOlifantLocaleCode(LocaleId.ENGLISH);
		ITm tm = repo.createTm(tmName, null, localeCode);
		assertEquals(tmName, tm.getName());
		list = repo.getTmNames();
		assertEquals(1, list.size());
		assertEquals(tmName, list.get(0));
		
		// Test adding locale
		assertEquals("EN", MongoHelper.findCollEntryValue(db, Repository.TM_COLL, Repository.TM_COL_NAME, tmName, Repository.TM_COL_LOCALES));
		assertEquals("Text~EN,Codes~EN",MongoHelper.findCollEntryValue(db, Repository.TM_COLL, Repository.TM_COL_NAME, tmName, Repository.TM_COL_SEG_FIELDS));
		tm.addLocale(DbUtil.toOlifantLocaleCode(LocaleId.FRENCH));
		Thread.sleep(100);		
		assertEquals("EN,FR", MongoHelper.findCollEntryValue(db, Repository.TM_COLL, Repository.TM_COL_NAME, tmName, Repository.TM_COL_LOCALES));
		assertEquals("Text~EN,Codes~EN,Text~FR,Codes~FR",MongoHelper.findCollEntryValue(db, Repository.TM_COLL, Repository.TM_COL_NAME, tmName, Repository.TM_COL_SEG_FIELDS));
		tm.renameLocale(DbUtil.toOlifantLocaleCode(LocaleId.FRENCH), DbUtil.toOlifantLocaleCode(LocaleId.GERMAN));
		Thread.sleep(100);		
		assertEquals("EN,DE", MongoHelper.findCollEntryValue(db, Repository.TM_COLL, Repository.TM_COL_NAME, tmName, Repository.TM_COL_LOCALES));
		assertEquals("Text~EN,Codes~EN,Text~DE,Codes~DE",MongoHelper.findCollEntryValue(db, Repository.TM_COLL, Repository.TM_COL_NAME, tmName, Repository.TM_COL_SEG_FIELDS));
		tm.deleteLocale(DbUtil.toOlifantLocaleCode(LocaleId.GERMAN));
		Thread.sleep(100);		
		assertEquals("EN", MongoHelper.findCollEntryValue(db, Repository.TM_COLL, Repository.TM_COL_NAME, tmName, Repository.TM_COL_LOCALES));
		assertEquals("Text~EN,Codes~EN",MongoHelper.findCollEntryValue(db, Repository.TM_COLL, Repository.TM_COL_NAME, tmName, Repository.TM_COL_SEG_FIELDS));
	}
	
	@Test
	public void testAddRenameDeleteFieldTest ()
		throws SQLException, InterruptedException
	{
		if (skipTests)
			return;
		
		// There should be no TM
		List<String> list = repo.getTmNames();
		assertEquals(0, list.size());

		// Create a first TM
		String tmName = "first";
		String localeCode = DbUtil.toOlifantLocaleCode(LocaleId.ENGLISH);
		ITm tm = repo.createTm(tmName, null, localeCode);
		assertEquals(tmName, tm.getName());
		list = repo.getTmNames();
		assertEquals(1, list.size());
		assertEquals(tmName, list.get(0));
		
		// Test adding field
		assertEquals("Text~EN,Codes~EN",MongoHelper.findCollEntryValue(db, Repository.TM_COLL, Repository.TM_COL_NAME, tmName, Repository.TM_COL_SEG_FIELDS));
		tm.addField("field1");
		Thread.sleep(100);
		assertEquals("Text~EN,Codes~EN,field1",MongoHelper.findCollEntryValue(db, Repository.TM_COLL, Repository.TM_COL_NAME, tmName, Repository.TM_COL_SEG_FIELDS));
		tm.renameField("field1", "field1_updated");
		Thread.sleep(100);
		assertEquals("Text~EN,Codes~EN,field1_updated",MongoHelper.findCollEntryValue(db, Repository.TM_COLL, Repository.TM_COL_NAME, tmName, Repository.TM_COL_SEG_FIELDS));
		tm.deleteField("field1_updated");
		Thread.sleep(100);
		assertEquals("Text~EN,Codes~EN",MongoHelper.findCollEntryValue(db, Repository.TM_COLL, Repository.TM_COL_NAME, tmName, Repository.TM_COL_SEG_FIELDS));
	}
	
	@Test
	public void testSimpleTest ()
		throws SQLException
	{
		/*String testRepo = "testTMRepository";
		// Make sure the repository is not there
		Repository.delete(testRepo);

		// Create repository (step 1)
		IRepository repo = new Repository(testRepo);
		ProcesswithAPI.runMultipleTestsStep1(repo);

		// Run the second step with the existing repository (step 2)
		repo = new Repository(testRepo);
		ProcesswithAPI.runMultipleTestsStep2(repo);*/
	}

	@Test
	public void testMultiFieldsAdd ()
		throws SQLException
	{
		/*String testRepo = "testTMRepository";

		Repository.delete(testRepo);
		
		IRepository repo = new Repository(testRepo);
		ProcesswithAPI.runMultipleTestsStep3(repo);*/
	}
	
	
}
