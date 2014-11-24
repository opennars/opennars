/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */

package nars.plugin.farg;

import nars.core.NAR;
import nars.core.Plugin;
import nars.entity.Concept;

/**
 *
 * @author tc
 */
public class FARG implements Plugin {
    public int max_codelets=100;
    public int codelet_level=100;
    Workspace ws;
    
    @Override
    public boolean setEnabled(NAR n, boolean enabled) {
        if(enabled==true) {
            ws=new Workspace(this,n);
        }
        return true;
    }
}
