package nars.util.event;

import java.util.function.Consumer;

/**
 * Created by me on 9/15/15.
 */
public final class On<V> {

    final Consumer<V> reaction;
    final Topic topic;

    On(Topic t, Consumer<V> o) {
        this.reaction = o;
        this.topic = t;
    }

    final public void off() {
        topic.off(this);
    }
}
