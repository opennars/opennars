package nars.nal.meta;

import nars.nal.nal4.ProductN;
import nars.term.Term;
import nars.term.atom.Atom;

/**
 * just holds two terms, not really necessary
 */
public class TaskBeliefPair extends ProductN {

    private final Term[] t;
    public int volA, volB;
    public int structureA, structureB; //should use the long stuctureHash?

    //public final static Variable any = new Variable("%1"); //just use the first pattern variable because it will overlap with it

    public TaskBeliefPair() {
        this(Atom.Null, Atom.Null);
    }

    public TaskBeliefPair(Term a, Term b) {
        super(new Term[] { a, b });

        t = this.terms();


        volA = a.volume();
        structureA = a.structure();

        volB = b.volume();
        structureB = b.structure();
    }



    public final void set(final Term a, final Term b) {
        Term[] t = this.t;
        t[0] = a;
        t[1] = b;
    }



//    public final boolean substitutesMayExist(final TaskBeliefPair pattern) {
//        //return substitutesMayExistParanoid(pattern);
//        return substitutesMayExistFast(pattern);
//    }

//    private static boolean substitutesMayExistParanoid(PairMatchingProduct pattern) {
//        return true;
//    }


//    public final boolean substitutesMayExistFast(final TaskBeliefPair pattern) {
////        //the pattern structure will include a Product
////        if (impossibleToMatch(pattern.structure())) {
//////            System.out.println(
//////                "impossible? " +
//////                        Integer.toBinaryString(structure()) + " " + this +
//////                        Integer.toBinaryString(pattern.structure()) + " " + pattern
//////            );
////
////            return false;
////        }
//
////        if (volume() < pattern.volume())
////            return false;
//
//        //compare the task and belief individually to filter out more:
//
//        //final Term c = term(0);
//        if (Term.impossibleToMatch(structureA, pattern.structureA)) return false;
//
//        //final Term d = term(1);
//        if (Term.impossibleToMatch(structureB, pattern.structureB)) return false;
//
//
//        if (volA < pattern.volA)
//            return false;
//
//        if (volB < pattern.volB)
//            return false;
//
//
//        return true;
//    }


//    @Override
//    protected <T extends Term> T normalized(boolean destructive) {
//        return (T) this;
//    }

}
