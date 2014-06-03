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
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.googlecode.opennars.language;

import java.util.*;

import com.googlecode.opennars.parser.Symbols;

/**
 * A variable term.
 */
public class Variable extends Term {
    public enum VarType { INDEPENDENT, DEPENDENT, ANONYMOUS, QUERY }
    
    private CompoundTerm scope;
    private VarType type;
    
    public Variable() {
        super();
    }
    
    public Variable(String s) {
        name = s;
        scope = null;
        char prefix = s.charAt(0);
        if (prefix == Symbols.QUERY_VARIABLE_TAG)
            type = VarType.QUERY;
        else if (s.length() == 1)
            type = VarType.ANONYMOUS;
        else if (s.charAt(s.length()-1) == Symbols.COMPOUND_TERM_CLOSER) {
            type = VarType.DEPENDENT;
            int i = s.indexOf(Symbols.COMPOUND_TERM_OPENER);
            name = s.substring(0, i);       // ignore the dependency list
        } else
            type = VarType.INDEPENDENT;
    }

    private Variable(String n, CompoundTerm s, VarType t) {
        name = n;
        scope = s;
        type = t;
    }
    
    /**
     * Make a new Variable with the same name and type.
     * @return The new Variable
     */
    public Object clone() {
        return new Variable(name, scope, type);
    }

    // overwrite default, to be omitted in sorting
    public String getConstantName() {
        return ("" + Symbols.VARIABLE_TAG);
    }
    
    /**
     * Rename a variable temporally to distinguish it from variables in other Terms
     * @param first Whether it is the first term
     * @return The new name
     */
    public String getVarName(boolean first) {
        if (first)
            return Symbols.VARIABLE_TAG + "1" + name;
        else
            return Symbols.VARIABLE_TAG + "2" + name;
    }
    
    public void setName(String n) {
        name = n;
    }
    
    public CompoundTerm getScope() {
        return scope;
    }
    
    public void setScope(CompoundTerm s) {
        scope = s;
    }
    
    public VarType getType() {
        return type;
    }
    
    public void setType(VarType t) {
        type = t;
    }

    public boolean equals(Object that) {
        return (that instanceof Variable) && name.equals(((Variable) that).getSimpleName());
    }

    public String getSimpleName() {
        return name;
    }

    public String getName() {
        if (type != VarType.DEPENDENT)
            return name;
        else {
            StringBuffer buffer = new StringBuffer(name + "(");
            if (scope != null) {
                ArrayList<Variable> dependency = scope.getOpenVariables();
                if (dependency != null) {
                    for (Variable v : dependency)
                        if (v.getType() == VarType.INDEPENDENT)
                            buffer.append(v.toString());
                }
            }
            buffer.append(")");
            return buffer.toString();
        }
    }
    
    public boolean isConstant() {
        return false;                // overridding default
    }
    
    // move to RuleTable? the last two arguments must be clones
    /**
     * To unify two Terms, then apply the substitution to the two compounds
     * @param type The type of Variable to be unified
     * @param t1 The first Term to be unified
     * @param t2 The second Term to be unified
     * @param compound1 The first compound to be substituted
     * @param compound2 The second compound to be substituted
     * @return Whether a unification has been succeeded
     */
    public static boolean unify(VarType type, Term t1, Term t2, Term compound1, Term compound2) {
        if (t1.isConstant() && t1.equals(t2))       // to constant Terms are unified if equals
            return true;
        if (!(compound1 instanceof CompoundTerm) || !(compound2 instanceof CompoundTerm))
            return false;
        HashMap<String,Term> substitute = findSubstitute(type, t1, t2, new HashMap<String,Term>()); // find substitution
        if (substitute == null) // not unifiable
            return false;
        if (!substitute.isEmpty()) {
            ((CompoundTerm) compound1).substituteComponent(substitute, true);   // apply the substitution to the first compound
            ((CompoundTerm) compound2).substituteComponent(substitute, false);  // apply the substitution to the second compound
        }
        return true;
    }


    public static HashMap<String,Term> findSubstitute(VarType type, Term term1, Term term2) {
        return findSubstitute(type, term1, term2, new HashMap<String,Term>());
    }
    
    /**
     * To find a substitution that can unify two Terms without changing them
     * @param type The type of Variable to be substituted
     * @param term1 The first Term to be unified
     * @param term2 The second Term to be unified
     * @param subs The substitution formed so far
     * @return The substitution that unifies the two Terms
     */
    private static HashMap<String,Term> findSubstitute(VarType type, Term term1, Term term2, HashMap<String,Term> subs) {
        Term oldValue, t1, t2;
        if (term1.equals(term2))    // for constant, also shortcut for variable and compound
            return subs;
        if ((term1 instanceof Variable) && (((Variable) term1).getType() == type))  // the first Term is a unifiable Variable
            return findSubstituteVar(type, (Variable) term1, term2, subs, true);
        if ((term2 instanceof Variable) && (((Variable) term2).getType() == type))  // the second Term is a unifiable Variable
            return findSubstituteVar(type, (Variable) term2, term1, subs, false);
        if (term1 instanceof CompoundTerm) {
            if (!term1.getClass().equals(term2.getClass()))   // two compounds must be of the same type to be unified
                return null;
            if (!(((CompoundTerm) term1).size() == ((CompoundTerm) term2).size())) // two compounds must be of the same size, too
                return null;
            for (int i = 0; i < ((CompoundTerm) term1).size(); i++) {   // recursively unify components
                t1 = ((CompoundTerm) term1).componentAt(i);
                t2 = ((CompoundTerm) term2).componentAt(i);
                HashMap<String,Term> newSubs = findSubstitute(type, t1, t2, subs);
                if (newSubs == null)            // fails in one component means no substitution
                    return null;
                subs.putAll(newSubs);       // put new mappings into the table
            }
            return subs;     // solve: <x,x,2> and <y,3,y>
        }
        return null;
    }
    
    /**
     * To find a substitution that can unify a Vriable and a Term
     * @param type The type of Variable to be substituted
     * @param var The Variable to be unified
     * @param term The Term to be unified
     * @param subs The substitution formed so far
     * @param first If it is the first Term in unify
     * @return The substitution that unifies the two Terms, as "name-term" pairs
     */
    private static HashMap<String,Term> findSubstituteVar(VarType type, Variable var, Term term, HashMap<String,Term> subs, boolean first) {
        String name1 = var.getVarName(first);    // make a prefixed name for the avriable
        Term oldTerm = subs.get(name1);          // check if a mapping for that name exist
        if (oldTerm != null) {                  // processed variable
            if (first)
                return findSubstitute(type, oldTerm, term, subs);
            else
                return findSubstitute(type, term, oldTerm, subs);
        } else {                                // novel variable
            if (term instanceof Variable) {     // the other is also a variable
                String name2 = ((Variable) term).getVarName(!first);
                oldTerm = subs.get(name2);       
                if (oldTerm != null) {          // if the other has a substitute
                    if (first)
                        return findSubstitute(type, var, oldTerm, subs);
                    else
                        return findSubstitute(type, oldTerm, var, subs);
                }
            }
            subs.put(name1, term);  // check dependency for dependent variable!!!
            return subs;
        }
    }
}
