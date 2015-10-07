package nars.guifx.annotation;


public @interface Range {
    double min() default Double.NaN;
    double max() default Double.NaN;
}
