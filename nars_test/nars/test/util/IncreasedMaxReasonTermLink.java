package nars.test.util;

import nars.core.NAR;
import nars.core.build.DefaultNARBuilder;
import nars.gui.NARSwing;

/**
 * Tests the effect of increased Max Reason TermLink parameter
 * @author me
 */
public class IncreasedMaxReasonTermLink {
    
    public static void main(String[] args) {
        NAR n = new DefaultNARBuilder().build();
        n.param().maxReasonedTermLink.set(10);
        
        new NARSwing(n);
        
        
    }
    
}
