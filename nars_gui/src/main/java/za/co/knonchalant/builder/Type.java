package za.co.knonchalant.builder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation indicating a specific EType to be applied to a field.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Type {
    /**
     * @return the type of the field
     */
    EType value();

    /**
     * @return any specific tag to be used for this type.
     */
    String tag() default "";
}
