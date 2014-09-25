package nars;

import nars.core.NAR;
import nars.core.build.DefaultNARBuilder;
import nars.gui.NARSwing;

/**
 *
 * @author me
 */
public class NARPrologSwing extends NARSwing {

    public NARPrologSwing(NAR n) {
        super(n);
    }
    
    public static void main(String[] args) {
        NAR pn = new DefaultNARBuilder().build();
        PrologContext prologContext = new PrologContext(pn);
        
        new NARPrologSwing(pn);
        pn.addInput("'Prolog enabled");
        pn.cycle(1);
    }
}
