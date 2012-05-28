package net.sf.okapi.tm.olifant.common;

import static org.junit.Assert.*;

import org.junit.Test;

import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;

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
