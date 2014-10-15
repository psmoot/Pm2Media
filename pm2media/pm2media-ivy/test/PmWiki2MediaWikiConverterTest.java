import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class PmWiki2MediaWikiConverterTest {

	PmWiki2MediaWikiConverter converter;
	final String pmwikiPrefix="http://www.pmwiki.org/testwiki/pmwiki.php/";
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		converter = new PmWiki2MediaWikiConverter()
				.withImagePrefix("Image")
				.withSourceWikiPrefix(pmwikiPrefix);
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
	public void testConvertSimpleMarkup() {
		assertEquals(converter.convertMarkup("hi"), "hi");
		assertEquals(converter.convertMarkup("'^super^'"), "<sup>super</sup>");
		assertEquals(converter.convertMarkup("'_subscript_'"), "<sub>subscript</sub>");
		assertEquals(converter.convertMarkup("[+hi+]"), "<big>hi</big>");
	}
	
	@Test
	public void testRemoveUselessHtmlTags() {
		assertEquals(converter.convertMarkup("first <st1:personname name=\"bob\">second</st1:personname> third"), "first second third");
		assertEquals(converter.convertMarkup("first <st1:personname name=\"bob\">second third"), "first second third");
		assertEquals(converter.convertMarkup("first <o:p name=\"bob\">second </o:p>third"), "first second third");
	}

	@Test
	public void testRemoveRedirects() {
		assertEquals("#REDIRECT [[the/new/page]]", converter.convertMarkup("(:redirect the/new/page:)"));
	}
	
	@Test
	public void testPmwikiPrefix() {
		converter.withSourceWikiPrefix("http://www.pmwiki.com/source/prefix/pmwiki.php/Main/Page");
		assertEquals("www.pmwiki.com/source/prefix/pmwiki.php/", converter.getSourceWikiPrefix());

		converter.withSourceWikiPrefix("https://www.pmwiki.com/source/prefix/pmwiki.php/Main/Page");
		assertEquals("www.pmwiki.com/source/prefix/pmwiki.php/", converter.getSourceWikiPrefix());
	}

	@Test
	public void testRemoveHrefs() {
		assertEquals("[http://www.example.com|Example.com]",
				converter.convertMarkup("<a href=\"http://www.example.com\">Example.com</a>"));
		assertEquals("[http://www.example.com|Example.com]",
				converter.convertMarkup("<a   href=\"http://www.example.com\"  >Example.com</a  >"));
		assertEquals("[http://www.example.com]",
				converter.convertMarkup("<a href=\"http://www.example.com\" />"));
		assertEquals("[http://www.example.com]",
				converter.convertMarkup("<a    href=\"http://www.example.com\"   />"));
		assertEquals("[http://www.example.com]",
				converter.convertMarkup("<a href=\"http://www.example.com\"/>"));
		
		assertEquals("[http://www.example.com|<nowiki>[</nowiki>]",
				converter.convertMarkup("<a href=\"http://www.example.com\">[</a>"));
	}
	
	@Test
	public void testConvertExternalHrefsToInternal() {
		assertEquals("[[page]]",
				converter.convertMarkup("<a href=\"" + pmwikiPrefix + "page\" />"));
		assertEquals("[[page|Page text]]",
				converter.convertMarkup("<a href=\"" + pmwikiPrefix + "page\">Page text</a>"));
		
		assertEquals("[[page|Page]]",
				converter.convertMarkup("[[" + pmwikiPrefix + "page | Page]]"));
		assertEquals("[[page/other/page|Page]]",
				converter.convertMarkup("[[" + pmwikiPrefix + "page/other/page | Page]]"));
	}
}
