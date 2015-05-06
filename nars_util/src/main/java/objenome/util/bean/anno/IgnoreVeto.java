package objenome.util.bean.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Changes of fields annotated with this annotation cannot be prevented by a VetoableChangeListener.
 * 
 * @author Peter Fichtner
 */
@Target(value = ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreVeto {
    // marker, no attributes
}