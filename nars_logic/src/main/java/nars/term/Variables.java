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
        return new FindSubst(type, map1, map2, random).get(term1, term2);
    }

    /**
     * map is a 2-element array of HashMap<Term,Term>. it may be null, in which case
     * the maps will be instantiated as necessary.
     * this is to delay the instantiation of the 2 HashMap until necessary to avoid
     * wasting them if they are not used.
     */
    @Deprecated public static boolean findSubstitute(final char type, final Term term1, final Term term2, final Map<Term, Term>[] map, final Random random) {
        FindSubst f = new FindSubst(type, map[0], map[1], random);
        boolean b = f.get(term1, term2);
        map[0] = f.map0;
        map[1] = f.map1;
        return b;
    }

    private static class FindSubst {
        private final char type;
        private final Map<Term, Term> map0, map1;
        private final Random random;

        /**
         */
        private FindSubst(char type, Map map0, Map map1, Random random) {
            if (map0 == null)
                map0 = Global.newHashMap(0);
            if (map1 == null)
                map1 = Global.newHashMap(0);

            this.type = type;
            this.map0 = map0;
            this.map1 = map1;
            this.random = random;

        }

        @Deprecated public Map<Term, Term>[] getMap() {
            return new Map[] { map0, map1 };
        }

        public final boolean get(final Term term1, final Term term2) {
            //print("Before", term1, term2);
            boolean r = next(term1, term2);
            //print("  " + r + " After", null, null);
            return r;
        }

        @Override
        public String toString() {
            return type + ":" + map0 + ","+ map1;
        }

        private void print(String prefix, Term a, Term b) {
            System.out.print(prefix);
            if (a!=null)
                System.out.println(" " + a + " ||| " + b);
            else
                System.out.println();
            System.out.println("     " + this);
        }

        /** recursess into the next sublevel of the term */
        protected boolean next(Term term1, Term term2) {

            final boolean term1HasVar = term1.hasVar(type);
            final boolean term2HasVar = term2.hasVar(type);

            final Variable term1Var = term1 instanceof Variable ? (Variable) term1 : null;
            final Variable term2Var = term2 instanceof Variable ? (Variable) term2 : null;

            final boolean termsEqual = term1.equals(term2);
            if (term1Var!=null && term2Var!=null && termsEqual) {
                return true;
            }

            if (term1Var!=null && term1Var.getType() == type) {

                final Term t = map0.get(term1Var);

                if (t != null)
                    return next(t, term2);

                if (term2Var!=null && term2Var.getType() == type) {
                    Variable commonVar = CommonVariable.make(term1, term2);
                    map0.put(term1Var, commonVar);
                    map1.put(term2Var, commonVar);
                } else {
                    if (term2Var!=null) {
                        //https://github.com/opennars/opennars/commit/dd70cb81d22ad968ece86a549057cd19aad8bff3

                        boolean t1Query = term1Var.getType() == Symbols.VAR_QUERY;
                        boolean t2Query = term2Var.getType() == Symbols.VAR_QUERY;

                        if ((t2Query && !t1Query) || (!t2Query && t1Query)) {
                            return false;
                        }
                    }

                    map0.put(term1Var, term2);
                    if (term1Var instanceof CommonVariable) {
                        map1.put(term1Var, term2);
                    }
                }

                return true;

            } else if (term2Var!=null && term2Var.getType() == type) {

                final Term t = map1.get(term2Var);

                if (t != null)
                    return next(term1, t);

                map1.put(term2Var, term1);
                if (term2Var instanceof CommonVariable) {
                    map0.put(term2Var, term1);
                }

                return true;

            } else if ((term1HasVar || term2HasVar) && (term1 instanceof Compound) && ((term1.operator() == term2.operator()))) {
                final Compound cTerm1 = (Compound) term1;
                final Compound cTerm2 = (Compound) term2;
                if (cTerm1.length() != cTerm2.length()) {
                    return false;
                }
                //TODO simplify comparison with Image base class
                if ((cTerm1 instanceof Image) && (((Image) cTerm1).relationIndex != ((Image) cTerm2).relationIndex)) {
                    return false;
                }

                final Term[] list;
                final int clen = cTerm1.length();
                if (cTerm1.isCommutative() && clen > 1) {
                    if (clen == 2) {
                        list = new Term[2];
                        boolean order = random.nextBoolean();
                        int tries = 0;
                        boolean solved;
                        do {
                            if (order) {
                                list[0] = cTerm1.term[0];
                                list[1] = cTerm1.term[1];
                            } else {
                                list[0] = cTerm1.term[1];
                                list[1] = cTerm1.term[0];
                            }
                            order = !order;
                            solved = next(cTerm2, list);
                            tries++;
                        } while (tries < 2 && !solved);
//                        if (solved) {
//                            if (tries > 1)
//                                System.out.println("got it " + tries);
//                        }
                        return solved;
                    }
                    else {
                        list = cTerm1.cloneTerms();
                        Compound.shuffle(list, random);
                    }
                }
                else {
                    list = cTerm1.term;
                }

                return next(cTerm2, list);
            }

            return termsEqual;
        }

        /** a branch for comparing a particular permutation, called from the main next() */
        protected boolean next(Compound c, Term[] list) {
            for (int i = 0; i < list.length; i++) {
                final Term t1 = list[i];
                final Term t2 = c.term[i];
                if (!next(t1, t2)) {
                    return false;
                }
            }

            return true;
        }
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
            int len = bv1.length + bv2.length + 1;
            byte[] c = new byte[len];
            System.arraycopy(bv1, 0, c, 0, bv1.length);
            System.arraycopy(bv2, 0, c, bv1.length, bv2.length);
            c[c.length-1] = '$';
            return new CommonVariable(c);
        }

        CommonVariable(byte[] b) {
            super(b);
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
