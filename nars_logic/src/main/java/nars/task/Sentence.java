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

import com.google.common.hash.PrimitiveSink;
import nars.Memory;
import nars.NAR;
import nars.Symbols;
import nars.budget.Item;
import nars.nal.nal5.Conjunction;
import nars.nal.nal7.Intermval;
import nars.nal.nal7.Sequence;
import nars.nal.nal7.TemporalRules;
import nars.nal.nal7.Tense;
import nars.task.stamp.AbstractStamper;
import nars.task.stamp.Stamp;
import nars.term.*;
import nars.term.transform.TermVisitor;
import nars.truth.*;
import nars.util.data.Util;
import nars.util.data.id.Named;

import java.io.Serializable;
import java.util.*;

/**
 * A Sentence is an abstract class, mainly containing a Term, a TruthValue, and
 * a Stamp.
 * <p>
 * It is used as the premises and conclusions of all logic rules.
 */
public class Sentence<T extends Compound> extends Item<Sentence<T>> implements Cloneable, Stamp, Named<Sentence<T>>, Termed, Truthed, Sentenced, Serializable, AbstractStamper {


    protected T term;
    
    /**
     * The punctuation also indicates the type of the Sentence: 
     * Judgment, Question, Goal, or Quest.
     * Represented by characters: '.', '?', '!', or '@'
     */
    public final char punctuation;
    
    /**
     * The truth value of Judgment, or desire value of Goal
     * TODO can we make this final eventually like it was before.. Concept.discountBeliefConfidence needed to mutate the truth on discount
     */
    public Truth truth;
    

    transient private int hash;



    /**
     * Partial record of the derivation path
     */
    private long[] evidentialSet = null;


    private long creationTime = Stamp.TIMELESS;

    private long occurrenceTime = Stamp.ETERNAL;

    private int duration = 0;
    private boolean cyclic;


    @Deprecated Sentence(char punctuation) {
        super(0,0,0);
        this.punctuation = punctuation;
        this.truth = null;
        this.term = null;
    }

    Sentence(Term invalidTerm, char punctuation, Truth newTruth, AbstractStamper newStamp) {
        this((T)Sentence.termOrException(invalidTerm), punctuation, newTruth, newStamp);
    }

    @Deprecated
    public Sentence(T seedTerm, final char punctuation, final Truth truth) {
        this(seedTerm, punctuation, truth, 0, 0, 0);
    }
    Sentence(T term, final char punctuation, final Truth truth, float p, float d, float q) {
        super(p,d,q);
        this.punctuation = punctuation;

        boolean isQuestionOrQuest = isQuestion() || isQuest();
        if (isQuestionOrQuest) {
            this.truth = null;
        }
        else if ( truth == null ) {
            throw new RuntimeException("Judgment and Goal sentences require non-null truth value");
        }
        else {
            this.truth = truth;
        }

        if (term instanceof Sequence) {
            this.term = (T) ((Sequence)term).cloneRemovingSuffixInterval();
        }
        else {
            this.term = term;
        }

        invalidateHash();

    }

    /**
     * Create a Sentence with the given fields
     *
     * @param seedTerm The Term that forms the content of the sentence
     * @param punctuation The punctuation indicating the type of the sentence
     * @param truth The truth value of the sentence, null for question
     * @param stamp The stamp of the sentence indicating its derivation time and
     * @param normalize if false, normalization is not attempted and the compound will be used as-is
     * base
     */
    public Sentence(T seedTerm, final char punctuation, final Truth truth, AbstractStamper stamp) {
        this(seedTerm, punctuation, truth);


        //apply the stamp to this
        stamp.applyToStamp(this);


//        if ((isQuestion() || isQuest()) && !isEternal()) {
//            //need to clone in case this stamp is shared by others which are not to eternalize it
//            //stamp = stamp.cloneEternal();
//            if (Global.DEBUG_NONETERNAL_QUESTIONS)
//                throw new RuntimeException("Questions and Quests require eternal tense");
//        }



    }



    
    /**
     * To check whether two sentences are equal
     *
     * @param that The other sentence
     * @return Whether the two sentences have the same content
     */
    @Override
    public boolean equals(final Object that) {
        if (this == that) return true;
        if (that instanceof Sentence) {
            return equivalentTo((Sentence) that);
        }
        return false;
    }

    /** compares all sentence fields, after comparing hash (which includes them all) */
    private boolean equivalentTo(final Sentence that) {
        //if (that.hashCode()!=hashCode()) return false;
        return equivalentTo(that, true, true, true, true, false);
    }

    /**
     * To produce the hashcode of a sentence, which consists of:
     *
     *
     * @return A hashcode
     */
    @Override
    public int hashCode() {
        if (this.hash == 0) {

            //stamp (evidentialset, occurrencetime), truth, term, punctuation

            int hashStamp = Util.hash(Arrays.hashCode(getEvidentialSet()), (int)this.occurrenceTime);

            if (truth == null)
                this.hash = (Util.hash(hashStamp, getTerm().hashCode()) * 31) + punctuation;
            else
                this.hash = (Util.hash(hashStamp, getTerm().hashCode(), truth.hashCode()) * 31) + punctuation;

        }

        return hash;
    }

    public void hash(PrimitiveSink into) {

        into.putBytes(getTerm().bytes());
        into.putByte((byte)punctuation);

        getStamp().hash(into);

        Truth t = truth;
        if (t != null) {
            into.putFloat(t.getFrequency());
            into.putFloat(t.getConfidence());
        }
        into.putLong(occurrenceTime);
        for (long e : getEvidentialSet())
            into.putLong(e);
    }


    /**
     * Check whether different aspects of sentence are equivalent to another one
     *
     * @param that The other judgment
     * @return Whether the two are equivalent
     */
    public boolean equivalentTo(final Sentence that, final boolean punctuation, final boolean term, final boolean truth, final boolean stamp, final boolean creationTime) {

        if (this == that) return true;

        final char thisPunc = this.punctuation;

        if (truth) {
            if (this.truth==null) {
                if (that.truth!=null) return false;
            }
            else {
                if (!this.truth.equals(that.truth)) return false;
            }
        }

        if (punctuation) {
            if (thisPunc!=that.punctuation) return false;
        }

        if (term) {
            if (!equalTerms(that)) return false;
        }

        if (stamp) {
            //uniqueness includes every aspect of stamp except creation time
            //<patham9> if they are only different in creation time, then they are the same
            if (!this.equalStamp(that, true, creationTime, true))
                return false;
        }



        return true;
    }



    /** returns a valid sentence CompoundTerm, or throws an exception */
    public static Compound termOrException(Term t) {
        if (invalidSentenceTerm(t))
            throw new RuntimeException(t + " not valid sentence content");
        return ((Compound)t);
    }

    /** returns a valid sentence CompoundTerm, or returns null */
    public static <X extends Compound> X termOrNull(Term t) {
        //if (invalidSentenceTerm(t))
            //return null;
        Term x = t.normalized();
//        if (Global.DEBUG) {
//            if (invalidSentenceTerm(x)) {
//                throw new RuntimeException("invalidity determined after normalization, wtf: " + t + " became " + x);
//            }
//        }

//        X x = (X)t.normalized();
        if (invalidSentenceTerm(x)) {
            return null;
        }

        return (X)x;
    }

    @Override
    public Sentence setCreationTime(long creationTime) {
        this.creationTime = creationTime;
        invalidateHash();
        return this;
    }

    public Sentence setOccurrenceTime(long occurrenceTime) {
        this.occurrenceTime = occurrenceTime;
        invalidateHash();
        return this;
    }

    public Sentence setOccurrenceTime(Tense tense, int duration) {
        return setOccurrenceTime(getCreationTime(), tense, duration);
    }

    public Sentence setOccurrenceTime(long creation, Tense tense, int duration) {
        return setOccurrenceTime(Stamp.getOccurrenceTime(creation, tense, duration));
    }


    protected void invalidateHash() {
        this.hash = 0;
    }

    public Sentence setEternal() {
        setTime(getCreationTime(), Stamp.ETERNAL);
        return this;
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

    /**
     * Clone the Sentence
     *
     * @return The clone
     */
    @Override
    public Sentence clone() {
        return clone(getTerm());
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
    /** Clone with a different Term */
    public <X extends Compound> Sentence<X> clone(X t) {
        X ct = termOrNull(t);
        if (ct == null) return null;

        if (ct.equals(getTerm())) {
            //throw new RuntimeException("Clone with " + t + " would produces exact sentence");
            return (Sentence<X>) this;
        }

        return clone_(ct);
    }

    protected <X extends Compound> Sentence<X> clone_(X t) {
        return new Sentence(t, punctuation,
                truth!=null ? BasicTruth.clone(truth) : null,
                this);
    }

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


    public ProjectedTruth projection(final long targetTime, final long currentTime) {

        final Truth currentTruth = truth;

                        
        if (!isEternal() && (targetTime != Stamp.ETERNAL)) {
            ProjectedTruth eternalTruth  = TruthFunctions.eternalize(currentTruth);

            long occurrenceTime = getOccurrenceTime();
            float factor = TruthFunctions.temporalProjection(occurrenceTime, targetTime, currentTime);
            float projectedConfidence = factor * currentTruth.getConfidence();
            if (projectedConfidence > eternalTruth.getConfidence()) {
                return new ProjectedTruth(currentTruth.getFrequency(), projectedConfidence, currentTruth.getEpsilon(), targetTime);
            }
            else {
                return eternalTruth;
            }

        } else {
            return new ProjectedTruth(currentTruth, getOccurrenceTime());
            //return truth;
        }
    }

    /** calculates projection truth quality without creating new TruthValue instances */
    public float projectionTruthQuality(long targetTime, long currentTime, boolean problemHasQueryVar) {
        return projectionTruthQuality(truth, targetTime, currentTime, problemHasQueryVar);

    }

    /** calculates projection truth quality without creating new TruthValue instances */
    public float projectionTruthQuality(final Truth t, long targetTime, long currentTime, boolean problemHasQueryVar) {
        return t.projectionQuality(this, targetTime, currentTime, problemHasQueryVar);
    }



    /**
     * Recognize a Question
     *
     * @return Whether the object is a Question
     */
    public boolean isQuestion() {
        return (punctuation == Symbols.QUESTION);
    }

    /**
     * Recognize a Judgment
     *
     * @return Whether the object is a Judgment
     */
    public boolean isJudgment() {
        return (punctuation == Symbols.JUDGMENT);
    }

    public boolean isGoal() {
        return (punctuation == Symbols.GOAL);
    }
 
    public boolean isQuest() {
        return (punctuation == Symbols.QUEST);
    }    
    
    public boolean hasQueryVar() {
        return getTerm().hasVarQuery();
    }

    public boolean isRevisible() {
        return !((getTerm() instanceof Conjunction) && getTerm().hasVarDep());
    }


    public int getTemporalOrder() {
        int t = getTerm().getTemporalOrder();
        if (t == TemporalRules.ORDER_INVALID)
            throw new RuntimeException(this + " has INVALID temporal order");
        return t;
    }



    /**
     * Get a String representation of the sentence
     *
     * @return The String
     */
    @Override
    public String toString() {
        throw new RuntimeException("should not need to be called");
        //return getKey().toString();
    }

    @Override
    public Sentence name() {
        return this;
    }

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

    public CharSequence toString(NAR nar, boolean showStamp) {
        return toString(nar.memory, showStamp);
    }

    public CharSequence toString(final Memory memory, final boolean showStamp) {
        return toString(new StringBuilder(), memory, showStamp);
    }

    @Deprecated public StringBuilder toString(StringBuilder buffer, final Memory memory, final boolean showStamp) {
        return toString(buffer, memory, true, true);
    }
    /**
     * Get a String representation of the sentence for display purpose
     *
     * @param buffer provided StringBuilder to append to
     * @param memory may be null in which case the tense is expressed in numbers without any relativity to memory's current time or duration
     * @return The String
     */
    @Deprecated public StringBuilder toString(StringBuilder buffer, final Memory memory, final boolean term, final boolean showStamp) {

        String contentName;
        if (term) {
             contentName = getTerm().toString();
        }
        else contentName = "";

        final CharSequence tenseString;
        if (memory!=null) {
            tenseString = getTense(memory.time(), memory.duration());
        }
        else {
            appendOcurrenceTime((StringBuilder) (tenseString = new StringBuilder()));
        }
        
        
        CharSequence stampString = showStamp ? stampAsStringBuilder() : null;
        
        int stringLength = contentName.length() + tenseString.length() + 1 + 1;
                
        if (truth != null)
            stringLength += 11;
        
        if (showStamp)
            stringLength += stampString.length()+1;
        
        if (buffer == null)
            buffer = new StringBuilder(stringLength);
        else
            buffer.ensureCapacity(stringLength);
        buffer.append(contentName).append(punctuation);
        
        if (tenseString.length() > 0)
            buffer.append(' ').append(tenseString);
        
        if (truth != null) {
            buffer.append(' ');
            truth.appendString(buffer, 2);
        }
        
        if (showStamp)
            buffer.append(' ').append(stampString);
        
        return buffer;
    }

    final public boolean equalTerms(final Sentence s) {
        return getTerm().equals(s.getTerm());
    }

    final public boolean equalPunctuations(Sentence s) {
        return punctuation == s.punctuation;
    }

    public final boolean isEternal() {
        return occurrenceTime == Stamp.ETERNAL;
    }


    @Override
    public long[] getEvidentialSet() {
        return evidentialSet;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public int getDuration() {
        return duration;
    }

    public long getOccurrenceTime() {
        return occurrenceTime;
    }



    public boolean after(Sentence s, int duration) {
        return TemporalRules.occurrsAfter(s, this);
    }

    public boolean before(Sentence s, int duration) {
        return TemporalRules.occurrsAfter(this, s);
    }



    /** applies this Sentence's stamp information to a target Sentence (implementing IStamp) */
    @Override public void applyToStamp(Stamp target) {
        target.setDuration(getDuration());
        target.setTime(getCreationTime(), getOccurrenceTime());
        target.setEvidentialSet(getEvidentialSet());
        target.setCyclic(isCyclic());
    }

    @Override
    public Sentence setEvidentialSet(long[] evidentialSet) {
        if (evidentialSet!=null) {
            this.evidentialSet = evidentialSet;
        }

        invalidateHash();
        return this;
    }

    public Sentence setDuration(int duration) {
        this.duration = duration;
        return this;
    }

    /** WARNING: calling this should not change the value of term, but just the
     * particular instance that it references
     */
    public void setTermInstance(final T term) {

        //intermval generally contains unique information that should not be replaced
        if (this.term instanceof Intermval)
            return;

        this.term = term;
    }


    public static final class ExpectationComparator implements Comparator<Sentence> {
        final static Comparator the = new ExpectationComparator();
        @Override public int compare(final Sentence b, final Sentence a) {
            return Float.compare(a.truth.getExpectation(), b.truth.getExpectation());
        }
    }
    public static final class ConfidenceComparator implements Comparator<Sentence> {
        final static Comparator the = new ExpectationComparator();
        @Override public int compare(final Sentence b, final Sentence a) {
            return Float.compare(a.truth.getConfidence(), b.truth.getConfidence());
        }
    }
    
    public static List<Sentence> sortExpectation(Collection<Sentence> s) {
        List<Sentence> l = new ArrayList(s);
        Collections.sort(l, ExpectationComparator.the);
        return l;
    }
    public static List<Sentence> sortConfidence(Collection<Sentence> s) {
        List<Sentence> l = new ArrayList(s);
        Collections.sort(l, ConfidenceComparator.the);
        return l;
    }
    
    /** performs some (but not exhaustive) tests on a term to determine some cases where it is invalid as a sentence content
     * returns true if the term is invalid for use as sentence content term
     * */
    public static final boolean invalidSentenceTerm(final Term t) {
        if (!(t instanceof Compound)) { //(t instanceof Interval) || (t instanceof Variable)
            return true;
        }

        if (t instanceof Statement) {
            Statement st = (Statement) t;

            /* A statement sentence is not allowed to have a independent variable as subj or pred"); */
            if (st.subjectOrPredicateIsIndependentVar())
                return true;

            if (Statement.invalidStatement(st))
                return true;

        }


        //ok valid
        return false;
    }

    /**
     * The content of a Sentence is a Term
     */
    @Override
    public T getTerm() {
        return term;
    }

    @Override
    public Truth getTruth() {
        return truth;
    }


    @Deprecated public static class SubTermVarCollector implements TermVisitor {
        private final List<Variable> vars;

        public SubTermVarCollector(List<Variable> vars) {
            this.vars = vars;
        }

        @Override public void visit(final Term t, final Term parent) {
            if (t instanceof Variable) {
                Variable v = ((Variable)t);
                vars.add(v);
            }
        }
    }

    public Sentence getStamp() {
        return this;
    }

    @Override
    public Sentence getSentence() {
        return this;
    }

    @Override
    public boolean isCyclic() {
        return cyclic;
    }

    @Override
    public void setCyclic(boolean cyclic) {
        this.cyclic = cyclic;
    }

}
