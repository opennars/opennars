package nars.op;

import nars.NAR;

import java.io.Serializable;

/**
 * NAR plugin interface
 */
@Deprecated interface IOperator extends Serializable {

    /** called when plugin is activated (enabled = true) / deactivated (enabled=false) */
    public boolean setEnabled(NAR n, boolean enabled);
    
    /*default public CharSequence name() {
        return this.getClass().getSimpleName();
    }*/


}
