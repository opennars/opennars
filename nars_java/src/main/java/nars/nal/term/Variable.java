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
package nars.nal.term;


import nars.io.Texts;

import static nars.io.Symbols.*;

/**
 * A variable term, which does not correspond to a concept
 */
public class Variable extends Atom {

    
    

    
    /** caches the type character for faster lookup than charAt(0) */
    private transient char type = 0;
    
    private final Term scope;

    private transient int hash;
    

    public Variable(final CharSequence name) {
        this(name, null);        
    }
    
    /**
     * Constructor, from a given variable name
     *
     * @param name A String read from input
     */
    protected Variable(final CharSequence n, final Term scope) {
        super(n);
        this.type = n.charAt(0);
        if (!validVariableType(type))
            throw new RuntimeException("Invalid variable type: " + n);
        this.scope = scope != null ? scope : this;
        this.hash = 0; //calculate lazily
    }



    
    /**
     * Clone a Variable
     * If this is unscoped, the result will be unscoped also.
     * If this is scoped, it will have the same scope.
     *
     * @return The cloned Variable
     */
    @Override
    public Variable clone() {
        return new Variable(name(), scope == this ? null : scope);
    }

    public Variable clone(Compound newScope) {
        return new Variable(name(), newScope);
    }

    /** clones the variable with its scope removed/reset */
    public Variable cloneUnscoped() {
        return new Variable(name(), null);
    }

    /**
     * Get the type of the variable
     *
     * @return The variable type
     */
    public char getType() {
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




    /** tests equal name and scope. if either or both are unscoped, the instances are not equal. */
    @Override public boolean equals(final Object that) {
        if (this == that) return true;
        if (!(that instanceof Variable)) return false;
        if (!hasScope()) return false;

        Variable v = (Variable)that;
        if (!name().equals(v.name())) return false;
        /*if (getScope() == this) {
            if (v.getScope()!=v) return false;
        }*/
        return (v.getScope().equals(getScope()));
    }
    
    public boolean equalsTerm(Object that) {
        //TODO factor these comparisons into 2 nested if's
        Variable v = (Variable)that;
        if ((v.scope == v) && (scope == this))
            //both are unscoped, so compare by name only
            return name().equals(v.name());
        else if ((v.scope!=v) && (scope==this))
            return false;
        else if ((v.scope==v) && (scope!=this))
            return false;
        else {
            if (!name().equals(v.name()))
                return false;

            if (scope == v.scope) return true;

            if (scope.hashCode()!=v.scope.hashCode())
                return false;

            //WARNING infinnite loop can happen if the two scopes start equaling echother
            //we need a special equals comparison which ignores variable scope when recursively
            //called from this
            //until then, we'll use the name for comparison because it wont 
            //invoke infinite recursion

            return scope.equals(v.scope);
        }
    }

    @Override
    public int hashCode() {
        if (hash == 0) {
            if (hasScope())
                this.hash = 31 * name.hashCode() + scope.hashCode();            
            else
                this.hash = name.hashCode();
        }
        return hash;
    }

    
    


    @Override public boolean hasVar() {
        return true;
    }
    @Override public boolean hasVarIndep() {
        return getType() == VAR_INDEPENDENT;
    }
    @Override public boolean hasVarDep() {
        return getType() == VAR_DEPENDENT;
    }
    @Override public boolean hasVarQuery() {
        return getType() == VAR_QUERY;
    }

    public boolean isCommon() {
        CharSequence n = name();
        int l = n.length();        
        return n.charAt(l - 1) == '$';
    }

    public Term getScope() {
        return scope;
    }

    public boolean hasScope() { return scope != this; }


    public static boolean validVariableType(final char c) {
        return (c == VAR_QUERY) || (c == VAR_DEPENDENT) || (c == VAR_INDEPENDENT);
    }
    
    private static final int MAX_CACHED_VARNAME_INDEXES = 64;
    private static final CharSequence[] vn1 = new CharSequence[MAX_CACHED_VARNAME_INDEXES];
    private static final CharSequence[] vn2 = new CharSequence[MAX_CACHED_VARNAME_INDEXES];
    private static final CharSequence[] vn3 = new CharSequence[MAX_CACHED_VARNAME_INDEXES];
    
    
    public static CharSequence getName(char type, int index) {
        if (index > MAX_CACHED_VARNAME_INDEXES)
            return newName(type, index);
        
        
        CharSequence[] cache;
        switch (type) {
            case VAR_INDEPENDENT: cache = vn1; break;
            case VAR_DEPENDENT: cache = vn2; break;
            case VAR_QUERY: cache = vn3; break;
            default:
                throw new RuntimeException("Invalid variable type");
        }
        
        CharSequence c = cache[index];
        if (c == null) {
            c = newName(type, index);
            cache[index] = c;
        }
            
        return c;
    }
    
    protected static CharSequence newName(char type, int index) {
        
        int digits = (index >= 256 ? 3 : ((index >= 16) ? 2 : 1));
        StringBuilder cb  = new StringBuilder(1 + digits).append(type);
        do {
            cb.append(  Character.forDigit(index % 16, 16) ); index /= 16;
        } while (index != 0);
        return cb.toString();

    }

    public static int compare(final Variable a, final Variable b) {
        int i = Texts.compare(a.name(), b.name());
        if (i == 0)
            return a.getScope().compareTo(b.getScope());
        return i;
    }
}
