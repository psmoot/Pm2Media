import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * MediaWiki is the class representing a MediaWiki installation.
 * 
 * @author Johannes Perl
 * 
 */
public class MediaWiki {
	/** Common parameters for all wikis */
	private WikiParams wikiParams;
	public final void setWikiParams(final WikiParams wikiParams) {
		this.wikiParams = wikiParams;
	}
	public final MediaWiki withWikiParams(final WikiParams wikiParams) {
		setWikiParams(wikiParams);
		return this;
	}
	
	/** name of login page [eg. Special:Userlogin|Spezial:Anmelden] */
	private String loginPagePath;
	public final MediaWiki withLoginPage(final String loginPage) {
		this.loginPagePath = loginPage;
		return this;
	}
	
	/** Name of upload page for images */
	private String uploadPage;
	public final MediaWiki withUploadPage(final String uploadPage) {
		this.uploadPage = uploadPage;
		return this;
	}
	
	/** containing cookie text */
	static String cookie = "";

	/** ArrayList containing all image names in order to prevent multiple upload */
	ArrayList<String> attachments = new ArrayList<String>();

	private final WebClient webClient = new WebClient();

	private ArticleCache mediawikiCache;

	public MediaWiki() {
		webClient.addRequestHeader("Accept-Encoding", "");
		mediawikiCache = new ArticleCache("mediaWiki");
		mediawikiCache.cleanCache();
	}

	/**
	 * Posts an article into a MediaWiki.
	 * 
	 * @param article
	 *            the article to be posted
	 */
	public void postArticle(Article article) {
		String editLink = wikiParams.getURL() + "?title=" + article.getPathInWiki("/") + "&action=edit";

		saveToCache(article);
		
		if (! Pm2MediaPrefs.getBoolProperty(Pm2MediaPrefs.MEDIAWIKI_UPLOAD_ARTICLES)) {
			Logger.getInstance().log("Skipping upload of " + article.getPathInWiki("/") + ".");
			return;
		}
		
		// posting content into MediaWiki
		try {
			Logger.getInstance().log(editLink, Logger.Mode.READ);
			WikiPage page = new WikiPage(webClient.getPage(editLink)).withFormName("editform");

			page.setTextAreaValue("wpTextbox1", article.getBody());
			page.setTextInputValue("wpSummary", Pm2Media.CHANGE_SUMMARY);
			Logger.getInstance().logPage(page, "article-post-page.html");
			
			HtmlPage submitPage = page.clickSubmit("wpSave");
			Logger.getInstance().logPage(submitPage, "article-post-results-page.html");
		}
		catch (Exception e) {
			Logger.getInstance().logError("Failed to upload article " + article.getPathInWiki("/") + ": " + e.getLocalizedMessage());
			e.printStackTrace();
		}

	}
	
	private void saveToCache(Article article) {
		if (Pm2MediaPrefs.getBoolProperty(Pm2MediaPrefs.MEDIAWIKI_USE_CACHE)) {
			mediawikiCache.cacheArticle(article.getPathInWiki("/"), article.getBody());
		}
	}
	
	/**
	 * Logs the specified user into a MediaWiki.
	 * 
	 * @return the success of the login process
	 */
	public boolean login() {
		// login with specified user
		try {
			WikiPage loginPage = new WikiPage(webClient.getPage(this.wikiParams.getURL() + "/" + loginPagePath)).withFormName("userlogin");
			Logger.getInstance().logPage(loginPage, "login-page.html");

			// set username and password
			loginPage.setTextInputValue("wpName", wikiParams.getUsername());
			loginPage.setTextInputValue("wpPassword", wikiParams.getPassword());
			loginPage.setCheckboxInputValue("wpRemember", false);

			Logger.getInstance().logPage(loginPage, "pre-submit-page.html");
			
			// login via submit button
			final HtmlPage loginResultsPage = loginPage.clickSubmit("wpLoginAttempt");
			Logger.getInstance().logPage(loginResultsPage, "login-results-page.html");

			if (! loginResultsPage.asXml().toString().contains("errorbox")) {
				return true;
			}
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Uploads an attachment onto a MediaWiki.
	 * 
	 * @param attachment
	 *            the attachment to be uploaded
	 */
	public final void upload(final Attachment attachment) {
		if (attachments.contains(attachment.getFileName())) {
			return; /* image already uploaded */
		}
		attachments.add(attachment.getFileName());			

		if (! Pm2MediaPrefs.getBoolProperty(Pm2MediaPrefs.MEDIAWIKI_UPLOAD_ATTACHMENTS)) {
			Logger.getInstance().log("Skipping upload of " + attachment.getFileName() + ".");
			return;
		}
		
		File localCopy = new File(attachment.getFileName());

		try {
			String uploadURL = wikiParams.getURL() + "/" + this.uploadPage;
			WikiPage uploadPage = new WikiPage(webClient.getPage(uploadURL))
				.withFormId("mw-upload-form");

			Logger.getInstance().log("Uploading " + attachment.getFileName());

			uploadPage.setFileInputValue("wpUploadFile", localCopy); // Source file name
			uploadPage.setCheckboxInputValue("wpIgnoreWarning", true);

			// submit form
			Logger.getInstance().logPage(uploadPage, "image_upload_page.html");
			HtmlPage submitPage = uploadPage.clickSubmit("wpUpload");
			Logger.getInstance().logPage(submitPage, "lastImageUpload.html");
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			localCopy.delete();			
		}
	}
}
