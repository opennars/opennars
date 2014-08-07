package nars.language;

import java.util.HashMap;
import nars.core.Parameters;
import nars.io.Symbols;
import nars.storage.Memory;

/**
 * Static utility class for static methods related to Variables
 */
public class Variables {

    
    
    public static boolean findSubstitute(final char type, final Term term1, final Term term2, final HashMap<Term, Term> map1, final HashMap<Term, Term> map2, int depth) {
        
        if (depth == Parameters.MAX_VARIABLE_SUBSTITUTION_DEPTH) return false;
        //temporary
        //InferenceTracer.guardStack("findSubstitute", type, term1, term2, map1, map2);        
        
        Term t;                
        if ((term1 instanceof Variable) && (((Variable) term1).getType() == type)) {
            final Variable var1 = (Variable) term1;
            t = map1.get(var1);                        
            
            if (t != null) {
                return findSubstitute(type, t, term2, map1, map2, depth+1);
            } else {
                if ((term2 instanceof Variable) && (((Variable) term2).getType() == type)) {
                    Variable CommonVar = makeCommonVariable(term1, term2);
                    map1.put(var1, CommonVar);
                    map2.put(term2, CommonVar);
                } else {
                    map1.put(var1, term2);
                    if (var1.common) {
                        map2.put(var1, term2);
                    }
                }
                return true;
            }
        } else if ((term2 instanceof Variable) && (((Variable) term2).getType() == type)) {
            final Variable var2 = (Variable) term2;
            t = map2.get(var2);
            if (t != null) {
                return findSubstitute(type, term1, t, map1, map2, depth+1);
            } else {
                map2.put(var2, term1);
                if (var2.common) {
                    map1.put(var2, term1);
                }
                return true;
            }
        } else if ((term1 instanceof CompoundTerm) && term1.getClass().equals(term2.getClass())) {
            final CompoundTerm cTerm1 = (CompoundTerm) term1;
            final CompoundTerm cTerm2 = (CompoundTerm) term2;
            if (cTerm1.size() != (cTerm2).size()) {
                return false;
            }
            if ((cTerm1 instanceof ImageExt) && (((RelIndexCompoundTerm) cTerm1).relationIndex != ((RelIndexCompoundTerm) cTerm2).relationIndex) || (cTerm1 instanceof ImageInt) && (((RelIndexCompoundTerm) cTerm1).relationIndex != ((RelIndexCompoundTerm) cTerm2).relationIndex)) {
                return false;
            }
            Term[] list = cTerm1.cloneTerms();
            if (cTerm1.isCommutative()) {
                CompoundTerm.shuffle(list, Memory.randomNumber);
            }
            for (int i = 0; i < cTerm1.size(); i++) {
                Term t1 = list[i];
                Term t2 = cTerm2.term[i];
                if (!findSubstitute(type, t1, t2, map1, map2, depth+1)) {
                    return false;
                }
            }
            return true;
        }
        return term1.equals(term2);        
    }
    
    /**
     * To recursively find a substitution that can unify two Terms without
     * changing them
     *
     * @param type The type of Variable to be substituted
     * @param term1 The first Term to be unified
     * @param term2 The second Term to be unified
     * @param map1 The substitution for term1 formed so far
     * @param map2 The substitution for term2 formed so far
     * @return Whether there is a substitution that unifies the two Terms
     */
    public static boolean findSubstitute(final char type, final Term term1, final Term term2, final HashMap<Term, Term> map1, final HashMap<Term, Term> map2) {
        return findSubstitute(type, term1, term2, map1, map2, 0);        
    }

    /**
     * Check whether a string represent a name of a term that contains a
     * variable
     *
     * @param n The string name to be checked
     * @return Whether the name contains a variable
     */
    public static boolean containVar(final String n) {
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

    public static boolean containVar(final Term[] n) {
        final int l = n.length;
        for (int i = 0; i < l; i++) {
            if (n[i].containsVar()) return true;
        }
        return false;
    }    
    /**
     * To unify two terms
     *
     * @param type The type of variable that can be substituted
     * @param t1 The first term
     * @param t2 The second term
     * @return Whether the unification is possible
     */
    public static boolean unify(final char type, final Term t1, final Term t2) {
        return unify(type, t1, t2, t1, t2);
    }

    /**
     * To unify two terms
     *
     * @param type The type of variable that can be substituted
     * @param t1 The first term to be unified
     * @param t2 The second term to be unified
     * @param compound1 The compound containing the first term
     * @param compound2 The compound containing the second term
     * @return Whether the unification is possible
     */
    public static boolean unify(final char type, final Term t1, final Term t2, final Term compound1, final Term compound2) {
        final HashMap<Term, Term> map1 = new HashMap<>(4);
        final HashMap<Term, Term> map2 = new HashMap<>(4);
        final boolean hasSubs = findSubstitute(type, t1, t2, map1, map2);
        if (hasSubs) {
            if (!map1.isEmpty()) {
                ((CompoundTerm) compound1).applySubstitute(map1);
                compound1.renameVariables();
            }
            if (!map2.isEmpty()) {
                ((CompoundTerm) compound2).applySubstitute(map2);
                compound2.renameVariables();
            }
        }
 
        return hasSubs;
    }

    /**
     * Check whether a string represent a name of a term that contains a query
     * variable
     *
     * @param n The string name to be checked
     * @return Whether the name contains a query variable
     */
    public static boolean containVarQuery(final String n) {
        return n.indexOf(Symbols.VAR_QUERY) >= 0;
    }

    public static Variable makeCommonVariable(final Term v1, final Term v2) {
        return new Variable(v1.toString() + v2.toString() + '$');
    }


    /**
     * Check whether a string represent a name of a term that contains a
     * dependent variable
     *
     * @param n The string name to be checked
     * @return Whether the name contains a dependent variable
     */
    public static boolean containVarDep(final String n) {
        return n.indexOf(Symbols.VAR_DEPENDENT) >= 0;
    }

    public static boolean containVarDepOrIndep(final String n) {
        final int l = n.length();
        for (int i = 0; i < l; i++) {
            char c = n.charAt(i);
            if ((c == Symbols.VAR_INDEPENDENT) || (c == Symbols.VAR_DEPENDENT)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether a string represent a name of a term that contains an
     * independent variable
     *
     * @param n The string name to be checked
     * @return Whether the name contains an independent variable
     */
    public static boolean containVarIndep(final String n) {
        return n.indexOf(Symbols.VAR_INDEPENDENT) >= 0;
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
        return findSubstitute(type, term1, term2, new HashMap<Term, Term>(), new HashMap<Term, Term>());
    }
    
}
