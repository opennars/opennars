package nars.term.transform;

import nars.term.Term;
import nars.term.TermVector;
import nars.term.match.EllipsisMatch;
import nars.util.data.array.IntArrays;
import nars.util.math.Combinations;

import java.util.Set;

/**
 * Created by me on 12/22/15.
 */
public class Choose2 extends Termutator {

    final Combinations comb;
    private final Set<Term> yFree;
    private final Term[] x;
    private final Term xEllipsis;
    private final FindSubst f;
    private final ShuffledSubterms yy;
    boolean state;

    @Override
    public String toString() {

            return "Choose2{" +
                    "yFree=" + yFree +
                    ", xEllipsis=" + xEllipsis +
                    ", x=" + x[0] + ',' + x[1] +
                    '}';

    }

    public Choose2(FindSubst f, Term xEllipsis, Term[] x, Set<Term> yFree) {
        super(xEllipsis);
        this.f = f;
        this.x = x;
        this.yFree = yFree;
        this.xEllipsis = xEllipsis;
        int ysize = yFree.size();
        //yy = yFree.toArray(new Term[ysize]);
        yy = new ShuffledSubterms(f.random, new TermVector(yFree));
        comb = new Combinations(yy.size(), 2);
    }

    @Override
    public int getEstimatedPermutations() {
        return comb.getTotal()*2;
    }

    @Override
    public void reset() {
        comb.reset();
        state = true;
    }

    @Override
    public boolean hasNext() {
        return comb.hasNext() || !state;
    }

    @Override
    public boolean next() {

        int[] c = state ? comb.next() : comb.prev();
        state = !state;

        Term y1 = yy.term(c[0]);
        int c1 = c[1];
        IntArrays.reverse(c); //swap to try the reverse next iteration

        FindSubst f = this.f;
        Term[] x = this.x;

        if (f.match(x[0], y1)) {


            Term y2 = yy.term(c1);

            if (f.match(x[1], y2)) {
                return f.putXY(xEllipsis,
                            new EllipsisMatch(yFree, y1, y2));
            }
        }


        return false;
    }
}
