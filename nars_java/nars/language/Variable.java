/*
 * Variable.java
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
package nars.language;

import java.util.*;

import nars.io.Symbols;
import nars.storage.Memory;

/**
 * A variable term, which does not correspond to a concept
 */
public class Variable extends Term {

    /**
     * Constructor, from a given variable name
     *
     * @param s A String read from input
     */
    public Variable(final String s) {
        super(s);
    }

    /**
     * Clone a Variable
     *
     * @return The cloned Variable
     */
    @Override
    public Object clone() {
        return new Variable(getName());
    }

    /**
     * Get the type of the variable
     *
     * @return The variable type
     */
    public char getType() {
        return getName().charAt(0);
    }

    /**
     * A variable is not constant
     *
     * @return false
     */
    @Override
    public boolean isConstant() {
        return false;
    }

    /**
     * The syntactic complexity of a variable is 0, because it does not refer to
     * any concept.
     *
     * @return The complexity of the term, an integer
     */
    @Override public short getComplexity() {
        return 0;
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
     * Check whether a string represent a name of a term that contains a
     * dependent variable
     *
     * @param n The string name to be checked
     * @return Whether the name contains a dependent variable
     */
    public static boolean containVarDep(final String n) {
        return n.indexOf(Symbols.VAR_DEPENDENT) >= 0;
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

    /**
     * Check whether a string represent a name of a term that contains a
     * variable
     *
     * @param n The string name to be checked
     * @return Whether the name contains a variable
     */
    public static boolean containVar(final String n) {
        //return containVarIndep(n) || containVarDep(n) || containVarQuery(n);
        
        //replaced with one iteration
        final int l = n.length();
        for (int i = 0; i < l; i++) {
            char c = n.charAt(i);
            if ((c == Symbols.VAR_INDEPENDENT) || (c == Symbols.VAR_DEPENDENT) || (c == Symbols.VAR_QUERY))
                return true;
        }
        return false;
    }
    
    public static boolean containDepOrIndepVar(final String n) {
        final int l = n.length();
        for (int i = 0; i < l; i++) {
            char c = n.charAt(i);
            if ((c == Symbols.VAR_INDEPENDENT) || (c == Symbols.VAR_DEPENDENT))
                return true;
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
        final HashMap<Term, Term> map1 = new HashMap<>();
        final HashMap<Term, Term> map2 = new HashMap<>();
        final boolean hasSubs = findSubstitute(type, t1, t2, map1, map2); // find substitution
        if (hasSubs) {
            //renameVar(map1, compound1, "-1");
            //renameVar(map2, compound2, "-2");
            if (!map1.isEmpty()) {
                ((CompoundTerm) compound1).applySubstitute(map1);
                ((CompoundTerm) compound1).renameVariables();
            }
            if (!map2.isEmpty()) {
                ((CompoundTerm) compound2).applySubstitute(map2);
                ((CompoundTerm) compound2).renameVariables();
            }
        }
        return hasSubs;
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
    public static boolean findSubstitute(final char type, final Term term1, final Term term2,
            final HashMap<Term, Term> map1, final HashMap<Term, Term> map2) {
        Term t;
        if ((term1 instanceof Variable) && (((Variable) term1).getType() == type)) {
            final Variable var1 = (Variable) term1;
            t = map1.get(var1);
            if (t != null) {    // already mapped
                return findSubstitute(type, t, term2, map1, map2);
            } else {            // not mapped yet
                 if ((term2 instanceof Variable) && (((Variable) term2).getType() == type)) {
                    Variable CommonVar = makeCommonVariable(term1, term2);
                    map1.put(var1, CommonVar);  // unify
                    map2.put(term2, CommonVar);  // unify
                 } else {
                    map1.put(var1, term2);  // elimination
                    if (isCommonVariable(var1)) {
                        map2.put(var1, term2);
                    }
                }
                return true;
            }
        } else if ((term2 instanceof Variable) && (((Variable) term2).getType() == type)) {
            final Variable var2 = (Variable) term2;
            t = map2.get(var2);
            if (t != null) {    // already mapped
                return findSubstitute(type, term1, t, map1, map2);
            } else {            // not mapped yet
                map2.put(var2, term1);  // elimination
                if (isCommonVariable(var2)) {
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
            if ((cTerm1 instanceof ImageExt) && (((ImageExt) cTerm1).getRelationIndex() != ((ImageExt) cTerm2).getRelationIndex())
                || (cTerm1 instanceof ImageInt) && (((ImageInt) cTerm1).getRelationIndex() != ((ImageInt) cTerm2).getRelationIndex())) {
                return false;
            }
            ArrayList<Term> list = cTerm1.cloneComponents();
            if (cTerm1.isCommutative()) {
                Collections.shuffle(list, Memory.randomNumber);
            }
            
            for (int i = 0; i < cTerm1.size(); i++) {   // assuming matching order
                Term t1 = list.get(i);
                Term t2 = cTerm2.componentAt(i);
                if (!findSubstitute(type, t1, t2, map1, map2)) {
                    return false;
                }
            }
            return true;
        }
        return term1.equals(term2); // for atomic constant terms
    }

    private static Variable makeCommonVariable(final Term v1, final Term v2) {               
        return new Variable(v1.getName() + v2.getName() + '$');
    }
    
    private static boolean isCommonVariable(final Variable v) {
        String s = v.getName();
        return s.charAt(s.length() - 1) == '$';
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

    /**
     * Rename the variables to prepare for unification of two terms
     * 
     * @param map The substitution so far
     * @param term The term to be processed 
     * @param suffix The suffix that distinguish the variables in one premise
     * from those from the other
     */
//    private static void renameVar(final HashMap<Term, Term> map, final Term term, final String suffix) {
//        if (term instanceof Variable) {
//            final Term t = map.get(term);
//            if (t == null) {    // new mapped yet
//                map.put(term, new Variable(term.getName() + suffix));  // rename             
//            }
//        } else if (term instanceof CompoundTerm) {
//            for (final Term t : ((CompoundTerm) term).components) {   // assuming matching order, to be refined in the future
//                renameVar(map, t, suffix);
//            }
//        }
//    }

    /**
     * variable terms are listed first alphabetically
     *
     * @param that The Term to be compared with the current Term
     * @return The same as compareTo as defined on Strings
     */
    @Override
    public final int compareTo(final Term that) {
        return (that instanceof Variable) ? getName().compareTo(that.getName()) : -1;
    }
}
