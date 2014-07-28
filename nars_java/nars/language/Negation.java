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

import java.util.*;

import nars.io.Symbols.Operator;
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
    private Negation(ArrayList<Term> arg) {
        super(arg);
    }

    /**
     * Constructor with full values, called by clone
     *
     * @param n The name of the term
     * @param cs Component list
     * @param open Open variable list
     * @param i Syntactic complexity of the compound
     */
    private Negation(String n, ArrayList<Term> cs, boolean con, short i) {
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
    public Object clone() {
        return new Negation(getName(), (ArrayList<Term>) cloneList(components), isConstant(), complexity);
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
            return ((CompoundTerm) t).cloneComponents().get(0);
        }         // (--,(--,P)) = P
        final ArrayList<Term> argument = new ArrayList<>(1);
        argument.add(t);
        return make(argument, memory);
    }

    /**
     * Try to make a new Negation. Called by StringParser.
     *
     * @return the Term generated from the arguments
     * @param argument The list of components
     * @param memory Reference to the memory
     */
    public static Term make(final ArrayList<Term> argument, final Memory memory) {
        if (argument.size() != 1) {
            return null;
        }
        final String name = makeCompoundName(Operator.NEGATION, argument);
        final Term t = memory.nameToTerm(name);
        return (t != null) ? t : new Negation(argument);
    }

    /**
     * Get the operator of the term.
     *
     * @return the operator of the term
     */
    @Override
    public Operator operator() {
        return Operator.NEGATION;
    }
}
