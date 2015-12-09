package objenome.util.bean.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark the method to be of the given type.
 * 
 * @author Peter Fichtner
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PropertyChangeEventMethod {

    /**
     * Type this method presents.
     * 
     * @author Peter Fichtner
     */
    enum Type {
        /** Method accepts a Listener and adds it to the list of listeners */
        ADD_LISTENER,
        /** Method accepts a Listener and removes it from the list of listeners */
        REMOVE_LISTENER,
        /** Method accepts a VetoableListener and adds it to the list of listeners */
        ADD_VETO_LISTENER,
        /** Method accepts a VetoableListener and removes it from the list of listeners */
        REMOVE_VETO_LISTENER
    }

    Type value();

}
