import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.NodeVisitor;

import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * PmWiki is the class representing a PmWiki installation.
 * 
 * @author Johannes Perl
 * 
 */
public class PmWiki {
	/** Common parameters for all wikis. */
	private WikiParams wikiParams;
	public PmWiki withWikiParams(WikiParams wikiParams) {
		this.wikiParams = wikiParams;
		return this;
	}
	public WikiParams getWikiParams() {
		return wikiParams;
	}
	
	private String indexPageName;
	public PmWiki withIndexPageName(String indexPageName) {
		this.indexPageName = indexPageName;
		return this;
	}

	private final WebClient webClient = new WebClient();

	private ArticleCache articleCache;
	
	/** sequence marking start of content of PmWiki */
	static String contentStart = "<!--PageText-->";

	/** sequence marking end of content of PmWiki */
	static String contentEnd = "<!--PageFooterFmt-->";

	/**
	 * Class constructor specifying URL and local path to PmWiki.
	 */
	public PmWiki() {
		Pm2MediaPrefs.getBoolProperty(Pm2MediaPrefs.PMWIKI_USE_CACHE);
		articleCache = new ArticleCache();
	}

	public void initializeCredentials() {
		DefaultCredentialsProvider creds = new DefaultCredentialsProvider();
		creds.addCredentials(wikiParams.getUsername(), wikiParams.getPassword());
		webClient.setCredentialsProvider(creds);
	}
	
	/**
	 * Returns a NodeList containing all links to articles of PmWiki.
	 * 
	 * @return all links to articles parsed form the index page of PmWiki
	 */
	public Set<String> getLinksFromIndexPage() {
		String page = "", input = "";
		String pmWikiIndexURL = "";

		initializeCredentials();
		
		// NodeList for the <a> tags
		NodeList linkTags = new NodeList();
		final Set<String> links = new HashSet<String>();
		
		try {
			pmWikiIndexURL = wikiParams.getURL() + "/" + indexPageName;
			Logger.getInstance().log(pmWikiIndexURL, Logger.Mode.OPEN);
			HtmlPage allPagesPage = webClient.getPage(pmWikiIndexURL);
			page = allPagesPage.asXml().toString();
			Logger.getInstance().log("Retrieved index page.");
			Logger.getInstance().logPage(allPagesPage, "pmwiki-index-page.html");
		}
		catch (MalformedURLException e) {
			Logger.getInstance().logError("Failed to open index page, malformed URL " + pmWikiIndexURL);
			e.printStackTrace();
			return links;
		}
		catch (IOException e) {
			Logger.getInstance().logError("Failed to open index page, exception " + e.getMessage());
			e.printStackTrace();
			return links;
		}

		// trimming the PmWiki page to the content
		input = page.substring(
				page.indexOf(contentStart) + contentStart.length(),
				page.indexOf(contentEnd));

		// Find all a tags on the index page
		try {
			Parser parser = new Parser();
			parser.setInputHTML(input);
			Logger.getInstance().log(pmWikiIndexURL, Logger.Mode.PARSE);
			NodeFilter filter = new AndFilter(new TagNameFilter("a"), new HasAttributeFilter("class", "wikilink"));
			linkTags = parser.extractAllNodesThatMatch(filter);

			linkTags.visitAllNodesWith(new NodeVisitor() {
				@Override
				public void visitTag(Tag tag) {
					LinkTag linkTag = (LinkTag) tag;
					links.add(linkTag.getLink());
				}
			});
		}
		catch (ParserException e) {
			Logger.getInstance().logError("Failed to open index page, parser exception " + e.getMessage());
			e.printStackTrace();
		}

		// number of link tags found
		Logger.getInstance().log("Found " + links.size() + " <a> tags.");

		return links;
	}

	/**
	 * Returns whether a link has to be parsed and converted into MediaWiki
	 * syntax (returns false for PmWiki specific pages).
	 * 
	 * @param articleName
	 *            the name of the article
	 * @param articleNamespace
	 *            the namespace of the article
	 * @return the relevance of the article for the MediaWiki
	 */
	public static boolean articleIsRelevant(String articleName,
			String articleNamespace) {
		// eliminating all pages, that are PmWiki specific
		if (articleName.equalsIgnoreCase("AllPages")
				|| articleName.equalsIgnoreCase("RecentChanges")
				|| articleName.equalsIgnoreCase("AllRecentChanges")
				|| articleName.equalsIgnoreCase("Sandbox")
				|| articleName.equalsIgnoreCase("SideBar")
				|| articleName.equalsIgnoreCase("Other-Contrib")
				|| articleName.equalsIgnoreCase("WikiSandbox")
				|| articleName.equalsIgnoreCase("AuthorContributions")
				|| articleNamespace.equalsIgnoreCase("Site")
				|| articleName.equalsIgnoreCase("?")
				|| articleNamespace.equalsIgnoreCase("PmWiki")) {
			return false;
		}

		return true;
	}

	/**
	 * Gets an article from a PmWiki.
	 */
	public Article getArticle(String articleLink, String articleNamespace, String articleName) {
		Article article = new Article(articleNamespace, articleName, "");

		if (getArticleFromCache(article)) {
			return article;
		}
		
		try {
			String articleURL;
			if (articleLink.indexOf('?') != -1) {
				articleURL = articleLink + "&";
			} else {
				articleURL = articleLink + "?";
			}

			articleURL += "action=edit";
			Logger.getInstance().log(articleURL, Logger.Mode.READ);
			HtmlPage page = webClient.getPage(articleURL);
			DomElement textEditElement = page.getElementByName("text");
			String text = textEditElement.asText();
			HtmlElement cancelButton = (HtmlElement) page.getElementByName("cancel");
			cancelButton.click();
			
			article.setBody(text);

			if (Pm2MediaPrefs.getBoolProperty(Pm2MediaPrefs.PMWIKI_USE_CACHE)) {
				articleCache.cacheArticle(article.getPathInWiki("/"),  article.getBody());
			}
			
			// pattern matching all attachments
			Pattern detectAttachments = Pattern.compile("Attach:(.*?)\\.([\\w]{3,4})");
			Matcher matcher = detectAttachments.matcher(text);

			while (matcher.find()) {
				String fileName = "attachments/" + matcher.group(1).trim() + "."
						+ matcher.group(2);
				String baseURL = wikiParams.getURL().substring(0, wikiParams.getURL().lastIndexOf('/'));
				String fileURL = baseURL + "/uploads/" + article.getPathInWiki(".") + "/" + fileName;
				File file = new File(fileURL);
				
				if (file.length() < Pm2Media.MAX_FILESIZE) {
					article.addAttachment(new Attachment(fileName, fileURL));
				} else {
					Logger.getInstance().log(
							"File " + fileName + " exceeding max filesize",
							Logger.Mode.ERROR);
				}
			}
		}
		catch (MalformedURLException e) {
			Logger.getInstance().log(e.getMessage(), Logger.Mode.ERROR);
		}
		catch (IOException e) {
			Logger.getInstance().log(e.getMessage(), Logger.Mode.ERROR);
		}

		return article;
	}
	
	private boolean getArticleFromCache(Article article) {
		final String articlePath = article.getPathInWiki("/");
		if (articleCache.isArticleCached(articlePath)) {
			article.setBody(articleCache.getCachedArticle(articlePath));
			return true;
		} else {
			return false;
		}
	}
	
	public void download(final Attachment attachment) {
		try {
			WebRequest request = new WebRequest(new URL(attachment.getFileURL()));
			InputStream is = webClient.loadWebResponse(request).getContentAsStream();
			
			File localFile = new File(attachment.getFileName());
			OutputStream os = new FileOutputStream(localFile);
			
			byte[] bytes = new byte[1024 * 10];
			int bytesRead = is.read(bytes);
			while (bytesRead > 0) {
				os.write(bytes, 0, bytesRead);
				bytesRead = is.read(bytes);
			}
			os.close();
		} catch (MalformedURLException e) {
			Logger.getInstance().logError("Malformed URL: " + e.getMessage());
		} catch (FileNotFoundException e) {
			Logger.getInstance().logError("Could not open source file: " + e.getMessage());
		} catch (IOException e) {
			Logger.getInstance().logError("Error writing downloaded file: " + e.getMessage());
		}
	}
}
