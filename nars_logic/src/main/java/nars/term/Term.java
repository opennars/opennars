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


import nars.Op;
import nars.Symbols;
import nars.nal.nal7.TemporalRules;
import nars.term.transform.TermVisitor;
import nars.util.data.id.Identified;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.Map;

public interface Term extends Cloneable, Comparable, Identified, Termed, Serializable {


    @Override
    default Term getTerm() {
        return this;
    }

    public Op operator();


    /** volume = total number of terms = complexity + # total variables */
    public int volume();

    /** total number of leaf terms, excluding variables which have a complexity of zero */
    public int complexity();



    void recurseTerms(final TermVisitor v, Term parent);

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
            case Symbols.VAR_PATTERN:
                /* if this is the case, its always the case because
                   then its the meta-matcher which asks
                   who only operators with PATTERN variables
                 */
                return true;
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
        return (operator()== t.operator());
    }


    default public byte[] bytes() {
        return name().bytes();
    }



    public long structureHash();

    /** lower 31 bits of structure hash (32nd bit reserved for +/-) */
    public int structure();





    default public void append(Writer w, boolean pretty) throws IOException {
        //try {
            name().append(w, pretty);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    default public StringBuilder toStringBuilder(boolean pretty) {
        return name().toStringBuilder(pretty);
    }


    default String toStringCompact() {
        return toString();
    }

    /** returns the effective term as substituted by the set of subs */
    default Term substituted(final Map<Term, Term> subs) {

        if (this instanceof Compound) {
            return ((Compound) this).applySubstitute(subs);
        }
        else if (this instanceof Variable) {
            return subs.get(this);
        }

        return this;
    }


    default public boolean impossibleSubterm(final Term target) {
        return ((impossibleStructure(target.structure())) ||
                (impossibleSubTermVolume(target.volume())));
    }
    default public boolean impossibleSubTermOrEquality(final Term target) {
        return ((impossibleStructure(target.structure())) ||
                (impossibleSubTermOrEqualityVolume(target.volume())));
    }



    default public boolean impossibleStructure(final Term c) {
        return impossibleStructure(c.structure());
    }

    public boolean impossibleStructure(final int possibleSubtermStructure);


    public boolean impossibleSubTermVolume(final int otherTermVolume);


    /** if it's larger than this term it can not be equal to this.
     * if it's larger than some number less than that, it can't be a subterm.
     */
    default public boolean impossibleSubTermOrEqualityVolume(int otherTermsVolume) {
        return otherTermsVolume > volume();
    }


    /** tests if contains a term in the structural hash */
    default public boolean has(final Op op) {
        return (structure() & (1<<op.ordinal())) > 0;
    }

    default boolean levelValid(final int nal) {
        return operator().levelValid(nal);
    }


//    default public boolean hasAll(final Op... op) {
//        //TODO
//    }
//    default public boolean hasAny(final Op... op) {
//        //TODO
//    }

}

