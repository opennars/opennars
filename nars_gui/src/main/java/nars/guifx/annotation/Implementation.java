package nars.guifx.annotation;

import java.lang.annotation.*;

/**
 * Represents a choice of implementation classes
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Repeatable(value = Implementations.class)
public @interface Implementation {
    Class value();
}
