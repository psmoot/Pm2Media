package org.gitub.pm2media;

import java.util.HashSet;
import java.util.Set;

/**
 * Article is the class representing a PmWiki article with text, images and
 * files.
 * 
 * @author Johannes Perl
 * 
 */
public class Article {
/** Article namespace (first component of article name). */
	private String namespace;

	/** Article name (second component of article name). */
	private String name;
	
	/** The text of the article. */
	private String body;

	/** the attachments (files, images) the article contains. */
	private Set<Attachment> attachments;

	/**
	 * Class constructor specifying name and text of the article.
	 * 
	 * @param namespace
	 *            Namespace, first component of article name
	 *            
	 * @param name
	 * 			  Name, second component of article name
	 * 
	 * @param body
	 *            the text of the article
	 */
	public Article(final String namespace, final String name, final String body) {
		attachments = new HashSet<Attachment>();
		
		this.namespace = namespace;
		this.name = name;
		this.body = body.trim();
	}

	/**
	 * Adds an attachment (file, image) to an Article.
	 * 
	 * @param attachment
	 *            the attachment to be added to an Article
	 */
	public final void addAttachment(final Attachment attachment) {
		attachments.add(attachment);
	}

	/**
	 * Gets the name of the article.
	 * 
	 * @return the name of the article
	 */
	public final String getPathInWiki(final String separator) {
		return namespace + separator + name;
	}

	/**
	 * Gets the body of the article.
	 * 
	 * @return the body of the article
	 */
	public final String getBody() {
		return body;
	}

	/**
	 * Gets the attachments of an article (images, files).
	 * 
	 * @return the attachments (images, files) of an article
	 */
	public final Set<Attachment> getAttachments() {
		return attachments;
	}

	/**
	 * Sets the body of an article.
	 * 
	 * @param body
	 *            the text of the article to be set
	 */
	public final void setBody(final String body) {
		this.body = body.trim();
	}

	public final void convertBody(final PmWiki2MediaWikiConverter converter) {
		body = converter.convertMarkup(body);
	}
}
