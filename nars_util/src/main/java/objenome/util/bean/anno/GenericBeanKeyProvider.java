package objenome.util.bean.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides access to the generic access key. If the access key attribute is not "value" the
 * appropriate attribute has to be annotated with {@link GenericBeanKeyMethod}.
 * 
 * @see GenericBeanKeyMethod
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface GenericBeanKeyProvider {
    // marker, no attributes
}
