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
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.language;

import nars.core.Parameters;
import nars.inference.TemporalRules;

/**
 * Term is the basic component of Narsese, and the object of processing in NARS.
 * <p>
 * A Term may have an associated Concept containing relations with other Terms.
 * It is not linked in the Term, because a Concept may be forgot while the Term
 * exists. Multiple objects may represent the same Term.
 */
public class Term implements Cloneable, Comparable<Term> {

    private String name;
    private int nameHash;
    
    /**
     * Default constructor that build an internal Term
     */
    protected Term() {
        setName(null);
    }

    /**
     * Constructor with a given name
     *
     * @param name A String as the name of the Term
     */
    public Term(final String name) {
        setName(name);
    }

    /**
     * Reporting the name of the current Term.
     *
     * @return The name of the term as a String
     */
    public String getName() {
        return name;
    }

    /**
     * Make a new Term with the same name.
     *
     * @return The new Term
     */
    @Override
    public Object clone() {
        return new Term(name);
    }

    /**
     * Equal terms have identical name, though not necessarily the same
     * reference.
     *
     * @return Whether the two Terms are equal
     * @param that The Term to be compared with the current Term
     */
    @Override
    public boolean equals(final Object that) {
        if (!(that instanceof Term)) return false;
        Term t = (Term)that;
        if (t.nameHash!=nameHash) {
            return false;
        }        
        return name.equals(t.getName());
    }

    /**
     * Produce a hash code for the term
     *
     * @return An integer hash code
     */
    @Override
    public int hashCode() {
        //return (name != null ? name.hashCode() : 7);
        return nameHash;
    }

    /**
     * Check whether the current Term can name a Concept.
     *
     * @return A Term is constant by default
     */
    public boolean isConstant() {
        return true;
    }
    
    public int getTemporalOrder() {
        return TemporalRules.ORDER_NONE;
    }

    /**
     * Blank method to be override in CompoundTerm
     */
    public void renameVariables() {
    }

    /**
     * The syntactic complexity, for constant atomic Term, is 1.
     *
     * @return The complexity of the term, an integer
     */
    public int getComplexity() {
        return 1;
    }

    /** only method that should modify Term.name. also caches hashcode */
    protected void setName(final String name) {
        if (name!=null) {
            if (name.length() <= Parameters.INTERNED_TERM_NAME_MAXLEN) {
                this.name = name.intern();
            }
            else {
                this.name = name;
            }
            this.nameHash = name.hashCode();            
        }
        else {
            this.name = null;
            this.nameHash = 7 + getClass().hashCode();
        }
    }
    
    /**
     * Orders among terms: variable < atomic < compound
     * @param that The Term to be compared with the current Term
     * @return The same as compareTo as defined on Strings
     */
    @Override
    public int compareTo(final Term that) {
        if (that instanceof CompoundTerm) {
            return -1;
        } else if (that instanceof Variable) {
            return 1;
        } else {
            return name.compareTo(that.getName());            
        }
    }

    /**
     * Recursively check if a compound contains a term
     *
     * @param target The term to be searched
     * @return Whether the two have the same content
     */
    public boolean containTerm(final Term target) {
        return equals(target);
    }

    /** whether this contains a term in its components. */
    public boolean containComponent(final Term target) {
        return equals(target);
    }

    /**
     * The same as getName by default, used in display only.
     *
     * @return The name of the term as a String
     */
    @Override
    public final String toString() {
        return name;
    }

}
