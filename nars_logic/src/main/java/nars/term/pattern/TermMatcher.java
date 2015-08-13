package nars.term.pattern;

import nars.term.Termed;

import java.util.function.Predicate;

/**
 * Created by me on 6/6/15.
 */
public interface TermMatcher extends Predicate<Termed> {

    @Override
    default boolean test(Termed t) {
        return match(t);
    }

    boolean match(Termed t);

    default boolean matchRecursively(Termed t, boolean includeRoot, int maxDepth) {
        //TODO
        return false;
    }

}
