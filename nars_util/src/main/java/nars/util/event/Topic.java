
package nars.util.event;

import nars.util.data.id.Named;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * notifies subscribers when a value is emitted
 */
public interface Topic<V extends Serializable> extends Serializable, Named<String> {

    void delete();

    List<Consumer<V>> all();


    /** registers to all public Topic fields in an object */
    static Active all(final Object obj, BiConsumer<String /* fieldName*/, Object /* value */> f) {


        Active s = new Active();

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

    static <V extends Serializable> Active onAll(final Consumer<V> o, final Topic<V>... w) {
        Active r = new Active(w.length);
    
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