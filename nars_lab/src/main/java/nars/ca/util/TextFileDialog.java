package nars.ca.util;


import java.awt.AWTEvent;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;


/**
 * Shows a text file in a dialog box.
 * It has a vertical scrollbar and a close button.
 *
 * @author Edwin Martin
 */
public class TextFileDialog extends Dialog {
	/**
	 * Constructs a TextFileDialog.
	 * @param parent parent frame
	 * @param title window title
	 * @param filename filename to show (from jar-file)
	 */
	public TextFileDialog( Frame parent, String title, String filename, int posX, int posY ) {
		super( parent, title );
		String manualText;
		Button okButton = new Button(" Close ");
		okButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					close();
				}
			}
		);

		EasyFile file = new EasyFile( this.getClass().getResourceAsStream( filename ) );
		try {
			manualText = file.readText();
		} catch (IOException e) {
			manualText = "";
		}
		
		TextArea manualTextArea = new TextArea( manualText, 20, 50, TextArea.SCROLLBARS_VERTICAL_ONLY );
		manualTextArea.setBackground( Color.white );	// improve readability
		manualTextArea.setForeground( Color.black );	// we don't want white text on a white background
		manualTextArea.setEditable(false);

		Panel buttonPanel = new Panel();
		buttonPanel.setLayout( new FlowLayout( FlowLayout.CENTER ) );
		buttonPanel.add( okButton );
		
		this.add( "Center", manualTextArea );
		this.add( "South", buttonPanel );
		this.pack();
		this.enableEvents(Event.WINDOW_DESTROY);
		this.setLocation( posX, posY );
		this.show();
	}

	/**
	 * Close dialog box.
	 */
	private void close() {
		this.hide();
		this.dispose();
	}

	/**
	 * Handle close window button.
	 * @see java.awt.Component#processEvent(java.awt.AWTEvent)
	 */
	public void processEvent( AWTEvent e ) {
		if ( e.getID() == Event.WINDOW_DESTROY )
			close();
	}
}