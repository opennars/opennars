
package nars.util.event;

import nars.util.data.id.Named;

import java.lang.reflect.Field;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * notifies subscribers when a value is emitted
 */
public interface Topic<V> extends Named<String> {

    void delete();

    //List<Consumer<V>> all();


    static Active all(final Object obj, BiConsumer<String /* fieldName*/, Object /* value */> f) {
        return all(obj, f, (key)->true);
    }

    static void each(final Object obj, Consumer<Field /* fieldName*/> f) {
        /** TODO cache the fields because reflection may be slow */
        for (Field field : obj.getClass().getFields()) {
            Class returnType = field.getType();
            if (returnType.equals(Topic.class)) {
                f.accept(field);
            }

            //System.out.println(obj + "  " + f + " " + returnType);
        }

    }


    /** registers to all public Topic fields in an object */
    static Active all(final Object obj, BiConsumer<String /* fieldName*/, Object /* value */> f, Predicate<String> includeKey) {

        Active s = new Active();

        each(obj, (field) -> {
            final String fieldName = field.getName();
            if (includeKey!=null && !includeKey.test(fieldName))
                return;

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

        });


        return s;
    }



    /** TODO rename to 'out' to match Streams api */
    void emit(V arg);

    On on(Consumer<V> o);
    void off(On<V> reaction);

    @SafeVarargs
    static <V> Active onAll(final Consumer<V> o, final Topic<V>... w) {
        Active r = new Active(w.length);
    
        for (final Topic<V> c : w)
            r.add( c.on(o) );
        
        return r;
    }

    int size();

    boolean isEmpty();


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