package nars.event;

/** Observes events emitted by EventEmitter */
public interface Reaction<C> {
    public void event(Class<? extends C> event, Object... args);
}
