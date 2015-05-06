package objenome.util.bean.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Interfaces can have Initializers that are called for each created proxy instance. An Initializer
 * has to declare a public static method taking an instance of the interface type as parameter.
 * 
 * @author Peter Fichtner
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InitializerMethod {
    // marker, no attributes
}
