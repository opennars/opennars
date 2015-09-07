package nars.term;

import nars.Memory;
import nars.Op;
import nars.nal.nal1.Inheritance;
import nars.nal.nal2.Similarity;
import nars.nal.nal5.Junction;
import nars.term.transform.FindSubst;
import org.apache.commons.collections.map.Flat3Map;
import org.infinispan.commons.equivalence.ByteArrayEquivalence;

import java.util.Map;
import java.util.Random;

/**
 * Static utility class for static methods related to Variables
 * TODO rename all *substitute* methods to *unify*
 */
public class Variables {

    public static boolean findSubstitute(final Op varType, final Term term1, final Term term2, final Map<Term, Term> map1, final Map<Term, Term> map2, final Memory memory) {
        return findSubstitute(varType, term1, term2, map1, map2, memory.random);
    }

    public static boolean findSubstitute(final Op varType, final Term term1, final Term term2, final Map<Term, Term> map1, final Map<Term, Term> map2, final Random random) {
        return new FindSubst(varType, map1, map2, random).next(term1, term2);
    }

    /**
     * map is a 2-element array of HashMap<Term,Term>. it may be null, in which case
     * the maps will be instantiated as necessary.
     * this is to delay the instantiation of the 2 HashMap until necessary to avoid
     * wasting them if they are not used.
     */
    @Deprecated public static boolean findSubstitute(final Op varType, final Term term1, final Term term2, final Map<Term, Term>[] map, final Random random) {
        return new FindSubst(varType, map[0], map[1], random).next(term1, term2);
    }


//    public static final boolean containVar(final Term[] t) {
//        for (final Term x : t)
//            if (x instanceof Variable)
//                return true;
//        return false;
//    }


    /**
     * To unify two terms
     *
     * @param varType The varType of variable that can be substituted
     * @param t    The first and second term as an array, which will have been modified upon returning true
     * @return Whether the unification is possible.  't' will refer to the unified terms
     */
    public static boolean unify(final Op varType, final Term[] t, final Random random) {
        return unify(varType, t[0], t[1], t, random);
    }


    /**
     * To unify two terms
     *
     * @param varType      The varType of variable that can be substituted
     * @param compound1 The compound containing the first term, possibly modified
     * @param compound2 The compound containing the second term, possibly modified
     * @param t         The first and second term as an array, which will have been modified upon returning true
     * @return Whether the unification is possible.  't' will refer to the unified terms
     */
    public static boolean unify(final Op varType, final Term t1, final Term t2, final Term[] compound, final Random random) {
        final Map<Term, Term> map[] = new Map[2]; //begins empty: null,null

        final boolean hasSubs = findSubstitute(varType, t1, t2, map, random);
        if (hasSubs) {
            final Term a = applySubstituteAndRenameVariables(((Compound) compound[0]), map[0]);
            if (a == null) return false;

            final Term b = applySubstituteAndRenameVariables(((Compound) compound[1]), map[1]);
            if (b == null) return false;


            if(compound[0] instanceof Variable && compound[0].hasVarQuery() && (a.hasVarIndep() || a.hasVarDep()) ) {
                return false;
            }
            if(compound[1] instanceof Variable && compound[1].hasVarQuery() && (b.hasVarIndep() || b.hasVarDep()) ) {
                return false;
            }

            //only set the values if it will return true, otherwise if it returns false the callee can expect its original values untouched
            compound[0] = a;
            compound[1] = b;
            return true;
        }
        return false;
    }

    /**
     * appliesSubstitute and renameVariables, resulting in a cloned object,
     * will not change this instance
     */
    private static Term applySubstituteAndRenameVariables(final Compound t, final Map<Term, Term> subs) {
        if ((subs == null) || (subs.isEmpty())) {
            //no change needed
            return t;
        }

        Term r = t.applySubstitute(subs);

        if (r == null) return null;

        if (r.equals(t)) return t;

        return r;
    }

//    /**
//     * Check whether a string represent a name of a term that contains a query
//     * variable
//     *
//     * @param n The string name to be checked
//     * @return Whether the name contains a query variable
//     */
//    public static boolean containVarQuery(final CharSequence n) {
//        return Texts.containsChar(n, Symbols.VAR_QUERY);
//    }

//    /**
//     * Check whether a string represent a name of a term that contains a
//     * dependent variable
//     *
//     * @param n The string name to be checked
//     * @return Whether the name contains a dependent variable
//     */
//    public static boolean containVarDep(final CharSequence n) {
//        return Texts.containsChar(n, Symbols.VAR_DEPENDENT);
//    }

//    public static Variable makeCommonVariable(final Term v1, final Term v2) {
//
//        return
//        //return new Variable(v1.toString() + v2.toString() + '$');
//    }


    public static class CommonVariable extends Variable {

        public static CommonVariable make(Term v1, Term v2) {
            //TODO use more efficient string construction
            byte[] bv1 = v1.bytes();
            byte[] bv2 = v2.bytes();

            //lexical ordering: swap
            if (ByteArrayEquivalence.INSTANCE.compare(bv1, bv2) > 0) {
                byte[] t = bv1;
                bv1 = bv2;
                bv2 = t;
            }

            int len = bv1.length + bv2.length + 1;
            byte[] c = new byte[len];
            System.arraycopy(bv1, 0, c, 0, bv1.length);
            System.arraycopy(bv2, 0, c, bv1.length, bv2.length);

            c[c.length-1] = '$';
            return new CommonVariable(c);
        }

        CommonVariable(byte[] b) {
            super(b, true /* scoped, so that CommonVariables with equal names can be considered equal */);
        }


    }

//    public static boolean containVarDepOrIndep(final CharSequence n) {
//        final int l = n.length();
//        for (int i = 0; i < l; i++) {
//            char c = n.charAt(i);
//            if ((c == Symbols.VAR_INDEPENDENT) || (c == Symbols.VAR_DEPENDENT)) {
//                return true;
//            }
//        }
//        return false;
//    }

//    /**
//     * Check whether a string represent a name of a term that contains an
//     * independent variable
//     *
//     * @param n The string name to be checked
//     * @return Whether the name contains an independent variable
//     */
//    public static boolean containVarIndep(final CharSequence n) {
//        return Texts.containsChar(n, Symbols.VAR_INDEPENDENT);
//    }    

    /**
     * test the invalidity of a term which may be using independent variables
     */
    public static boolean indepVarUsedInvalid(Term T) {


        //if its a conjunction/disjunction, this is invalid: (&&,<$1 --> test>,<$1 --> test2>), while this isnt: (&&,<$1 --> test ==> <$1 --> test2>,others)
        //this means we have to go through the conjunction, and check if the component is a indepVarUsedInvalid instance, if yes, return true
        //
        if (T instanceof Junction) {
            for (Term t : ((Compound<?>) T)) {
                if (indepVarUsedInvalid(t)) {
                    return true;
                }
            }
        }

        if (!(T instanceof Inheritance) && !(T instanceof Similarity)) {
            return false;
        }

        return T.hasVarIndep();
    }

    /**
     * Check if two terms can be unified
     *
     * @param varType  The varType of variable that can be substituted
     * @param term1 The first term to be unified
     * @param term2 The second term to be unified
     * @return Whether there is a substitution
     */
    public static boolean hasSubstitute(final Op varType, final Term term1, final Term term2, final Random random) {
        return findSubstitute(varType, term1, term2,
                new Flat3Map(), new Flat3Map(),
                random);
    }

}
