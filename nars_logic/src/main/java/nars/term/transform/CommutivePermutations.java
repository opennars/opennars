package nars.term.transform;

import nars.term.TermContainer;

/**
 * Created by me on 12/22/15.
 */
public class CommutivePermutations extends Termutator {
    final ShuffledSubterms perm;
    private final TermContainer y;
    private final FindSubst f;

    @Override
    public String toString() {

            return "CommutivePermutations{" +
                    "perm=" + perm.compound +
                    ", y=" + y +
                    '}';
    }

    public CommutivePermutations(FindSubst f, TermContainer x, TermContainer Y) {
        super(x);
        this.perm = new ShuffledSubterms(f.random, x);
        this.y = Y;
        this.f = f;
    }

    @Override
    public int getEstimatedPermutations() {
        return perm.total();
    }

    @Override
    public boolean next() {
        perm.next();
        return f.matchLinear(perm, y);
    }

    @Override
    public void reset() {
        perm.reset();
    }

    @Override
    public boolean hasNext() {
        return perm.hasNext();
    }
}
