package nars.tuprolog.gui.ide;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GenericFrame
    extends JFrame
{
    
    private static final long serialVersionUID = 1L;

    private String iconPathName = "img/tuProlog.gif";
    private JFrame mainWindow=null;


    public GenericFrame()
    {
        this("frame");
    }

    public GenericFrame(String title)
    {
        this(title, null, 0, 0);
    }

    public GenericFrame(String title, JFrame mainWindow, int width, int height)
    {
        this(title, mainWindow, width, height, false);
    }

    public GenericFrame(String title, JFrame mainWindow, int width, int height, boolean onFront)
    {
        this(title, mainWindow, width, height, onFront, false);
    }
    
    public GenericFrame(String title, JFrame mainWindow, int width, int height, boolean onFront, boolean dimensionsBlocked)
    {
        setVisible(false);
        this.mainWindow=mainWindow;
        setTitle(title);
        /*
        if (iconPathName != "") {
            // Set a title bar icon
            ImageIcon icon = new ImageIcon(getClass().getResource(getFrameIcon()));
            setIconImage(icon.getImage());
        }*/

        if ((width > 0) && (height > 0)) {
            Insets insets = getInsets();
            width += insets.left + insets.right;
            height += insets.top + insets.bottom;
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            width = Math.min(width, screenSize.width);
            height = Math.min(height, screenSize.height);
            setBounds((screenSize.width - width) / 2, (screenSize.height - height) / 2, width, height);
        }

        if(onFront)
        {
            addWindowListener(new WindowListener());
        }
        else
        {
            addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent event) {
                    dispose();
                }
            });
        }
        
        if(dimensionsBlocked)
        {
            setResizable(false);
        }
    }
    
    public void setFrameIcon(String imagePathName) {
        iconPathName = imagePathName;
    }

    public String getFrameIcon() {
        return iconPathName;
    }

    public void onClose()
    {
        mainWindow.setEnabled(true);
        mainWindow.setVisible(true);
        dispose();
    }

    class WindowListener extends WindowAdapter
    {
        public void windowClosing(WindowEvent w)
        {
            onClose();
        }
    }

}

