package nars.util.event;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * Created by me on 9/15/15.
 */
public final class On<V> implements Serializable {

    public final Consumer<V> reaction;
    public final Topic<V> topic;

    On(Topic<V> t, Consumer<V> o) {
        this.reaction = o;
        this.topic = t;
    }

    final public void off() {
        topic.off(this);
    }
}
