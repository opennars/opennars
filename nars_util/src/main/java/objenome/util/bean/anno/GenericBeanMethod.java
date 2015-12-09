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
public @interface GenericBeanMethod {

    /**
     * Type this method presents.
     * 
     * @author Peter Fichtner
     */
    enum Type {
        /** Method accepts a key and returns a value */
        GENERIC_GET,
        /** Method accepts key and value */
        GENERIC_SET,
        /** Method accepts a key and returns true if the attribute was set yet */
        IS_SET,
        /** Method returns all keys the bean supports */
        KEYS
    }

    Type value();

}
