package automenta.vivisect.swing;

import automenta.vivisect.Video;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;

import javax.swing.*;


/**
 * Button using FontAwesome icon as a label
 */
public class AwesomeButton extends JButton {


    public AwesomeButton(char faCode) {
        super();
        setFont(Video.FontAwesome);
        setText(String.valueOf(faCode));
    }
}
