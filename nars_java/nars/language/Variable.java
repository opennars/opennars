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

import java.util.Objects;

/**
 * A variable term, which does not correspond to a concept
 */
public class Variable extends Term {
    

    
    /** caches the type character for faster lookup than charAt(0) */
    private transient char type = 0;
    
    private Term scope;

    private transient int hash;
    

    public Variable(final CharSequence name) {
        this(name, null);        
    }
    
    /**
     * Constructor, from a given variable name
     *
     * @param name A String read from input
     */
    protected Variable(final CharSequence name, final Term scope) {
        super(name);
        setScope(scope);
    }

    @Override
    protected void setName(CharSequence newName) {
        super.setName(newName);
        type = newName.charAt(0);
    }

    public void setScope(final Term scope) {
        this.scope = scope != null ? scope : this;
        this.hash = Objects.hash(name, scope);
    }
    
    /**
     * Clone a Variable
     *
     * @return The cloned Variable
     */
    @Override
    public Variable clone() {
        return new Variable(name(), scope);
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


    @Override public boolean hasVar() {
        return true;
    }
    @Override public boolean hasVarIndep() {
        return isIndependentVariable();
    }
    @Override public boolean hasVarDep() {
        return isDependentVariable();
    }
    @Override public boolean hasVarQuery() {
        return isQueryVariable();
    }
    

    @Override public boolean equals(final Object that) {
        if (that == this) return true;
        if (!(that instanceof Variable)) return false;
        Variable v = (Variable)that;
        if ((v.scope == v) && (scope == this))
            //both are unscoped, so compare by name only
            return (v.name().equals(name()));        
        else
            return (v.scope.equals(scope)) && (v.name().equals(name()));
    }

    @Override
    public int hashCode() {
        return hash;
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

    public Term getScope() {
        return scope;
    }

    
}
