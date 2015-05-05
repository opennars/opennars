package nars.tuprolog.gui.ide;

import javax.swing.*;
import java.io.File;

public class PrologFileChooser extends JFileChooser
{

    private static final long serialVersionUID = 1L;

    /**
	 * dialog type to display, it's used to know when display a showConfirmDialog
	 */
    private String type=null;

    public PrologFileChooser(String file,String type)
    {
        super(file);
        this.type=type;
    }

    public void approveSelection ()
    {
        boolean approve = true;

        File f = getSelectedFile ();

        if ((f.exists ()) && (type.equals("save")))//if user want save on a file that already exists
        {
            approve = JOptionPane.showConfirmDialog (this, "The file '" + f
                    + "' already exists.\r\n\r\nDo you want to replace it?", "Confirm",
                      JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
        }

        if (approve)
            super.approveSelection ();
    }
}
