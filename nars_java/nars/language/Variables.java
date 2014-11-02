package nars.language;

import java.util.HashMap;
import java.util.Map;
import nars.core.Memory;
import nars.io.Symbols;

/**
 * Static utility class for static methods related to Variables
 */
public class Variables {
    
    public static boolean findSubstitute(final char type, final Term term1, final Term term2, final Map<Term, Term> map1, final Map<Term, Term> map2) {
        return findSubstitute(type, term1, term2, new Map[] { map1, map2 });
    }
    
    /** map is a 2-element array of HashMap<Term,Term>. it may be null, in which case
     * the maps will be instantiated as necessary.  
     * this is to delay the instantiation of the 2 HashMap until necessary to avoid
     * wasting them if they are not used.
     */
    public static boolean findSubstitute(final char type, final Term term1, final Term term2, final Map<Term, Term>[] map) {

        final boolean term1Var = term1 instanceof Variable;
        final boolean term2Var = term2 instanceof Variable;
        final boolean termsEqual = term1.equals(term2);
        if (!term1Var && !term2Var && termsEqual) {
            return true;
        }       
        
        Term t;                
        if (term1Var && (((Variable) term1).getType() == type)) {
            final Variable var1 = (Variable) term1;            
            t = map[0]!=null ? map[0].get(var1) : null;
            
            if (t != null) {
                return findSubstitute(type, t, term2, map);
            } else {
                
                if (map[0] == null) {  map[0] = new HashMap(); map[1] = new HashMap(); }
                
                if ((term2 instanceof Variable) && (((Variable) term2).getType() == type)) {
                    Variable CommonVar = makeCommonVariable(term1, term2);                    
                    map[0].put(var1, CommonVar);
                    map[1].put(term2, CommonVar);
                } else {
                    map[0].put(var1, term2);
                    if (var1.isCommon()) {
                        map[1].put(var1, term2);
                    }
                }
                return true;
            }
        } else if (term2Var && (((Variable) term2).getType() == type)) {
            final Variable var2 = (Variable) term2;            
            t = map[1]!=null ? map[1].get(var2) : null;
            
            if (t != null) {
                return findSubstitute(type, term1, t, map);
            } else {
                
                if (map[0] == null) {  map[0] = new HashMap(); map[1] = new HashMap(); }
                
                map[1].put(var2, term1);
                if (var2.isCommon()) {
                    map[0].put(var2, term1);
                }
                return true;
            }
        } else if ((term1 instanceof CompoundTerm) && term1.getClass().equals(term2.getClass())) {
            final CompoundTerm cTerm1 = (CompoundTerm) term1;
            final CompoundTerm cTerm2 = (CompoundTerm) term2;
            if (cTerm1.size() != cTerm2.size()) {
                return false;
            }
            if ((cTerm1 instanceof ImageExt) && (((ImageExt) cTerm1).relationIndex != ((ImageExt) cTerm2).relationIndex) || (cTerm1 instanceof ImageInt) && (((ImageInt) cTerm1).relationIndex != ((ImageInt) cTerm2).relationIndex)) {
                return false;
            }
            Term[] list = cTerm1.cloneTerms();
            if (cTerm1.isCommutative()) {
                CompoundTerm.shuffle(list, Memory.randomNumber);
            }
            for (int i = 0; i < cTerm1.size(); i++) {
                Term t1 = list[i];
                Term t2 = cTerm2.term[i];
                if (!findSubstitute(type, t1, t2, map)) {
                    return false;
                }
            }
            return true;
        }
        
        return termsEqual;        
    }


    /**
     * Check whether a string represent a name of a term that contains a
     * variable
     *
     * @param n The string name to be checked
     * @return Whether the name contains a variable
     */
    public static boolean containVar(final CharSequence n) {
        if (n == null) return false;
        final int l = n.length();
        for (int i = 0; i < l; i++) {
            switch (n.charAt(i)) {                
                case Symbols.VAR_INDEPENDENT:
                case Symbols.VAR_DEPENDENT:
                case Symbols.VAR_QUERY:
                    return true;
            }
        }        
        return false;
    }
    
    public static final boolean containVar(final Term[] t) {
        for (final Term x : t)
            if (x instanceof Variable)
                return true;
        return false;
    }
    

    /**
     * To unify two terms
     *
     * @param type The type of variable that can be substituted
     * @param t The first and second term as an array, which will have been modified upon returning true
     * @return Whether the unification is possible.  't' will refer to the unified terms
     */
    public static boolean unify(final char type, final Term[] t) {
        return unify(type, t[0], t[1], t);
    }

 
    /**
     * To unify two terms
     *
     * @param type The type of variable that can be substituted
     * @param compound1 The compound containing the first term, possibly modified
     * @param compound2 The compound containing the second term, possibly modified
     * @param t The first and second term as an array, which will have been modified upon returning true
     * @return Whether the unification is possible.  't' will refer to the unified terms
     */
    public static boolean unify(final char type, final Term t1, final Term t2, final Term[] compound) {        
        final Map<Term, Term> map[] = new Map[2]; //begins empty: null,null
        
        final boolean hasSubs = findSubstitute(type, t1, t2, map);
        if (hasSubs) {                        
            compound[0] = applySubstituteAndRenameVariables(((CompoundTerm)compound[0]), map[0]);
            if (compound[0] == null) return false;
            
            compound[1] = applySubstituteAndRenameVariables(((CompoundTerm)compound[1]), map[1]);
            if (compound[1] == null) return false;
            
            return true;
        }
        return false;
    }

    /** appliesSubstitute and renameVariables, resulting in a cloned object, 
     *  will not change this instance  */
    private static Term applySubstituteAndRenameVariables(final CompoundTerm t, final Map<Term, Term> subs) {
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
        return new Variable(v1.toString() + v2.toString() + '$');
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
     * Check whether a string represent a name of a term that contains an
     * independent variable
     *
     * @param n The string name to be checked
     * @return Whether the name contains an independent variable
     */
    public static boolean indepVarUsedInvalid(Term T) {
        if(!(T instanceof Inheritance) && !(T instanceof Similarity)) {
            return false;
        }

        return T.hasVarIndep();
    }

    /**
     * Check if two terms can be unified
     *
     * @param type The type of variable that can be substituted
     * @param term1 The first term to be unified
     * @param term2 The second term to be unified
     * @return Whether there is a substitution
     */
    public static boolean hasSubstitute(final char type, final Term term1, final Term term2) {
        return findSubstitute(type, term1, term2, new HashMap<>(), new HashMap<>());
    }
    
}
