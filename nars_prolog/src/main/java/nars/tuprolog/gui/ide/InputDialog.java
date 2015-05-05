package nars.tuprolog.gui.ide;

import nars.tuprolog.event.ReadEvent;
import nars.tuprolog.event.ReadListener;
import nars.tuprolog.lib.UserContextInputStream;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.ByteArrayInputStream;

/**
 * 
 * This class has been changed from JDialog to JPanel
 * to be inserted inside the ConsoleDialog
 * 
 */
public class InputDialog extends JPanel{

	private static final long serialVersionUID = 1L;
	private JTextArea inputText;
	private UserContextInputStream stream;
	
	public InputDialog(UserContextInputStream str)
	{
		initComponent();
		stream = str;
		stream.setReadListener(new ReadListener(){
			@Override
			public void readCalled(ReadEvent event) {
				setVisible(true);
				inputText.requestFocus();
				inputText.setCaretPosition(0);
			}
		});
	}
	
	public void initComponent()
	{
		this.setLayout(new BorderLayout(0, 0));
		inputText = new JTextArea();
		this.add(inputText, BorderLayout.CENTER);
		
		inputText.addKeyListener(new KeyListener()
		{
			public void keyPressed(KeyEvent arg0) {
				if(arg0.getKeyCode() == KeyEvent.VK_ENTER)
				{ 
					stream.putInput(new ByteArrayInputStream(inputText.getText().getBytes()));
					setVisible(false);
					inputText.setText("");
				}
			}
			@Override
			public void keyReleased(KeyEvent arg0) {}
			@Override
			public void keyTyped(KeyEvent arg0) {}
		});
		this.setVisible(false);
	}	
}
