
package nars.util.event;

import java.lang.reflect.Field;
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
    public static OnTopics all(final Object obj, BiConsumer<String /* fieldName*/,Object /* value */> f) {


        OnTopics s = new OnTopics();

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

    On on(Consumer<V> o);
    void off(On<V> reaction);

    public static <V> OnTopics onAll(final Consumer<V> o, final Topic<V>... w) {
        OnTopics r = new OnTopics(w.length);
    
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