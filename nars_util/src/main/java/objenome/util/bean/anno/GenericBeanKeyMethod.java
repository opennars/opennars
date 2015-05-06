package objenome.util.bean.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * If a GenericBeanKey has more then one attribute or the attribute's name is not <code>value</code>
 * the method has to be marked with this annotation.
 * 
 * @see GenericBeanKeyProvider
 * @author Peter Fichtner
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface GenericBeanKeyMethod {
    // marker, no attributes
}
