package nars.term;


public class TermSet extends TermVector {

    @Override
    public void init(Term[] term) {
        super.init(
            Terms.toSortedSetArray(term)
            );
    }
}
