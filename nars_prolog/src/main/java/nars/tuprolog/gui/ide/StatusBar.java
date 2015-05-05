package nars.tuprolog.gui.ide;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class StatusBar extends JLabel implements PropertyChangeListener
{
    
    private static final long serialVersionUID = 1L;

    public StatusBar()
    {
        super();
        setPreferredSize(new Dimension(100, 16));
    }
    
/*    public SwingStatusBar(int x, int y)
    {
        super();
        setPreferredSize(new Dimension(x, y));
    }*/

    public void propertyChange(PropertyChangeEvent event)
    {
        String propertyName = event.getPropertyName();
        if (propertyName.equals("StatusMessage"))
            setStatusMessage(event.getNewValue().toString());
    }

    public void setStatusMessage(String message)
    {
        setText(""+message);
    }

    public void setFontDimension(int dimension)
    {
        Font font = new Font(this.getFont().getName(),this.getFont().getStyle(),dimension);
        this.setFont(font);
        setPreferredSize(new Dimension(100, dimension+4));
    }
}
