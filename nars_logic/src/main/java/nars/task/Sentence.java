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
import nars.nal.nal7.Tense;
import nars.term.Compound;
import nars.term.Statement;
import nars.term.Term;
import nars.term.Termed;
import nars.truth.*;
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
public interface Sentence<T extends Compound> extends Cloneable, Stamp, Named<Sentence<T>>, Termed, Truthed, Serializable {

    char getPunctuation();
    @Override
    long[] getEvidence();
    @Override
    long getCreationTime();
    @Override
    long getOccurrenceTime();

    @Override
    Sentence setCreationTime(long c);
    @Override
    Sentence setOccurrenceTime(long o);
    //public Sentence setDuration(int d);



//    /**
//     * Create a Sentence with the given fields
//     *
//     * @param seedTerm The Term that forms the content of the sentence
//     * @param punctuation The punctuation indicating the type of the sentence
//     * @param truth The truth value of the sentence, null for question
//     * @param copyStampFrom The stamp of the sentence indicating its derivation time and
//     * @param normalize if false, normalization is not attempted and the compound will be used as-is
//     * base
//     */
//    public Sentence(T seedTerm, final char punctuation, final Truth truth, Sentence copyStampFrom) {
//        this(seedTerm, punctuation, truth);
//
//
//        //apply the stamp to this
//        copyStampFrom.applyToStamp(this);
//
//
////        if ((isQuestion() || isQuest()) && !isEternal()) {
////            //need to clone in case this stamp is shared by others which are not to eternalize it
////            //stamp = stamp.cloneEternal();
////            if (Global.DEBUG_NONETERNAL_QUESTIONS)
////                throw new RuntimeException("Questions and Quests require eternal tense");
////        }
//
//
//
//    }

//    /** returns a valid sentence CompoundTerm, or throws an exception */
//    public static Compound termOrException(Term t) {
//        if (invalidSentenceTerm(t))
//            throw new RuntimeException(t + " not valid sentence content");
//        return ((Compound)t);
//    }

//    static List<Sentence> sortExpectation(Collection<Sentence> s) {
//        List<Sentence> l = new ArrayList(s);
//        Collections.sort(l, ExpectationComparator.the);
//        return l;
//    }
//
//    static List<Sentence> sortConfidence(Collection<Sentence> s) {
//        List<Sentence> l = new ArrayList(s);
//        Collections.sort(l, ConfidenceComparator.the);
//        return l;
//    }

//    public void hash(PrimitiveSink into) {
//
//        into.putBytes(getTerm().bytes());
//        into.putByte((byte)punctuation);
//
//        getStamp().hash(into);
//
//        Truth t = truth;
//        if (t != null) {
//            into.putFloat(t.getFrequency());
//            into.putFloat(t.getConfidence());
//        }
//        into.putLong(occurrenceTime);
//        for (long e : getEvidentialSet())
//            into.putLong(e);
//    }

    /** performs some (but not exhaustive) tests on a term to determine some cases where it is invalid as a sentence content
     * returns true if the term is invalid for use as sentence content term
     * TODO invert boolean to: isValidSentenceTerm
     * */
    static boolean invalidSentenceTerm(final Term t) {

        if (t instanceof Statement) {
            Statement st = (Statement) t;

            /* A statement sentence is not allowed to have a independent variable as subj or pred"); */
            if (st.subjectOrPredicateIsIndependentVar())
                return true;

            if (Statement.invalidStatement(st))
                return true;

            return false;
        }
        else {
            return (!(t instanceof Compound));//(t instanceof CyclesInterval) || (t instanceof Variable)
        }

    }





    /**
     * Check whether different aspects of sentence are equivalent to another one
     *
     * @param that The other judgment
     * @return Whether the two are equivalent
     */
    boolean equivalentTo(final Sentence that, final boolean punctuation, final boolean term, final boolean truth, final boolean stamp, final boolean creationTime);


    default Sentence setOccurrenceTime(Tense tense, int duration) {
        return setOccurrenceTime(getCreationTime(), tense, duration);
    }

    default Sentence setOccurrenceTime(long creation, Tense tense, int duration) {
        return setOccurrenceTime(Tense.getOccurrenceTime(creation, tense, duration));
    }


//
//    public Sentence clone(final boolean makeEternal) {
//        Sentence clon = clone(term);
//        if(clon.getOccurrenceTime()!=Stamp.ETERNAL && makeEternal) {
//            //change occurence time of clone
//            clon.setEternal();
//        }
//        return clon;
//    }

    default Sentence setEternal() {
        setTime(getCreationTime(), Stamp.ETERNAL);
        return this;
    }

//    public final <X extends Compound> Sentence<X> clone(final Term t, final Class<? extends X> necessaryTermType) {
//        X ct = termOrNull(t);
//        if (ct == null) return null;
//
//        if (!ct.getClass().isInstance(necessaryTermType))
//            return null;
//
//        if (ct.equals(term)) {
//            return (Sentence<X>) this;
//        }
//        return clone_(ct);
//
//    }
//

//    /**
//     * Clone the Sentence
//     *
//     * @return The clone
//     */
//    default public Sentence clone() {
//        return clone(getTerm());
//    }

//    /** Clone with a different Term */
//    default public <X extends Compound> Sentence<X> clone(X t) {
//        X ct = termOrNull(t);
//        if (ct == null) return null;
//
//        if (ct.equals(getTerm())) {
//            //throw new RuntimeException("Clone with " + t + " would produces exact sentence");
//            return (Sentence<X>) this;
//        }
//
//        return new Sentence<X>((X)ct, punctuation,
//                truth!=null ? new DefaultTruth(truth) : null,
//                getPriority(), getDurability(), getQuality());
//    }

//    public final Sentence clone(final CompoundTerm t) {
//        //sentence content must be compoundterm
//        if (t instanceof CompoundTerm) {
//            return this.clone((CompoundTerm)t);
//        }
//        return null;
//    }

//
//    /**
//      * project a judgment to a difference occurrence time
//      *
//      * @param targetTime The time to be projected into
//      * @param currentTime The current time as a reference
//      * @return The projected belief
//      */
//    @Deprecated public Sentence projectionSentence(final long targetTime, final long currentTime) {
//
//        final Truth newTruth = projection(targetTime, currentTime);
//
//        final boolean eternalizing = (newTruth instanceof EternalizedTruthValue);
//
//        Sentence s = new Sentence(term, punctuation, newTruth, this);
//
//        s.setOccurrenceTime(eternalizing ? Stamp.ETERNAL : targetTime);
//
//        return s;
//    }

    //projects the truth to a certain time, covering all 4 cases as discussed in
    //https://groups.google.com/forum/#!searchin/open-nars/task$20eteneral/open-nars/8KnAbKzjp4E/rBc-6V5pem8J
    default ProjectedTruth projection(final long targetTime, final long currentTime) {

        final Truth currentTruth = getTruth();
        long occurrenceTime = getOccurrenceTime();

        if(targetTime == Stamp.ETERNAL && Tense.isEternal(occurrenceTime)) {
            return new ProjectedTruth(currentTruth, targetTime);                 //target and itself is eternal so return the truth of itself
        }
        else
        if(targetTime != Stamp.ETERNAL && Tense.isEternal(occurrenceTime)) {
            return new ProjectedTruth(currentTruth, targetTime);                 //target is not eternal but itself is,
        }                                                                        //note: we don't need to project since itself holds for every moment.
        else
        if (targetTime == Stamp.ETERNAL && !Tense.isEternal(occurrenceTime)) { //target is eternal, but ours isnt, so we need to eternalize it
            return TruthFunctions.eternalize(currentTruth);
        }
        else {
            //ok last option is that both are tensed, in this case we need to project to the target time
            //but since also eternalizing is valid, we use the stronger one.
            ProjectedTruth eternalTruth = TruthFunctions.eternalize(currentTruth);
            float factor = TruthFunctions.temporalProjection(targetTime, occurrenceTime, currentTime);
            float projectedConfidence = factor * currentTruth.getConfidence();
            if(projectedConfidence > eternalTruth.getConfidence()) {
                return new ProjectedTruth(currentTruth.getFrequency(), projectedConfidence, targetTime);
            }
            else {
                return eternalTruth;
            }
        }
    }

    /** calculates projection truth quality without creating new TruthValue instances */
    default float projectionTruthQuality(long targetTime, long currentTime, boolean problemHasQueryVar) {
        return projectionTruthQuality(getTruth(), targetTime, currentTime, problemHasQueryVar);
    }

    /** calculates projection truth quality without creating new TruthValue instances */
    default float projectionTruthQuality(final Truth t, long targetTime, long currentTime, boolean problemHasQueryVar) {
        return t.projectionQuality(this, targetTime, currentTime, problemHasQueryVar);
    }

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

    default int getTemporalOrder() {
        int t = getTerm().getTemporalOrder();
        if (t == Tense.ORDER_INVALID)
            throw new RuntimeException(this + " has INVALID temporal order");
        return t;
    }


    default StringBuilder appendTo(StringBuilder sb) {
        return appendTo(sb, null);
    }

    /**
     * Overridden in Task
     */
    StringBuilder appendTo(StringBuilder sb, @Nullable Memory memory);

//    /**
//     * Get a String representation of the sentence for key of Task and TaskLink
//     * We don't cache the Sentence string for 2 reasons:
//     *      1. it is not ordinarily generated except for output
//     *      2. if the stamp or other component changes, it would need to be re-calculated
//     */
//    public CharSequence getKey() {
//        //final String contentName = getTerm().toString();
//
//        final boolean showOcurrenceTime = !isEternal(); //((punctuation == Symbols.JUDGMENT) || (punctuation == Symbols.QUESTION));
//        //final String occurrenceTimeString =  ? stamp.getOccurrenceTimeString() : "";
//
//        //final CharSequence truthString = truth != null ? truth.name() : null;
//
//        int stringLength = 0; //contentToString.length() + 1 + 1/* + stampString.baseLength()*/;
//        if (truth != null) {
//            stringLength += (showOcurrenceTime ? 8 : 0) + 11 /*truthString.length()*/;
//        }
//
//        String termString = getTerm().toString();
//
//        stringLength += termString.length();
//
//        //suffix = [punctuation][ ][truthString][ ][occurenceTimeString]
//        final StringBuilder suffix = new StringBuilder(stringLength).append(termString).append(punctuation);
//
//        if (truth != null) {
//            suffix.append(' ');
//            truth.appendString(suffix, false);
//        }
//        if (showOcurrenceTime) {
//            suffix.append(" {"); //space + stamp opener
//            appendOcurrenceTime(suffix);
//            suffix.append('}'); //stamp closer
//        }
//
//        if (suffix.length()!=stringLength) {
//            System.err.println("length mismatch: " + suffix.length() + " != " + stringLength);
//        }
//        return suffix;
////        return Texts.yarn(Global.ROPE_TERMLINK_TERM_SIZE_THRESHOLD,
////                contentName,//.toString(),
////                suffix); //.toString());
//        //key = new FlatCharArrayRope(StringUtil.getCharArray(k));
//    }

    @Override
    default Sentence name() {
        return this;
    }

    default CharSequence toString(NAR nar, boolean showStamp) {
        return toString(nar.memory, showStamp);
    }

    default CharSequence toString(final Memory memory, final boolean showStamp) {
        return appendTo(new StringBuilder(), memory, showStamp);
    }

    default @Deprecated
    StringBuilder appendTo(StringBuilder buffer, @Nullable final Memory memory, final boolean showStamp) {
        final boolean notCommand = getPunctuation()!=Symbols.COMMAND;
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
    StringBuilder appendTo(StringBuilder buffer, @Nullable final Memory memory, final boolean term, final boolean showStamp, boolean showBudget, boolean showLog);


    default boolean equalTerms(final Sentence s) {
        return getTerm().equals(s.getTerm());
    }

    default boolean equalPunctuations(Sentence s) {
        return getPunctuation() == s.getPunctuation();
    }


    default boolean isTimeless() {
        return getOccurrenceTime() == Stamp.TIMELESS;
    }



//    @Override
//    public Sentence setEvidentialSet(long[] evidentialSet) {
//        if (evidentialSet!=null) {
//            this.evidentialSet = evidentialSet;
//        }
//
//        invalidateHash();
//        return this;
//    }





    default boolean concurrent(final Sentence s, final int duration) {
        return Tense.concurrent(s.getOccurrenceTime(), getOccurrenceTime(), duration);
    }


    @Override
    T getTerm();
    @Override
    Truth getTruth();

    default boolean isQuestOrQuestion() {
        return isQuestion() || isQuest();
    }
    default boolean isJudgmentOrGoal() {
        return isJudgment() || isGoal();
    }

    final class ExpectationComparator implements Comparator<Sentence>, Serializable {
        final static Comparator the = new ExpectationComparator();
        @Override public int compare(final Sentence b, final Sentence a) {
            return Float.compare(a.getExpectation(), b.getExpectation());
        }
    }

    final class ConfidenceComparator implements Comparator<Sentence>, Serializable {
        final static Comparator the = new ExpectationComparator();
        @Override public int compare(final Sentence b, final Sentence a) {
            return Float.compare(a.getConfidence(), b.getConfidence());
        }
    }

//    @Deprecated public static class SubTermVarCollector implements TermVisitor {
//        private final List<Variable> vars;
//
//        public SubTermVarCollector(List<Variable> vars) {
//            this.vars = vars;
//        }
//
//        @Override public void visit(final Term t, final Term parent) {
//            if (t instanceof Variable) {
//                Variable v = ((Variable)t);
//                vars.add(v);
//            }
//        }
//    }
}
