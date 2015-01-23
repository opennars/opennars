/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */

package nars.operator.app.farg;

import nars.core.NAR;
import nars.core.Plugin;
import nars.logic.entity.Term;
import nars.util.bag.LevelBag;

/**
 *
 * @author tc
 */
public class FluidAnalogiesAgents implements Plugin {
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
