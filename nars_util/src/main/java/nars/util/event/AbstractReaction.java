package nars.util.event;

/**
 * Created by me on 5/5/15.
 */
abstract public class AbstractReaction implements Reaction {

    protected final EventEmitter source;
    protected EventEmitter.Registrations active;
    protected final Class[] events;

    public AbstractReaction(EventEmitter source, boolean active, Class... events) {
        super();

        this.source = source;
        this.events = events;

        setActive(active);
    }

    public void setActive(boolean b) {

        if (b && (this.active==null)) {
            this.active = source.on(this, events);
        }
        else if (!b && (this.active!=null)) {
            this.active.off();
            this.active = null;
        }

    }

    public void on() {
        setActive(true);
    }

    public void off() { setActive(false); }

    protected void emit(Class channel, Object... signal) {
        source.emit(channel, signal);
    }

    public boolean isActive() {
        return active!=null;
    }

}
