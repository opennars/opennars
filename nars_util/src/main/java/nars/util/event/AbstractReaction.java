package nars.util.event;

/**
 * Created by me on 5/5/15.
 */
abstract public class AbstractReaction implements Reaction<Class> {

    transient protected EventEmitter source;
    transient protected EventEmitter.Registrations active;
    protected Class[] events;

    public AbstractReaction() {
        this(null);
    }

    public AbstractReaction(EventEmitter source, Class... events) {
        this(source, true, events);
    }

    public AbstractReaction(EventEmitter source, boolean active, Class... events) {
        super();

        this.events = events;
        this.source = source;

        setActive(active);
    }

    /** called when added via zero-arg constructor, dont call directly, HACK */
    public void start(EventEmitter source) {
        if (getSource() == null) {
            this.source = source;
            setActive(true);
        }
    }

    public Class[] getEvents() {
        return this.events;
    }


    public void setActive(boolean b) {

        EventEmitter s = getSource();

        if (b && (this.active==null)) {
            this.active = s.on(this, getEvents());
        }
        else if (!b && (this.active!=null)) {
            this.active.off();
            this.active = null;
        }

    }

    public EventEmitter getSource() {
        return source;
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
