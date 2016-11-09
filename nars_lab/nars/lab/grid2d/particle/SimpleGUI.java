package nars.lab.grid2d.particle;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;



public class SimpleGUI extends JFrame{
	
	static JTextField particlenumberTF, particlesizeTF, tileTF;
	
	private final JButton runB;
    private final JLabel particlenumberL;
	private JLabel particlesizeL, tileL;
	
	private final String[] selectionTitles = {"v5","v7"};
	private final JComboBox<String> versionList = new JComboBox<>(selectionTitles);
	
	private final int WIDTH = 700;
	private int HEIGHT = 300;
	
	private final RunButtonHandler runBHandler;
	
	
	public SimpleGUI()
	{
		//Set GUI to look like native OS instead of crappy java default look
	    try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); //make it look pretty
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    particlenumberL = new JLabel ("Number of Particles?");	    
	    particlenumberTF = new JTextField();
	    particlenumberTF.setText("500000");
	    
	    particlesizeL = new JLabel ("Particle Size?");	    
	    particlesizeTF = new JTextField();
	    particlesizeTF.setText("1");
	    
	    
	    tileL = new JLabel ("Number of Tiles?");    
	    tileTF = new JTextField();
	    tileTF.setText("64");
	   
	    
		//Specify handlers for each button and add (register) ActionListeners to each button.
	    runB = new JButton("RUN");
	    runBHandler = new RunButtonHandler();
		runB.addActionListener(runBHandler);
		
		this.setTitle("Particle Simulation Program");
		
		versionList.setSelectedItem("v7");

		
		Container pane = this.getContentPane();
		pane.setLayout(new GridLayout(4,2));
		
		pane.add(particlenumberL);
		pane.add(particlenumberTF);
		pane.add(particlesizeL);
		pane.add(particlesizeTF);
		pane.add(tileL);
		pane.add(tileTF);
		pane.add(versionList);
		pane.add(runB);
		
		this.setSize(WIDTH, HEIGHT);
		this.setFocusable(true);
		this.setVisible(true);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	    
	}
	
	public class RunButtonHandler implements ActionListener
	{
                @Override
		public void actionPerformed(ActionEvent e)
		{
			System.out.println("Button Registered");
			
			if (versionList.getSelectedItem().equals("v5"))
			{
				new ParticleSystem_v5();
			}
			
			else if (versionList.getSelectedItem().equals("v7"))
			{
				new ParticleSystem_v7();
			}
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		new SimpleGUI();	

	}

}
