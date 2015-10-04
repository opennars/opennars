package nars.util.event;

import nars.util.data.list.FasterList;
import org.infinispan.commons.util.WeakValueHashMap;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/** single-thread synchronous (in-thread) event emitter with direct array access
 * */
public class DefaultTopic<V extends Serializable> implements Topic<V> {


    //TODO extract this to Topics and a graph metamodel of the events

    static WeakValueHashMap<String, Topic<?>> topics = new WeakValueHashMap<>();

    private FasterList<Consumer> consumers = new FasterList();
    private Consumer[] consumerArray = null;

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
        super();
        this.id = id;
        register(this);
    }

    @Override
    final public void emit(final V arg) {
        final Consumer[] vv = this.consumerArray;
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
        if (consumers.add(o))
            updateArray();
        return d;
    }

    private void updateArray() {

        //TODO for safe atomicity while the events are populated, buffer additions to a sub-list,
        //and apply them if a flag is set on the next read

        final FasterList<Consumer> consumers = this.consumers;

        if (!consumers.isEmpty()) {
            Consumer[] a = this.consumerArray;
            if (a == null)
                a = new Consumer[consumers.size()+1]; //+1 for padding
            this.consumerArray = consumers.toNullTerminatedUnpaddedArray(a);
            if (consumerArray[consumerArray.length-1]!=null)
                System.err.println("wtf");
        }
        else {
            this.consumerArray = null;
        }

    }


    @Override
    public final void off(On<V> o) {
        //if (Global.DEBUG) {
          //  if (o.topic != this)
          //      throw new RuntimeException(this + " is not " + o);
        //}

        if (!consumers.remove(o.reaction))
            throw new RuntimeException(this + " has not " + o.reaction);

        updateArray();
    }



    @Override
    public void delete() {
        unregister(this);
        consumers.clear();
    }

}
