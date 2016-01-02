package nars.term;


import java.util.Collection;
import java.util.Set;

public class TermSet extends TermVector {

    public static TermSet the(Term... x) {
        return new TermSet(Terms.toSortedSetArray(x));
    }
    public static TermSet the(Set<Term> x) {
        return new TermSet(x);
    }

//    public static TermSet newTermSetPresorted(Term... presorted) {
//        return new TermSet(presorted);
//    }

    protected TermSet(Term... x) {
        super(x);
    }
    protected TermSet(Set<Term> x) {
        super((Collection)x);
    }


}
