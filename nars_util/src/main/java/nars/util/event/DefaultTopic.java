package nars.util.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/** single-thread synchronous (in-thread) event emitter with direct array access
 * NOT WORKING YET
 * */
public class DefaultTopic<V> extends CopyOnWriteArrayList<Consumer<V>> implements Topic<V> {


    public class Subscription<V>  {

        final Consumer<V> reaction;

        Subscription(Consumer<V> o) {
            this.reaction = o;
        }

        public void off() {
            remove(reaction);
        }
    }


    @Override
    public List<Consumer<V>> all() {
        return this;
    }

    @Override
    public void emit(final V arg) {
        for (int i = 0, cSize = size(); i < cSize; i++) {
            get(i).accept(arg);
        }
    }

    @Override
    public Subscription on(Consumer<V> o) {
        Subscription d = new Subscription(o);
        add(o);
        return d;
    }

    @Override
    public void delete() {
        clear();
    }

}
