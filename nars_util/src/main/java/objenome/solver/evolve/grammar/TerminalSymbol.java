/*
 * Copyright 2007-2013
 * Licensed under GNU Lesser General Public License
 * 
 * This file is part of EpochX: genetic programming software for research
 * 
 * EpochX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * EpochX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with EpochX. If not, see <http://www.gnu.org/licenses/>.
 * 
 * The latest version is available from: http://www.epochx.org
 */
package objenome.solver.evolve.grammar;

/**
 * A terminal node of a parse tree, that was constructed to represent a specific
 * instantiation of a {@link GrammarLiteral} of a grammar.
 *
 * @see NonTerminalSymbol
 * @see GrammarLiteral
 */
public class TerminalSymbol implements Symbol {

    // The associated grammar node.
    private GrammarLiteral literal;

    /**
     * Constructs a <code>TerminalSymbol</code> for the given
     * <code>GrammarLiteral</code>.
     *
     * @param literal the grammar node that this symbol is an instantiation of.
     */
    public TerminalSymbol(GrammarLiteral literal) {
        this.literal = literal;
    }

    /**
     * Returns a <code>String</code> representation of this terminal symbol,
     * which is the value of the underlying grammar literal.
     *
     * @return a <code>String</code> representation of this terminal symbol.
     */
    @Override
    public String toString() {
        return literal.toString();
    }

    /**
     * Creates and returns a copy of this terminal symbol. The underlying
     * grammar rule is only shallow copied to the clone.
     *
     * @return a <code>TerminalSymbol</code> which is a copy of this instance.
     */
    @Override
    public TerminalSymbol clone() {
        TerminalSymbol clone = null;
        try {
            clone = (TerminalSymbol) super.clone();
        } catch (CloneNotSupportedException e) {
            // This shouldn't ever happen - if it does then everything is
            // going to blow up anyway.
            assert false;
        }

        // Shallow copy the grammar rules.
        clone.literal = literal;

        return clone;
    }

    /**
     * Tests the given <code>Object</code> for equality with this terminal
     * symbol. The objects are considered to be equal if the argument is an
     * instance of <code>TerminalSymbol</code> and the underlying grammar
     * literals have matching literal values according to the
     * <code>String</code> <code>equals</code> method.
     *
     * @param obj the <code>Object</code> to test for equality.
     * @return <code>true</code> if the given <code>Object</code> is equal to
     * this non-terminal according to the contract outlined above and
     * <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TerminalSymbol) {
            TerminalSymbol objSymbol = (TerminalSymbol) obj;

            return toString().equals(objSymbol.toString());
        } else {
            return false;
        }
    }
}
