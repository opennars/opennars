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
import nars.term.transform.Substitution;
import nars.util.data.Util;
import nars.util.utf8.Utf8;

import java.io.IOException;
import java.util.Map;

/**
 * A variable term, which does not correspond to a concept
 */
abstract public class Variable extends AbstractStringAtom {

    protected Variable(final byte[] n) {
        super(n);
    }

    protected Variable(final byte[] n, Op specificOp) {
        super(n, specificOp);
    }

//    Variable(final String name) {
//        super(Utf8.toUtf8(name));
//    }



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


    final static int MAX_VARIABLE_CACHED_PER_TYPE = 16;

    /** numerically-indexed variable instance cache; prevents duplicates and speeds comparisons */
    final static Variable[][] varCache = new Variable[4][MAX_VARIABLE_CACHED_PER_TYPE];

    public static Variable the(Op type, int counter) {
        if (counter < MAX_VARIABLE_CACHED_PER_TYPE) {
            final Variable[] vct = varCache[typeIndex(type)];
            Variable existing = vct[counter];
            if (existing!=null)
                return existing;
            else {
                return vct[counter] = _the(type, counter);
            }
        }

        return _the(type, counter);
    }

    static Variable _the(Op type, int counter) {
        return the(type, Util.intAsByteArray(counter));
    }

    public static int typeIndex(Op o) {
        switch (o) {
            case VAR_PATTERN: return 0;
            case VAR_DEPENDENT: return 1;
            case VAR_INDEPENDENT: return 2;
            case VAR_QUERY: return 3;
        }
        throw new RuntimeException(o + " not a variable");
    }

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
     * necessary because VAR_PATTERN are hidden from substructure */
    public static boolean hasPatternVariable(Term t) {
        return t.or( x ->
            x.op() == Op.VAR_PATTERN
        );
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

    @Override public final Term substituted(Map<Term, Term> subs) {
        Term x = subs.get(this);
        if (x != null)
            return x;
        return this;
    }
    
    @Override
    public final Term substituted(Substitution s) {
        Term x = s.getXY(this);
        if (x != null)
            return x;
        return this;
    }

    /**
     * The syntactic complexity of a variable is 0, because it does not refer to
     * any concept.
     *
     * @return The complexity of the term, an integer
     */
    @Override public final int complexity() {       return 0;   }


    public static final class VarDep extends Variable {

        public VarDep(byte[] name)  { super(name);        }

        @Override public final int structure() { return Op.VAR_DEPENDENT.bit();        }

        @Override public final Op op() { return Op.VAR_DEPENDENT; }

        @Override public final int vars() { return 1; }
        @Override public final int varDep() {  return 1; }
        @Override public final int varIndep() { return 0;}
        @Override public final int varQuery() { return 0; }
    }

    public static final class VarIndep extends Variable {



        public VarIndep(byte[] name) { super(name);         }

        @Override public final int structure() {  return Op.VAR_INDEPENDENT.bit();        }

        @Override public final Op op() { return Op.VAR_INDEPENDENT; }

        @Override public final int vars() { return 1; }

        @Override public final int varDep() {  return 0;        }
        @Override public final int varIndep() { return 1;}
        @Override public final int varQuery() { return 0; }

    }

    public static final class VarQuery extends Variable {



        public VarQuery(byte[] name) {  super(name);         }

        @Override public final int structure() {  return Op.VAR_QUERY.bit();        }

        @Override public final Op op() { return Op.VAR_QUERY; }

        @Override public final int vars() { return 1; }

        @Override public final int varDep() {  return 0;        }
        @Override public final int varIndep() { return 0;}
        @Override public final int varQuery() { return 1; }

    }


    public static class VarPattern extends Variable {


        public VarPattern(byte[] name) {  super(name);         }

        @Override public final int structure() { return 0; } //Op.VAR_PATTERN.bit();        }

        @Override public final Op op() { return Op.VAR_PATTERN; }

        /** pattern variable hidden in the count 0 */
        @Override public final int vars() { return 0; }

        @Override public final int varDep() {  return 0;        }
        @Override public final int varIndep() { return 0;}
        @Override public final int varQuery() { return 0; }

    }
}
