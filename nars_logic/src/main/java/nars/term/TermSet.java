package nars.term;


public class TermSet extends TermVector {

//    public static TermSet newTermSet(Term... unsorted) {
//        return new TermSet(Terms.toSortedSetArray(unsorted));
//    }

    public static TermSet newTermSetPresorted(Term... presorted) {
        return new TermSet(presorted);
    }

    protected TermSet(Term... sortedAndUnduplicated) {
        super(sortedAndUnduplicated);
    }
}
