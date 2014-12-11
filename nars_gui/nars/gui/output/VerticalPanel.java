/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.gui.output;

import automenta.vivisect.swing.NPanel;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author me
 */
abstract public class VerticalPanel extends NPanel {

    protected final JPanel content;
    protected final JPanel contentWrap;
    
    public VerticalPanel() {
        super(new BorderLayout());
        
        content = new JPanel(new GridBagLayout());
        contentWrap = new JPanel(new BorderLayout());
        contentWrap.add(content, BorderLayout.NORTH);
        add(new JScrollPane(contentWrap), BorderLayout.CENTER);
        
    }
    
    public void addPanel(int index, JComponent j) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        
        gc.gridx = 0;
        gc.weightx = 1.0;
        gc.weighty = 0.0;
        gc.gridy = 0;
        
        content.add(j, gc);
        gc.gridy++;
        
    }
    
    
    
}
