/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */

package nars.plugin.mental;

import nars.core.NAR;
import nars.core.Plugin;
import nars.operator.mental.Consider;

/**
 *
 * @author tc
 */
public class AddConsider implements Plugin { //this one we forgot in 1.6.1 release, so why not just add it by a plugin:

    @Override
    public boolean setEnabled(NAR n, boolean enabled) {
        n.addPlugin(new Consider());
        return true;
    }
}
