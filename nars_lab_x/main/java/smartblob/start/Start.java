package smartblob.start;

import smartblob.common.CoreUtil;
import smartblob.smartblob.ui.SmartblobsPanel;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;



/** starts Smartblob in a window */
public class Start{
	
	public static void main(String args[]){
		JFrame window = new JFrame("Smartblob 0.1 in-progress physics without smarts so far");
		window.setJMenuBar(newMenubar());
		window.add(new SmartblobsPanel());
		window.setSize(new Dimension(650,450));
		CoreUtil.moveToScreenCenter(window);
		window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		window.setVisible(true);
	}
	
	protected static JMenuBar newMenubar(){
		JMenuBar m = new JMenuBar();
		
		JMenu file = new JMenu("File");
		file.setMnemonic(KeyEvent.VK_F);
		m.add(file);
		
		JMenuItem newHumanainet = new JMenuItem(new AbstractAction("New"){
			public void actionPerformed(ActionEvent e){
				System.out.println("action new");
			}
		});
		newHumanainet.setMnemonic(KeyEvent.VK_N);
		file.add(newHumanainet);
		
		JMenuItem open = new JMenuItem(new AbstractAction("Open"){
			public void actionPerformed(ActionEvent e){
				System.out.println("action open");
			}
		});
		open.setMnemonic(KeyEvent.VK_O);
		file.add(open);
		
		JMenuItem save = new JMenuItem(new AbstractAction("Save"){
			public void actionPerformed(ActionEvent e){
				System.out.println("action save");
			}
		});
		save.setMnemonic(KeyEvent.VK_S);
		file.add(save);
		
		JMenuItem saveAs = new JMenuItem(new AbstractAction("Save As"){
			public void actionPerformed(ActionEvent e){
				System.out.println("action saveas");
			}
		});
		saveAs.setMnemonic(KeyEvent.VK_A);
		file.add(saveAs);
		
		/*JMenuItem saveAsHtml = new JMenuItem(new AbstractAction("Save As Html (out only, no open from here)"){
			public void actionPerformed(ActionEvent e){
				System.out.println("action saveAsHtml");
			}
		});
		saveAsHtml.setMnemonic(KeyEvent.VK_H);
		file.add(saveAsHtml);
		*/
		
		JMenu opensource = new JMenu("OpenSource");
		opensource.setMnemonic(KeyEvent.VK_O);
		m.add(opensource);
		
		opensource.add(new JLabel("<html>This program is GNU GPL 2+ kind of opensource, created by Ben F Rayfield.<br>You can unzip this jar file (in any unzipping program) to get the source code.<br>Take it apart, play with it, see how it works, and build your own GPL'ed opensource programs.</html>"));
		
		JMenu whatIsThis = new JMenu("What is this?");
		m.add(whatIsThis);
		
		whatIsThis.add(new JLabel("<html>Physics is per triangle, not on balls. I'm just testing it with groups of triangles<br> shaped as balls to start with. They bend a little when bouncing and vibrating after that.<br>This is a work in progress. Its not smart yet. I'll hook in the smarts after physics is working better.<br>Smartblob is a game for Artificial Intelligence (AI) to play,<br>and for people to play while controlling parts of AIs or they can play it only with other AIs.<br>Its designed to be very flexible, able to bend into all possible shapes, in theory.<br>Its not finished so it doesnt do that yet.<br>I've got AI code working (like in Physicsmata 2.0.0 in the boltzmann demo), but no smarts are hooked into the blobs yet.<br>The plan is each AI, using the Statsys interface, will control a vector stream with 1 dimension for each piece of the smartblob,<br>which may be volume of a triangle or distance constraint between their corners.<br>Through that, the smartblobs will grab, reshape, and use eachother as tools,<br>while being a platform to build a variety of fun games and custom bendable smart objects<br> that reshape themselves and eachother and bounce and grab those ways.<br>I'm still figuring out why the physics destabilize after a few second or minute, probably something about not normalizing or keeping track of energy.</html>"));
		
		return m;
	}

}
