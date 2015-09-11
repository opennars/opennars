
package nars.util.event;

import nars.util.data.list.FasterList;

import java.util.List;
import java.util.function.Consumer;

/**
 * notifies subscribers when a value is emitted
 */
abstract public interface Topic<V>  {

    abstract void delete();

    

    abstract public List<Consumer<V>> all();




    public interface EventRegistration {
        public void off();
    }


    public abstract void emit(V arg);

    DefaultTopic.Subscription on(Consumer<V> o);



    public static class Registrations extends FasterList<DefaultTopic.Subscription> {

        Registrations(int length) {
            super(length);
        }

//        public void resume() {
//            for (Registration r : this)
//                r.resume();
//        }
//        public void pause() {
//            for (Registration r : this)
//                r.pause();
//        }
//        public void cancelAfterUse() {
//            for (Registration r : this)
//                r.cancelAfterUse();
//        }

        public synchronized void off() {
            for (int i = 0; i < this.size(); i++) {
                this.get(i).off();
            }
            clear();
        }
        
    }


    
    public static <V> Registrations onAll(final Consumer<V> o, final Topic<V>... w) {
        Registrations r = new Registrations(w.length);
    
        for (final Topic<V> c : w)
            r.add( c.on(o) );
        
        return r;
    }

//
//    @Override
//    @Deprecated public void emit(Class channel, Object arg) {
//
//        if (!(arg instanceof Object[]))
//            super.emit(channel, new Object[] { arg });
//        else
//            super.emit(channel, arg);
//    }

}