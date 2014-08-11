/*
 * Disjunction.java
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

import java.util.Collection;
import java.util.TreeSet;
import nars.io.Symbols;
import nars.io.Symbols.NativeOperator;
import nars.storage.Memory;

/** 
 * A disjunction of Statements.
 */
public class Disjunction extends CompoundTerm {

    /**
     * Constructor with partial values, called by make
     * @param n The name of the term
     * @param arg The component list of the term
     */
    private Disjunction(final CharSequence name, final Term[] arg) {
        super(name, arg);
    }

    /**
     * Constructor with full values, called by clone
     * @param n The name of the term
     * @param cs Component list
     * @param open Open variable list
     * @param i Syntactic complexity of the compound
     */
    private Disjunction(CharSequence n, Term[] arg, boolean con, short i) {
        super(n, arg, con, i);
        /*if (arg.size()<2) {
            throw new RuntimeException("Conjunction requires >=2 term");
        } */       
    }

    
    @Override
    public int getMinimumRequiredComponents() {
        return 1;
    }
    
    /**
     * Clone an object
     * @return A new object
     */
    @Override
    public Disjunction clone() {
        return new Disjunction(name(), cloneTerms(), isConstant(), complexity);
    }

    /**
     * Try to make a new Disjunction from two term. Called by the inference rules.
     * @param term1 The first component
     * @param term2 The first component
     * @param memory Reference to the memory
     * @return A Disjunction generated or a Term it reduced to
     */
    public static Term make(Term term1, Term term2, Memory memory) {
        TreeSet<Term> set;
        if (term1 instanceof Disjunction) {
            set = new TreeSet<>(((CompoundTerm) term1).cloneTermsList());
            if (term2 instanceof Disjunction) {
                set.addAll(((CompoundTerm) term2).cloneTermsList());
            } // (&,(&,P,Q),(&,R,S)) = (&,P,Q,R,S)
            else {
                set.add(term2.clone());
            }                          // (&,(&,P,Q),R) = (&,P,Q,R)
        } else if (term2 instanceof Disjunction) {
            set = new TreeSet<>(((CompoundTerm) term2).cloneTermsList());
            set.add(term1.clone());                              // (&,R,(&,P,Q)) = (&,P,Q,R)
        } else {
            set = new TreeSet<>();
            set.add(term1.clone());
            set.add(term2.clone());
        }
        return make(set, memory);
    }

    /**
     * Try to make a new IntersectionExt. Called by StringParser.
     * @param argList a list of Term as term
     * @param memory Reference to the memory
     * @return the Term generated from the arguments
     */
    public static Term make(Collection<Term> argList, Memory memory) {
        TreeSet<Term> set = new TreeSet<>(argList); // sort/merge arguments
        return make(set, memory);
    }

    /**
     * Try to make a new Disjunction from a set of term. Called by the public make methods.
     * @param set a set of Term as term
     * @param memory Reference to the memory
     * @return the Term generated from the arguments
     */
    public static Term make(TreeSet<Term> set, Memory memory) {
        if (set.size() == 1) {
            return set.first();
        }                         // special case: single component
        Term[] argument = set.toArray(new Term[set.size()]);
        CharSequence name = makeCompoundName(Symbols.NativeOperator.DISJUNCTION, argument);
        Term t = memory.conceptTerm(name);
        return (t != null) ? t : new Disjunction(name, argument);
    }

    /**
     * Get the operator of the term.
     * @return the operator of the term
     */
    @Override
    public NativeOperator operator() {
        return NativeOperator.DISJUNCTION;
    }

    /**
     * Disjunction is commutative.
     * @return true for commutative
     */
    @Override
    public boolean isCommutative() {
        return true;
    }
}
