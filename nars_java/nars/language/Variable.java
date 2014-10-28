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

    /** caches the type character for faster lookup than charAt(0) */
    private transient char type = 0;
    
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

    @Override
    protected void setName(CharSequence newName) {
        super.setName(newName);
        type = newName.charAt(0);
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
        v.type = getType();
        return v;
    }

    /**
     * Get the type of the variable
     *
     * @return The variable type
     */
    public char getType() {
        if (type == 0)
            type = name().charAt(0);
        return type;
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
     * variable terms are listed first alphabetically
     *
     * @param that The Term to be compared with the current Term
     * @return The same as compareTo as defined on Strings
     */
    @Override
    public final int compareTo(final AbstractTerm that) {
        return (that instanceof Variable) ? ((Comparable)name()).compareTo(that.name()) : -1;
    }

    boolean isQueryVariable() { return getType() == '?';    }
    boolean isDependentVariable() { return getType() == '#';    }
    boolean isIndependentVariable() { return getType() == '$';    }

    boolean isCommon() {     
        CharSequence n = name();
        int l = n.length();        
        return n.charAt(l - 1) == '$';
    }

    
}
