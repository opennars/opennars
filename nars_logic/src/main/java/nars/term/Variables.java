package nars.term;

import nars.Global;
import nars.Memory;
import nars.Symbols;
import nars.nal.nal1.Inheritance;
import nars.nal.nal2.Similarity;
import nars.nal.nal4.Image;
import nars.nal.nal5.Junction;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Static utility class for static methods related to Variables
 * TODO rename all *substitute* methods to *unify*
 */
public class Variables {

    public static boolean findSubstitute(final char type, final Term term1, final Term term2, final Map<Term, Term> map1, final Map<Term, Term> map2, final Memory memory) {
        return findSubstitute(type, term1, term2, map1, map2, memory.random);
    }

    public static boolean findSubstitute(final char type, final Term term1, final Term term2, final Map<Term, Term> map1, final Map<Term, Term> map2, final Random random) {
        return findSubstitute(type, term1, term2, new Map[]{map1, map2}, random);
    }

    /**
     * map is a 2-element array of HashMap<Term,Term>. it may be null, in which case
     * the maps will be instantiated as necessary.
     * this is to delay the instantiation of the 2 HashMap until necessary to avoid
     * wasting them if they are not used.
     */
    public static boolean findSubstitute(final char type, final Term term1, final Term term2, final Map<Term, Term>[] map, final Random random) {

        final boolean term1HasVar = term1.hasVar(type);
        final boolean term2HasVar = term2.hasVar(type);


        final boolean term1Var = term1 instanceof Variable;
        final boolean term2Var = term2 instanceof Variable;
        final boolean termsEqual = term1.equals(term2);
        if (!term1Var && !term2Var && termsEqual) {
            return true;
        }
        
        
        /*if (termsEqual) {
            return true;
        }*/

        int ds = 4; //initial size of hashmap below TODO tune

        Term t;
        if (term1Var && (((Variable) term1).getType() == type)) {
            final Variable var1 = (Variable) term1;
            t = map[0] != null ? map[0].get(var1) : null;

            if (t != null) {
                return findSubstitute(type, t, term2, map, random);
            } else {

                if (map[0] == null) {
                    map[0] = Global.newHashMap(ds);
                    map[1] = Global.newHashMap(ds);
                }

                if ((term2 instanceof Variable) && (((Variable) term2).getType() == type)) {
                    Variable CommonVar = makeCommonVariable(term1, term2);
                    map[0].put(var1, CommonVar);
                    map[1].put(term2, CommonVar);
                } else {
                    if (term2 instanceof Variable) {
                        //https://github.com/opennars/opennars/commit/dd70cb81d22ad968ece86a549057cd19aad8bff3

                        boolean t1Query = ((Variable) term1).getType() == Symbols.VAR_QUERY;
                        boolean t2Query = ((Variable) term2).getType() == Symbols.VAR_QUERY;

                        if ((t2Query && !t1Query) || (!t2Query && t1Query)) {
                            return false;
                        }
                    }

                    map[0].put(var1, term2);
                    if (var1.isCommon()) {
                        map[1].put(var1, term2);
                    }
                }
                return true;
            }
        } else if (term2Var && (((Variable) term2).getType() == type)) {
            final Variable var2 = (Variable) term2;
            t = map[1] != null ? map[1].get(var2) : null;

            if (t != null) {
                return findSubstitute(type, term1, t, map, random);
            } else {

                if (map[0] == null) {
                    map[0] = Global.newHashMap(ds);
                    map[1] = Global.newHashMap(ds);
                }

                map[1].put(var2, term1);
                if (var2.isCommon()) {
                    map[0].put(var2, term1);
                }
                return true;
            }
        } else if ((term1HasVar || term2HasVar) && (term1 instanceof Compound) && (Terms.equalType(term1, term2))) {
            final Compound cTerm1 = (Compound) term1;
            final Compound cTerm2 = (Compound) term2;
            if (cTerm1.length() != cTerm2.length()) {
                return false;
            }
            //TODO simplify comparison with Image base class
            if ((cTerm1 instanceof Image) && (((Image) cTerm1).relationIndex != ((Image) cTerm2).relationIndex)) {
                return false;
            }
            Term[] list = cTerm1.cloneTerms();
            if (cTerm1.isCommutative()) {
                Compound.shuffle(list, random);
            }
            for (int i = 0; i < cTerm1.length(); i++) {
                Term t1 = list[i];
                Term t2 = cTerm2.term[i];
                if (!findSubstitute(type, t1, t2, map, random)) {
                    return false;
                }
            }
            return true;
        }

        return termsEqual;
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
     * @param type The type of variable that can be substituted
     * @param t    The first and second term as an array, which will have been modified upon returning true
     * @return Whether the unification is possible.  't' will refer to the unified terms
     */
    public static boolean unify(final char type, final Term[] t, final Random random) {
        return unify(type, t[0], t[1], t, random);
    }


    /**
     * To unify two terms
     *
     * @param type      The type of variable that can be substituted
     * @param compound1 The compound containing the first term, possibly modified
     * @param compound2 The compound containing the second term, possibly modified
     * @param t         The first and second term as an array, which will have been modified upon returning true
     * @return Whether the unification is possible.  't' will refer to the unified terms
     */
    public static boolean unify(final char type, final Term t1, final Term t2, final Term[] compound, final Random random) {
        final Map<Term, Term> map[] = new Map[2]; //begins empty: null,null

        final boolean hasSubs = findSubstitute(type, t1, t2, map, random);
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

    public static Variable makeCommonVariable(final Term v1, final Term v2) {
        //TODO use more efficient string construction
        byte[] bv1 = v1.bytes();
        byte[] bv2 = v2.bytes();
        int len = bv1.length + bv2.length + 1;
        byte[] c = new byte[len];
        System.arraycopy(bv1, 0, c, 0, bv1.length);
        System.arraycopy(bv2, 0, c, bv1.length, bv2.length);
        c[c.length-1] = '$';
        return new Variable(c);

        //return new Variable(v1.toString() + v2.toString() + '$');
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
            for (Term t : ((Compound) T)) {
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
     * @param type  The type of variable that can be substituted
     * @param term1 The first term to be unified
     * @param term2 The second term to be unified
     * @return Whether there is a substitution
     */
    public static boolean hasSubstitute(final char type, final Term term1, final Term term2, final Random random) {
        return findSubstitute(type, term1, term2, new HashMap<>(), new HashMap<>(), random);
    }

}
