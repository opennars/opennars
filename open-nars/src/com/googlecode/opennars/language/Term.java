/*
 * Term.java
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

import com.googlecode.opennars.inference.SyllogisticRules;
import com.googlecode.opennars.main.Memory;
import com.googlecode.opennars.parser.Symbols;
import com.googlecode.opennars.parser.TermVisitor;

/**
 * Term is the basic component of Narsese, and the object of processing in NARS.
 * <p>
 * A Term may or may not have an associated Concept containing relations with other Terms. It
 * is not linked in the Term, because a Concept may be forgot, while the Term exists.
 */
public class Term implements Cloneable, Comparable<Term> {
    /**
     * A Term is identified uniquely by its name, a sequence of characters in a given alphabet.
     */
    protected String name;              // name of the term, an ASCII string (can be changed to Unicode)
    
    /**
     * Default constructor
     */
    protected Term() {}
    
    /**
     * Constructor with a given name
     * @param name A String as the name of the Term
     */
    public Term(String name) {
        this.name = name;
    }
    
    /**
     * The same as getName, used in display.
     * @return The name of the term as a String
     */
    public final String toString() {
        return name;
    }
    
    /**
     * Reporting the name of the current Term.
     * @return The name of the term as a String
     */
    public String getName() {
        return name;
    }
    
    /**
     * Default, to be overrided in variable Terms.
     * @return The name of the term as a String
     */
    public String getConstantName() {
        return name;
    }
    
    /**
     * Make a new Term with the same name.
     * @return The new Term
     */
    public Object clone() {
        return new Term(name);
    }
    
    /**
     * Equal terms have identical name, though not necessarily the same reference.
     * @return Whether the two Terms are equal
     * @param that The Term to be compared with the current Term
     */
    public boolean equals(Object that) {
        return (that instanceof Term) && getName().equals(((Term) that).getName());
    }
    
    /**
     * The default complexity, for constant automic Term, is 1.
     * @return The conplexity of the term, an integer
     */
    public int getComplexity() {
        return 1;
    }
        
    /**
     * Check the relative order of two Terms.
     * @param that The Term to be compared with the current Term
     * @return The same as compareTo as defined on Strings when the constant parts are compared
     */
    public final int compareTo(Term that) {
        int i = this.getConstantName().compareTo(that.getConstantName());       // based on the constant part first
        return (i != 0) ? i : this.getName().compareTo(that.getName());
    }
    
    /**
     * Check whether the current Term can name a Concept.
     * @return a Term is constant by default
     */
    public boolean isConstant() {
        return true;
    }
    
    public CompoundTerm.TemporalOrder getTemporalOrder() {
        return CompoundTerm.TemporalOrder.NONE;
    }
    
    public final boolean containQueryVariable() {                           // to be revised
        return (name.indexOf(Symbols.QUERY_VARIABLE_TAG) >= 0);
    }
    
    /**
     * Accept a visitor
     * @param <R> Return type
     * @param <A> Argument type
     * @param v Visitor
     * @param arg Argument
     * @return an instance of the return type
     */
    public <R,A> R accept(TermVisitor<R,A> v, A arg) {
    	return v.visit(this, arg);
    }
}
