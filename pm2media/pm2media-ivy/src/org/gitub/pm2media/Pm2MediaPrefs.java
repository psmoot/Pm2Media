package org.gitub.pm2media;

import java.io.InputStream;
import java.util.Properties;

public class Pm2MediaPrefs {

	public static final String CACHE_NAME_FORMAT = "pm2media.%s.cacheDirectory";
	
	public static final String PMWIKI_USE_CACHE = "pmwiki.useCache";
	public static final String PMWIKI_DOWNLOAD_ATTACHMENTS = "pmwiki.downloadAttachments";
	
	public static final String MEDIAWIKI_USE_CACHE = "mediawiki.useCache";
	public static final String MEDIAWIKI_UPLOAD_ARTICLES = "mediawiki.uploadArticles";
	public static final String MEDIAWIKI_UPLOAD_ATTACHMENTS = "mediawiki.uploadAttachemnts";
	
	private static final Pm2MediaPrefs INSTANCE = new Pm2MediaPrefs();
	
	private Properties props = new Properties();
	
	static public Pm2MediaPrefs getInstance() {
		return INSTANCE;
	}
	
	private Pm2MediaPrefs() {
		try {
			InputStream input = GUI.class.getResourceAsStream("/pm2media.properties");
			props.load(input);
		}
		catch (Exception e) {
			;
		}
	}
	
	public static String getProperty(final String key, final String defaultValue) {
		return getInstance().props.getProperty(key, defaultValue);
	}

	public static String getProperty(final String key) {
		return getInstance().props.getProperty(key);
	}
	
	public static boolean getBoolProperty(final String key) {
		String value = "";
		try {
			value = getInstance().props.getProperty(key);
			return (value != null && value.equalsIgnoreCase("true"));
		} catch (NullPointerException e) {
			e.printStackTrace();
			return false;
		}
	}
}
