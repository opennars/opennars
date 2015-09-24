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
import nars.util.utf8.Byted;
import nars.util.utf8.Utf8;

import static nars.Symbols.*;

/**
 * A variable term, which does not correspond to a concept
 */
public class Variable extends Atom {


    public final Op op;


    public static Variable make(Op varType, byte[] baseName) {
        return make(varType.ch, baseName);
    }

    public static Variable make(char varType, String baseName) {
        return make(varType, Utf8.toUtf8(baseName));
    }

    public static Variable make(char ch, byte[] baseName) {
        int bl = baseName.length;
        final byte[] name = new byte[bl + 1];
        name[0] = (byte)ch;
        System.arraycopy(baseName, 0, name, 1, bl);

        return new Variable(name);
    }

    @Override
    public int compareTo(Object that) {
        if (this == that) return 0;
        if (that instanceof Variable) {
            return Byted.compare(this, (Variable)that);
        }
        return -1; /** variables have earlier sorting order than non-variables */
    }


//    public Variable(char varType, final String name) {
//        super(Utf8.toUtf8((byte) varType, name));
//        this.type = varType;
//        this.scope = false;
//    }

    public Variable(final byte[] n) {
        super(n);

        switch ((char)n[0]) {
            case VAR_INDEPENDENT:
                op = Op.VAR_INDEPENDENT;
                break;
            case VAR_DEPENDENT:
                op = Op.VAR_DEPENDENT;
                break;
            case VAR_QUERY:
                op = Op.VAR_QUERY;
                break;
            case VAR_PATTERN:
                op = Op.VAR_PATTERN;
                break;
            default:
                throw new RuntimeException("Invalid variable type");

        }



    }

    @Override
    final public Op op() {
        return op;
    }

    @Override
    final public int structure() {
        final int i = op().ordinal();
        if (i < 31) {
            //include only non-pattern variables in structure
            return 1 << i;
        }
        return 0;
    }

    /**
     * Constructor, from a given variable name
     *
     * @param name A String read from input
     */
    public Variable(final String n) {
        this(Utf8.toUtf8(n));
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
        throw new RuntimeException("n/a");
        //return new Variable(bytes());
        //return this;
    }



//    /** clones the variable with its scope removed/reset */
//    public Variable cloneUnscoped() {
//        return clone(false);
//    }


    /**
     * The syntactic complexity of a variable is 0, because it does not refer to
     * any concept.
     *
     * @return The complexity of the term, an integer
     */
    @Override public final int complexity() {
        return 0;
    }





    /** tests equal name and scope. if either or both are unscoped, the instances are not equal. */
    @Override public boolean equals(final Object that) {
        if (this == that) return true;
        if (!(that instanceof Variable)) return false;

        final Variable vthat = ((Variable) that);

        /*Op vop = vthat.op;
        if (vop != op)
            return false; //different type*/

//        if (!isScoped()) return false;
//        if (!vthat.isScoped()) return false;

        return Byted.equals(this, vthat);
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


    @Override final public boolean hasVar(final Op type) {
        return op == type;
    }

    @Override public final boolean hasVar() { return true;     }
    @Override public final int vars() {
        return 1;
    }


    public final boolean hasVarPat() { return op == Op.VAR_PATTERN;    }

    @Override public final boolean hasVarDep() { return op == Op.VAR_DEPENDENT;    }
    @Override public final int varDep() {
        return op == Op.VAR_DEPENDENT ? 1 : 0;
    }

    @Override public final boolean hasVarIndep() { return op == Op.VAR_INDEPENDENT;    }
    @Override public final int varIndep() {
        return op == Op.VAR_INDEPENDENT ? 1 : 0;
    }

    @Override public final boolean hasVarQuery() { return op == Op.VAR_QUERY;    }
    @Override public final int varQuery() {
        return op == Op.VAR_QUERY ? 1 : 0;
    }


//    public boolean isCommon() {
//        //TODO there is a faster way to make this test rather than forming the String
//        final byte[] n = bytes();
//        final int l = n.length;
//        return n[l - 1] == '$';
//    }


//    public final boolean isScoped() { return scope; }


//    public static boolean validVariableType(final char c) {
//        switch (c) {
//            case VAR_QUERY: return true;
//            case VAR_DEPENDENT: return true;
//            case VAR_INDEPENDENT: return true;
//            case VAR_PATTERN: return true;
//        }
//        return false;
//    }


    /** TODO cache for unscoped variable terms */

    private static final int MAX_CACHED_VARNAME_INDEXES = 128;
    private static final byte[][] vn1 = new byte[MAX_CACHED_VARNAME_INDEXES][];
    private static final byte[][] vn2 = new byte[MAX_CACHED_VARNAME_INDEXES][];
    private static final byte[][] vn3 = new byte[MAX_CACHED_VARNAME_INDEXES][];
    private static final byte[][] vn4 = new byte[MAX_CACHED_VARNAME_INDEXES/2][];
    
    
    public static byte[] name(final Op type, final int index) {
        if (index >= MAX_CACHED_VARNAME_INDEXES)
            return newName(type, index);


        byte[][] cache;
        switch (type) {
            case VAR_INDEPENDENT: cache = vn1; break;
            case VAR_DEPENDENT: cache = vn2; break;
            case VAR_QUERY: cache = vn3; break;
            case VAR_PATTERN: cache = vn4; break;
            default:
                throw new RuntimeException("Invalid variable type");
        }

        byte[] c = cache[index];
        if (c == null) {
            cache[index] = c = newName(type, index);
        }
            
        return c;
    }
    
    protected static byte[] newName(final Op type, final int index) {

        if (index < 36) {
            byte x = Utf8.base36(index);
            return new byte[] { (byte)type.ch, x};
        }
        else if (index < (36*36)){
            byte x1 = Utf8.base36(index%36);
            byte x2 = Utf8.base36(index/36);
            return new byte[] { (byte)type.ch, x2, x1};
        }
        else {
            throw new RuntimeException("variable index out of range for this method");
        }



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

    public static Variable the(Op type, int counter) {
        return new Variable(name(type, counter));
    }


//    /** returns the default dependent variable */
//    public static Variable theDependent() {
//        return the(Op.VAR_DEPENDENT, 0);
//    }
//
//    /** returns the default independent variable */
//    public static Variable theIndependent() {
//        return the(Op.VAR_INDEPENDENT, 0);
//    }


    /** necessary because VAR_PATTERN are hidden from substructure */
    public static boolean hasPatternVariable(Term t) {
        final boolean[] has = {false};
        t.recurseTerms((t1, superterm) -> {
            if (!has[0]) {
                if (t1 instanceof Variable)
                    if ((((Variable) t1).op == Op.VAR_PATTERN))
                        has[0] = true;
            }
        });
        return has[0];
    }
}
