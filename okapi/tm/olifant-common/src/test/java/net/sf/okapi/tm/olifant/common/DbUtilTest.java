package net.sf.okapi.tm.olifant.common;

import static org.junit.Assert.*;

import org.junit.Test;

import net.sf.okapi.common.LocaleId;

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
	
}
