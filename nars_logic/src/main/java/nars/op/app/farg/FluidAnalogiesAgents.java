/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */

package nars.op.app.farg;

import nars.NAR;
import nars.op.IOperator;
import nars.nal.term.Term;
import nars.bag.impl.LevelBag;

/**
 *
 * @author tc
 */
public class FluidAnalogiesAgents implements IOperator {
    public int max_codelets=100;
    public int codelet_level=100;
    Workspace ws;
    LevelBag<Codelet,Term> coderack;
    
    @Override
    public boolean setEnabled(NAR n, boolean enabled) {
        if(enabled) {
            if (coderack!=null)
                coderack.clear();
            ws=new Workspace(this,n);
        }
        return true;
    }
    
}
