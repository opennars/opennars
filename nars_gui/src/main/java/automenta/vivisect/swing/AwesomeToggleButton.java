package automenta.vivisect.swing;

import nars.Video;

import javax.swing.*;
import java.awt.*;


/** ToggleButton with FontAwesome icon */
public class AwesomeToggleButton extends JToggleButton {

    private final char codeUnselected;
    private final char codeSelected;

    public AwesomeToggleButton(char faCodeUnselected, char faCodeSelected) {
        codeUnselected = faCodeUnselected;
        codeSelected = faCodeSelected;
        setFont(Video.FontAwesome);
        setText(String.valueOf(faCodeUnselected));
    }

    @Override
    public void setSelected(boolean b) {
        super.setSelected(b);
    }

    @Override
    public void paint(Graphics g) {
        if (isSelected()) {
            setText(String.valueOf(codeSelected));
        } else {
            setText(String.valueOf(codeUnselected));
        }
        super.paint(g); //To change body of generated methods, choose Tools | Templates.
    }
}
