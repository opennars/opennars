package nars.util.event;

/** Observes events emitted by EventEmitter */
public interface Reaction<K> {

    public void event(K event, Object... args);

}
