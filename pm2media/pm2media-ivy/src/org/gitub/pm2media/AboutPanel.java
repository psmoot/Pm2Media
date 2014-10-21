package org.gitub.pm2media;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * AboutPanel is the class to show who developed the program.
 *
 * @author Johannes Perl
 */
public class AboutPanel extends JPanel {

	private static final long serialVersionUID = 1L; // NOPMD by smootp on 10/21/14 2:21 PM

	private static int fontHeight = 15; // NOPMD by smootp on 10/21/14 2:21 PM
	
	/**
	 * Class constructor.
	 */
	public AboutPanel()	{
		super();
		
		setLayout(new GridBagLayout());

		final JLabel title = new JLabel("Pm2Media");
		title.setFont(new Font("Dialog", Font.BOLD, fontHeight));
		add(title, new GridBagConstraints(0, 0, 2, 1, 0, 0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 10, 0), 0, 0));
		
		final JLabel description = new JLabel("A PmWiki to MediaWiki converter.");
		title.setFont(new Font("Dialog", Font.BOLD, fontHeight));
		add(description, new GridBagConstraints(0, 1, 2, 1, 0, 0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 10, 0), 0, 0));

		final JLabel jung = new JLabel("By Johannes Perl");
		add(jung, new GridBagConstraints(0, 2, 1, 1, 0, 0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 0), 0, 0));
	}
}
