package nars.term;


import java.util.Collection;

import static nars.term.Terms.toSortedSetArray;

public class TermSet<X extends Term> extends TermVector<X> {

    public static TermSet the(Term... x) {
        return new TermSet(toSortedSetArray(x));
    }

    public static TermSet the(Collection<Term> x) {
        return new TermSet(toSortedSetArray(x));
    }

//    public static TermSet newTermSetPresorted(Term... presorted) {
//        return new TermSet(presorted);
//    }

    private TermSet(X... x) {
        super(x);
    }

    @Override public final boolean isSorted() {
        return true;
    }
}
