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

import com.gs.collections.api.CharIterable;
import com.gs.collections.impl.set.mutable.primitive.CharHashSet;

/**
 * Grammar literals are a type of grammar node, that represent the terminals of
 * a grammar. They are those nodes which are never found on the left-hand side
 * of grammar rules and as such contain no productions. Instead,
 * <code>GrammarLiterals</code> each have a value which is the string they
 * represent in the grammar string. The final source of a program that is valid
 * according to a grammar will be made up solely of these terminal values.
 */
public class GrammarLiteral implements GrammarNode {

    // The literal value of this grammar terminal.
    private String value;

    /**
     * Constructs a terminal symbol with the specified value.
     *
     * @param value snippet of source that this terminal represents.
     */
    public GrammarLiteral(String value) {
        this.value = value;
    }

    /**
     * Tests whether the given string contains any of the <code>char</code>s in
     * the provided array.
     *
     * @param str an input string to test for specific <code>char</code>s
     * @param chrs an array of characters to look for in <code>str</code>
     * @return <code>true</code> if <code>str</code> contains one or more
     * characters from the <code>chrs</code> array, and <code>false</code> if it
     * contains none
     */
    public static boolean containsAny(CharSequence str, char[] chrs) {
        CharIterable x = new CharHashSet(chrs);

        for (int i = 0; i < str.length(); i++) {
            if (x.contains(str.charAt(i)))
                return true;
        }
        return false;
    }

    /**
     * Returns the literal value of this grammar terminal.
     *
     * @return this grammar literal's value.
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the literal value of this grammar terminal.
     *
     * @param value the new grammar literal value to set.
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Returns a <code>String</code> representation of this grammar literal.
     *
     * @return a string representation of this grammar literal.
     */
    @Override
    public String toString() {
        return escape(value);
    }

    /*
     * If the input string contains any illegal chars then the whole string is
     * wrapped in quotes and returned. Otherwise the original string is
     * returned unmodified.
     */
    private String escape(String input) {
        char[] escapeChars = {'>', '<', '|'};
        if (containsAny(input, escapeChars)) {
            StringBuilder buffer = new StringBuilder();
            buffer.append('\"');
            buffer.append(input);
            buffer.append('\"');
            input = buffer.toString();
        }

        return input;
    }
}
