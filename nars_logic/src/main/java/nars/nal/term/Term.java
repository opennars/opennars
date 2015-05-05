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
package nars.nal.term;


import nars.Memory;
import nars.io.Symbols;
import nars.nal.NALOperator;
import nars.nal.Named;
import nars.nal.Terms;
import nars.nal.nal7.TemporalRules;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.nio.ByteOrder;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;

public interface Term extends Cloneable, Comparable<Term>, Named<byte[]>, Termed {


    default Term getTerm() {
        return this;
    }

    public NALOperator operator();

    public short getComplexity();

    public void recurseTerms(final TermVisitor v, Term parent);

    public void recurseSubtermsContainingVariables(final TermVisitor v, Term parent);

    public int containedTemporalRelations();


    default public void recurseTerms(final TermVisitor v) {
        recurseTerms(v, null);
    }


    /**
     * Check whether the current Term can name a Concept.
     *
     * @return A Term is constant by default
     */
    public boolean isConstant();

    public boolean isNormalized();

    /** returns the normalized form of the term, or this term itself if normalization is unnecessary */
    default public Term normalized() {
        return this;
    }


    public boolean containsTerm(final Term target);
    public boolean containsTermRecursivelyOrEquals(final Term target);

    @Deprecated default public Term ensureNormalized(String role) {
        if (hasVar() && !isNormalized()) {
            //System.err.println(this + " is not normalized but as " + role + " should have already been");
            //System.exit(1);
            throw new RuntimeException(this + " is not normalized but as " + role + " should have already been");
        }
        return this;
    }



    default public boolean isExecutable(final Memory mem) {
        return false;
    }


    /** shallow clone, using the same subterm references */
    public Term clone();

    /** deep clone, creating clones of all subterms recursively */
    public Term cloneDeep();

    /**
     * Whether this compound term contains any variable term
     *
     * @return Whether the name contains a variable
     */
    public boolean hasVar();

    default public int getTemporalOrder() {
        return TemporalRules.ORDER_NONE;
    }

    default boolean hasVar(final char type) {
        switch (type) {
            case Symbols.VAR_DEPENDENT:
                return hasVarDep();
            case Symbols.VAR_INDEPENDENT:
                return hasVarIndep();
            case Symbols.VAR_QUERY:
                return hasVarQuery();
        }
        throw new RuntimeException("Invalid variable type: " + type);
    }


    public boolean hasVarIndep();

    public boolean hasVarDep();

    public boolean hasVarQuery();

    default public boolean equalsType(final Term t) {
        return Terms.equalType(this, t);
    }

    default public boolean equalsName(final Term t) {
        final byte[] a = name();
        final byte[] b = t.name();
        if (a == b) return true;
        return (hashCode() == t.hashCode() && equals2(a, b));
    }

    /** ordinary array equals comparison with some conditions removed */
    static boolean equals2(final byte[] a, final byte[] a2) {
        /*if (a==a2)
            return true;
        if (a==null || a2==null)
            return false;*/

        int length = a.length;
        if (a2.length != length)
            return false;

        //backwards
        for (int i=length-1; i>=0; i--)
            if (a[i] != a2[i])
                return false;

        return true;
    }

}

