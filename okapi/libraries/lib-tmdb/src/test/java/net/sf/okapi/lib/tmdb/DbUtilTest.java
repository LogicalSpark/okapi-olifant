package net.sf.okapi.lib.tmdb;

import static org.junit.Assert.*;

import org.junit.Test;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.lib.tmdb.DbUtil;

public class DbUtilTest {
	
	@Test
	public void testLocaleCodes () {
		String code = DbUtil.toOlifantLocaleCode(LocaleId.ENGLISH);
		assertEquals("EN", code);
		
		code = DbUtil.toOlifantLocaleCode(LocaleId.fromString("de-DE-u-email-co-phonebk-x-linux"));
		assertEquals("DE_DE-u-email-co-phonebk-x-linux", code);
		
		LocaleId locId = DbUtil.fromOlifantLocaleCode(code);
		assertEquals("de-de-u-email-co-phonebk-x-linux", locId.toString());
		
		code = DbUtil.toOlifantLocaleCode(LocaleId.fromString("de-DE"));
		assertEquals("DE_DE", code);
		
		code = DbUtil.toOlifantLocaleCode(LocaleId.fromString("es-419"));
		assertEquals("ES_419", code);
	}

	@Test
	public void testGetFieldLocale () {
		assertEquals("EN", DbUtil.getFieldLocale("Name"+DbUtil.LOC_SEP+"EN"));
		assertEquals("EN_GB", DbUtil.getFieldLocale("Name"+DbUtil.LOC_SEP+"EN_GB"));
		assertNull(DbUtil.getFieldLocale("Name"));
		assertNull(DbUtil.getFieldLocale(DbUtil.FLAG_NAME));
		assertNull(DbUtil.getFieldLocale(DbUtil.SEGKEY_NAME));
		assertNull(DbUtil.getFieldLocale(DbUtil.TUREF_NAME));
	}
	
	@Test
	public void testFragmentConversions () {
		TextFragment tf1 = new TextFragment("A");
		tf1.append(TagType.OPENING, "b", "<b>");
		tf1.append("B");
		tf1.append(TagType.CLOSING, "b", "</b>");
		
		DbUtil util = new DbUtil();
		String[] res = util.fragmentToTmFields(tf1);
		
		// Test no change
		TextFragment tf2 = util.tmFieldsToFragment(res[0], res[1]);
		assertEquals(tf1.toText(), tf2.toText());
	}

}
