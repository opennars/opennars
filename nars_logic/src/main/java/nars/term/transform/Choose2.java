package nars.term.transform;

import nars.term.Term;
import nars.term.match.CollectionEllipsisMatch;

import java.util.Set;

/**
 * Created by me on 12/22/15.
 */
public class Choose2 extends Termutator {

    private final Set<Term> yFree;
    private final Term[] x;
    private final Term xEllipsis;
    private final FindSubst f;
    private int shuffle, shuffle2;
    private final Term[] yy;
    private int a, b;

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
        yy = yFree.toArray(new Term[ysize]);
    }

    @Override
    public int getEstimatedPermutations() {
        int n = yFree.size();
        return (n) * (n - 1);
    }

    @Override
    public void reset() {
        int l = yFree.size();
        this.shuffle = f.random.nextInt(l - 1); //randomize starting offset
        this.shuffle2 = f.random.nextInt(l - 1); //randomize starting offset
        this.a = l;
        this.b = l;
    }

    @Override
    public boolean hasNext() {
        return (a > 0);
    }

    @Override
    public boolean next() {
        final int ysize = yy.length;


        int ya;

        int yb;
        do {

            ya = (a + shuffle) % ysize;

            yb = (b + shuffle2 ) % ysize;

            //System.out.println(a + " (" + shuffle + ") " + b + "(" + shuffle + ") ---> " + ya + " " + yb);

            b--;

            if (b <= 0) {
                a--;
                b = ysize;
            }

        }while (yb == ya);


        Term y1 = yy[ya];

        if (f.match(x[0], y1)) {

            Term y2 = yy[yb];
            if (f.match(x[1], y2)) {
                return f.putXY(xEllipsis, new CollectionEllipsisMatch(yFree, y1, y2));
            }
        }
        return false;
    }
}
