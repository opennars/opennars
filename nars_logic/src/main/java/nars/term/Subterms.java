package nars.term;

import java.util.function.Consumer;

/**
 * Abstracts across different implementations of classes
 * that store a tuple of subterms.
 */
public interface Subterms<T extends Term> {
    int structure();

    T term(int i);

    int volume();

    int complexity();

    int length();

    boolean containsTerm(Term t);

    void forEach(Consumer<? super T> action);
}
