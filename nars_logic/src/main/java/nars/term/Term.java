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
import nars.term.compound.Compound;
import nars.term.match.VarPattern;
import nars.term.visit.SubtermVisitor;

import java.io.IOException;


public interface Term extends Termed, Comparable, Termlike {



    @Override default Term term() {
        return this;
    }

    @Override
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



    default void recurseTerms(SubtermVisitor v) {
        recurseTerms(v, null);
    }

    void recurseTerms(SubtermVisitor v, Compound parent);


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
        return hasAny(op.bit());
    }




//    default boolean hasAll(int structuralVector) {
//        final int s = structure();
//        return (s & structuralVector) == s;
//    }
//

    @Override
    default boolean isAny(int structuralVector) {
        int s = op().bit();
        return (s & structuralVector) == s;
    }
    /** for multiple Op comparsions, use Op.or to produce an int and call isAny(int vector) */
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

    default boolean hasEllipsis() {
        return false;
    }

    default boolean hasVarPattern() {
        return VarPattern.hasPatternVariable(this);
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


    default boolean levelValid(int nal) {

        if (nal >= 8) return true;

        int mask = Op.NALLevelEqualAndAbove[nal];
        return (structure() | mask) == mask;
    }

    default String structureString() {
        return String.format("%16s",
                Integer.toBinaryString(structure()))
                    .replace(" ", "0");
    }



    default boolean containsTemporal() {
        //TODO construct bit vector for one comparison
        return isAny(Op.TemporalBits);
    }


    default int opRel() {
        return op().ordinal() | (-1 << 16);
    }

//    default public boolean hasAll(final Op... op) {
//        //TODO
//    }
//    default public boolean hasAny(final Op... op) {
//        //TODO
//    }

}

