package nars.operator.io;

import nars.logic.entity.AbstractTask;

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
