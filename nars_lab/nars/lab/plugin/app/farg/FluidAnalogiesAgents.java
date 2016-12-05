/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */

package nars.lab.plugin.app.farg;

import nars.NAR;
import nars.util.Plugin;
import nars.language.Term;
import nars.storage.LevelBag;

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
        if(enabled==true) {
            ws=new Workspace(this,n);
        }
        return true;
    }
    
}
