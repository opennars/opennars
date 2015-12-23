package nars.term.transform;

import nars.term.TermContainer;

/**
 * Created by me on 12/22/15.
 */
public class CommutivePermutations extends Termutator {
    final ShuffledSubterms perm;
    private final TermContainer y;
    private final FindSubst f;
    private transient String id;

    @Override
    public String toString() {
        if (this.id == null) {
            return this.id = "CommutivePermutations{" +
                    "perm=" + perm.compound /* should be in normal sorted order if commutive */ +
                    ", y=" + y +
                    '}';
        }
        return this.id;
    }

    public CommutivePermutations(FindSubst f, TermContainer x, TermContainer Y) {
        this.perm = new ShuffledSubterms(f.random, x);
        this.y = Y;
        this.f = f;
    }

    public int getEstimatedPermutations() {
        return perm.total();
    }

    public boolean next() {
        //if (perm.hasNext())
        perm.next();

        boolean b = f.matchLinear(perm, y, 0, perm.size());

        return b;
    }

    public void reset() {
        perm.reset();
        //perm.next();
    }

    public boolean hasNext() {
        return perm.hasNext();
    }
}
