package nars.gui;

import java.awt.Graphics;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
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
    
    boolean painted=false;
    @Override
    public void paint(Graphics g) {
        super.paint(g);

        if (!painted) {
            painted = true;
            this.setDividerLocation(0.75);
        }
    }
}
