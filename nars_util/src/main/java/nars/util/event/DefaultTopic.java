package nars.util.event;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/** single-thread synchronous (in-thread) event emitter with direct array access
 * */
public class DefaultTopic<V extends Serializable> extends CopyOnWriteArrayList<Consumer<V>> implements Topic<V> {

    @Override
    public List<Consumer<V>> all() {
        return this;
    }

    @Override
    final public void emit(final V arg) {
        for (int i = 0, cSize = size(); i < cSize; i++) {
            get(i).accept(arg);
        }
    }

    @Override
    final public On on(Consumer<V> o) {
        On d = new On(this,o);
        add(o);
        return d;
    }


    @Override
    public final void off(On<V> o) {
        if (o.topic!=this)
            throw new RuntimeException(this + " is not " + o);

        if (!remove(o.reaction))
            throw new RuntimeException(this + " has not " + o.reaction);
    }



    @Override
    public void delete() {
        clear();
    }

}
