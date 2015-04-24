package nars.nario;

import nars.nario.level.Level;
import nars.nario.level.LevelGenerator;

import javax.swing.*;
import java.awt.*;

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



    @Override
    public void lose() {

    }

    public static void main(String[] args)     {
        new Run();
    }
}