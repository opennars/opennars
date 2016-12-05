package nars.console;

import nars.core.control.AbstractTask;

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
