package nars.nal.nal3;

/**
 * Common parent class for IntersectInt and IntersectExt
 */
public interface Intersect {

//    static Term intersect(boolean intOrExt, Term[] t) {
//        return GenericCompound.COMPOUND(
//                intOrExt ? Op.INTERSECT_INT : Op.INTERSECT_EXT,
//                t);
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
