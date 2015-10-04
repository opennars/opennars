package nars.util.event;

import org.infinispan.commons.util.WeakValueHashMap;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/** single-thread synchronous (in-thread) event emitter with direct array access
 * */
public class DefaultTopic<V extends Serializable> extends ArraySharingList<Consumer<V>> implements Topic<V> {


    //TODO extract this to Topics and a graph metamodel of the events

    static WeakValueHashMap<String, Topic<?>> topics = new WeakValueHashMap<>();




    public static void register(Topic<?> t) {
        topics.put(t.name(), t);
    }
    public static void unregister(Topic<?> t) {
        topics.remove(t.name());
    }

    static AtomicInteger topicSerial = new AtomicInteger();
    static int nextTopicID() {
        return topicSerial.incrementAndGet();
    }

    final String id;

    @Override
    public String name() {
        return id;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public DefaultTopic() {
        this(Integer.toString(nextTopicID(), 36));
    }

    DefaultTopic(String id) {
        super(i -> new Consumer[i]);
        this.id = id;
        register(this);
    }

    @Override
    final public void emit(final V arg) {
        final Consumer[] vv = nullTerminatedArray();
        if (vv == null) return;

        for (int i = 0; ; i++) {
            try {
                final Consumer c = vv[i];
                if (c == null)
                    break; //null terminator hit
                c.accept(arg);
            }
            catch (ArrayIndexOutOfBoundsException e) {
                System.err.println();
            }
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
        //if (Global.DEBUG) {
          //  if (o.topic != this)
          //      throw new RuntimeException(this + " is not " + o);
        //}

        if (!remove(o.reaction))
            throw new RuntimeException(this + " has not " + o.reaction);
    }



    @Override
    public void delete() {
        unregister(this);
        data.clear();
    }

}
