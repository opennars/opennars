package nars.io;

import nars.control.AbstractTask;

/**
 *
 * @author me
 */
public class Reboot extends AbstractTask<CharSequence> {

    @Override
    public CharSequence name() {
        return "Reboot";
    }
    
}
