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
package nars.term.variable;


import nars.$;
import nars.Op;
import nars.term.Termlike;
import nars.term.atom.AbstractStringAtom;

/**
 * A variable term, which does not correspond to a concept
 */
public abstract class Variable extends AbstractStringAtom {

    protected Variable(String n) {
        super(n);
    }

    protected Variable(byte[] n) {
        super(n);
    }

    protected Variable(String n, Op specificOp) {
        super(n, specificOp);
    }

    public static Variable v(Op varType, String baseName) {
        return v(varType.ch, baseName);
    }

    public static Variable v(char ch, String name) {
        return $.v(ch, name);
    }


    public static final int MAX_VARIABLE_CACHED_PER_TYPE = 16;

    /**
     * numerically-indexed variable instance cache; prevents duplicates and speeds comparisons
     */
    public static final Variable[][] varCache = new Variable[4][MAX_VARIABLE_CACHED_PER_TYPE];

//    public static Op typeIndex(char c) {
//        switch (c) {
//            case '%':
//                return Op.VAR_PATTERN;
//            case '#':
//                return Op.VAR_DEP;
//            case '$':
//                return Op.VAR_INDEP;
//            case '?':
//                return Op.VAR_QUERY;
//        }
//        throw new RuntimeException(c + " not a variable");
//    }

    //    //TODO replace this with a generic counting method of how many subterms there are present
//    public static int numPatternVariables(Term t) {
//        t.value(new TermToInt()) //..
////        final int[] has = {0};
////        t.recurseTerms((t1, superterm) -> {
////            if (t1.op() == Op.VAR_PATTERN)
////                has[0]++;
////        });
////        return has[0];
//    }


    /**
     * true if it has or is a pattern variable
     * necessary because VAR_PATTERN are hidden from substructure
     */
    public static boolean hasPatternVariable(Termlike t) {
        return t.or(x -> x.op() == Op.VAR_PATTERN);
    }


    /**
     * The syntactic complexity of a variable is 0, because it does not refer to
     * any concept.
     *
     * @return The complexity of the term, an integer
     */
    @Override
    public final int complexity() {
        return 0;
    }

    /** produce a normalized version of this identified by the serial integer */
    public abstract Variable normalize(int serial);


    public static final class VarDep extends Variable {

        public VarDep(String name) {
            super(Op.VAR_DEP.ch + name);
        }

        @Override
        public Variable normalize(int serial) {
            return $.v(Op.VAR_DEP, serial);
        }

        @Override
        public Op op() {
            return Op.VAR_DEP;
        }

        @Override
        public int vars() {
            return 1;
        }

        @Override
        public int varDep() {
            return 1;
        }

        @Override
        public int varIndep() {
            return 0;
        }

        @Override
        public int varQuery() {
            return 0;
        }
    }

    public static final class VarIndep extends Variable {


        public VarIndep(String name) {
            super(Op.VAR_INDEP.ch + name);
        }

        @Override
        public Variable normalize(int serial) {
            return $.v(Op.VAR_INDEP, serial);
        }

        @Override
        public Op op() {
            return Op.VAR_INDEP;
        }

        @Override
        public int vars() {
            return 1;
        }

        @Override
        public int varDep() {
            return 0;
        }

        @Override
        public int varIndep() {
            return 1;
        }

        @Override
        public int varQuery() {
            return 0;
        }

    }

    public static final class VarQuery extends Variable {


        public VarQuery(String name) {
            super(Op.VAR_QUERY.ch + name);
        }

        @Override
        public Variable normalize(int serial) {
            return $.v(Op.VAR_QUERY, serial);
        }

        @Override
        public Op op() {
            return Op.VAR_QUERY;
        }

        @Override
        public int vars() {
            return 1;
        }

        @Override
        public int varDep() {
            return 0;
        }

        @Override
        public int varIndep() {
            return 0;
        }

        @Override
        public int varQuery() {
            return 1;
        }

    }


}
