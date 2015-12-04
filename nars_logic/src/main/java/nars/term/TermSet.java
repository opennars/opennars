package nars.term;


public class TermSet extends TermVector {

    public TermSet(Term... t) {
        super(
            Terms.toSortedSetArray(t)
        );
    }
}
