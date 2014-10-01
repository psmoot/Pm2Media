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

	private static final long serialVersionUID = 1L;

	private final int fontHeight = 15;
	
	/**
	 * Class constructor.
	 */
	public AboutPanel()	{
		setLayout(new GridBagLayout());

		JLabel title = new JLabel("Pm2Media");
		title.setFont(new Font("Dialog", Font.BOLD, fontHeight));
		add(title, new GridBagConstraints(0, 0, 2, 1, 0, 0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 10, 0), 0, 0));
		
		JLabel description = new JLabel("A PmWiki to MediaWiki converter.");
		title.setFont(new Font("Dialog", Font.BOLD, fontHeight));
		add(description, new GridBagConstraints(0, 1, 2, 1, 0, 0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 10, 0), 0, 0));

		JLabel jung = new JLabel("By Johannes Perl");
		add(jung, new GridBagConstraints(0, 2, 1, 1, 0, 0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 0), 0, 0));
	}
}
