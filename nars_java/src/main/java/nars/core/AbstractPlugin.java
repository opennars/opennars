package nars.core;

/**
* Created by me on 1/12/15.
*/
abstract public class AbstractPlugin implements Plugin, EventEmitter.EventObserver {

    private EventEmitter.Registrations regist = null;

    @Override
    public boolean setEnabled(NAR n, boolean enabled) {
        if (enabled) {
            regist = n.memory.event.on(this, getEvents());
        }
        else  {
            if (regist!=null) {
                regist.cancel();
                regist = null;
            }
        }
        return true;
    }

    /** list of event channels to listen to */
    abstract public Class[] getEvents();

    /** called when plugin is enabled */
    abstract public void onEnabled(NAR n);

    /** called when plugin is disabled */
    abstract public void onDisabled(NAR n);
}
