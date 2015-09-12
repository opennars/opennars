
package nars.util.event;

import nars.util.data.list.FasterList;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * notifies subscribers when a value is emitted
 */
abstract public interface Topic<V>  {

    abstract void delete();

    

    abstract public List<Consumer<V>> all();



    /** registers to all public Topic fields in an object */
    public static Registrations all(final Object obj, BiConsumer<String /* fieldName*/,Object /* value */> f) {


        Registrations s = new Registrations();

        /** TODO cache the fields because reflection may be slow */
        for (Field field : obj.getClass().getFields()) {
            Class returnType = field.getType();
            if (returnType.equals(Topic.class)) {
                final String fieldName = field.getName();
                try {
                    Topic t = ((Topic) field.get(obj));

                    // could send start message: f.accept(f.getName(),  );

                    s.add(
                        t.on((nextValue) -> f.accept(
                                  fieldName /* could also be the Topic itself */,
                                  nextValue
                        )));

                } catch (IllegalAccessException e) {
                    f.accept( fieldName, e);
                }
            }

            //System.out.println(obj + "  " + f + " " + returnType);
        }

        return s;
    }



    void emit(V arg);

    DefaultTopic.Subscription on(Consumer<V> o);



    public static class Registrations extends FasterList<DefaultTopic.Subscription> {

        Registrations(int length) {
            super(length);
        }
        Registrations() {
            this(1);
        }
        public Registrations(DefaultTopic.Subscription... r) {
            super(r.length);
            Collections.addAll(this, r);
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

        public void add(DefaultTopic.Subscription... elements) {
            Collections.addAll(this, elements);
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