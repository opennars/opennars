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
import nars.nal.nal7.Temporal;
import nars.term.transform.TermVisitor;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;


public interface Term extends TermContainer, Cloneable, Comparable, Termed, Serializable {


    @Override
    default Term getTerm() {
        return this;
    }

    Op op();


    /** volume = total number of terms = complexity + # total variables */
    @Override
    int volume();

    /** total number of leaf terms, excluding variables which have a complexity of zero */
    @Override
    int complexity();



    void recurseTerms(final TermVisitor v, Term parent);

//    void recurseSubtermsContainingVariables(final TermVisitor v, Term parent);



    default void recurseTerms(final TermVisitor v) {
        recurseTerms(v, null);
    }

    /** number of subterms. if atomic, length=1 */
    int length();

    boolean isNormalized();

    /** returns the normalized form of the term, or this term itself if normalization is unnecessary */
    <T extends Term> T normalized();
    /*{
        return (T) this;
    }*/

    /** careful: this will modify the term and should not be used unless the instance is new and unreferenced. */
    default <T extends Term> T normalizeDestructively() {
        return (T) this;
    }

    boolean containsTerm(final Term target);

    boolean containsTermRecursively(Term target);

    boolean equalsOrContainsTermRecursively(final Term target);



//    default char[] chars(boolean pretty) {
//        return name().chars(pretty);
//    }

    /** shallow clone, using the same subterm references */
    Term clone();

    /** deep clone, creating clones of all subterms recursively */
    Term cloneDeep();

    /**
     * Whether this compound term contains any variable term
     *
     * @return Whether the name contains a variable
     */
    boolean hasVar();

    default int getTemporalOrder() {
        return Temporal.ORDER_NONE;
    }

    boolean hasVar(final Op type);



    /** # of contained independent variables */
    int varIndep();
    /** # of contained dependent variables */
    int varDep();
    /** # of contained query variables */
    int varQuery();


    /** total # of variables, excluding pattern variables */
    int vars();

//    /** tests if num variables of any type exceed a value */
//    default public boolean varsInAnyTypeMoreThan(final int n) {
//        return (varDep() > n) || (varIndep() > n) || (varQuery() > n);
//    }

//    default boolean hasVarPattern() { return varPattern()!=0; }

    default boolean hasVarIndep() {
        return varIndep()!=0;
    }

    default boolean hasVarDep() {
        return varDep()!=0;
    }

    default boolean hasVarQuery() {
        return varQuery()!=0;
    }

//    @Deprecated default public boolean equalsType(final Term t) {
//        return (op()== t.op());
//    }

    byte[] bytes();

    int getByteLen();


//    default public byte[] bytes() {
//        return name().bytes();
//    }


    /** self+subterm types bitvector */
    @Override
    int structure();


    void append(Appendable w, boolean pretty) throws IOException;

//    default public void append(Writer w, boolean pretty) throws IOException {
//        //try {
//            name().append(w, pretty);
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
//    }

    StringBuilder toStringBuilder(boolean pretty);

//    default public StringBuilder toStringBuilder(boolean pretty) {
//        return name().toStringBuilder(pretty);
//    }

    String toString(boolean pretty);
//    default public String toString(boolean pretty) {
//        return toStringBuilder(pretty).toString();
//    }

    default String toStringCompact() {
        return toString();
    }

    /** returns the effective term as substituted by the set of subs */
    default Term substituted(final Map<Term, Term> subs) {

        //TODO hypothesis: if # of variables of the specified type
        //exceeds entries in subs, then match is probably
        //impossible

        if (this instanceof Compound) {
            return ((Compound) this).applySubstitute(subs);
        }
        else if (op()==Op.VAR_PATTERN) { //this instanceof Variable) {
            /*if (this.op()!=Op.VAR_PATTERN) {
                throw new RuntimeException("variable is not pattern");
            }*/
            return subs.get(this);
        }

        return this;
    }









    default boolean levelValid(final int nal) {
        return op().levelValid(nal);
    }

    /** called during construction, or after serialization,
     * to recompute any transient fields (hashes, etc..)  */
    void rehash();


//    default public boolean hasAll(final Op... op) {
//        //TODO
//    }
//    default public boolean hasAny(final Op... op) {
//        //TODO
//    }

}

