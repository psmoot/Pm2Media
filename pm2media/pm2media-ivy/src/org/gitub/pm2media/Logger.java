package org.gitub.pm2media;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Singleton class for logging.  It logs to stdout and to the GUI.
 * 
 * @author smootp
 *
 */
public final class Logger {
	/** Singleton instance of logger. */
	private static final Logger INSTANCE = new Logger();
	
	/** Attribute Set for error messages. */
	private final SimpleAttributeSet errorSet;

	/** Attribute Set for non error messages. */
	private final SimpleAttributeSet defaultSet;

	/** date format for the log. */
	private final SimpleDateFormat dateFormat;
	
	/** Private constructor to prevent anyone from instantiating Logger object. */
	private Logger() {
		errorSet = new SimpleAttributeSet();
		StyleConstants.setForeground(errorSet, Color.RED);
		StyleConstants.setFontFamily(errorSet, "SansSerif");
		StyleConstants.setFontSize(errorSet, 12);

		defaultSet = new SimpleAttributeSet();
		StyleConstants.setForeground(defaultSet, Color.BLACK);
		StyleConstants.setFontFamily(defaultSet, "SansSerif");
		StyleConstants.setFontSize(defaultSet, 12);
		
		dateFormat = new SimpleDateFormat("HH:mm:ss");
	};
	
	/** Phases of operations which might cause errors. */
	public enum Mode { OPEN, PARSE, POST, READ, ERROR, UPLOAD }
	
	/** 
	 * Return singleton instance of logger. 
	 * @return The Logger singleton.
	 */
	public static Logger getInstance() {
		return INSTANCE;
	}
	
	/**
	 * Prints out a logText on the standard output.
	 * 
	 * @param logText
	 *            the text to be logged
	 * @param mode
	 *            the operation which cased the log message
	 */
	public void log(final String logText, final Mode mode) {
		String modeText = "";

		switch (mode) {
			case OPEN:
				modeText = "Opening";
				break;
			case READ:
				modeText = "Reading";
				break;
			case PARSE:
				modeText = "Parsing";
				break;
			case POST:
				modeText = "Posting";
				break;
			case ERROR:
				modeText = "Error";
				break;
			case UPLOAD:
				modeText = "Uploading";
				break;
			default:
				modeText = "";
		}

		if (mode == Mode.ERROR) {
			Logger.getInstance().logError(modeText + " " + logText);
		} else {
			Logger.getInstance().log(modeText + " " + logText);
		}

	}
	
	/**
	 * Prints out a logText on the standard output.
	 * 
	 * @param logText
	 *            the text to be logged
	 */
	public void log(final String logText) {
		Date date = new Date();
		GUI.getInstance().log(dateFormat.format(date) + ": " + logText, defaultSet);
	}

	/**
	 * Prints out a logText on the standard output.
	 * 
	 * @param logText
	 *            the text to be logged
	 */
	public void logError(final String logText) {
		Date date = new Date();
		GUI.getInstance().log(dateFormat.format(date) + ": " + logText, errorSet);
	}

	public void logPage(final WikiPage page, final String filename) {
		logPage(page.getPage(), filename);
	}

	public void logPage(final HtmlPage page, final String filename) {
		try {
			logPage(page.asXml().toString(), filename);
		} catch (Exception e) {
			logError("Failed to log page " + page.getTitleText() + " to file " + filename + ".");
			e.printStackTrace();
		}
	}

	public void logPage(final String text, final String filename) {
		try {
			FileWriter fw = new FileWriter(new File(filename));
			fw.write(text);
			fw.close();
		} catch (Exception e) {
			logError("Failed to log to file " + filename + ".");
			e.printStackTrace();
		}	
	}
}
