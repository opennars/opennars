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

import nars.inference.TemporalRules;

/**
 * Term is the basic component of Narsese, and the object of processing in NARS.
 * <p>
 * A Term may have an associated Concept containing relations with other Terms.
 * It is not linked in the Term, because a Concept may be forgot while the Term
 * exists. Multiple objects may represent the same Term.
 */
abstract public class Term implements Cloneable, Comparable<Term> {
    

    /**
     * Equal terms have identical name, though not necessarily the same
     * reference.
     *
     * @return Whether the two Terms are equal
     * @param that The Term to be compared with the current Term
     */
    /*@Override
    public boolean equals(final Object that) {
        if (!(that instanceof Term)) return false;
        Term t = (Term)that;
        if (t.hashCode()!=hashCode()) {
            return false;
        }        
        return name.equals(t.getName());
    }*/
    @Override
    abstract public boolean equals(Object that);


    @Override
    abstract public Term clone();
    
    /**
     * Check whether the current Term can name a Concept.
     *
     * @return A Term is constant by default
     */
    abstract public boolean isConstant();
    
    /**
     * Temporal order; default = none
     */
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
    abstract public short getComplexity();


    
//    /**
//     * @param that The Term to be compared with the current Term
//     * @return The same as compareTo as defined on Strings
//     */
//    @Override
//    abstract public int compareTo(final Term that) {
//        //This removes this class's dependency on CompoundTerm
//        if (that.getClass() == getClass())
//            return name.compareTo(that.getName());
//        return that.getClass().getName().compareTo(getClass().getName());
//        
//        //previously: Orders among terms: variable < atomic < compound
//        /*if (that instanceof CompoundTerm) {
//            return -1;
//        } else if (that instanceof Variable) {
//            return 1;
//        } else {
//            return name.compareTo(that.getName());            
//        }*/
//    }

    /**
     * Whether this compound term contains any variable term
     *
     * @return Whether the name contains a variable
     */
    abstract public boolean containsVar();
    
    /**
     * Recursively check if a compound contains a term. Implements more complex behavior in CompoundTerm subclass.
     *
     * @param target The term to be searched
     * @return Whether the two have the same content
     */
    public boolean containsTermRecursively(final Term target) {
        return equals(target);
    }

    /** whether this contains a term in its components. Implements more complex behavior in CompoundTerm subclass.*/
    public boolean containsTerm(final Term target) {
        return equals(target);
    }

    /**
     * The same as getName by default, used in display only.
     *
     * @return The name of the term as a String
     */    
    @Override
    abstract public String toString();

}
