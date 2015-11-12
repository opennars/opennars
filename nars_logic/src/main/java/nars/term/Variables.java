package nars.term;

/**
 * Static utility class for static methods related to Variables
 * TODO rename all *substitute* methods to *unify*
 */
public class Variables {

//    public static boolean findSubstitute(final Op varType, final Term term1, final Term term2, final Map<Term, Term> map1, final Map<Term, Term> map2, final Memory memory) {
//        return findSubstitute(varType, term1, term2, map1, map2, memory.random);
//    }


    //    public static final boolean containVar(final Term[] t) {
//        for (final Term x : t)
//            if (x instanceof Variable)
//                return true;
//        return false;
//    }


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

//    /**
//     * test the invalidity of a term which may be using independent variables
//     */
//    public static boolean indepVarUsedInvalid(Term T) {
//
//
//        //if its a conjunction/disjunction, this is invalid: (&&,<$1 --> test>,<$1 --> test2>), while this isnt: (&&,<$1 --> test ==> <$1 --> test2>,others)
//        //this means we have to go through the conjunction, and check if the component is a indepVarUsedInvalid instance, if yes, return true
//        //
//        if (T instanceof Junction) {
//            for (Term t : ((Junction<?>)T)) {
//                if (indepVarUsedInvalid(t)) {
//                    return true;
//                }
//            }
//        }
//
//        if (!(T instanceof Inheritance) && !(T instanceof Similarity)) {
//            return false;
//        }
//
//        return T.hasVarIndep();
//    }



}
