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
import nars.nal.nal7.Order;
import nars.term.transform.FindSubst;
import nars.term.transform.Subst;
import nars.term.visit.SubtermVisitor;

import java.io.IOException;
import java.util.Collection;
import java.util.function.Predicate;


public interface Term extends Termed, Comparable, Termlike {



    @Override default Term getTerm() {
        return this;
    }

    Op op();

    /** syntactic help */
    default boolean op(Op equalTo) {
        return op() == equalTo;
    }

    /** volume = total number of terms = complexity + # total variables */
    @Override
    int volume();

    /** total number of leaf terms, excluding variables which have a complexity of zero */
    @Override
    int complexity();


    @Override
    int structure();


    /** number of subterms. if atomic, size=0 */
    @Override
    int size();

    /** returns the normalized form of the term, or this term itself if normalization is unnecessary */
    <T extends Term> T normalized();


    default void recurseTerms(SubtermVisitor v) {
        recurseTerms(v, null);
    }

    void recurseTerms(SubtermVisitor v, Term parent);



    /** recurses all subterms while the result of the predicate is true;
     *  returns true if all true
     *  */
    boolean and(Predicate<Term> v);

    /** recurses all subterms until the result of the predicate becomes true;
     *  returns true if any true
     * */
    boolean or(Predicate<Term> v);


    /**
     * Commutivity in NARS means that a Compound term's
     * subterms will be unique and arranged in order (compareTo)
     *
     * <p>
     * commutative CompoundTerms: Sets, Intersections Commutative Statements:
     * Similarity, Equivalence (except the one with a temporal order)
     * Commutative CompoundStatements: Disjunction, Conjunction (except the one
     * with a temporal order)
     *
     * @return The default value is false
     */
    boolean isCommutative();

    /**
     * Whether this compound term contains any variable term
     */
    default boolean hasVar() {
        return vars() > 0;
    }

    default Order getTemporalOrder() {
        return op().getTemporalOrder();
    }

    //boolean hasVar(final Op type);


    /** tests if contains a term in the structural hash
     *  WARNING currently this does not detect presence of pattern variables
     * */
    default boolean hasAny(Op op) {
//        if (op == Op.VAR_PATTERN)
//            return Variable.hasPatternVariable(this);
        return hasAny((1<<  op.ordinal()));
    }




//    default boolean hasAll(int structuralVector) {
//        final int s = structure();
//        return (s & structuralVector) == s;
//    }
//

    default boolean hasAny(int structuralVector) {
        int s = structure();
        return (s & structuralVector) != 0;
    }
    default boolean isAny(int structuralVector) {
        int s = op().bit();
        return (s & structuralVector) != 0;
    }
    /** for multiple Op comparsions, use Op.or */
    default boolean isAny(Op op) {
        return isAny(op.bit());
    }

    /** # of contained independent variables */
    int varIndep();
    /** # of contained dependent variables */
    int varDep();
    /** # of contained query variables */
    int varQuery();


    /** total # of variables, excluding pattern variables */
    int vars();

    default boolean hasVarIndep() {
        return varIndep()!=0;
    }

    default boolean hasVarDep() {
        return varDep()!=0;
    }

    default boolean hasVarQuery() {
        return varQuery()!=0;
    }

    /** set the system's perceptual duration (cycles)
     *  for measuring event timings
     *  can return a different term than 'this', in
     *  case the duration causes a reduction or some
     *  other transformation. in any case, the callee should
     *  replace the called term with the result
     * */
    default void setDuration(int duration) {
        //nothing
    }


    /** return the Universal NAL byte encoding of the term */
    byte[] bytes();

    /** # of bytes the encoding will consume. a term needs to be able to know this in advance whether or not the bytes are generated */
    int bytesLength();


//    /** like hashCode except it permits the Term to recompute the hash (to be used in certain controlled situations, otherwise use hashCode() since it's is simpler  */
//    default int rehashCode() { return hashCode(); }

//    default public byte[] bytes() {
//        return name().bytes();
//    }


//    /** self+subterm types bitvector */
//    @Override
//    int structure();


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


    //@Deprecated Term substituted(final Map<Term, Term> subs);

//    default Term substituted(final Map<Term, Term> subs) {
//        if (subs.isEmpty()) return this;
//        return substituted(new MapSubstitution(subs));
//    }



//    /** returns the effective term as substituted by the set of subs */
//    default Term substituted(final Map<Variable, Term> subs) {
//
//        //TODO hypothesis: if # of variables of the specified type
//        //exceeds entries in subs, then match is probably
//        //impossible
//
//        if (this instanceof Compound) {
//            return ((Compound) this).applySubstitute(subs);
//        }
//        else if (op()==Op.VAR_PATTERN) { //this instanceof Variable) {
//            /*if (this.op()!=Op.VAR_PATTERN) {
//                throw new RuntimeException("variable is not pattern");
//            }*/
//            return subs.get(this);
//        }
//
//        return this;
//    }
//








    default boolean levelValid(int nal) {
        return op().levelValid(nal);
    }


    default String structureString() {
        return String.format("%16s",
                Integer.toBinaryString(structure()))
                    .replace(" ", "0");
    }



//    //TODO
//    static final int TemporalStructure =
//            hasAny(Op.PARALLEL) || hasAny(Op.SEQUENCE) ||
//                    hasAny(Op.EQUIVALENCE_AFTER) || hasAny(Op.EQUIVALENCE_WHEN) ||
//                    hasAny(Op.IMPLICATION_AFTER) || hasAny(Op.IMPLICATION_WHEN) || hasAny(Op.IMPLICATION_BEFORE);

    default boolean containsTemporal() {
        //TODO construct bit vector for one comparison
        return hasAny(Op.PARALLEL) || hasAny(Op.SEQUENCE) ||
                hasAny(Op.EQUIV_AFTER) || hasAny(Op.EQUIV_WHEN) ||
                hasAny(Op.IMPLICATION_AFTER) || hasAny(Op.IMPLICATION_WHEN) || hasAny(Op.IMPLICATION_BEFORE);
    }


    static Term apply(Term x, Subst f) {
        Term y = f.getXY(x);

        //attempt 1: apply known substitution
        //containsTerm prevents infinite recursion
        if ((y == null || y.containsTerm(x)))
            return null;

        return y;
    }

    default Term apply(Subst f) {
        return apply(f, false);
    }

    default Term apply(Subst f, boolean fullMatch) {
        return Term.apply(this, f);
    }

    default Term applyOrSelf(FindSubst f) {
        Term y = f.getXY(this);
        if (y == null)
            return this;
        return y;
    }


    /** resolve the this term according to subst by appending to sub.
     * return false if this term fails the substitution */
    default boolean applyTo(Subst f, Collection<Term> sub, boolean fullMatch) {
        Term u = apply(f, fullMatch);
        if (u == null) {
            if (!fullMatch)
                u = this;
            else
                return false;
        }
        /*else
            changed |= (u!=this);*/

        sub.add(u);
        return true;
    }




//    default public boolean hasAll(final Op... op) {
//        //TODO
//    }
//    default public boolean hasAny(final Op... op) {
//        //TODO
//    }

}

