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
package nars.term;


import nars.Symbols;
import nars.nal.NALOperator;
import nars.nal.nal7.TemporalRules;
import nars.term.transform.TermVisitor;
import nars.util.data.id.Identified;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;

public interface Term extends Cloneable, Comparable, Identified, Termed, Serializable {


    default Term getTerm() {
        return this;
    }

    public NALOperator operator();


    /** total number of terms = complexity + # total variables */
    /*default public int getMass() {

        return getComplexity() + getTotalVariables();
    }*/
    public int getMass();

    /** total number of leaf terms, excluding variables which have a complexity of zero */
    public int getComplexity();



    public void recurseTerms(final TermVisitor v, Term parent);

    public void recurseSubtermsContainingVariables(final TermVisitor v, Term parent);

    public int containedTemporalRelations();


    default public void recurseTerms(final TermVisitor v) {
        recurseTerms(v, null);
    }

    /** number of subterms. if atomic, length=1 */
    public int length();

    /**
     * Check whether the current Term can name a Concept.
     *
     * @return A Term is constant by default
     */
    public boolean isConstant();

    public boolean isNormalized();

    /** returns the normalized form of the term, or this term itself if normalization is unnecessary */
    default <T extends Term> T normalized() {
        return (T) this;
    }

    /** careful: this will modify the term and should not be used unless the instance is new and unreferenced. */
    default <T extends Term> T normalizeDestructively() {
        return (T) this;
    }

    public boolean containsTerm(final Term target);
    public boolean containsTermRecursivelyOrEquals(final Term target);

    @Deprecated default public Term ensureNormalized(String role) {
        if (requiresNormalizing() && !isNormalized()) {
            //System.err.println(this + " is not normalized but as " + role + " should have already been");
            //System.exit(1);
            throw new RuntimeException(this + " is not normalized but as " + role + " should have already been");
        }
        return this;
    }

    boolean requiresNormalizing();


    default char[] chars(boolean pretty) {
        return name().chars(pretty);
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


    /** # of contained independent variables */
    public int varIndep();
    /** # of contained dependent variables */
    public int varDep();
    /** # of contained query variables */
    public int varQuery();


    /** total # of variables */
    public int getTotalVariables();

    /** tests if num variables of any type exceed a value */
    default public boolean varsInAnyTypeMoreThan(final int n) {
        return (varDep() > n) || (varIndep() > n) || (varQuery() > n);
    }


    default public boolean hasVarIndep() {
        return varIndep()!=0;
    }

    default public boolean hasVarDep() {
        return varDep()!=0;
    }

    default public boolean hasVarQuery() {
        return varQuery()!=0;
    }

    @Deprecated default public boolean equalsType(final Term t) {
        return Terms.equalType(this, t);
    }


    default public byte[] bytes() {
        return name().bytes();
    }


    /** returns a bitvector representing the presence of ths term and its subterms */
    default long structuralHash() {
        return (1 << operator().ordinal());
    }


    /** lower 32 bits of structure hash */
    default public int subtermStructure() {
        return (int)(structuralHash() & 0xffffffff);
    }


    default public void append(Writer w, boolean pretty) throws IOException {
        try {
            name().append(w, pretty);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    default public StringBuilder toStringBuilder(boolean pretty) {
        return name().toStringBuilder(pretty);
    }



}

