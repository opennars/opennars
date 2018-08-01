package org.opennars.util;

import java.util.List;
import java.util.function.Predicate;

public class ListUtil {
    public static<T> T findAny(final List<T> l, final Predicate<T> predicate) {
        for( final T i : l) {
            if(predicate.test(i)) {
                return i;
            }
        }

        return null;
    }
}
