package nars.gui;

import java.awt.Graphics;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import nars.gui.input.TextInputPanel;
import nars.gui.output.LogPanel;
import nars.gui.output.SwingLogPanel;

/**
 * Combines input panel with a log output panel, divided by a splitpane
 */
public class ConsolePanel extends JSplitPane {
    
    public ConsolePanel(NARControls narControls) {
        super(JSplitPane.VERTICAL_SPLIT);
        
        LogPanel outputLog = new SwingLogPanel(narControls);
        add(outputLog, 0);
        
        TextInputPanel inputPanel = new TextInputPanel(narControls.nar);
        add(inputPanel, 1);
    }
    
    int cnt=0;
    @Override
    public void paint(Graphics g) {
        super.paint(g);
   
        if(cnt<5) { //according to stack overflow, the paint method is the only way to do this correctly
            cnt++; //but it seems still not be guranteed 
            this.setDividerLocation(0.75); //strangely on second paint it works, to make sure I put it to 5
        }
    }
    
}
