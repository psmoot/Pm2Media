import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class PmWiki2MediaWikiConverterTest {

	PmWiki2MediaWikiConverter converter;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		converter = new PmWiki2MediaWikiConverter();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testWithImagePrefix() {
		final String imagePrefix="test";
		converter.withImagePrefix(imagePrefix);
		assertEquals(imagePrefix, converter.getImagePrefix());
	}

	@Test
	public void testConvertMarkup() {
		assertEquals(converter.convertMarkup("hi"), "hi");
		assertEquals(converter.convertMarkup("'^super^'"), "<sup>super</sup>");
		assertEquals(converter.convertMarkup("'_subscript_'"), "<sub>subscript</sub>");
		assertEquals(converter.convertMarkup("[+hi+]"), "<big>hi</big>");
	}

}
