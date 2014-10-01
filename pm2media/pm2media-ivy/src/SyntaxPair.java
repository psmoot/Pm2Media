/**
 * SyntaxPair is the class representing a pair of equivalent PmWiki and
 * MediaWiki syntax.
 * 
 * @author Johannes Perl
 * 
 */
public class SyntaxPair {

	/** the PmWiki syntax */
	private String pmWSyntax;

	/** the MediaWiki syntax */
	private String mWSyntax;

	/**
	 * Class constructor specifying PmWiki and MediaWiki syntax for a specific
	 * format element.
	 * 
	 * @param pmWSyntax
	 *            the PmWiki syntax for a specific format element
	 * @param mWSyntax
	 *            the MediaWiki syntax for a specific format element
	 */
	public SyntaxPair(final String pmWSyntax, String mWSyntax) {
		this.pmWSyntax = pmWSyntax;
		this.mWSyntax = mWSyntax;
	}

	/**
	 * Returns the PmWiki syntax for a specific format element.
	 * 
	 * @return the PmWiki syntax for a specific format element.
	 */
	public final String getPmWSyntax() {
		return pmWSyntax;
	}

	/**
	 * Sets the PmWiki syntax for a specific format element.
	 * 
	 * @param pmWSyntax
	 *            the PmWiki syntax for a specific format element
	 */
	public final void setPmWSyntax(final String pmWSyntax) {
		this.pmWSyntax = pmWSyntax;
	}

	/**
	 * Returns the MediaWiki syntax for a specific format element.
	 * 
	 * @return the MediaWiki syntax for a specific format element.
	 */
	public final String getMWSyntax() {
		return mWSyntax;
	}

	/**
	 * Sets the MediaWiki syntax for a specific format element.
	 * 
	 * @param syntax
	 *            the MediaWiki syntax for a specific format element
	 */
	public final void setMWSyntax(final String syntax) {
		mWSyntax = syntax;
	}
}
