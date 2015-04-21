/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.gui.output;

import automenta.vivisect.swing.NPanel;
import nars.gui.VerticalLayout;

import javax.swing.*;
import java.awt.*;

/**
 *
 * @author me
 */
public class VerticalPanel extends NPanel {

    protected final JPanel content;
    //protected final JPanel contentWrap;
    private final JScrollPane scrollPane;
    
    public VerticalPanel() {
        super(new BorderLayout());
        
        content = new JPanel(new VerticalLayout());
        //contentWrap = new JPanel(new BorderLayout());
        //contentWrap.add(content, BorderLayout.NORTH);
        add(scrollPane = new JScrollPane(content), BorderLayout.CENTER);
        
    }
    
    public void scrollBottom() {
        content.validate();

        JScrollBar vertical = scrollPane.getVerticalScrollBar();
        //vertical.setValue( vertical.getMaximum() );
        //vertical.setValue( (int)content.getPreferredSize().getHeight() );
        vertical.setValue(Integer.MAX_VALUE);
    }

    public void addVertically(JComponent j) {
        content.add(j);
    }

    public void addVertically(JComponent j, int index) {
        content.add(j, index);
    }

    public void removeVertically(JComponent j) {
        content.remove(j);
    }

    @Override
    protected void visibility(boolean appearedOrDisappeared) {
    
    }


    public void removeAllVertically() {
        content.removeAll();
        validate();
    }
}
