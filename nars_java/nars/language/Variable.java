/*
 * Variable.java
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

/**
 * A variable term, which does not correspond to a concept
 */
public class Variable extends Term {

    public Variable() {
        super();
    }

    
    /**
     * Constructor, from a given variable name
     *
     * @param s A String read from input
     */
    public Variable(final String s) {
        super(s);
    }

    /**
     * Clone a Variable
     *
     * @return The cloned Variable
     */
    @Override
    public Variable clone() {
        Variable v = new Variable();
        v.name = name(); //apply name directly, no need to invoke setName()
        return v;
    }

    /**
     * Get the type of the variable
     *
     * @return The variable type
     */
    public char getType() {
        return name().charAt(0);
    }
    

    /**
     * A variable is not constant
     *
     * @return false
     */
    @Override
    public boolean isConstant() {
        return false;
    }

    /**
     * The syntactic complexity of a variable is 0, because it does not refer to
     * any concept.
     *
     * @return The complexity of the term, an integer
     */
    @Override public short getComplexity() {
        return 0;
    }


    @Override
    public boolean containVar() {
        return true;
    }

    




    /**
     * Rename the variables to prepare for unification of two terms
     * 
     * @param map The substitution so far
     * @param term The term to be processed 
     * @param suffix The suffix that distinguish the variables in one premise
     * from those from the other
     */
//    private static void renameVar(final HashMap<Term, Term> map, final Term term, final String suffix) {
//        if (term instanceof Variable) {
//            final Term t = map.get(term);
//            if (t == null) {    // new mapped yet
//                map.put(term, new Variable(term.getName() + suffix));  // rename             
//            }
//        } else if (term instanceof CompoundTerm) {
//            for (final Term t : ((CompoundTerm) term).term) {   // assuming matching order, to be refined in the future
//                renameVar(map, t, suffix);
//            }
//        }
//    }

    /**
     * variable terms are listed first alphabetically
     *
     * @param that The Term to be compared with the current Term
     * @return The same as compareTo as defined on Strings
     */
    @Override
    public final int compareTo(final Term that) {
        return (that instanceof Variable) ? ((Comparable)name()).compareTo(that.name()) : -1;
    }
}
