import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

/**
 * Class to cache articles on local disk.
 * 
 * @author smootp
 *
 */
public class ArticleCache {
	private final String cacheDirectoryName;
	
	private void createCacheDir(final String directoryName) {
		File cacheDir = new File(directoryName);
		
		try {
			if (! cacheDir.exists()) {
				cacheDir.mkdirs();
			}
		} catch (SecurityException e) {
			Logger.getInstance().logError("Could not create cache directory.");
			e.printStackTrace();
		}
	}
	
	public ArticleCache() {
		cacheDirectoryName = Pm2MediaPrefs.getProperty("pm2media.cacheDirectory", "article-cache");
	}
	
	protected void cleanCache() {
		File cacheDirectory = new File(cacheDirectoryName);
		
//		try {
//			FileUtils.cleanDirectory(cacheDirectory);
//		} catch (IOException e) {
//			// Don't worry, we don't care.
//			;
//		}
//		catch (IllegalArgumentException e) {
//			// Most likely directory does not exist. Ignore this too.
//			;
//		}
		FileUtils.deleteQuietly(cacheDirectory);
	}
	
	public void cacheArticle(final String articleName, final String articleBody) {
		File cachedArticle = new File(cacheDirectoryName + "/" + articleName);
		createCacheDir(cachedArticle.getParent());
		try {
			FileUtils.writeStringToFile(cachedArticle, articleBody);
		} catch (IOException e) {
			// Don't worry, it just won't be cached.
			;
		}
	}

	public boolean isArticleCached(final String articleName) {
		File cachedArticleName = new File(cacheDirectoryName + "/" + articleName);
		return cachedArticleName.exists();
	}
	
	public String getCachedArticle(final String articleName) {
		File cachedArticle = new File(cacheDirectoryName + "/" + articleName);
		String articleBody = "";

		if (isArticleCached(articleName)) {
			try {
				articleBody = FileUtils.readFileToString(cachedArticle);
			} catch (IOException e) {
				Logger.getInstance().logError(
						"Failed to read article " + articleName
								+ " from cache.");
				e.printStackTrace();
			}
		}
		
		return articleBody;
	}
}
