package nars.util.event;

/** Observes events emitted by EventEmitter */
@FunctionalInterface
public interface Reaction<K,V> {

    void event(K event, V args);

}
