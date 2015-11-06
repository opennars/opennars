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
import nars.util.data.Util;
import nars.util.utf8.Utf8;

import java.io.IOException;

/**
 * A variable term, which does not correspond to a concept
 */
abstract public class Variable extends Atom {

    public Variable(final byte[] n) {
        super(n);
    }

    Variable(final String name) {
        super(Utf8.toUtf8(name));
    }

    Variable() {
        super();
    }

    public static Variable the(Op varType, byte[] baseName) {
        return the(varType.ch, baseName);
    }

    public static Variable the(char varType, String baseName) {
        return the(varType, Utf8.toUtf8(baseName));
    }

    public static Variable the(char ch, byte[] name) {
         switch (ch) {
            case Symbols.VAR_DEPENDENT:
                return new VarDep(name);
            case Symbols.VAR_INDEPENDENT:
                return new VarIndep(name);
            case Symbols.VAR_QUERY:
                return new VarQuery(name);
            case Symbols.VAR_PATTERN:
                return new VarPattern(name);
            default:
                throw new RuntimeException("invalid variable type: " + ch);
        }

    }

    public static Variable the(Op type, int counter) {
        return the(type, Util.intAsByteArray(counter));
    }

    //TODO replace this with a generic counting method of how many subterms there are present
    public static int numPatternVariables(Term t) {
        final int[] has = {0};
        t.recurseTerms((t1, superterm) -> {
            if (t1.op() == Op.VAR_PATTERN)
                has[0]++;
        });
        return has[0];
    }

    /** necessary because VAR_PATTERN are hidden from substructure */
    public static boolean hasPatternVariable(Term t) {
        final boolean[] has = {false};
        t.recurseTerms((t1, superterm) -> {
            if (!has[0]) {
                if (t1.op() == Op.VAR_PATTERN)
                    has[0] = true;
            }
        });
        return has[0];
    }

    @Override
    public final void append(Appendable w, boolean pretty) throws IOException {
        w.append(op().ch);
        super.append(w, pretty);
    }

    @Override
    public final String toString() {
        return Utf8.fromUtf8toString(op().ch, bytes());
    }

    //    @Override
//    final public Op op() {
//        switch (byt0()) {
//            case VAR_INDEPENDENT:
//                return Op.VAR_INDEPENDENT;
//            case VAR_DEPENDENT:
//                return Op.VAR_DEPENDENT;
//            case VAR_QUERY:
//                return Op.VAR_QUERY;
//            case VAR_PATTERN:
//                return Op.VAR_PATTERN;
//            default:
//                throw new RuntimeException("Invalid variable type");
//        }
//    }

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
     * The syntactic complexity of a variable is 0, because it does not refer to
     * any concept.
     *
     * @return The complexity of the term, an integer
     */
    @Override public final int complexity() {
        return 0;
    }

//    /** tests equal name and scope. if either or both are unscoped, the instances are not equal. */
//    @Override public boolean equals(final Object that) {
//        if (this == that) return true;
//        if (!(that instanceof Variable)) return false;
//
//        final Variable vthat = ((Variable) that);
//
//        /*Op vop = vthat.op;
//        if (vop != op)
//            return false; //different type*/
//
////        if (!isScoped()) return false;
////        if (!vthat.isScoped()) return false;
//
//        return Byted.equals(this, vthat);
//    }

//    @Override final public boolean hasVar(final Op type) {
//        return op() == type;
//    }


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

    @Override public final boolean hasVar() { return true;     }

    @Override public final int vars() {
        if (op() == Op.VAR_PATTERN) return 0;
        return 1;
    }

    public final boolean hasVarPattern() { return op() == Op.VAR_PATTERN;    }

    @Override public final boolean hasVarDep() { return op() == Op.VAR_DEPENDENT;    }

    @Override public final int varDep() {
        return hasVarDep() ? 1 : 0;
    }
    
    @Override public final boolean hasVarIndep() { return op() == Op.VAR_INDEPENDENT;    }
    
    @Override public final int varIndep() {
        return hasVarIndep()  ? 1 : 0;
    }

    //    /** returns the part of the variable name beyond the intial type indicator character */
//    public String getIdentifier() {
//        return toString().substring(1);
//    }

    @Override public final boolean hasVarQuery() { return op() == Op.VAR_QUERY;    }


//    /** returns the default dependent variable */
//    public static Variable theDependent() {
//        return the(Op.VAR_DEPENDENT, 0);
//    }
//
//    /** returns the default independent variable */
//    public static Variable theIndependent() {
//        return the(Op.VAR_INDEPENDENT, 0);
//    }

    @Override public final int varQuery() {
        return hasVarQuery() ? 1 : 0;
    }


    public static class VarDep extends Variable {

        public VarDep(byte[] name) {
            super(name);
        }

        @Override public final Op op() { return Op.VAR_DEPENDENT; }
    }

    public static class VarIndep extends Variable {

        public VarIndep(byte[] name) {
            super(name);
        }

        @Override public final Op op() { return Op.VAR_INDEPENDENT; }
    }

    public static class VarQuery extends Variable {

        public VarQuery(byte[] name) {
            super(name);
        }

        @Override public final Op op() { return Op.VAR_QUERY; }
    }


    public static class VarPattern extends Variable {

        public VarPattern(byte[] name) {
            super(name);
        }

        @Override public final Op op() { return Op.VAR_PATTERN; }
    }
}
