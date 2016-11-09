package nars.lab.nario;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Run extends MarioComponent {
    
    public Run() {
        super();

        JFrame frame = new JFrame("Super NARio");
        
        JPanel marioWrap = new JPanel(new BorderLayout());
        marioWrap.add(this, BorderLayout.CENTER);        
        frame.setContentPane(marioWrap);
        frame.setSize(800,600);
        frame.setResizable(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((screenSize.width-frame.getWidth())/2, (screenSize.height-frame.getHeight())/2);
        
        frame.setVisible(true);
        
        
    }
    
    public static void main(String[] args)     {
        new Run();
    }
}