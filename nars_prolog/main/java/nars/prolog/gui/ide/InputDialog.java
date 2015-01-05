package nars.prolog.gui.ide;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.ByteArrayInputStream;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import nars.prolog.event.ReadEvent;
import nars.prolog.event.ReadListener;
import nars.prolog.lib.UserContextInputStream;

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
					stream.putInput(new ByteArrayInputStream(inputText.getText().toString().getBytes()));
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
