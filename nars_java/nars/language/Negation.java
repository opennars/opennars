/*
 * Negation.java
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

import nars.io.Symbols.NativeOperator;
import nars.storage.Memory;

/**
 * A negation of a statement.
 */
public class Negation extends CompoundTerm {

    /**
     * Constructor with partial values, called by make
     *
     * @param n The name of the term
     * @param arg The component list of the term
     */
    private Negation(final CharSequence name, final Term[] arg) {
        super(name, arg);
    }

    /**
     * Constructor with full values, called by clone
     *
     * @param n The name of the term
     * @param cs Component list
     * @param open Open variable list
     * @param i Syntactic complexity of the compound
     */
    private Negation(final CharSequence n, final Term[] cs, final boolean con, final short i) {
        super(n, cs, con, i);
    }

    @Override
    public int getMinimumRequiredComponents() {
        return 1;
    }
    
    /**
     * Clone an object
     *
     * @return A new object
     */
    @Override
    public Negation clone() {
        return new Negation(name(), cloneTerms(), isConstant(), complexity);
    }

    /**
     * Try to make a Negation of one component. Called by the inference rules.
     *
     * @param t The component
     * @param memory Reference to the memory
     * @return A compound generated or a term it reduced to
     */
    public static Term make(final Term t, final Memory memory) {
        if (t instanceof Negation) {
            return ((Negation) t).cloneTerms()[0];
        }         // (--,(--,P)) = P
        return make(new Term[] { t }, memory);
    }

    /**
     * Try to make a new Negation. Called by StringParser.
     *
     * @return the Term generated from the arguments
     * @param argument The list of term
     * @param memory Reference to the memory
     */
    public static Term make(final Term[] argument, final Memory memory) {
        if (argument.length != 1) {
            return null;
        }
        final CharSequence name = makeCompoundName(NativeOperator.NEGATION, argument);
        final Term t = memory.conceptTerm(name);
        return (t != null) ? t : new Negation(name, argument);
    }

    /**
     * Get the operator of the term.
     *
     * @return the operator of the term
     */
    @Override
    public NativeOperator operator() {
        return NativeOperator.NEGATION;
    }
}
