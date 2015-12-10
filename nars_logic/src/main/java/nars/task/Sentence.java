/*
 * Sentence.java
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
package nars.task;

import nars.Memory;
import nars.NAR;
import nars.Symbols;
import nars.nal.nal5.Conjunction;
import nars.nal.nal7.Order;
import nars.term.Statement;
import nars.term.Term;
import nars.term.Termed;
import nars.term.compound.Compound;
import nars.truth.Stamp;
import nars.truth.Truth;
import nars.truth.Truthed;
import nars.util.data.id.Named;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Comparator;

/**
 * A Sentence is an abstract class, mainly containing a Term, a TruthValue, and
 * a Stamp.
 * <p>
 * It is used as the premises and conclusions of all logic rules.
 */
public interface Sentence extends Stamp, Named<Sentence>, Termed, Truthed {

    /** performs some (but not exhaustive) tests on a term to determine some cases where it is invalid as a sentence content
     * returns the compound valid for a Task if so,
     * otherwise returns null
     * */
    static Compound validTaskTerm(Term t) {
        if (invalidTaskTerm(t))
            return null;
        return ((Compound)t);
    }

    /** only need the positive version of it which calls this */
    @Deprecated static boolean invalidTaskTerm(Term t) {
        if (t.op().isStatement()) {
            Compound st = (Compound) t;

            /* A statement sentence is not allowed to have a independent variable as subj or pred"); */
            if (Statement.subjectOrPredicateIsIndependentVar(st))
                return true;

            return Statement.invalidStatement(st);

        }
        else {
            return (!(t instanceof Compound));//(t instanceof CyclesInterval) || (t instanceof Variable)
        }
    }

    char getPunctuation();

    @Override
    long[] getEvidence();

    @Override
    long getCreationTime();

    @Override
    Sentence setCreationTime(long c);
    //public Sentence setDuration(int d);



    /**
     * Recognize a Question
     *
     * @return Whether the object is a Question
     */
    default boolean isQuestion() {
        return (getPunctuation() == Symbols.QUESTION);
    }

    /**
     * Recognize a Judgment
     *
     * @return Whether the object is a Judgment
     */
    default boolean isJudgment() {
        return (getPunctuation() == Symbols.JUDGMENT);
    }

    default boolean isGoal() {
        return (getPunctuation() == Symbols.GOAL);
    }
    
    default boolean isQuest() {
        return (getPunctuation() == Symbols.QUEST);
    }

    default boolean isCommand()  {
        return (getPunctuation() == Symbols.COMMAND);
    }

    default boolean hasQueryVar() {
        return getTerm().hasVarQuery();
    }

    default boolean isRevisible() {
        Term t = getTerm();
        return !(t instanceof Conjunction && t.hasVarDep());
    }

    default Order getTemporalOrder() {
        return getTerm().getTemporalOrder();
    }


    default StringBuilder appendTo(StringBuilder sb) {
        return appendTo(sb, null);
    }

    /**
     * Overridden in Task
     */
    StringBuilder appendTo(StringBuilder sb, @Nullable Memory memory);

    @Override
    default Sentence name() {
        return this;
    }

    default CharSequence toString(NAR nar, boolean showStamp) {
        return toString(nar.memory, showStamp);
    }

    default CharSequence toString(Memory memory, boolean showStamp) {
        return appendTo(new StringBuilder(), memory, showStamp);
    }

    @Deprecated
    default
    StringBuilder appendTo(StringBuilder buffer, @Nullable Memory memory, boolean showStamp) {
        boolean notCommand = getPunctuation()!=Symbols.COMMAND;
        return appendTo(buffer, memory, true, notCommand, notCommand, true);
    }

    /**
     * Get a String representation of the sentence for display purpose
     *
     * @param buffer provided StringBuilder to append to
     * @param memory may be null in which case the tense is expressed in numbers without any relativity to memory's current time or duration
     * @param showBudget
     * @return The String
     */
    StringBuilder appendTo(StringBuilder buffer, @Nullable Memory memory, boolean term, boolean showStamp, boolean showBudget, boolean showLog);

    @Override
    Compound getTerm();
    @Override
    Truth getTruth();

    default boolean isQuestOrQuestion() {
        return isQuestion() || isQuest();
    }
    default boolean isJudgmentOrGoal() {
        return isJudgment() || isGoal();
    }

    final class ExpectationComparator implements Comparator<Sentence>, Serializable {
        static final Comparator the = new ExpectationComparator();
        @Override public int compare(Sentence b, Sentence a) {
            return Float.compare(a.getExpectation(), b.getExpectation());
        }
    }

    final class ConfidenceComparator implements Comparator<Sentence>, Serializable {
        static final Comparator the = new ExpectationComparator();
        @Override public int compare(Sentence b, Sentence a) {
            return Float.compare(a.getConfidence(), b.getConfidence());
        }
    }

}
