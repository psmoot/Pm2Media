/**
 * Attachment is the class to save files or images being on an article page in
 * the Article class.
 * 
 * @author Johannes Perl
 * 
 */
public class Attachment {

	private String fileName;

	/** URL to file on source wiki. */
	private String fileURL;

	/**
	 * Class constructor specifying file and filename.
	 * 
	 * @param fileName
	 *            name of the file
	 * @param fileURL
	 *            the filepath
	 */
	public Attachment(final String fileName, final String fileURL) {
		this.fileName = fileName;
		this.fileURL = fileURL;
	}

	/**
	 * Returns the name of the file.
	 * 
	 * @return the name of the file
	 */
	public final String getFileName() {
		return fileName;
	}

	/**
	 * Returns the filepath.
	 * 
	 * @return the path of the file
	 */
	public final String getFileURL() {
		return fileURL;
	}

	/**
	 * Sets the name of the file.
	 * 
	 * @param fileName
	 *            the name of the file to be set
	 */
	public final void setFileName(final String fileName) {
		this.fileName = fileName;
	}

	/**
	 * Sets the file path.
	 * 
	 * @param fileURL
	 *            the file path to be set
	 */
	public final void setFileURL(final String fileURL) {
		this.fileURL = fileURL;
	}
}
