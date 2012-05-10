package net.sf.okapi.lib.tmdb;

import static org.junit.Assert.*;

import org.junit.Test;

public class StringDiffTest {
	
	@Test
	public void testStringDiff1 () {
		StringDiff sd = new StringDiff();
		String res = sd.convert(sd.calculate("word", "World"));
		assertEquals("<del>w</del><ins>W</ins>or<ins>l</ins>d", res);
	}

	@Test
	public void testStringDiff2 () {
		StringDiff sd = new StringDiff();
		String res = sd.convert(sd.calculate("word", ""));
		assertEquals("<del>word</del>", res);
	}

	@Test
	public void testStringDiff3 () {
		StringDiff sd = new StringDiff();
		String res = sd.convert(sd.calculate("", "word"));
		assertEquals("<ins>word</ins>", res);
	}

	@Test
	public void testStringDiff4 () {
		StringDiff sd = new StringDiff();
		String res = sd.convert(sd.calculate("This is a simple test", "it is a sample test."));
		assertEquals("<del>Th</del>i<del>s</del><ins>t</ins> is a s<del>i</del><ins>a</ins>mple test<ins>.</ins>", res);
	}

}
