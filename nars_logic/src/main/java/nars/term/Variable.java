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
package nars.term;


import nars.Op;
import nars.Symbols;

import static nars.Symbols.*;

/**
 * A variable term, which does not correspond to a concept
 */
public class Variable extends Atom {

    /** returns a bitvector representing the presence of ths term and its subterms */
    @Override
    public long structuralHash() {
        final int o = operator().ordinal();
        if (o <= 31)
            return (1 << o);
        return 0;
    }

    @Override
    public int compareTo(Object that) {
        if (this == that) return 0;
        if (that instanceof Variable) {
            return Variable.compare(this, (Variable)that);
        }
        return -1; /** variables have earlier sorting order than non-variables */
    }

    /** caches the type character for faster lookup than charAt(0) */
    private transient char type = 0;
    
    private final boolean scope;


    public Variable(final String name) {
        this(name, false);
    }
    public Variable(final byte[] name) {
        this(name, false);
    }

//    public Variable(char varType, final String name) {
//        super(Utf8.toUtf8((byte) varType, name));
//        this.type = varType;
//        this.scope = false;
//    }

    public Variable(final byte[] n, final boolean scope) {
        super(n);
        this.type = ensureValidVariableType((char)n[0]);
        this.scope = scope;
    }

    @Override
    final public Op operator() {
        switch (type) {
            case VAR_INDEPENDENT: return Op.VAR_INDEPENDENT;
            case VAR_DEPENDENT: return Op.VAR_DEPENDENT;
            case VAR_QUERY: return Op.VAR_QUERY;
            case VAR_PATTERN: return Op.VAR_PATTERN;
            default:
                throw new RuntimeException("Invalid variable type");
        }
    }

    /**
     * Constructor, from a given variable name
     *
     * @param name A String read from input
     */
    public Variable(final String n, final boolean scope) {
        super(n);
        this.type = ensureValidVariableType(n.charAt(0));
        this.scope = scope;
    }


    public final static char ensureValidVariableType(final byte c) {
        return ensureValidVariableType((char)c);
    }

    public final static char ensureValidVariableType(final char c) {
        if (!validVariableType(c))
            throw new RuntimeException("Invalid variable type: " + c);
        return c;
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
        //return new Variable(name(), scope == this ? null : scope);
        return this;
    }

    public Variable clone(boolean newScope) {
        if (newScope!=scope)
            return new Variable(bytes(), newScope);
        return this;
    }

    /** clones the variable with its scope removed/reset */
    public Variable cloneUnscoped() {
        return clone(false);
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
    @Override public int getComplexity() {
        return 0;
    }





    /** tests equal name and scope. if either or both are unscoped, the instances are not equal. */
    @Override public boolean equals(final Object that) {
        if (this == that) return true;
        if (!(that instanceof Variable)) return false;

        final Variable vthat = ((Variable) that);

        if (getType() == VAR_PATTERN && vthat.getType() == VAR_PATTERN) {
            return equalTo(vthat);
        }

        if (!isScoped()) return false;
        if (!vthat.isScoped()) return false;


        return super.equals(that);
    }

    public static int compare(final Variable a, final Variable b) {
        if (a == b) return 0;

        int nameCompare = a.name().compare(b.name());
        if (nameCompare != 0)
            return nameCompare;

        //otherwise, if they have the same name:
        boolean ascoped = a.isScoped();
        boolean bscoped = b.isScoped();

        if (!ascoped && !bscoped) {
            //if the two variables are each without scope, they are not equal.
            //so use their identityHashCode to determine a stable ordering
            int as = System.identityHashCode(a.scope);
            int bs = System.identityHashCode(b.scope);
            return Integer.compare(as, bs);
        }
        else if (ascoped && !bscoped) {
            return 1;
        }
        else if (/*bscoped && */ !ascoped) {
            return -1;
        }
        else {
            return 0; //must be equal
        }
    }
//    public boolean equalsTerm(Object that) {
//        //TODO factor these comparisons into 2 nested if's
//        Variable v = (Variable)that;
//
//        if ((v.scope == v) && (scope == this))
//            //both are unscoped, so compare by name only
//            return name().equals(v.name());
//        else if ((v.scope!=v) && (scope==this))
//            return false;
//        else if ((v.scope==v) && (scope!=this))
//            return false;
//        else {
//            if (!name().equals(v.name()))
//                return false;
//
//            if (scope == v.scope) return true;
//
//            if (scope.hashCode()!=v.scope.hashCode())
//                return false;
//
//            //WARNING infinnite loop can happen if the two scopes start equaling echother
//            //we need a special equals comparison which ignores variable scope when recursively
//            //called from this
//            //until then, we'll use the name for comparison because it wont
//            //invoke infinite recursion
//
//            return scope.equals(v.scope);
//        }
//    }


    @Override
    public boolean hasVar(char type) {
        return (type == getType());
    }

    @Override public boolean hasVar() { return true;     }
    @Override public int getTotalVariables() {
        return 1;
    }


    @Override public boolean hasVarDep() { return getType() == VAR_DEPENDENT;    }
    @Override public int varDep() { return hasVarDep() ? 1 : 0;    }

    @Override public boolean hasVarIndep() { return getType() == VAR_INDEPENDENT;    }
    @Override public int varIndep() { return hasVarIndep() ? 1 : 0;    }

    @Override public boolean hasVarQuery() { return getType() == VAR_QUERY;    }
    @Override public int varQuery() { return hasVarQuery() ? 1 : 0;    }


//    public boolean isCommon() {
//        //TODO there is a faster way to make this test rather than forming the String
//        final byte[] n = bytes();
//        final int l = n.length;
//        return n[l - 1] == '$';
//    }


    public boolean isScoped() { return scope; }


    public static boolean validVariableType(final char c) {
        switch (c) {
            case VAR_QUERY: return true;
            case VAR_DEPENDENT: return true;
            case VAR_INDEPENDENT: return true;
            case VAR_PATTERN: return true;
        }
        return false;
    }


    /** TODO cache for unscoped variable terms */

    private static final int MAX_CACHED_VARNAME_INDEXES = 64;
    private static final byte[][] vn1 = new byte[MAX_CACHED_VARNAME_INDEXES][];
    private static final byte[][] vn2 = new byte[MAX_CACHED_VARNAME_INDEXES][];
    private static final byte[][] vn3 = new byte[MAX_CACHED_VARNAME_INDEXES][];
    
    
    public static byte[] name(final char type, final int index) {
        if (index > MAX_CACHED_VARNAME_INDEXES)
            return newName(type, index);


        byte[][] cache;
        switch (type) {
            case VAR_INDEPENDENT: cache = vn1; break;
            case VAR_DEPENDENT: cache = vn2; break;
            case VAR_QUERY: cache = vn3; break;
            default:
                throw new RuntimeException("Invalid variable type");
        }

        byte[] c = cache[index];
        if (c == null) {
            c = newName(type, index);
            cache[index] = c;
        }
            
        return c;
    }
    
    protected static byte[] newName(final char type, final int index) {

        byte x;
        if (index < 10)
            x = (byte)('0' + index);
        else if (index < (10+26))
            x = (byte)(index + 'a');
        else
            throw new RuntimeException("variable index out of range for this method");

        return new byte[] { (byte)type, x};

//        int digits = (index >= 256 ? 3 : ((index >= 16) ? 2 : 1));
//        StringBuilder cb  = new StringBuilder(1 + digits).append(type);
//        do {
//            cb.append(  Character.forDigit(index % 16, 16) ); index /= 16;
//        } while (index != 0);
//        return cb.toString();

    }

//    /** returns the part of the variable name beyond the intial type indicator character */
//    public String getIdentifier() {
//        return toString().substring(1);
//    }

    public static Variable the(char varDependent, int counter) {
        return new Variable(name(varDependent, counter));
    }
    public static Variable theUnscoped(final char varDependent, final int counter) {
        return new Variable(name(varDependent, counter), true);
    }

    /** returns the default dependent variable */
    public static Variable theDependent() {
        return the(Symbols.VAR_DEPENDENT, 0);
    }

    /** returns the default independent variable */
    public static Variable theIndependent() {
        return the(Symbols.VAR_INDEPENDENT, 0);
    }


}
