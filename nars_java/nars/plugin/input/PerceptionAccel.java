/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.plugin.input;

import nars.core.EventEmitter;
import nars.core.NAR;
import nars.core.Plugin;

/**
 *
 * @author tc
 */
public class PerceptionAccel implements Plugin, EventEmitter.EventObserver {

    @Override
    public boolean setEnabled(NAR n, boolean enabled) {
        throw new UnsupportedOperationException("Not supported yet.");
        /*if(enabled) {
            
        }*/
        //return true;
    }

    @Override
    public void event(Class event, Object[] args) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
