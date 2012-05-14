package net.sf.okapi.lib.tmdb;

import static org.junit.Assert.*;

import org.junit.Test;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.lib.tmdb.DbUtil;

public class ContentFormatTest {
	
	@Test
	public void testFragmentConversions () {
		TextFragment tf1 = new TextFragment("A");
		tf1.append(TagType.OPENING, "b", "<b>");
		tf1.append("B");
		tf1.append(TagType.CLOSING, "b", "</b>");
		
		ContentFormat cntFmt = new ContentFormat();
		String[] res = cntFmt.fragmentToTmFields(tf1);
		
		// Test no change
		TextFragment tf2 = cntFmt.tmFieldsToFragment(res[0], res[1]);
		assertEquals(tf1.toText(), tf2.toText());
	}

}
