package nars.gui.output;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import processing.core.PApplet;

/**
 * Processing.org Panel
 */
abstract public class PPanel extends PApplet {

    public PPanel() {
        super();
    }



    @Override
    public void setup() {
        noLoop();
        
    }

    

    public JPanel newPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.add(this, BorderLayout.CENTER);
        init();
        return p;
    }
    

}

