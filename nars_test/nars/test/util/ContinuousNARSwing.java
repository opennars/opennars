package nars.test.util;

import nars.core.NAR;
import nars.core.build.ContinuousBagNARBuilder;
import nars.gui.NARSwing;

/**
 *
 * @author me
 */


public class ContinuousNARSwing {

    public static void main(String[] arg) {
        NAR cn = new ContinuousBagNARBuilder(true).setConceptBagSize(8192).build();
        
        NARSwing w = new NARSwing(cn);
    }
}
