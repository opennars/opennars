package nars.op;

import nars.NAR;
import nars.event.NARReaction;
import nars.util.event.EventEmitter;

/**
* Created by me on 1/12/15.
*/
@Deprecated abstract public class NARReaction2 extends NARReaction implements IOperator {


    private EventEmitter.Registrations regist = null;

    public NARReaction2(NAR n, Class... events) {
        super(n, events);
    }

    @Override
    public boolean setEnabled(NAR n, boolean enabled) {
        if (enabled) {
            regist = n.memory.event.on(this, getEvents());

            onEnabled(n);

        }
        else  {
            cancel();

            onDisabled(n);
        }
        return true;
    }



    /** called when plugin is enabled */
    abstract public void onEnabled(NAR n);

    /** called when plugin is disabled */
    abstract public void onDisabled(NAR n);

    /** manually cancel this plugin by removing its event registration */
    public boolean cancel() {
        if (regist!=null) {
            regist.off();
            regist = null;
            return true;
        }
        return false;
    }
}
