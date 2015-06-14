/*
 * SetExt.java
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
import static nars.io.Symbols.NativeOperator.SET_EXT_CLOSER;
import static nars.io.Symbols.NativeOperator.SET_EXT_OPENER;
import nars.storage.Memory;

/**
 * An extensionally defined set, which contains one or more instances.
 */
public class SetExt extends SetTensional {

    /**
     * Constructor with partial values, called by make
     * @param n The name of the term
     * @param arg The component list of the term
     */
    private SetExt(final CharSequence name, final Term[] arg) {
        super(name, arg);
    }

    /**
     * Constructor with full values, called by clone
     * @param n The name of the term
     * @param cs Component list
     * @param open Open variable list
     * @param i Syntactic complexity of the compound
     */
    private SetExt(final CharSequence n, final Term[] cs, final boolean con, final short i) {
        super(n, cs, con, i);
    }

    
    /**
     * Clone a SetExt
     * @return A new object, to be casted into a SetExt
     */
    @Override
    public SetExt clone() {
        return new SetExt(name(), cloneTerms(), isConstant(), getComplexity());
    }

    /**
     * Try to make a new set from one component. Called by the inference rules.
     * @param t The compoment
     * @param memory Reference to the memeory
     * @return A compound generated or a term it reduced to
     */
    public static Term make(final Term t, final Memory memory) {
        final TreeSet<Term> set = new TreeSet<>();
        set.add(t);
        return make(set, memory);
    }

    /**
     * Try to make a new SetExt. Called by StringParser.
     * @return the Term generated from the arguments
     * @param argList The list of term
     * @param memory Reference to the memeory
     */
    public static Term make(final Collection<Term> argList, final Memory memory) {
        TreeSet<Term> set = new TreeSet<>(argList); // sort/merge arguments
        return make(set, memory);
    }

    /**
     * Try to make a new compound from a set of term. Called by the public make methods.
     * @param set a set of Term as compoments
     * @param memory Reference to the memeory
     * @return the Term generated from the arguments
     */
    public static Term make(final TreeSet<Term> set, final Memory memory) {
        if (set.isEmpty()) {
            return null;
        }
        Term[] argument = set.toArray(new Term[set.size()]);
        return make(argument, memory);
    }

    private static Term make(final Term[] termSet, final Memory memory) {
        final CharSequence name = makeSetName(SET_EXT_OPENER.ch, termSet, SET_EXT_CLOSER.ch);
        final Term t = memory.conceptTerm(name);
        return (t != null) ? t : new SetExt(name, termSet);
    }
    
    /**
     * Get the operator of the term.
     * @return the operator of the term
     */
    @Override
    public NativeOperator operator() {
        return NativeOperator.SET_EXT_OPENER;
    }


    /**
     * Make a String representation of the set, override the default.
     * @return true for communitative
     */
    @Override
    public CharSequence makeName() {
        return makeSetName(SET_EXT_OPENER.ch, term, SET_EXT_CLOSER.ch);
    }
}

