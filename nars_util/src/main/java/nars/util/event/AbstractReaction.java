package nars.util.event;

/**
 * Reaction that manages its registration state
 */
abstract public class AbstractReaction<K,V> implements Reaction<K,V> {

    transient protected EventEmitter<K,V> source;
    transient protected EventEmitter.Registrations active;
    protected final K[] events;

    public AbstractReaction() {
        this(null);
    }

    @SafeVarargs //todo: "possible heap pollution" hereafter with noted with this annotation to change the type checking behavior.
    public AbstractReaction(EventEmitter<K,V> source, K... events) {
        this(source, true, events);
    }

    @SafeVarargs
    public AbstractReaction(EventEmitter<K,V> source, boolean active, K... events) {
        super();

        this.events = events;
        this.source = source;

        setActive(active);
    }



    /** called when added via zero-arg constructor, dont call directly, HACK */
    public void start(EventEmitter<K,V> source) {
        if (getSource() == null) {
            this.source = source;
            setActive(true);
        }
    }

    public K[] getEvents() {
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

    public EventEmitter<K,V> getSource() {
        return source;
    }

    public void on() {
        setActive(true);
    }

    public void off() { setActive(false); }

    protected void emit(K channel, V signal) {
        source.emit(channel, signal);
    }

    public boolean isActive() {
        return active!=null;
    }

}
