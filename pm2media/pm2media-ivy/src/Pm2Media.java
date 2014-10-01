import java.util.HashSet;
import java.util.Set;

import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;

/**
 * Pm2Media is the class configuring the whole pm2media project.
 * 
 * @author Johannes Perl
 * 
 */
public class Pm2Media {
	/** Parameters for source wiki (PmWiki). */
	private WikiParams pmWikiParams = new WikiParams();

	/** namespace to be converted to wiki main namespace. */
	private String pmWikiMainNamespace;

	/** Parameters for destination wiki (MediaWiki). */
	private WikiParams mwWiki = new WikiParams();
	
	/** link to login page. */
	private String mWikiLoginPage;

	/** link to upload page. */
	private String mWikiUploadPage;

	/** prefix of images: English => Image, GERMAN => Bild */
	private String mWikiImagePrefix;

	/** user agent of converter. */
	static final String USER_AGENT = "PmW2MW_Converter v 0.1";

	static final String CHANGE_SUMMARY = "Pm2Media Bot entry";

	/** max filesize in bytes to be uploaded. */
	static final long MAX_FILESIZE = 1024 * 1024 * 20;

	/** gui of the converter. */
	GUI gui;

	private String pmWikiIndexPageName;

	Pm2Media() {

	}
	
	/**
	 * Sets the properties needed for the PmWiki to MediaWiki conversion to be
	 * able to convert articles into MediaWiki syntax and post them into a
	 * MediaWiki.
	 * 
	 * @param pmwURL
	 *            the URL to PmWiki
	 * @param pmwMainNamespace
	 *            the main namespace of PmWiki who should be converted to main
	 *            namespace of MediaWiki
	 * @param mwURL
	 *            the URL to MediaWiki
	 * @param mwUsername
	 *            the username of MediaWiki user who should post the converted
	 *            articles
	 * @param mwPassword
	 *            the password of the user
	 * @param mwLoginPage
	 *            the Name of the MediaWiki login page (eg. Special:Userlogin
	 *            for English MediaWiki, Spezial:Anmelden for German MediaWiki)
	 * @param gui
	 *            the GUI of the converter
	 */
	public final void setProperties(String pmwURL,
			String pwmUsername, 
			String pmwPassword,
			String pmwMainNamespace, 
			String pmwIndexPageName,
			String mwURL, 
			String mwUsername,
			String mwPassword, 
			String mwLoginPage,
			String mwUploadPage, 
			String mwImagePrefix, 
			GUI gui) {
		pmWikiParams.setURL(pmwURL);
		pmWikiParams.setUsername(pwmUsername);
		pmWikiParams.setPassword(pmwPassword);
		this.pmWikiMainNamespace = pmwMainNamespace;
		this.pmWikiIndexPageName = pmwIndexPageName;
		
		mwWiki.setURL(mwURL);
		mwWiki.setUsername(mwUsername);
		mwWiki.setPassword(mwPassword);
		this.mWikiLoginPage = mwLoginPage;
		this.mWikiUploadPage = mwUploadPage;
		this.mWikiImagePrefix = mwImagePrefix;
		this.gui = gui;
	}

	/**
	 * Execute whole conversion process
	 */
	public void convert() {
		Logger.getInstance().log("Started conversion process");

		// variable saving startTime
		long startTime = System.currentTimeMillis();

		// PmWiki to get content from
		PmWiki pmWiki = new PmWiki()
				.withWikiParams(this.pmWikiParams)
				.withIndexPageName(pmWikiIndexPageName);

		// MediaWiki to post content in
		MediaWiki mediaWiki = new MediaWiki()
				.withWikiParams(mwWiki)
				.withLoginPage(mWikiLoginPage)
				.withUploadPage(mWikiUploadPage);
		if (!mediaWiki.login()) {
			Logger.getInstance().log("Could not login into MediaWiki. Exiting.");
			return;
		}

		Set<String> articleLinks = pmWiki.getLinksFromIndexPage();
		Article article = null;

		PmWiki2MediaWikiConverter converter = new PmWiki2MediaWikiConverter()
			.withImagePrefix(mWikiImagePrefix);
			
		// parsing every PmWiki article
		int i = 1;
		for (String articleLink : articleLinks) {
			String articleName, articleNamespace;

			Logger.getInstance().log("Processing " + articleLink + " (" + i + " of " + articleLinks.size() + ").");
			i += 1;
			
			if (articleLink.substring(this.pmWikiParams.getURL().length()).indexOf(".") != -1) {
				String tmpArticleLink = articleLink.substring(this.pmWikiParams.getURL()
						.length());
				String[] tmp = tmpArticleLink.split("\\.");
				articleNamespace = tmp[0].substring(tmp[0].indexOf('=') + 1);
				articleName = tmp[1];
			}
			else {
				String[] tmp = articleLink.split("/");
				articleNamespace = tmp[tmp.length - 2];
				articleName = tmp[tmp.length - 1];
			}

			// don't parse articles which are PmWiki specific
			if (!PmWiki.articleIsRelevant(articleName, articleNamespace)) {
				continue;
			}

			try {
				// parsing relevant pages
				article = pmWiki.getArticle(articleLink, articleNamespace,
						articleName);

				// nothing to post
				if (article.getBody().isEmpty()) {
					continue;
				}

				article.convertBody(converter);

				mediaWiki.postArticle(article);

				for (Attachment attachment : article.getAttachments()) {
					if (Pm2MediaPrefs
							.getBoolProperty(Pm2MediaPrefs.PMWIKI_DOWNLOAD_ATTACHMENTS)) {
						pmWiki.download(attachment);

						if (Pm2MediaPrefs
								.getBoolProperty(Pm2MediaPrefs.MEDIAWIKI_UPLOAD_ATTACHMENTS)) {
							mediaWiki.upload(attachment);
						}
					}
				}
			} catch (Exception e) {
				Logger.getInstance().logError(
						"Exception while converting article " + articleName
								+ ": " + e.getLocalizedMessage());
				e.printStackTrace();
			}
		}

		// variable saving startTime
		long requiredTime = (System.currentTimeMillis() - startTime) / 1000;

		Logger.getInstance().log(
				"Convertion ended successfully in " + requiredTime
						+ " seconds.");
	}


	/**
	 * Returns the URL to the PmWiki.
	 * 
	 * @return the URL to the PmWiki
	 */
	public String getPmWikiURL() {
		return pmWikiParams.getURL();
	}

	/**
	 * Sets the URL to the PmWiki.
	 * 
	 * @param pmWikiURL
	 *            the URL to the PmWiki
	 */
	public void setPmWikiURL(String pmWikiURL) {
		pmWikiParams.setURL(pmWikiURL);
	}

	/**
	 * Returns the PmWiki main namespace to be converted to the MediaWiki:Main
	 * namespace
	 * 
	 * @return the PmWiki namespace to be MediaWiki:Main namespace
	 */
	public String getPmWikiMainNamespace() {
		return pmWikiMainNamespace;
	}
	
	/**
	 * Set the PmWiki user name
	 * 
	 * @param data.getPmWikiUsername()
	 */
	public void setPmWikiUsername(String Username) {
		pmWikiParams.setUsername(Username);
	}
	
	/**
	 * Return the PmWiki user name.
	 * 
	 * @return the PmWiki username.
	 */
	public String getPmWikiUsername() {
		return pmWikiParams.getUsername();
	}

	/**
	 * Set the PmWiki password
	 * 
	 * @param data.getPmWikiPassword()
	 */
	public void setPmWikiPassword(String Password) {
		pmWikiParams.setPassword(Password);
	}
	
	/**
	 * Return the PmWiki user password.
	 * 
	 * @return the PmWiki password.
	 */
	public String getPmWikiPassword() {
		return pmWikiParams.getPassword();
	}

	/**
	 * Sets the PmWiki main namespace to be converted to the MediaWiki:Main
	 * 
	 * @param pmWikiMainNamespace
	 *            the PmWiki namespace to be MediaWiki:Main namespace
	 */
	public void setPmWikiMainNamespace(String MainNamespace) {
		pmWikiMainNamespace = MainNamespace;
	}

	/**
	 * Returns the URL to the MediaWiki.
	 * 
	 * @return the URL to the MediaWiki
	 */
	public String getMWikiURL() {
		return mwWiki.getURL();
	}

	/**
	 * Sets the url to the MediaWiki.
	 * 
	 * @param wikiURL
	 *            the url to the MediaWiki
	 */
	public void setMWikiURL(String wikiURL) {
		mwWiki.setURL(wikiURL);
	}

	/**
	 * Returns the name of the MediaWiki user.
	 * 
	 * @return the name of the MediaWiki user.
	 */
	public String getMWikiUsername() {
		return mwWiki.getUsername();
	}

	/**
	 * Sets the name of the MediaWiki user.
	 * 
	 * @param wikiUsername
	 *            the name of the MediaWiki user
	 */
	public void setMWikiUsername(String wikiUsername) {
		mwWiki.setUsername(wikiUsername);
	}

	/**
	 * Gets the password of the MediaWiki user.
	 * 
	 * @return the password of the MediaWiki user.
	 */
	public String getMWikiPassword() {
		return mwWiki.getPassword();
	}

	/**
	 * Sets the password of the user posting and uploading images into
	 * MediaWiki.
	 * 
	 * @param wikiPassword
	 *            the password of the MediaWiki user
	 */
	public void setMWikiPassword(String wikiPassword) {
		mwWiki.setPassword(wikiPassword);
	}

	/**
	 * Returns the name of the MediaWiki login page (eg. Special:Userlogin for
	 * English MediaWiki, Spezial:Anmelden for German MediaWiki)
	 * 
	 * @return the Name of the MediaWiki login page (eg. Special:Userlogin for
	 *         English MediaWiki, Spezial:Anmelden for German MediaWiki)
	 */
	public String getMWikiLoginPage() {
		return mWikiLoginPage;
	}

	/**
	 * Sets the name of the MediaWiki login page (eg. Special:Userlogin for
	 * English MediaWiki, Spezial:Anmelden for German MediaWiki)
	 * 
	 * @param wikiLoginPage
	 *            the Name of the MediaWiki login page (eg. Special:Userlogin
	 *            for English MediaWiki, Spezial:Anmelden for German MediaWiki)
	 */
	public void setMWikiLoginPage(String wikiLoginPage) {
		mWikiLoginPage = wikiLoginPage;
	}

	/**
	 * Returns the name of the MediaWiki upload page (e.g. Special:Upload for
	 * English MediaWiki, Spezial:Hochladen for German MediaWiki)
	 * 
	 * @return the Name of the MediaWiki upload page (e.g. Special:Upload for
	 *         English MediaWiki, Spezial:Hochladen for German MediaWiki)
	 */
	public String getMWikiUploadPage() {
		return mWikiUploadPage;
	}

	/**
	 * Sets the name of the MediaWiki upload page (e.g. Special:Upload for
	 * English MediaWiki, Spezial:Hochladen for German MediaWiki)
	 * 
	 * @param wikiUploadPage
	 *            the Name of the MediaWiki upload page (e.g. Special:Upload for
	 *            English MediaWiki, Spezial:Hochladen for German MediaWiki).
	 */
	public void setMWikiUploadPage(String wikiUploadPage) {
		mWikiUploadPage = wikiUploadPage;
	}

	/**
	 * Returns the specified image prefix (e.g. Image for English MediaWiki,
	 * Bild for German MediaWiki)
	 */
	public String getmWikiImagePrefix() {
		return mWikiImagePrefix;
	}

	/**
	 * Sets the specified image prefix (e.g. Image for English MediaWiki, Bild
	 * for German MediaWiki)
	 */
	public void setmWikiImagePrefix(String ImagePrefix) {
		mWikiImagePrefix = ImagePrefix;
	}

	/**
	 * Returns the GUI of the converter.
	 * 
	 * @return the GUI of the converter
	 */
	public GUI getGui() {
		return gui;
	}

	/**
	 * Sets the GUI of the converter.
	 * 
	 * @param gui
	 *            the GUI of the converter
	 */
	public void setGui(GUI pGui) {
		gui = pGui;
	}
}
