package net.sf.okapi.lib.tmdb;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;
import org.xml.sax.SAXException;

import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.resource.Code;

public class ContentFormatTest {
	
	@Test
	public void testTmFieldRoundTrip1 () {
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

	@Test
	public void testFullCodesRoudTrip1 ()
		throws SAXException, IOException
	{
		TextFragment tf1 = new TextFragment("A");
		Code code = tf1.append(TagType.OPENING, "b", "<b>");
		code.setOuterData("<bpt i='1'>&lt;b></bpt>");
		tf1.append("B");
		code = tf1.append(TagType.CLOSING, "b", "</b>");
		code.setOuterData("<ept i='1'>&lt;/b></ept>");
		
		ContentFormat cntFmt = new ContentFormat();
		String res = cntFmt.fragmentToFullCodesText(tf1);
		
		// Test no change
		TextFragment tf2 = cntFmt.fullCodesTextToFragment(res);
		assertEquals(tf1.toText(), tf2.toText());
	}

}
