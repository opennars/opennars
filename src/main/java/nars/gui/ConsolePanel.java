package nars.gui;

import nars.gui.input.TextInputPanel;
import nars.gui.output.LogPanel;
import nars.gui.output.SwingLogPanel;

import javax.swing.*;
import java.awt.*;

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

        if(cnt<5) {
            cnt++;
            this.setDividerLocation(0.75);
        }
    }
}
