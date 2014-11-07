package nars.core;

import java.io.Serializable;

/**
 * NAR plugin interface
 */
public interface Plugin extends Serializable {

    /** called when plugin is activated (enabled = true) / deactivated (enabled=false) */
    public boolean setEnabled(NAR n, boolean enabled);
    
    default public CharSequence name() {
        return this.getClass().getSimpleName();
    }
    

}
