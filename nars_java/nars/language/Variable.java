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

/**
 * A variable term, which does not correspond to a concept
 */
public class Variable extends Term {

    /**
     * Constructor, from a given variable name
     *
     * @param s A String read from input
     */
    public Variable(String s) {
        super(s);
    }

    /**
     * Clone a Variable
     *
     * @return The cloned Variable
     */
    @Override
    public Object clone() {
        return new Variable(name);
    }

    /**
     * Get the type of the variable
     *
     * @return The variable type
     */
    public char getType() {
        return name.charAt(0);
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
    @Override
    public int getComplexity() {
        return 0;
    }

    /**
     * Check whether a string represent a name of a term that contains an
     * independent variable
     *
     * @param n The string name to be checked
     * @return Whether the name contains an independent variable
     */
    public static boolean containVarIndep(String n) {
        return n.indexOf(Symbols.VAR_INDEPENDENT) >= 0;
    }

    /**
     * Check whether a string represent a name of a term that contains a
     * dependent variable
     *
     * @param n The string name to be checked
     * @return Whether the name contains a dependent variable
     */
    public static boolean containVarDep(String n) {
        return n.indexOf(Symbols.VAR_DEPENDENT) >= 0;
    }

    /**
     * Check whether a string represent a name of a term that contains a query
     * variable
     *
     * @param n The string name to be checked
     * @return Whether the name contains a query variable
     */
    public static boolean containVarQuery(String n) {
        return n.indexOf(Symbols.VAR_QUERY) >= 0;
    }

    /**
     * Check whether a string represent a name of a term that contains a
     * variable
     *
     * @param n The string name to be checked
     * @return Whether the name contains a variable
     */
    public static boolean containVar(String n) {
        return containVarIndep(n) || containVarDep(n) || containVarQuery(n);
    }

    /**
     * To unify two terms
     *
     * @param type The type of variable that can be substituted
     * @param t1 The first term
     * @param t2 The second term
     * @return Whether the unification is possible
     */
    public static boolean unify(char type, Term t1, Term t2) {
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
    public static boolean unify(char type, Term t1, Term t2, Term compound1, Term compound2) {
        HashMap<Term, Term> map1 = new HashMap<>();
        HashMap<Term, Term> map2 = new HashMap<>();
        boolean hasSubs = findSubstitute(type, t1, t2, map1, map2); // find substitution
        if (hasSubs) {
            renameVar(map1, compound1, "-1");
            renameVar(map2, compound2, "-2");
            if (!map1.isEmpty()) {
                ((CompoundTerm) compound1).applySubstitute(map1);
            }
            if (!map2.isEmpty()) {
                ((CompoundTerm) compound2).applySubstitute(map2);
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
    private static boolean findSubstitute(char type, Term term1, Term term2,
            HashMap<Term, Term> map1, HashMap<Term, Term> map2) {
        Term t;
        if (term1 instanceof Variable) {
            Variable var1 = (Variable) term1;
            t = map1.get(var1);
            if (t != null) {    // already mapped
                return findSubstitute(type, t, term2, map1, map2);
            } else {            // not mapped yet
                if (var1.getType() == type) {
                    if ((term2 instanceof Variable) && (((Variable) term2).getType() == type)) {
                        Variable var = new Variable(var1.getName() + term2.getName());
                        map1.put(var1, var);  // unify
                        map2.put(term2, var);  // unify
                    } else {
                        map1.put(var1, term2);  // elimination
                    }
                } else {    // different type
                    map1.put(var1, new Variable(var1.getName() + "-1"));  // rename             
                }
                return true;
            }
        }
        if (term2 instanceof Variable) {
            Variable var2 = (Variable) term2;
            t = map2.get(var2);
            if (t != null) {    // already mapped
                return findSubstitute(type, term1, t, map1, map2);
            } else {            // not mapped yet
                if (var2.getType() == type) {
                    map2.put(var2, term2);  // unify
                } else {
                    map2.put(var2, new Variable(var2.getName() + "-2"));  // rename             
                }
                return true;
            }
        }
        if ((term1 instanceof CompoundTerm) && term1.getClass().equals(term2.getClass())) {
            CompoundTerm cTerm1 = (CompoundTerm) term1;
            CompoundTerm cTerm2 = (CompoundTerm) term2;
            if (cTerm1.size() != (cTerm2).size()) {
                return false;
            }
            for (int i = 0; i < cTerm1.size(); i++) {   // assuming matching order, to be refined in the future
                Term t1 = cTerm1.componentAt(i);
                Term t2 = cTerm2.componentAt(i);
                boolean haveSub = findSubstitute(type, t1, t2, map1, map2);
                if (!haveSub) {
                    return false;
                }
            }
            return true;
        }
        return term1.equals(term2); // for atomic constant terms
    }

    /**
     * Check if two terms can be unified
     *
     * @param type The type of variable that can be substituted
     * @param term1 The first term to be unified
     * @param term2 The second term to be unified
     * @return Whether there is a substitution
     */
    public static boolean hasSubstitute(char type, Term term1, Term term2) {
        return findSubstitute(type, term1, term2, new HashMap<Term, Term>(), new HashMap<Term, Term>());
    }

    /**
     * Rename the variables to prepare for unification of two terms
     * @param map The substitution so far
     * @param term The term to be processed 
     * @param suffix The suffix that distinguish the variables in one premise from those from the other
     */
    private static void renameVar(HashMap<Term, Term> map, Term term, String suffix) {
        if (term instanceof Variable) {
            Term t = map.get(term);
            if (t == null) {    // new mapped yet
                map.put(term, new Variable(term.getName() + suffix));  // rename             
            }
        } else if (term instanceof CompoundTerm) {
            for (Term t : ((CompoundTerm) term).components) {   // assuming matching order, to be refined in the future
                renameVar(map, t, suffix);
            }
        }
    }

    /**
     * variable terms are listed first alphabetically
     *
     * @param that The Term to be compared with the current Term
     * @return The same as compareTo as defined on Strings
     */
    @Override
    public final int compareTo(Term that) {
        return (that instanceof Variable) ? name.compareTo(that.getName()) : -1;
    }
}
