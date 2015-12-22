/*
 * IntersectionInt.java
 *
 * Copyright (C) 2008  Pei Wang
 *
 * This file is part of Open-NARS.
 *
 * Open-NARS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Open-NARS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.nal.nal3;

import nars.Op;
import nars.term.Term;
import nars.term.Terms;

/**
 * A compound term whose intension is the intersection of the extensions of its term
 */
public class IntersectionInt extends Intersect {

    /**
     * Constructor with partial values, called by make
     * @param arg The component list of the term
     */
    private IntersectionInt(final Term[] arg) {
        super();
        
        init(flattenAndSort(arg));
    }

    public static Term[] flattenAndSort(Term[] args) {
        //determine how many there are with same order

        int expandedSize;
        while ((expandedSize = getFlattenedLength(args)) != args.length) {
            args = _flatten(args, expandedSize);
        }
        return Terms.toSortedSetArray(args);
    }

    public static IntersectionInt isIntersectionInt(Term t) {
        if (t instanceof IntersectionInt) {
            IntersectionInt c = (IntersectionInt) t;
            return c;
        }
        return null;
    }

    private static Term[] _flatten(Term[] args, int expandedSize) {
        final Term[] ret = new Term[expandedSize];
        int k = 0;
        for (int i = 0; i < args.length; i++) {
            Term a = args[i];
            IntersectionInt c = isIntersectionInt(a);
            if (c != null) {
                //arraycopy?
                for (Term t : c.cloneTerms()) {
                    ret[k++] = t;
                }
            } else {
                ret[k++] = a;
            }
        }

        return ret;
    }

    protected static int getFlattenedLength(Term[] args) {
        int sz = 0;
        for (int i = 0; i < args.length; i++) {
            Term a = args[i];
            IntersectionInt c = isIntersectionInt(a);
            if (c != null)
                sz += c.size();
            else
                sz += 1;
        }
        return sz;
    }

    /**
     * Clone an object
     * @return A new object, to be casted into a Conjunction
     */
    @Override
    public IntersectionInt clone() {
        return new IntersectionInt(terms.term);
    }

  @Override
    public Term clone(Term[] replaced) {
        return make(replaced);
    }
        

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

    public static Term make(Term[] t) {
        t = Terms.toSortedSetArray(t);
        switch (t.length) {
            case 0: throw new RuntimeException("zero arguments invalid for set");
            case 1: return t[0];
            //TODO case 2: return make(t[0], t[1]);
            //TODO 3: make(t[0], make(t[1], t[2]) ... etc??
            default:
               return new IntersectionInt(t); 
        }
    }
    
    
    /**
     * Get the operate of the term.
     * @return the operate of the term
     */
    @Override
    public final Op op() {
        return Op.INTERSECTION_INT;
    }


}
