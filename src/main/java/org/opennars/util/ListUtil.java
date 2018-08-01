package org.opennars.util;

import java.util.List;
import java.util.function.Predicate;

public class ListUtil {
    /**
     * tries to select the first element where the predicate matches from front (index 0) to the end of the list
     *
     * @param candidates the candidates from which the method may select the first one
     * @param predicate the checked predicate for each element
     * @param <T> generic type
     * @return element which matched first, null if none matched
     */
    public static<T> T findAny(final List<T> candidates, final Predicate<T> predicate) {
        for( final T i : candidates) {
            if(predicate.test(i)) {
                return i;
            }
        }

        return null;
    }
}
