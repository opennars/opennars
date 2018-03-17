/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.lab.ioutils;

import automenta.vivisect.swing.NWindow;
import nars.NAR;
import nars.config.Plugins;
import nars.gui.NARSwing;
import nars.gui.input.KeyboardInputPanel;

/**
 *
 * @author me
 */
public class KeyboardInputExample {
    
    public static void main(String[] args) {
        //NAR n = NAR.build(new Neuromorphic().realTime());
        //NAR n = NAR.build(new Default().realTime());
        //n.param.duration.set(100);
        
        NARSwing.themeInvert();
        
        NAR n = new NAR();
        
        
                
        new NARSwing(n).themeInvert();

        new NWindow("Direct Keyboard Input", new KeyboardInputPanel(n)).show(300, 100, false);
        
        n.start(100);
        
        
    }
}
