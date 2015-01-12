package nars.gui;

import nars.gui.input.TextInputPanel;
import nars.gui.output.LogPanel;
import nars.gui.output.SwingLogPanel;

import javax.swing.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ConsolePanel.class.getName()).log(Level.SEVERE, null, ex);
                }
                setDividerLocation(0.75);
            }
            
        });
        
        setDividerLocation(0.75);
        
    }
    
}
