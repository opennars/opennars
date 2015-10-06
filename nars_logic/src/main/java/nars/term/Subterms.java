package nars.term;

import java.util.function.Consumer;

/**
 * Abstracts across different implementations of classes
 * that store a tuple of subterms.
 */
public interface Subterms<T extends Term> extends TermContainer {


    T term(int i);


    int length();

    boolean containsTerm(Term t);

    void forEach(Consumer<? super T> action, int start, int stop);

    default void forEach(final Consumer<? super T> action) {
        forEach(action, 0, length());
    }
}
