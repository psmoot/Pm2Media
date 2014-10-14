import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PmWiki2MediaWikiConverter is the class to handle conversion of PmWiki syntax
 * to MediaWiki syntax.
 * 
 * @author Johannes Perl
 * 
 */
public class PmWiki2MediaWikiConverter {
	private String imagePrefix;
	
	public String getImagePrefix() {
		return imagePrefix;
	}

	public final PmWiki2MediaWikiConverter withImagePrefix(final String imagePrefix) {
		this.imagePrefix = imagePrefix;
		return this;
	}

	/**
	 * Address of PmWiki.  External links which start with this prefix are converted to links in
	 * the destination wiki.
	 */
	private String sourceWikiPrefix = "";
	
	public String getSourceWikiPrefix() {
		return sourceWikiPrefix;
	}
	
	public PmWiki2MediaWikiConverter withSourceWikiPrefix(final String prefix) {
		/*
		 * What we want is just the "server.com/blah/blah/pmwiki.php" portion.  Trim away
		 * anything else.
		 */
		sourceWikiPrefix = prefix;
		if (sourceWikiPrefix.startsWith("http://")) {
			sourceWikiPrefix = sourceWikiPrefix.replaceFirst("http://", "");
		} else if (sourceWikiPrefix.startsWith("https://")) {
			sourceWikiPrefix = sourceWikiPrefix.replaceFirst("https://", "");
		}
		
		sourceWikiPrefix = sourceWikiPrefix.replaceFirst("pmwiki.php/.*", "pmwiki.php/");
		return this;
	}
	
	/**
	 * Private class constructor.
	 */
	public PmWiki2MediaWikiConverter() {
	}

	private interface SyntaxConversion {
		String convert(final String text);
	};
	
	/**
	 * Converts a text of PmWiki syntax into a text of MediaWiki syntax.
	 * 
	 * @param text
	 *            the text to be converted
	 * @return the converted String
	 */
	public final String convertMarkup(final String text) {
		String newText = replacePmWikiSyntax(text);

		return newText;
	}

	/**
	 * Converts a text from PmWiki syntax to MediaWiki syntax.
	 *
	 * @param text
	 *            the text to be converted
	 * @return the converted text
	 */
	private String replacePmWikiSyntax(final String text) {
		List<SyntaxConversion> conversions = new ArrayList<SyntaxConversion>();
		
		conversions.addAll(Arrays.asList(
				new ReplaceSimpleSyntax(),
				new ReplaceInternalWikiLinks(),
				new ReplaceExternalWikiLinks(),
				new ReplaceSimpleTables(),
				new ReplaceAdvancedTables(),
				new ReplaceCenteredText(),
				new ReplaceDefinitions(),
				new ReplaceFileLinks(),
				new ReplaceHeadings(),
				new ReplaceMonotypeText(),
				new ReplaceRightAlignedText(),
				new ReplaceAttachmentLinks(),
				new ReplaceSource(),
				new RemoveUselessHtmlTags(),
				new ReplaceRedirects(),
				new ReplaceHrefs()
				));

		String convertedText = text;
		for (SyntaxConversion s : conversions) {
			try {
				String newText = s.convert(convertedText);
				convertedText = newText;
			} 
			catch (StackOverflowError e) {
				Logger.getInstance().logPage(text, s.getClass().getName() + "-stack-overflow.txt");
				Logger.getInstance().logError("Stack overflow while executing " + s.getClass().getName() + " conversion.");
			}
			catch (Exception e) {
				Logger.getInstance().logPage(text, s.getClass().getName() + "-pre-conversion.txt");
				Logger.getInstance().logError("Failed while executing " + s.getClass().getName() + " conversion.");
				e.printStackTrace();
			}
		}

		return convertedText;
	}

	private class ReplaceSimpleSyntax implements SyntaxConversion {
		/** array for simple matches. */
		private final SyntaxPair[] syntaxPairs = { 
				new SyntaxPair("\\\\\\", "\n\n\n\n"),
				new SyntaxPair("\\\\\n", "\n\n"), 
				new SyntaxPair("[[<<]]", "\n"),
				new SyntaxPair("\\\\ ", "<br />"), 
				new SyntaxPair("'^", "<sup>"),
				new SyntaxPair("^'", "</sup>"), 
				new SyntaxPair("'_", "<sub>"),
				new SyntaxPair("_'", "</sub>"), 
				new SyntaxPair("{+", "<u>"),
				new SyntaxPair("+}", "</u>"), 
				new SyntaxPair("{-", "<s>"),
				new SyntaxPair("-}", "</s>"), 
				new SyntaxPair("[+", "<big>"),
				new SyntaxPair("+]", "</big>"), 
				new SyntaxPair("[-", "<small>"),
				new SyntaxPair("-]", "</small>"), 
				new SyntaxPair("'+", "<big>"),
				new SyntaxPair("+'", "</big>"), 
				new SyntaxPair("\n'-", "<small>"),
				new SyntaxPair("-'", "</small>"),
				new SyntaxPair("(:toc:)", "__TOC__"),
				new SyntaxPair("[=", "<nowiki>"),
				new SyntaxPair("=]", "</nowiki>"), 
				new SyntaxPair("[@", "<pre>"),
				new SyntaxPair("@]", "</pre>"), 
				new SyntaxPair("->", ":"),
				new SyntaxPair("-->", "::"), 
				new SyntaxPair("%newwin%", ""),
				new SyntaxPair("[:randquote:]", "") };
		
		public String convert(final String text)  {
			
			String convertedText = text;
			for (SyntaxPair sp : syntaxPairs) {
				String newText = replaceAll(convertedText, sp.getPmWSyntax(), sp.getMWSyntax());
				convertedText = newText;
			}
			return convertedText;
		}
	}

	/**
	 * Converts all advanced tables of PmWiki to tables of MediaWiki.
	 * 
	 * @param text
	 *            the text to convert
	 * @return the converted text
	 */
	private class ReplaceAdvancedTables implements SyntaxConversion {
		public String convert(final String text) {
			
			String convertedText = text;
			/* pattern matching advanced table start */
			Pattern advtableStart = Pattern.compile("\\(:table (.*?):\\)\n");
			Matcher matcher = advtableStart.matcher(convertedText);
			while (matcher.find()) {
				String newText = replaceFirstQuoted(matcher, "\n{|" + matcher.group(1) + "\n");
				convertedText = newText;
				matcher = advtableStart.matcher(convertedText);
			}

			/* pattern matching table line end */
			Pattern advtableLineEnd = Pattern
					.compile("\n\\(:cellnr(.*?):\\)\\s{0,}(.*?)\n");
			matcher = advtableLineEnd.matcher(convertedText);
			while (matcher.find()) {
				String newText = replaceFirstQuoted(matcher, "\n |-\n \\|" + matcher.group(2) + "\n");
				convertedText = newText;
				matcher = advtableLineEnd.matcher(convertedText);
			}

			/* pattern matching table cell */
			Pattern advtableCell = Pattern.compile("\n\\(:cell(.*?):\\)");
			matcher = advtableCell.matcher(convertedText);
			while (matcher.find()) {
				String newText = replaceFirstQuoted(matcher, "\n |");
				convertedText = newText;
				matcher = advtableCell.matcher(convertedText);
			}

			/* pattern matching table end */
			Pattern advtableEnd = Pattern.compile("\n\\(:tableend:\\)");
			matcher = advtableEnd.matcher(convertedText);
			while (matcher.find()) {
				String newText = replaceFirstQuoted(matcher, "\n |}\n\n");
				convertedText = newText;
				matcher = advtableEnd.matcher(convertedText);
			}

			return convertedText;
		}
	}

	/**
	 * Converts all attachment links of PmWiki to attachment links of
	 * MediaWiki.<br /><br />
	 *
	 * <b>Conversion:</b><br />
	 * Attach:image.jpeg => [[Image:image.jpeg]]
	 * 
	 * @param text
	 *            the text to convert
	 * @return the converted text
	 */
	private class ReplaceAttachmentLinks implements SyntaxConversion {
		public String convert(final String text) {
			/** regex matching pictures. */
			Pattern imgAttachement = Pattern
					.compile("Attach:(.*?)\\.(?i)(jpeg|jpg|gif|png)");

			String convertedText = text;
			Matcher matcher = imgAttachement.matcher(convertedText);

			while (matcher.find()) {
				String newText = replaceFirstQuoted(matcher, "[[" + imagePrefix + ":" + matcher.group(1).trim()
						+ "." + matcher.group(2) + "]]");
				convertedText = newText;
				matcher = imgAttachement.matcher(convertedText);
			}

			/** regex matching files */
			Pattern fileAttachement = Pattern
					.compile("\\[\\[Attach:([^\\.]*?)\\.([\\w]{3,4})(.*?)\\]\\]");

			matcher = fileAttachement.matcher(convertedText);

			while (matcher.find()) {
				String newText = replaceFirstQuoted(matcher, "[[File:" + matcher.group(1).trim()
						+ "." + matcher.group(2).trim() + " " + matcher.group(3)
						+ "]]");
				convertedText = newText;
				matcher = fileAttachement.matcher(convertedText);
			}

			/** regex matching files */
			Pattern fileAttachement2 = Pattern
					.compile("Attach:([^\\.]*?)\\.([\\w]{3,4})");

			matcher = fileAttachement2.matcher(convertedText);

			while (matcher.find()) {
				String newText = replaceFirstQuoted(matcher, "[[File:" + matcher.group(1).trim()
						+ "." + matcher.group(2) + "]]");
				convertedText = newText;
				matcher = fileAttachement2.matcher(convertedText);
			}

			return convertedText;
		}
	}

	/**
	 * Converts all centered texts of PmWiki to centered texts of MediaWiki.<br /><br />
	 * 
	 * <b>Conversion:</b><br />
	 * %center% text => <center>text</center>
	 * 
	 * @param text
	 *            the text to convert
	 * @return the converted text
	 */
	private class ReplaceCenteredText implements SyntaxConversion {
		public String convert(final String text) {
			/** regex matching centered text */
			Pattern centerPattern = Pattern.compile("%center%(.*?)\n");

			String convertedText = text;
			Matcher matcher = centerPattern.matcher(convertedText);

			while (matcher.find()) {
				String newText = replaceFirstQuoted(matcher, "<center>" + matcher.group(1)
						+ "</center>");
				convertedText = newText;
				matcher = centerPattern.matcher(convertedText);
			}

			return convertedText;
		}
	}
	
	/**
	 * Converts page redirects.  The entire page should consist of nothing but the redirect code.
	 * 
	 * <b>Conversion:</b><br />
	 * (:redirect new/page/name:) => #REDIRECT new/page/name
	 * 
	 * @param text
	 *            the text to convert
	 * @return the converted text
	 */
	private class ReplaceRedirects implements SyntaxConversion {
		public String convert(final String text) {
			Matcher matcher = Pattern.compile("\\(:redirect\\s+(.*?):\\)", Pattern.DOTALL).matcher(text);

			if (matcher.find()) {
				return "#REDIRECT [[" + matcher.group(1) + "]]";
			} else
				return text;
		}
	}
	
	/**
	 * Replace <a href="...">text</a> patterns with [link|text].  Also replace just
	 * plain <a href="..." /> with [link].
	 *
	 * To avoid problems with deciding where the link ends, square brackets in the link text are
	 * wrapped in <wiki>[</nowiki> tags.
	 */
	private class ReplaceHrefs implements SyntaxConversion {
		private String wrapBrackets(final String text) {
			String convertedText = replaceAll(text, "[", "<nowiki>[</nowiki>");
			return replaceAll(convertedText, "]", "<nowiki>]</nowiki>");
		}
		
		/**
		 * If a href refers to the old pmwiki address, shorten it to an internal page
		 * reference in the mediawiki.
		 * 
		 * @param 	href 	Original address (http://www.pmwiki.org/xxx)
		 * @return 	Either original address or internal address within mediawiki
		 */
		private String pmwikiToMediawikiHrefs(final String href) {
			String newRef = href;
			
			if (href.startsWith("http://" + sourceWikiPrefix)) {
				newRef = newRef.replaceFirst("http://" + sourceWikiPrefix, "");
			} else if (href.startsWith("https://" + sourceWikiPrefix)) {
				newRef = newRef.replaceFirst("https://" + sourceWikiPrefix, "");
			}
			
			return newRef;
		}
		
		/**
		 * Return true if a reference is an internal reference and thus needs to use double
		 * square brackets.
		 * 
		 * @param href
		 * @return True if reference needs double brackets, false if single brackets are fine.
		 */
		private boolean needsSingleBrackets(final String href) {
			return href.startsWith("http://") || href.startsWith("https://");
		}

		/**
		 * Return either single or double bracket depending on whether reference is an internal
		 * or external link.
		 * @param 	href 	Reference we're trying to wrap.
		 * @param 	bracketChar 	Either [ or ].
		 * @return 	Either 	single or double bracketChar as appropriate.
		 */
		private String bracketForHref(final String href, String bracketChar) {
			if (needsSingleBrackets(href)) {
				return bracketChar;
			} else {
				return bracketChar + bracketChar;
			}
		}
		
		public String convert(final String text) {
			Pattern hrefPattern = Pattern.compile("<a\\s+href=\"(.*?)\"\\s*?>(.*?)</a\\s*?>", 
					Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
			Matcher matcher = hrefPattern.matcher(text);
			
			String convertedText = text;
			while (matcher.find()) {
				final String href = pmwikiToMediawikiHrefs(matcher.group(1));

				convertedText = matcher.replaceFirst(bracketForHref(href, "[") + href + "|" + wrapBrackets(matcher.group(2)) + bracketForHref(href, "]"));
				matcher = hrefPattern.matcher(convertedText);
			}
			
			hrefPattern = Pattern.compile("<a\\s+href=\"(.+?)\"\\s*?/>", 
					Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
			matcher = hrefPattern.matcher(convertedText);
			while (matcher.find()) {
				final String href = pmwikiToMediawikiHrefs(matcher.group(1));
				convertedText = matcher.replaceFirst(bracketForHref(href, "[") + href + bracketForHref(href, "]"));
			}
			
			return convertedText;
		}
	}
	
	/**
	 * Converts all definitions of PmWiki to definitions of MediaWiki.<br /><br />
	 * 
	 * <b>Conversion:</b><br />
	 * :item:definition => ;item:definition
	 * 
	 * @param text
	 *            the text to convert
	 * @return the converted text
	 */
	private class ReplaceDefinitions implements SyntaxConversion {
		public String convert(final String text) {
			String convertedText = text;
			/* pattern matching definition list: :item : definition */
			Pattern definition = Pattern.compile("(\n|^):(.*?):(.*?)\n");
			Matcher matcher = definition.matcher(convertedText);

			while (matcher.find()) {
				String start;
				if (matcher.group(1) != null) {
					start = matcher.group(1);
				} else {
					start = "";
				}
				String newText = replaceFirstQuoted(matcher, start + ";" + matcher.group(2).trim()
						+ ":" + matcher.group(3).trim() + "\n");
				convertedText = newText;
				matcher = definition.matcher(convertedText);
			}

			return text;
		}
	}

	/**
	 * Converts all file links of PmWiki to file links of MediaWiki.<br /><br />
	 * 
	 * <b>Conversion:</b><br />
	 * [[file://c:/windows/ | windows]] | [[file:\\c:/windows/ | windows]] =>
	 * [file://c:/windows/ windows]
	 * 
	 * @param text
	 *            the text to convert
	 * @return the converted text
	 */
	private class ReplaceFileLinks implements SyntaxConversion {
		public String convert(final String text) {
			String convertedText = text.replaceAll("file:\\\\\\\\", "file:\\/\\/");

			Pattern fileLink = Pattern
					.compile("\\[\\[\\s{0,1}file:\\/\\/(.*?)[\\|](.*?)\\]\\]");
			Matcher matcher = fileLink.matcher(convertedText);

			while (matcher.find()) {
				String url = matcher.group(1).replaceAll("\\\\", "/");
				String newText = replaceFirstQuoted(matcher, "[File://" + url.trim() + " "
						+ matcher.group(2).trim() + "]");
				convertedText = newText;
				matcher = fileLink.matcher(convertedText);
			}

			Pattern fileLink2 = Pattern
					.compile("\\[\\[\\s{0,1}file:\\/\\/(.*?)\\]\\]");
			matcher = fileLink2.matcher(convertedText);

			while (matcher.find()) {
				String url = matcher.group(1).replaceAll("\\\\", "/").trim();
				String newText = replaceFirstQuoted(matcher, "[File://" + url + "]");
				convertedText = newText;
				matcher = fileLink2.matcher(convertedText);
			}

			return convertedText;
		}
	}

	/**
	 * Converts all headings of PmWiki to headings of MediaWiki.<br /><br />
	 * 
	 * <b>Conversion:</b><br />
	 * !heading => == heading ==
	 * 
	 * @param text
	 *            the text to convert
	 * @return the converted text
	 */
	private class ReplaceHeadings implements SyntaxConversion {
		public String convert(final String text) {
			String convertedText = text;
			
			/* Patterns matching headings: !heading1, !!heading2, ... */
			Pattern[] heading = new Pattern[5];
			for (int i = 0; i < 5; i++) {
				heading[i] = Pattern.compile("(\n|^)!{" + (i + 1)
						+ "}\\s{0,}(.*?)\\s{0,}\n");
			}

			String mWHeadingSyntax = "=====";
			for (int i = heading.length - 1; i >= 0; i--) {
				if (i > 1) {
					mWHeadingSyntax = mWHeadingSyntax.substring(1);
				}

				Matcher matcher = heading[i].matcher(convertedText);
				while (matcher.find()) {
					String newText = replaceFirstQuoted(matcher, matcher.group(1) + mWHeadingSyntax
							+ " " + matcher.group(2) + " " + mWHeadingSyntax
							+ "\n\n");
					convertedText = newText;
					matcher = heading[i].matcher(convertedText);
				}
			}

			return convertedText;
		}
	}

	/**
	 * Converts all monotype texts of PmWiki to truetype texts of MediaWiki.<br /><br />
	 * 
	 * <b>Conversion:</b><br />@@monotype text@@ => <tt>truetype text</tt>
	 * 
	 * @param text
	 *            the text to convert
	 * @return the converted text
	 */
	private class ReplaceMonotypeText implements SyntaxConversion {
		/**
		 * Converts "@@text@@ ot <tt>text</tt>.
		 * 
		 * @param text Original text.
		 * 
		 * @return New string containing converted text.
		 */
		public String convert(final String text) {
			/** RegEx matching monotype font */
			//			final Pattern monotypePattern = Pattern.compile("@@(.*?){1,}@@",
			//					Pattern.DOTALL);
			final Pattern monotypePattern = Pattern.compile("@@(.*?)@@", Pattern.DOTALL);

			String convertedText = text;

			try {
				while (true) {
					// replacing monotype syntax
					Matcher matcher;
					matcher = monotypePattern.matcher(convertedText);

					boolean foundOne = false;
					foundOne = matcher.find();
					if (! foundOne) {
						break;
					}

					String newText;
					newText = replaceFirstQuoted(matcher,
							"<tt>" + matcher.group(1) + "</tt>");
					convertedText = newText;
				}
			} catch (StackOverflowError e) {
				Logger.getInstance().logError("Whoops, didn't catch stack overflow exception.");
				e.printStackTrace();
			}

			return convertedText;
		}
	}
	
	/**
	 * Converts all right aligned texts of PmWiki to right aligned texts of
	 * MediaWiki.<br /><br />
	 * 
	 * <b>Conversion:</b><br />
	 * %right% text => &lt;div align="right"&gt;text&lt;/div&gt;
	 * 
	 * @param text
	 *            the text to convert
	 * @return the converted text
	 */
	private class ReplaceRightAlignedText implements SyntaxConversion {
		public String convert(final String text) {
			String convertedText = text;
			
			/** regex matching right aligned text */
			Pattern right = Pattern.compile("%right%(.*?)\n");

			Matcher matcher = right.matcher(convertedText);

			while (matcher.find()) {
				String newText = replaceFirstQuoted(matcher, "<div align=\"right\">"
						+ matcher.group(1) + "</div>");
				convertedText = newText;
				matcher = right.matcher(convertedText);
			}

			return convertedText;
		}
	}

	/**
	 * Converts all simple tables of PmWiki to tables of MediaWiki.<br /><br />
	 * 
	 * @param text
	 *            the text to convert
	 * @return the converted text
	 */
	private class ReplaceSimpleTables implements SyntaxConversion {
		public String convert(final String text) {
			String convertedText = text;
			
			/** string containing elements of PmWiki syntax for regexp */
			final String pmWikiSyntaxElements = "!\\-':\\(\\%\\[\\{\\#\\*\\\\";

			/* pattern matching table start */
			Pattern tableStart = Pattern
					.compile("\\|\\|\\s{0,}(((border\\s{0,}=\\s{0,}\\d)|(align\\s{0,}=\\s{0,}[\\w])|(width\\s{0,}=\\s{0,}\\d\\s{0,}%)|(colspan\\s{0,}=\\s{0,}\\d\\s{0,}%)).*?\n)");
			Matcher matcher = tableStart.matcher(convertedText);
			while (matcher.find()) {
				String newText = replaceFirstQuoted(matcher, "\n{|" + matcher.group(1));
				convertedText = newText;
				matcher = tableStart.matcher(convertedText);
			}

			/* pattern matching table line end */
			Pattern tableLineEnd = Pattern.compile("\\|\\|\\s{0,1}\n");
			matcher = tableLineEnd.matcher(convertedText);
			while (matcher.find()) {
				String newText = replaceFirstQuoted(matcher, "\n |-\n");
				convertedText = newText;
				matcher = tableLineEnd.matcher(convertedText);
			}

			/* pattern matching table cell */
			Pattern tableCell = Pattern.compile("\n{0,}(\\|\\|)\\s{0,}([\\w"
					+ pmWikiSyntaxElements + "])|\n{0,}(\\|\\|)\\s{1}");
			matcher = tableCell.matcher(convertedText);
			String cellText = "";
			while (matcher.find()) {
				cellText = matcher.group(2);
				if (matcher.group(2) == null) {
					cellText = "";
				}

				String newText = replaceFirstQuoted(matcher, "\n |" + cellText);
				convertedText = newText;
				matcher = tableCell.matcher(convertedText);
			}

			/* pattern matching table end */
			Pattern tableEnd = Pattern.compile("\\|\\-(\n{0,}\\s{0,}[\\w"
					+ pmWikiSyntaxElements + "]|$|\n\\{\\|)");
			matcher = tableEnd.matcher(convertedText);
			while (matcher.find()) {
				String newText = replaceFirstQuoted(matcher, "|}\n\n" + matcher.group(1));
				convertedText = newText;
				matcher = tableEnd.matcher(convertedText);
			}

			/* pattern matching head cells */
			Pattern headerCell = Pattern.compile("\n \\|!(.*?)(\n|$)");
			matcher = headerCell.matcher(convertedText);
			while (matcher.find()) {
				String newText = replaceFirstQuoted(matcher, "\n |'''" + matcher.group(1).trim()	+ "'''\n");
				convertedText = newText;
				matcher = headerCell.matcher(convertedText);
			}

			/* pattern matching %center% or %right% within table */
			Pattern alignTable = Pattern.compile(
					"\\{\\|(.*?)%(center|right)%(.*?)\n \\|\\}", Pattern.MULTILINE);
			matcher = alignTable.matcher(convertedText);
			while (matcher.find()) {
				String newText = replaceFirstQuoted(matcher, "<div align=\"" + matcher.group(2)
						+ "\">\n{|\n" + matcher.group(1) + "" + matcher.group(3)
						+ "\n |}\n</div>");
				convertedText = newText;
				matcher = alignTable.matcher(convertedText);
			}

			return convertedText;
		}
	}

	/**
	 * Converts all definitions of PmWiki to definitions of MediaWiki.<br /><br />
	 * 
	 * <b>Conversion:</b><br />
	 * (:source lang=c:)code(:source:) => &lt;source lang="c"&gt;code&lt;/source&gt;
	 * 
	 * @param text
	 *            the text to convert
	 * @return the converted text
	 */
	private class ReplaceSource implements SyntaxConversion {
		public String convert(final String text) {
			String convertedText = text;
			
			/** RegEx matching source tag */
			final Pattern sourcePattern = Pattern.compile(
					"\\(:source lang=([a-z0-9]{1,}):\\)(.*?)(:source:)",
					Pattern.MULTILINE);

			// replacing source start tags
			Matcher matcher = sourcePattern.matcher(convertedText);

			while (matcher.find()) {
				String newText = replaceFirstQuoted(matcher, "<source lang=\"" + matcher.group(1)
						+ "\">" + matcher.group(2) + "</source>");
				convertedText = newText;
				matcher = sourcePattern.matcher(convertedText);
			}

			// replacing <br /> in source
			Pattern sourceBRPattern = Pattern.compile(
					"<source lang=\"(.*?)\">(.*?)(<br />)(.*?)</source>",
					Pattern.MULTILINE);
			matcher = sourceBRPattern.matcher(convertedText);

			while (matcher.find()) {
				String newText = replaceFirstQuoted(matcher, "<source lang=\"" + matcher.group(1) + "\">"
						+ matcher.group(2) + matcher.group(4) + "</source>");
				convertedText = newText;
				matcher = sourceBRPattern.matcher(convertedText);
			}

			return convertedText;
		}
	}
	
	/**
	 * Converts all internal wikilinks of PmWiki to internal wikilinks of
	 * MediaWiki.<br /><br />
	 * 
	 * <b>Conversion:</b><br />
	 * [[Group Page]] => [[Page]]
	 * 
	 * @param text
	 *            the text to convert
	 * @return the converted text
	 */
	private class ReplaceInternalWikiLinks implements SyntaxConversion {
		public String convert(final String text) {
			String convertedText = text;
			/** RegEx matching internal wiki links [[Wikilink]] */
			Pattern internalWikiLink = Pattern
					.compile("\\[\\[([\\w]{1,})[|]([\\w]{1,})\\]\\]");

			Matcher matcher = internalWikiLink.matcher(convertedText);

			while (matcher.find()) {
				String newText = replaceFirstQuoted(matcher, "[[" + matcher.group(1) + " "
						+ matcher.group(2) + "]]");
				convertedText = newText;
				matcher = internalWikiLink.matcher(convertedText);
			}

			/**
			 * regex matching internal wiki links [[Namespace.Link]]
			 * [[Namespace/Link]]
			 */
			Pattern internalWikiLink2 = Pattern
					.compile("\\[\\[([\\w]{1,})[\\.|\\/]([\\w\\| ]{1,})\\]\\]");

			matcher = internalWikiLink2.matcher(convertedText);

			while (matcher.find()) {
				String newText = replaceFirstQuoted(matcher, "[[" + matcher.group(1) + "/"
						+ matcher.group(2) + "]]");
				convertedText = newText;
				matcher = internalWikiLink2.matcher(convertedText);
			}

			return convertedText;
		}
	}
	
	/**
	 * Converts all external wikilinks of PmWiki to external wikilinks of
	 * MediaWiki.<br /><br />
	 * 
	 * <b>Conversion:</b><br />
	 * [[http://www.link.com/ | Linktext]] => [http://www.link.com/ Linktext]<br />
	 * [[mailto:address@domain.com]] => [mailto:address@domain.com]
	 * 
	 * @param text
	 *            the text to convert
	 * @return the converted text
	 */
	private class ReplaceExternalWikiLinks implements SyntaxConversion {
		public String convert(final String text) {
			String convertedText = text;
			
			/** RegEx matching external wiki links */
			Pattern externalWikiLink = Pattern
					.compile("\\[\\[(https{0,1}:\\/\\/(.*?))\\s{0,}\\|\\s{0,}(.*?)\\]\\]");

			Matcher matcher = externalWikiLink.matcher(convertedText);

			while (matcher.find()) {
				String newText = replaceFirstQuoted(matcher, "[" + matcher.group(1) + " "
						+ matcher.group(3) + "]");
				convertedText = newText;
				matcher = externalWikiLink.matcher(convertedText);
			}

			/** RegEx matching mailto links */
			Pattern mailtoLink = Pattern.compile("\\[\\[(mailto:.*?)\\]\\]");

			matcher = mailtoLink.matcher(convertedText);

			while (matcher.find()) {
				String newText = replaceFirstQuoted(matcher, "[" + matcher.group(1) + "]");
				convertedText = newText;
				matcher = mailtoLink.matcher(convertedText);
			}

			return convertedText;
		}
	}
	
	/**
	 * Replace HTML tags inserted by Microsoft Word.
	 * 
	 * Examples are the <st1:...> and <o:...> tags.  They have different stuff after the 
	 * colon so we have to match only up to there.
	 * 
	 * @author smootp
	 *
	 */
	private class RemoveUselessHtmlTags implements SyntaxConversion {
		public String convert(final String text) {
			final List<String> prefixes = Arrays.asList("st1", "o");
			String convertedText = text;
			
			for (String tagPrefix : prefixes) {
				Matcher matcher = Pattern.compile("</??" + tagPrefix + ":.+?>").matcher(convertedText);
				String newText = matcher.replaceAll("");
				convertedText = newText;
			}

			return convertedText;
		}
	}
	
	/**
	 * Replaces a String through a specified replacement.
	 * 
	 * @param original
	 *            the string in which the replacements shall be done
	 * @param search
	 *            the string to be replaced
	 * @param replace
	 *            the replacement
	 * @return String with all occurrences of search string replaced.
	 */
	private String replaceAll(final String original, final String search, final String replace) {
		Pattern pattern = Pattern.compile(search, 
				java.util.regex.Pattern.LITERAL + java.util.regex.Pattern.CASE_INSENSITIVE + java.util.regex.Pattern.UNICODE_CASE);
		Matcher matcher = pattern.matcher(original);
		String newText = matcher.replaceAll(replace);

		return newText;
	}
	
	private String replaceFirstQuoted(final Matcher matcher, final String replacement) {
		return matcher.replaceFirst(Matcher.quoteReplacement(replacement));
	}
}
