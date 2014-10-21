package org.github.pm2media;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.gitub.pm2media.ArticleCache;

/**
 * 
 */

/**
 * @author smootp
 *
 */
public class ArticleCacheTest {

	private final String cacheName = "test";
	ArticleCache ac;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		ac = new ArticleCache(cacheName);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		ac.cleanCache();
	}

	@Test
	public void testIfNotCached() {
		boolean isCached = ac.isArticleCached("test");
		assertFalse(isCached);
	}

	@Test
	public void testCreateDirectory() {
		final String articleName = "base/article";
		assertFalse(ac.isArticleCached(articleName));
	}
	
	@Test
	public void testCacheArticle() {
		ac.cacheArticle("test_article", "This is the body of the article");
	}
	
	@Test
	public void testRetrieveCachedArticle() {
		final String articleName = "test_article";
		final String articleBody = "Test article body.";
		ac.cacheArticle(articleName, articleBody);
		
		assertTrue(ac.isArticleCached(articleName));
		assertTrue(ac.getCachedArticle(articleName).equals(articleBody));
	}
	
	@Test
	public void testNestedCacheDirs() {
		String articleName = "test_dir/test_article";
		final String articleBody = "Test article body.";
		ac.cacheArticle(articleName, articleBody);
		
		assertTrue(ac.isArticleCached(articleName));
		assertTrue(ac.getCachedArticle(articleName).equals(articleBody));
		
		articleName = "test_dir/test_dir/test_dir/test_article";
		ac.cacheArticle(articleName, articleBody);
		
		assertTrue(ac.isArticleCached(articleName));
		assertTrue(ac.getCachedArticle(articleName).equals(articleBody));
	}
	
	@Test
	public void testPathsWithSpaces() {
		final String articleName = "test article";
		final String articleBody = "Test article body.";
		ac.cacheArticle(articleName, articleBody);
		
		assertTrue(ac.isArticleCached(articleName));
		assertTrue(ac.getCachedArticle(articleName).equals(articleBody));
	}
	
	@Test
	public void testBodyWithMultipleLines() {
		final String eol = System.getProperty("line.separator"); 
		final String articleName = "test_article";
		final String articleBody = "Test article body." + eol + "This is the second line." + eol + "This is third." + eol + eol + "Wheee!";
		ac.cacheArticle(articleName, articleBody);
		
		assertTrue(ac.isArticleCached(articleName));
		assertTrue(ac.getCachedArticle(articleName).equals(articleBody));
	}
	
	@Test
	public void testMultipleArticles() {
		String articleName = "test article %d";
		String articleBody = "Test article body copy %d.";
		
		final int testCount = 10;
		for (int i = 0; i < testCount; i++) {
			ac.cacheArticle(String.format(articleName, i), String.format(articleBody, i));
		}

		for (int i = 0; i < testCount; i++) {
			assertTrue(ac.isArticleCached(String.format(articleName, i)));
			assertTrue(ac.getCachedArticle(String.format(articleName, i)).equals(String.format(articleBody, i)));
		}
	}
}
