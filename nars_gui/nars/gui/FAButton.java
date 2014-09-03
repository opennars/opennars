package nars.gui;

import javax.swing.JButton;



/**
 * Button using FontAwesome icon as a label
 */
public class FAButton extends JButton {

    public FAButton(char faCode) {
        super();
        setFont(NARSwing.FontAwesome);
        setText(String.valueOf(faCode));
    }
}
