package nars.term;


public class TermSet extends TermVector {

    @Override
    public int init(Term[] term) {
        return super.init(
            Terms.toSortedSetArray(term)
            );
    }
}
