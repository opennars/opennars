package nars.nal.nal3;

import nars.Op;
import nars.term.Term;
import nars.term.compound.GenericCompound;


/**
 * Common parent class for IntersectInt and IntersectExt
 */
public interface Intersect {

    public static Term intersect(boolean intOrExt, Term[] t) {

        switch (t.length) {
            case 0: throw new RuntimeException("zero arguments invalid for set");
            case 1: return t[0];
            /*case 2:
                //flatten
                if ((t[0] instanceof SetTensional) &&
                    (t[1] instanceof SetTensional) && t[0].op() == t[1].op())
                        return IntersectionExt.make(t[0], t[1]);
                else {
                    //fall-through to default
                }
            } */
            //TODO 3: make(t[0], make(t[1], t[2]) ... etc??

        }
        return GenericCompound.c(
                intOrExt ? Op.INTERSECTION_INT : Op.INTERSECTION_EXT,
                t);
    }


//    /**
//     * Try to make a new compound from two term. Called by the logic rules.
//     * @param term1 The first compoment
//     * @param term2 The first compoment
//     * @return A compound generated or a term it reduced to
//     */
//    public static Term make(Term term1, Term term2) {
//
//        if ((term1 instanceof SetInt) && (term2 instanceof SetInt)) {
//            // set union
//            Term[] both = ObjectArrays.concat(
//                    ((SetInt) term1).terms(),
//                    ((SetInt) term2).terms(), Term.class);
//            return SetInt.make(both);
//        }
//        if ((term1 instanceof SetExt) && (term2 instanceof SetExt)) {
//            // set intersection
//
//            Set<Term> set = Terms.toSortedSet();
//            set.retainAll(Terms.toSet(term2));
//
//            //technically this can be used directly if it can be converted to array
//            //but wait until we can verify that TreeSet.toarray does it or write a helper function like existed previously
//            return SetExt.make(set.toArray(new Term[set.size()]));
//        }
//
//        if (term1 instanceof IntersectionExt) {
//            List<Term> se = Global.newArrayList();
//            ((Compound) term1).addTermsTo(se);
//            if (term2 instanceof IntersectionExt) {
//                // (&,(&,P,Q),(&,R,S)) = (&,P,Q,R,S)
//                ((Compound) term2).addTermsTo(se);
//            }
//            else {
//                // (&,(&,P,Q),R) = (&,P,Q,R)
//                se.add(term2);
//            }
//            return make(se.toArray(new Term[se.size()]));
//        } else if (term2 instanceof IntersectionExt) {
//            List<Term> se = Global.newArrayList();
//            // (&,R,(&,P,Q)) = (&,P,Q,R)
//            ((Compound) term2).addTermsTo(se);
//            se.add(term1);
//            return make(se.toArray(new Term[se.size()]));
//        } else {
//            return make(new Term[] { term1, term2 } );
//        }
//
//    }

    //    /**
//     * Try to make a new compound from two term. Called by the logic rules.
//     * @param term1 The first compoment
//     * @param term2 The first compoment
//     * @param memory Reference to the memory
//     * @return A compound generated or a term it reduced to
//     */
//    public static Term make(Term term1, Term term2) {
//
//        if ((term1 instanceof SetExt) && (term2 instanceof SetExt)) {
//            // set union
//            //TODO dont assume that all Sets are implemented via Compounds here:
//            Term[] both = ObjectArrays.concat(
//                    ((Compound) term1).term,
//                    ((Compound) term2).term, Term.class);
//            return SetExt.make(both);
//        }
//        if ((term1 instanceof SetInt) && (term2 instanceof SetInt)) {
//            // set intersection
//
//            Set<Term> set = Terms.toSortedSet();
//            set.retainAll(Terms.toSet(term2));
//
//            //technically this can be used directly if it can be converted to array
//            //but wait until we can verify that TreeSet.toarray does it or write a helper function like existed previously
//            return SetInt.make(set.toArray(new Term[set.size()]));
//        }
//
//        List<Term> se = new ArrayList();
//        if (term1 instanceof IntersectionInt) {
//            ((Compound) term1).addTermsTo(se);
//            if (term2 instanceof IntersectionInt) {
//                // (&,(&,P,Q),(&,R,S)) = (&,P,Q,R,S)
//                ((Compound) term2).addTermsTo(se);
//            }
//            else {
//                // (&,(&,P,Q),R) = (&,P,Q,R)
//                se.add(term2);
//            }
//        } else if (term2 instanceof IntersectionInt) {
//            // (&,R,(&,P,Q)) = (&,P,Q,R)
//            ((Compound) term2).addTermsTo(se);
//            se.add(term1);
//        } else {
//            se.add(term1);
//            se.add(term2);
//        }
//        return make(se.toArray(new Term[se.size()]));
//    }
//
//    public static Term make(Collection<Term> unsorted) {
//        return make(unsorted.toArray(new Term[unsorted.size()]));
//    }

}
