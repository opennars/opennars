package nars.meta.pre;

import nars.nal.nal4.ProductN;
import nars.term.Atom;
import nars.term.Term;

/**
 * TODO decide if the volume bounds are correct when varargs are involved,
 * and if not, make a special case
 */
public class PairMatchingProduct extends ProductN {

    public int volA, volB;
    public int structureA, structureB; //should use the long stuctureHash?

    //public final static Variable any = new Variable("%1"); //just use the first pattern variable because it will overlap with it

    public PairMatchingProduct() {
        this(Atom.Null, Atom.Null);
    }

    public PairMatchingProduct(Term a, Term b) {
        super(a, b);
    }

    @Override
    protected void init(final Term... term) {
        super.init(term);

        final Term a = term[0];
        volA = a.volume();
        structureA = a.structure();

        final Term b = term[1];
        volB = b.volume();
        structureB = b.structure();
    }


    public void set(final Term a, final Term b) {
        this.term[0] = a;
        this.term[1] = b;
        init(term);
    }

    @Override
    protected int getStructureBase() {
        return 0;
    }

    public final boolean substitutesMayExist(final PairMatchingProduct pattern) {
        //return substitutesMayExistParanoid(pattern);
        return substitutesMayExistFast(pattern);
    }

//    private static boolean substitutesMayExistParanoid(PairMatchingProduct pattern) {
//        return true;
//    }


    public final boolean substitutesMayExistFast(final PairMatchingProduct pattern) {
        //the pattern structure will include a Product
        if (impossibleToMatch(pattern.structure())) {
//            System.out.println(
//                "impossible? " +
//                        Integer.toBinaryString(structure()) + " " + this +
//                        Integer.toBinaryString(pattern.structure()) + " " + pattern
//            );

            return false;
        }

//        if (volume() < pattern.volume())
//            return false;

        if (volA < pattern.volA)
            return false;

        if (volB < pattern.volB)
            return false;

        //compare the task and belief individually to filter out more:

        final Term c = term[0];
        if (c.impossibleToMatch(pattern.structureA)) return false;

        final Term d = term[1];
        if (d.impossibleToMatch(pattern.structureB)) return false;

        return true;
    }


//    @Override
//    protected <T extends Term> T normalized(boolean destructive) {
//        return (T) this;
//    }

}
