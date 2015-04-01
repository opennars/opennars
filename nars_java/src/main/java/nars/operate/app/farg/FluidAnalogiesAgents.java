/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */

package nars.operate.app.farg;

import nars.NAR;
import nars.operate.IOperator;
import nars.nal.term.Term;
import nars.budget.bag.LevelBag;

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
