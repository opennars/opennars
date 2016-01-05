package nars.term.transform;

import nars.term.Term;
import nars.term.match.EllipsisMatch;

import java.util.Set;

/**
 * choose 1 at a time from a set of N, which means iterating up to N
 * will remove the chosen item(s) from Y if successful before returning
 */
public class Choose1 extends Termutator {

    private final Set<Term> yFree;
    private final Term x;
    private final Term xEllipsis;
    private final FindSubst f;
    private int shuffle;
    private final Term[] yy;
    private int count;

    @Override
    public String toString() {

        return "Choose1{" +
                "yFree=" + yFree +
                ", xEllipsis=" + xEllipsis +
                ", x=" + x +
                '}';
    }

    public Choose1(FindSubst f, Term xEllipsis, Term x, Set<Term> yFree) {
        super(xEllipsis);

        int ysize = yFree.size();
        if (ysize < 2) {
            throw new RuntimeException(yFree + " offers no choice");
        }

        this.f = f;
        this.x = x;


        this.yFree = yFree;
        this.xEllipsis = xEllipsis;

        yy = yFree.toArray(new Term[ysize]);
    }

    @Override
    public int getEstimatedPermutations() {
        return yy.length;
    }

    @Override
    public void reset() {
        int l = yy.length;
        this.count = l - 1;
        this.shuffle = f.random.nextInt(l - 1); //randomize starting offset
    }

    @Override
    public boolean hasNext() {
        return count >= 0;
    }

    @Override
    public boolean next() {
        final int ysize = yy.length;

        Term y = yy[(shuffle + count) % ysize];
        count--;

        if (y.equals(x))
            return true; //throw new RuntimeException("fault");

        boolean matched = f.match(x, y);

        if (matched) {
            return f.putXY(xEllipsis, new EllipsisMatch(yFree, y));
        }

        return false;
    }
}
