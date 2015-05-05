package nars.tuprolog.gui.ide;

import nars.tuprolog.Prolog;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class AboutFrame extends GenericFrame
{
    private static final long serialVersionUID = 1L;

    public AboutFrame(JFrame mainWindow)
    {
        super("About tuProlog IDE", mainWindow, 275, 135, true, true);
        initComponents();
    }

    private void initComponents()
    {
        Container c=this.getContentPane();
        JLabel icon=new JLabel();
        URL urlImage = ToolBar.getIcon("img/tuProlog.gif");
        icon.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(urlImage)));
        
        JLabel versionSystem=new JLabel(" tuProlog engine version " + Prolog.getVersion() );
        
        //String platformMessage = " " + alice.other.VersionInfo.getPlatform();
        //platformMessage += " platform version ";
        //platformMessage += alice.other.VersionInfo.getCompleteVersion();
        JLabel versionIDE=new JLabel(" tuProlog for " + nars.tuprolog.util.VersionInfo.getPlatform() + " version " + nars.tuprolog.util.VersionInfo.getCompleteVersion() + "   ");
        
        JLabel copyright=new JLabel(" Copyright 2001-2013 ");
        JLabel unibo=new JLabel(" Universita' di Bologna, Italy.");
        JLabel url=new JLabel(" http://tuprolog.unibo.it");
        
        c.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        c.add(icon,constraints);
        constraints.gridy=1;
        c.add(versionSystem,constraints);
        constraints.gridy=2;
        c.add(versionIDE,constraints);
        constraints.gridy=3;
        c.add(new JLabel(" "),constraints);
        constraints.gridy=4;
        c.add(url,constraints);
        constraints.gridy=5;
        c.add(unibo,constraints);
        constraints.gridy=6;
        c.add(copyright,constraints);
        pack();
        setVisible(true);
    }
}
