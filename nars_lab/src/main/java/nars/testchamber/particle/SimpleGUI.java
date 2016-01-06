//package nars.testchamber.particle;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//
//
//
//public class SimpleGUI extends JFrame{
//
//	static JTextField particlenumberTF, particlesizeTF, tileTF;
//
//	private final JButton runB;
//    private final JLabel particlenumberL;
//	private JLabel particlesizeL, tileL;
//
//	private final String[] selectionTitles = {"v5","v7"};
//	private final JComboBox<String> versionList = new JComboBox<>(selectionTitles);
//
//	private final int WIDTH = 700;
//	private int HEIGHT = 300;
//
//	private final RunButtonHandler runBHandler;
//
//
//	public SimpleGUI()
//	{
//		//Set GUI to look like native OS instead of crappy java default look
//	    try {
//			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); //make it look pretty
//		} catch (ClassNotFoundException | UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		particlenumberL = new JLabel ("Number of Particles?");
//	    particlenumberTF = new JTextField();
//	    particlenumberTF.setText("500000");
//
//	    particlesizeL = new JLabel ("Particle Size?");
//	    particlesizeTF = new JTextField();
//	    particlesizeTF.setText("1");
//
//
//	    tileL = new JLabel ("Number of Tiles?");
//	    tileTF = new JTextField();
//	    tileTF.setText("64");
//
//
//		//Specify handlers for each button and add (register) ActionListeners to each button.
//	    runB = new JButton("RUN");
//	    runBHandler = new RunButtonHandler();
//		runB.addActionListener(runBHandler);
//
//		setTitle("Particle Simulation Program");
//
//		versionList.setSelectedItem("v7");
//
//
//		Container pane = getContentPane();
//		pane.setLayout(new GridLayout(4,2));
//
//		pane.add(particlenumberL);
//		pane.add(particlenumberTF);
//		pane.add(particlesizeL);
//		pane.add(particlesizeTF);
//		pane.add(tileL);
//		pane.add(tileTF);
//		pane.add(versionList);
//		pane.add(runB);
//
//		setSize(WIDTH, HEIGHT);
//		setFocusable(true);
//		setVisible(true);
//		setDefaultCloseOperation(EXIT_ON_CLOSE);
//
//	}
//
//	public class RunButtonHandler implements ActionListener
//	{
//                @Override
//		public void actionPerformed(ActionEvent e)
//		{
//			System.out.println("Button Registered");
//
//			if ("v5".equals(versionList.getSelectedItem()))
//			{
//				new ParticleSystem_v5();
//			}
//
//			else if ("v7".equals(versionList.getSelectedItem()))
//			{
//				new ParticleSystem_v7();
//			}
//		}
//	}
//
//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//
//		new SimpleGUI();
//
//	}
//
// }
