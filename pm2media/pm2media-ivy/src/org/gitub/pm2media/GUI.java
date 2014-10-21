package org.gitub.pm2media;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;

/**
 * GUI is the class to be able to use pm2media with a graphical user interface.
 * 
 * @author Johannes Perl
 * 
 */
public final class GUI extends JFrame implements Runnable {
	private static GUI INSTANCE = new GUI();

	public static GUI getInstance() {
		return INSTANCE;
	}

	/** serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** text pane to show log messages. */
	private JTextPane logPane;

	/** scroll pane for log pane. */
	private JScrollPane logScrollPane;

	/** URL to PmWiki. */
	private JTextField txtPmWURL;

	/** main namespace of source wiki. */
	private JTextField txtPmWMain;

	private JTextField txtPmUsername;
	private JPasswordField txtPmPassword;

	/** URL to MediaWiki. */
	private JTextField txtMWURL;

	/** username of MediaWiki user posting content. */
	private JTextField txtMWUsername;

	/** password field for MediaWiki user. */
	private JPasswordField txtMWPassword;

	/**
	 * the Name of the MediaWiki login page (e.g. Special:Userlogin for English
	 * MediaWiki, Spezial:Anmelden for German MediaWiki)
	 */
	private JTextField txtMWLoginPage;

	/**
	 * the Name of the MediaWiki upload page (e.g. Special:Upload for English
	 * MediaWiki, Spezial:Hochladen for German MediaWiki)
	 */
	private JTextField txtMWUploadPage;

	/**
	 * the prefix of images in Wikitext (e.g. Image for English, Bild for
	 * German)
	 */
	private JTextField txtMWImagePrefix;

	private JTextField txtPmIndexPageName;

	public static void main(String[] args) {
		GUI.getInstance().setVisible(true);
		GUI.getInstance().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	/**
	 * GUI to be able to use converter without changing source code
	 * @throws IOException 
	 */
	private GUI() {
		setTitle("PmWiki2MediaWiki Converter");
		setSize(new Dimension(580, 678));

		GridBagLayout gridBagLayout = new GridBagLayout();
		getContentPane().setLayout(gridBagLayout);

		createMenuBar();

		int panelRow = 0;
		JPanel panelPmW = createPanel(panelRow++);
		addNameToPanel(panelPmW, "pmwiki.label");

		int row = 1;
		txtPmWURL = addInputToPanel(panelPmW, row++, "pmwiki.url");
		txtPmWMain = addInputToPanel(panelPmW, row++, "pmwiki.mainNamespace");
		txtPmIndexPageName = addInputToPanel(panelPmW, row++, "pmwiki.indexPageName");
		txtPmUsername = addInputToPanel(panelPmW, row++, "pmwiki.username");
		txtPmPassword = addPasswordInputToPanel(panelPmW, row++, "pmwiki.password");

		JPanel panelMW = createPanel(panelRow++); 
		addNameToPanel(panelMW, "MediaWiki");
		
		row = 1;
		txtMWURL = addInputToPanel(panelMW, row++, "mediawiki.url");
		txtMWLoginPage = addInputToPanel(panelMW, row++, "mediawiki.loginPage");
		txtMWUploadPage = addInputToPanel(panelMW, row++, "mediawiki.uploadPage");
		txtMWImagePrefix = addInputToPanel(panelMW, row++, "mediawiki.imagePrefix");
		txtMWUsername = addInputToPanel(panelMW, row++, "mediawiki.username");
		txtMWPassword = addPasswordInputToPanel(panelMW, row++, "mediawiki.password");

		addConvertButton(panelRow++);

		addLogPane(panelRow++);
	}

	private void addConvertButton(int row) {
		JButton convertButton = new JButton("Convert");
		convertButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(final ActionEvent e) {
				Thread thread = new Thread(GUI.this);
				thread.start();
			}

		});

		getContentPane().add(convertButton, new GridBagConstraints(0, row, 1, 1, 0, 0,
				GridBagConstraints.CENTER, 0, new Insets(0, 0, 0, 0), 1, 1));
	}

	private void addLogPane(final int row) {
		logPane = new JTextPane();
		logPane.setEditable(false);
		logScrollPane = new JScrollPane(logPane);
		logScrollPane.setPreferredSize(new Dimension(550, 200));
		logScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createBevelBorder(BevelBorder.RAISED, Color.GRAY, Color.WHITE,
						Color.GRAY, Color.WHITE), "LogArea", TitledBorder.LEFT,
						TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 12),
						Color.BLACK));
		getContentPane().add(logScrollPane, new GridBagConstraints(0, row, 1, 1, 0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(20, 0, 0, 0), 1, 1));
	}

	
private JPasswordField addPasswordInputToPanel(JPanel panel, final int row, final String inputNameProperty) {
	JPasswordField passwordField = new JPasswordField(Pm2MediaPrefs.getProperty(inputNameProperty, "")); 
	addInputControlToPanel(panel, row, Pm2MediaPrefs.getProperty(inputNameProperty + ".label"), passwordField);
	return passwordField;
}

private JTextField addInputToPanel(JPanel panel, final int row, final String inputNameProperty) {
	JTextField inputField = new JTextField(Pm2MediaPrefs.getProperty(inputNameProperty));
	addInputControlToPanel(panel, row, Pm2MediaPrefs.getProperty(inputNameProperty + ".label"), inputField);
	return inputField;
}

private void addInputControlToPanel(JPanel panel, final int row, final String inputName, final JTextComponent inputControl) {
		JLabel label = new JLabel(inputName);
		panel.add(label, new GridBagConstraints(1, row, 1, 1, 0, 0,
				GridBagConstraints.WEST, 0, new Insets(0, 0, 5, 5), 1, 1));

		inputControl.setPreferredSize(new Dimension(350, 20));
		panel.add(inputControl, new GridBagConstraints(2, row, 1, 1, 0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 20), 1, 1));
}

	private JLabel addNameToPanel(JPanel panel, final String panelNameProperty) {
		JLabel label = new JLabel(Pm2MediaPrefs.getProperty(panelNameProperty));
		label.setFont(new Font("SansSerif", Font.BOLD, 17));
		panel.add(label, new GridBagConstraints(1, 0, 1, 1, 0, 0,
				GridBagConstraints.WEST, 0, new Insets(0, 0, 5, 5), 1, 1));
		return label;
	}

	private JPanel createPanel(final int row) {
		JPanel panelPmW = new JPanel();
		GridBagLayout gbl_panelPmW = new GridBagLayout();
		gbl_panelPmW.rowHeights = new int[]{15, 0, 0, 0, 0};
		gbl_panelPmW.columnWidths = new int[] {30, 150, 350};
		panelPmW.setLayout(gbl_panelPmW);
		getContentPane().add(panelPmW, new GridBagConstraints(0, row, 1, 1, 0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		return panelPmW;
	}

	private void createMenuBar() {
		JMenuBar menubar = new JMenuBar();
		JMenu menuHelp = new JMenu("Help");
		menubar.add(menuHelp);
		JMenuItem about = new JMenuItem("About");
		about.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(final ActionEvent event) {
				JOptionPane.showMessageDialog(GUI.this, new AboutPanel(),
						"pm2media - About", JOptionPane.PLAIN_MESSAGE);
			}
		});

		menuHelp.add(about);
		setJMenuBar(menubar);
	}

	/**
	 * Starts the conversion. Is called when convert button is clicked.
	 */
	@Override
	public void run() {
		try {
			Pm2Media pm2Media = new Pm2Media();

			pm2Media.setProperties(txtPmWURL.getText(),
					txtPmUsername.getText(),
					new String(txtPmPassword.getPassword()),
					txtPmWMain.getText(), 
					txtPmIndexPageName.getText(),
					txtMWURL.getText(),
					txtMWUsername.getText(), 
					new String(txtMWPassword.getPassword()),
					txtMWLoginPage.getText(), 
					txtMWUploadPage.getText(),
					txtMWImagePrefix.getText(), this);
			pm2Media.convert();
		} catch (Exception e) {
			Logger.getInstance().logError("Exception while converting wiki: " + e.getLocalizedMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Shows a logText in the logPane.
	 * 
	 * @param logText
	 *            the text to be logged
	 * @param set
	 *            the SimpleAttributeSet defining font face, font color and font
	 *            size
	 */
	public void log(final String logText, final SimpleAttributeSet set) {
		try {
			logPane.getDocument().insertString(
					logPane.getDocument().getLength(), logText.concat("\n"),
					set);
			logScrollPane.repaint();
			// setting cursors position to the end of the logPane
			try {
				logPane.setCaretPosition(logPane.getText().length() - 1);
			}
			catch (IllegalArgumentException e) {
				logPane.setCaretPosition(logPane.getText().replace("\n", "")
						.length());
			}
			this.repaint();
		}
		catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
}
