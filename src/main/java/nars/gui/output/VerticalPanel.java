/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.gui.output;

import automenta.vivisect.swing.NPanel;

import javax.swing.*;
import java.awt.*;

/**
 *
 * @author me
 */
abstract public class VerticalPanel extends NPanel {

    protected final JPanel content;
    protected final JPanel contentWrap;
    private final JScrollPane scrollPane;
    
    public VerticalPanel() {
        super(new BorderLayout());
        
        content = new JPanel(new GridBagLayout());
        contentWrap = new JPanel(new BorderLayout());
        contentWrap.add(content, BorderLayout.NORTH);
        add(scrollPane = new JScrollPane(contentWrap), BorderLayout.CENTER);
        
    }
    
    public void scrollBottom() {
        JScrollBar vertical = scrollPane.getVerticalScrollBar();
        vertical.setValue( vertical.getMaximum() );        
    }
    
    public void addPanel(int index, JComponent j) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        
        gc.gridx = 0;
        gc.weightx = 1.0;
        gc.weighty = 0.0;
        gc.gridy = index;
        
        content.add(j, gc);
        
    }
    
    
    
}
