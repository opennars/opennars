package nars.util.event;

import java.util.function.Consumer;

/**
 * Represents the active state of a topic stream
 */
public final class On<V> {


    public final Consumer<V> reaction;
    public final Topic<V> topic;

    On(Topic<V> t, Consumer<V> o) {
        this.reaction = o;
        this.topic = t;
    }

    final public void off() {
        topic.off(this);
    }

    @Override
    public String toString() {
        return "On{" +
                topic.name() +
                '}';
    }
}
