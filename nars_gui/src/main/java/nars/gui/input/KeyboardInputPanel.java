/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.gui.input;

import nars.Video;
import automenta.vivisect.swing.NPanel;
import nars.NAR;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import static java.awt.BorderLayout.CENTER;
import static java.awt.event.KeyEvent.CHAR_UNDEFINED;
import static nars.io.Texts.n2;

/**
 * Direct keyboard input
 * @author me
 */
public class KeyboardInputPanel extends NPanel implements KeyListener, FocusListener {
    private final NAR nar;
    private final JTextArea text;
    private final String prefix;

    public KeyboardInputPanel(NAR n) {
        this(n, "keyboard");
    }

    public KeyboardInputPanel(NAR n, String prefix) {
        super(new BorderLayout());

        this.prefix = prefix;

        text = new JTextArea();
        text.setFont(Video.fontMono(16f).deriveFont(Font.BOLD));
        add(text, CENTER);
        
        this.nar = n;
        
        
        text.addKeyListener(this);
        text.addFocusListener(this);
    }

    @Override
    protected void visibility(boolean appearedOrDisappeared) {
        
    }

    /** can be adjusted according to how many other windows are active, etc. */
    public void setFocus(float freq, float conf) {
        nar.input("<" + prefix + " --> [focus]>. :|: %" + n2(freq) + ";" + n2(conf) + "%");
    }

    public void onCharTyped(char c, float priority, float freq, float conf) {        
        String charTerm = "" + c;/// + "\"";
        nar.input("$" + n2(priority) + "$ < {" + charTerm + "} --> " + prefix + ">. :|: %" + n2(freq) + ";" + n2(conf) + "%");
        //nar.input("<(&/, <" + charTerm + " --> " + prefix + ">, ?dt) =/> <?next --> \" + prefix + \">>?");
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
        char c = e.getKeyChar();
        if (c == CHAR_UNDEFINED)
            return;
        if (e.isActionKey())
            return;
        onCharTyped(c, 0.8f, 1.0f, 0.9f);
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void focusGained(FocusEvent e) {        
        setFocus(1f, 0.9f);
    }

    @Override
    public void focusLost(FocusEvent e) {        
        setFocus(0f, 0.9f);
    }
    
            
}
