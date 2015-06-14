/*
 * IntersectionInt.java
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
import nars.io.Symbols.NativeOperator;
import nars.storage.Memory;

/**
 * A compound term whose intension is the intersection of the extensions of its term
 */
public class IntersectionInt extends CompoundTerm {

    /**
     * Constructor with partial values, called by make
     * @param n The name of the term
     * @param arg The component list of the term
     */
    private IntersectionInt(final CharSequence name, final Term[] arg) {
        super(name, arg);
    }

    /**
     * Constructor with full values, called by clone
     * @param n The name of the term
     * @param cs Component list
     * @param open Open variable list
     * @param i Syntactic complexity of the compound
     */
    private IntersectionInt(final CharSequence n, final Term[] cs, final boolean con, final short i) {
        super(n, cs, con, i);
    }

    /**
     * Clone an object
     * @return A new object, to be casted into a Conjunction
     */
    @Override
    public IntersectionInt clone() {
        return new IntersectionInt(name(), cloneTerms(), isConstant(), complexity);
    }

    /**
     * Try to make a new compound from two term. Called by the inference rules.
     * @param term1 The first compoment
     * @param term2 The first compoment
     * @param memory Reference to the memory
     * @return A compound generated or a term it reduced to
     */
    public static Term make(Term term1, Term term2, Memory memory) {
        TreeSet<Term> set;
        if ((term1 instanceof SetExt) && (term2 instanceof SetExt)) {
            set = new TreeSet<>(((CompoundTerm) term1).cloneTermsList());
            set.addAll(((CompoundTerm) term2).cloneTermsList());           // set union
            return SetExt.make(set, memory);
        }
        if ((term1 instanceof SetInt) && (term2 instanceof SetInt)) {
            set = new TreeSet<>(((CompoundTerm) term1).cloneTermsList());
            set.retainAll(((CompoundTerm) term2).cloneTermsList());        // set intersection
            return SetInt.make(set, memory);
        }
        if (term1 instanceof IntersectionInt) {
            set = new TreeSet<>(((CompoundTerm) term1).cloneTermsList());
            if (term2 instanceof IntersectionInt) {
                set.addAll(((CompoundTerm) term2).cloneTermsList());
            } // (|,(|,P,Q),(|,R,S)) = (|,P,Q,R,S)
            else {
                set.add(term2.clone());
            }                          // (|,(|,P,Q),R) = (|,P,Q,R)
        } else if (term2 instanceof IntersectionInt) {
            set = new TreeSet<>(((CompoundTerm) term2).cloneTermsList());
            set.add(term1.clone());   // (|,R,(|,P,Q)) = (|,P,Q,R)
        } else {
            set = new TreeSet<>();
            set.add(term1.clone());
            set.add(term2.clone());
        }
        return make(set, memory);
    }

    /**
     * Try to make a new IntersectionExt. Called by StringParser.
     * @return the Term generated from the arguments
     * @param argList The list of term
     * @param memory Reference to the memory
     */
    public static Term make(Collection<Term> argList, Memory memory) {
        TreeSet<Term> set = new TreeSet<>(argList); // sort/merge arguments
        return make(set, memory);
    }

    /**
     * Try to make a new compound from a set of term. Called by the public make methods.
     * @param set a set of Term as compoments
     * @param memory Reference to the memory
     * @return the Term generated from the arguments
     */
    public static Term make(TreeSet<Term> set, Memory memory) {
        if (set.size() == 1) {
            return set.first();
        }                         // special case: single component
        Term[] argument = set.toArray(new Term[set.size()]);
        CharSequence name = makeCompoundName(NativeOperator.INTERSECTION_INT, argument);
        Term t = memory.conceptTerm(name);
        return (t != null) ? t : new IntersectionInt(name, argument);
    }

    /**
     * Get the operator of the term.
     * @return the operator of the term
     */
    @Override
    public NativeOperator operator() {
        return NativeOperator.INTERSECTION_INT;
    }

    /**
     * Check if the compound is communitative.
     * @return true for communitative
     */
    @Override
    public boolean isCommutative() {
        return true;
    }
}
