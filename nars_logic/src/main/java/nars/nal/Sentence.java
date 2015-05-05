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
package nars.nal;

import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.io.Symbols;
import nars.io.Texts;
import nars.nal.TruthFunctions.EternalizedTruthValue;
import nars.nal.nal5.Conjunction;
import nars.nal.nal7.Interval;
import nars.nal.nal7.TemporalRules;
import nars.nal.nal8.Operation;
import nars.nal.nal8.Operator;
import nars.nal.stamp.Stamp;
import nars.nal.stamp.Stamped;
import nars.nal.term.*;

import java.util.*;

/**
 * A Sentence is an abstract class, mainly containing a Term, a TruthValue, and
 * a Stamp.
 * <p>
 * It is used as the premises and conclusions of all logic rules.
 */
//TODO: make Named<byte[]>
public class Sentence<T extends Compound> implements Cloneable, Named<String>, Termed, TruthValue.Truthable, Stamped {




    public static interface Sentenced<T2 extends Compound> extends Termed {
        public Sentence<T2> getSentence();
    }

    /**
     * The content of a Sentence is a Term
     */
    public final T term;
    
    /**
     * The punctuation also indicates the type of the Sentence: 
     * Judgment, Question, Goal, or Quest.
     * Represented by characters: '.', '?', '!', or '@'
     */
    public final char punctuation;
    
    /**
     * The truth value of Judgment, or desire value of Goal     
     */
    public final TruthValue truth;
    
    /**
     * Partial record of the derivation path
     */
    public final Stamp stamp;

    /**
     * Whether the sentence can be revised
     */
    private boolean revisible;



    transient private int hash;


    public Sentence(Term invalidTerm, char punctuation, TruthValue newTruth, NAL.StampBuilder newStamp) {
        this((T)Sentence.termOrException(invalidTerm), punctuation, newTruth, newStamp);
    }

    public Sentence(T term, char punctuation, TruthValue newTruth, NAL.StampBuilder newStamp) {
        this(term, punctuation, newTruth, newStamp, true);
    }

    /**
     * Create a Sentence with the given fields
     *
     * @param seedTerm The Term that forms the content of the sentence
     * @param punctuation The punctuation indicating the type of the sentence
     * @param truth The truth value of the sentence, null for question
     * @param stamp The stamp of the sentence indicating its derivation time and
     * base
     */
    public Sentence(T seedTerm, final char punctuation, final TruthValue truth, final NAL.StampBuilder stamp, boolean normalize) {
        


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

        Stamp st = stamp.build();
        if ((isQuestion() || isQuest()) && !st.isEternal()) {
            //need to clone in case this stamp is shared by others which are not to eternalize it
            st = st.cloneEternal();
            if (Global.DEBUG_NONETERNAL_QUESTIONS)
                throw new RuntimeException("Questions and Quests require eternal tense");
        }


        //HACK special handling for conjunctions which may have a trailing interval
        if ((seedTerm instanceof Conjunction) && (seedTerm.getTemporalOrder() == TemporalRules.ORDER_FORWARD)) {
            st = st.clone(); //temporary: clone the stamp in case it's modified in getTerm; shouldnt be necessary when terms are rerranged

            T newSeedTerm = getTerm(seedTerm, st);
            if (newSeedTerm == null)
                throw new RuntimeException("Error creating a sentence for Conjunction term: " + seedTerm);
            if (newSeedTerm != seedTerm) {
                normalize = true;
                seedTerm = newSeedTerm;
            }

        }




        this.stamp = st;

        this.revisible = !((seedTerm instanceof Conjunction) && seedTerm.hasVarDep());

        this.term = normalize ? seedTerm.cloneNormalized() : seedTerm;
        if (term == null)
            throw new RuntimeException("Term for a new Sentence not valid or could not be normalized: " + seedTerm);

        if (Global.DEBUG) {
            if (invalidSentenceTerm(term))
                throw new RuntimeException("Invalid sentence content term: " + term + ", seedTerm=" + seedTerm);
        }


        this.term.ensureNormalized("Sentence term");

        this.hash = 0;

    }

    /** pre-processes a term for sentence's use; in most cases just returns the term itself */
    protected T getTerm(T t, Stamp st) {
        if (t instanceof Conjunction) {

            //cut interval at end for sentence in serial conjunction, and inbetween for parallel
            //TODO this can be extended to remove N suffix intervals, if we ever use the multiple interval thing
            Conjunction c=(Conjunction)t;
            if(c.getTemporalOrder()==TemporalRules.ORDER_FORWARD) {
                if(c.term[c.term.length-1] instanceof Interval) {
                    Term[] term2=new Term[c.term.length-1];
                    //TODO use System.arraycopy
                    for(int i=0;i<c.term.length-1;i++) {
                        term2[i]=c.term[i];
                    }
                    Term x = Conjunction.make(term2, c.getTemporalOrder());
                    if (!(x instanceof Compound)) return null;
                    T u = (T)x;

                    //if the resulting term is valid for a sentence, adjust the stamp and return the new term
                    if ((u = termOrNull(u)) != null) {

                        //ok we removed a part of the interval, we have to transform the occurence time of the sentence back
                        //accordingly
                        //TODO make this interval not hardcoded
                        long time = Interval.cycles(((Interval) c.term[c.term.length - 1]).magnitude, new Interval.AtomicDuration(st.getDuration()));
                        if (!st.isEternal())
                            st.setOccurrenceTime(st.getOccurrenceTime() - time);
                        return u;
                    }
                    else {
                        //the result would not be valid, so just return the original input
                        //this sentence should not be created but in the meantime, this will prevent it from having any effect
                        if (truth!=null)
                            truth.setConfidence(0);
                    }
                }
            }

        }
        return t;
    }


    public void setRevisible(boolean b) {
        this.revisible = b;
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
    private boolean equivalentTo(Sentence that) {
        if (that.hashCode()!=hashCode()) return false;
        return equivalentTo(that, true, true, true, true);
    }

    /**
     * To produce the hashcode of a sentence
     *
     * @return A hashcode
     */
    @Override
    public int hashCode() {
        if ((this.hash == 0) /*&& (stamp!=null)*/) {
            this.hash = Objects.hash(term, punctuation, truth, stamp);
        }
        return hash;
    }

    /**
     * Check whether different aspects of sentence are equivalent to another one
     *
     * @param that The other judgment
     * @return Whether the two are equivalent
     */
    public boolean equivalentTo(final Sentence that, final boolean punctuation, final boolean term, final boolean truth, final boolean stamp) {

        final char thisPunc = this.punctuation;

        if (this == that) return true;
        if (punctuation) {
            if (thisPunc!=that.punctuation) return false;
        }
        if (stamp) {
            //uniqueness includes every aspect of stamp except creation time
            //<patham9> if they are only different in creation time, then they are the same
            if (!this.stamp.equals(that.stamp, true, true, false, true))
                return false;
        }
        if (truth) {
            if (this.truth==null) {
                if (that.truth!=null) return false;
            }
            else {
                if (!this.truth.equals(that.truth)) return false;
            }
        }
        if (term) {
            if (!equalTerms(that)) return false;
        }

        return true;
    }

    /**
     * Clone the Sentence
     *
     * @return The clone
     */
    @Override
    public Sentence clone() {
        return clone(term);
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
        X x = (X)t.normalized();
//        if (Global.DEBUG) {
//            if (invalidSentenceTerm(x)) {
//                throw new RuntimeException("invalidity determined after normalization, wtf: " + t + " became " + x);
//            }
//        }

//        X x = (X)t.normalized();
        if (x == null || invalidSentenceTerm(x)) {
            return null;
        }

        return x;
    }

    
    public Sentence clone(final boolean makeEternal) {
        Sentence clon = clone(term);
        if(clon.stamp.getOccurrenceTime()!=Stamp.ETERNAL && makeEternal) {
            //change occurence time of clone
            clon.stamp.setEternal();
        }
        return clon;
    }


    public final <X extends Compound> Sentence<X> clone(final Term t, final Class<? extends X> necessaryTermType) {
        X ct = termOrNull(t);
        if (ct == null) return null;

        if (!ct.getClass().isInstance(necessaryTermType))
            return null;

        if (ct.equals(term)) {
            return (Sentence<X>) this;
        }
        return clone_(ct);

    }

    /** Clone with a different Term */
    public <X extends Compound> Sentence<? extends X> clone(Term t) {
        X ct = termOrNull(t);
        if (ct == null) return null;

        if (ct.equals(term)) {
            //throw new RuntimeException("Clone with " + t + " would produces exact sentence");
            return (Sentence<X>) this;
        }

        return clone_(ct);
    }

    protected <X extends Compound> Sentence<X> clone_(X t) {
        return new Sentence(t, punctuation,
                truth!=null ? new TruthValue(truth) : null,
                stamp.clone());
    }

//    public final Sentence clone(final CompoundTerm t) {
//        //sentence content must be compoundterm
//        if (t instanceof CompoundTerm) {
//            return this.clone((CompoundTerm)t);
//        }
//        return null;
//    }


    /**
      * project a judgment to a difference occurrence time
      *
      * @param targetTime The time to be projected into
      * @param currentTime The current time as a reference
      * @return The projected belief
      */    
    public Sentence projection(final long targetTime, final long currentTime) {
            
        final TruthValue newTruth = projectionTruth(targetTime, currentTime);
        
        final boolean eternalizing = (newTruth instanceof EternalizedTruthValue);

        Stamp newStamp = eternalizing ? stamp.cloneWithNewOccurrenceTime(Stamp.ETERNAL) : stamp.cloneWithNewOccurrenceTime(targetTime);

        return new Sentence(term, punctuation, newTruth, newStamp, false);
    }



    public TruthValue projectionTruth(final long targetTime, final long currentTime) {
        TruthValue newTruth = null;
                        
        if (!isEternal()) {
            newTruth = TruthFunctions.eternalize(truth);
            if (targetTime != Stamp.ETERNAL) {
                long occurrenceTime = stamp.getOccurrenceTime();
                float factor = TruthFunctions.temporalProjection(occurrenceTime, targetTime, currentTime);
                float projectedConfidence = factor * truth.getConfidence();
                if (projectedConfidence > newTruth.getConfidence()) {
                    newTruth = new TruthValue(truth.getFrequency(), projectedConfidence);
                }
            }
        }
        
        if (newTruth == null) newTruth = truth.clone();
        
        return newTruth;
    }

    /** calculates projection truth quality without creating new TruthValue instances */
    public float projectionTruthQuality(long targetTime, long currentTime, boolean problemHasQueryVar) {
        float freq = truth.getFrequency();
        float conf = truth.getConfidence();

        if (!isEternal() && (targetTime != getOccurrenceTime())) {
            conf = TruthFunctions.eternalizedConfidence(conf);
            if (targetTime != Stamp.ETERNAL) {
                long occurrenceTime = stamp.getOccurrenceTime();
                float factor = TruthFunctions.temporalProjection(occurrenceTime, targetTime, currentTime);
                float projectedConfidence = factor * truth.getConfidence();
                if (projectedConfidence > conf) {
                    conf = projectedConfidence;
                }
            }
        }

        if (problemHasQueryVar) {
            return TruthValue.expectation(freq, conf) / term.getComplexity();
        } else {
            return conf;
        }
    }



    /**
     * Recognize a Judgment
     *
     * @return Whether the object is a Judgment
     */
    public boolean isJudgment() {
        return (punctuation == Symbols.JUDGMENT);
    }

    /**
     * Recognize a Question
     *
     * @return Whether the object is a Question
     */
    public boolean isQuestion() {
        return (punctuation == Symbols.QUESTION);
    }

    public boolean isGoal() {
        return (punctuation == Symbols.GOAL);
    }
 
    public boolean isQuest() {
        return (punctuation == Symbols.QUEST);
    }    
    
    public boolean hasQueryVar() {
        return term.hasVarQuery();
    }

    public boolean isRevisible() {
        return revisible;
    }

    /*public Sentence setRevisible(final boolean b) {
        revisible = b;
        return this;
    }*/

    public int getTemporalOrder() {
        int t = term.getTemporalOrder();
        if (t == TemporalRules.ORDER_INVALID)
            throw new RuntimeException(this + " has INVALID temporal order");
        return t;
    }
    

    public Operator getOperator() {
        if (term instanceof Operation) {
             return (Operator) ((Statement) term).getPredicate();
        } else {
             return null;
        }
    }    
    
    /**
     * Get a String representation of the sentence
     *
     * @return The String
     */
    @Override
    public String toString() {
        return getKey().toString();
    }

    @Override
    public String name() {
        return toString();
    }

    /**
     * Get a String representation of the sentence for key of Task and TaskLink
     * We don't cache the Sentence string for 2 reasons:
     *      1. it is not ordinarily generated except for output
     *      2. if the stamp or other component changes, it would need to be re-calculated
     */
    public CharSequence getKey() {
        final String contentName = term.toString();

        final boolean showOcurrenceTime = !isEternal(); //((punctuation == Symbols.JUDGMENT) || (punctuation == Symbols.QUESTION));
        //final String occurrenceTimeString =  ? stamp.getOccurrenceTimeString() : "";

        //final CharSequence truthString = truth != null ? truth.name() : null;

        int stringLength = 0; //contentToString.length() + 1 + 1/* + stampString.baseLength()*/;
        if (truth != null) {
            stringLength += (showOcurrenceTime ? 8 : 0) + 11 /*truthString.length()*/;
        }

        //suffix = [punctuation][ ][truthString][ ][occurenceTimeString]
        final StringBuilder suffix = new StringBuilder(stringLength).append(punctuation);

        if (truth != null) {
            suffix.append(' ');
            truth.appendString(suffix, false);
        }
        if ((showOcurrenceTime) && (stamp!=null)) {
            suffix.append(" {"); //space + stamp opener
            stamp.appendOcurrenceTime(suffix);
            suffix.append('}'); //stamp closer
        }

        return Texts.yarn(Global.ROPE_TERMLINK_TERM_SIZE_THRESHOLD,
                contentName,//.toString(),
                suffix); //.toString());
        //key = new FlatCharArrayRope(StringUtil.getCharArray(k));
    }

    public CharSequence toString(NAR nar, boolean showStamp) {
        return toString(nar.memory, showStamp);
    }

    public CharSequence toString(final Memory memory, final boolean showStamp) {
        return toString(new StringBuilder(), memory, showStamp);
    }

    /**
     * Get a String representation of the sentence for display purpose
     *
     * @param buffer provided StringBuilder to append to
     * @param memory may be null in which case the tense is expressed in numbers without any relativity to memory's current time or duration
     * @return The String
     */
    @Deprecated public StringBuilder toString(StringBuilder buffer, final Memory memory, final boolean showStamp) {
    
        String contentName = term.toString();

        final CharSequence tenseString;
        if (memory!=null) {
            tenseString = stamp.getTense(memory.time(), memory.duration());
        }
        else {
            stamp.appendOcurrenceTime((StringBuilder) (tenseString = new StringBuilder()));
        }
        
        
        CharSequence stampString = showStamp ? stamp.name() : null;
        
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
            truth.appendString(buffer, true);
        }
        
        if (showStamp)
            buffer.append(' ').append(stampString);
        
        return buffer;
    }
    
   
    /**
     * Get the truth value (or desire value) of the sentence
     *
     * Should only be used in Concept's sentences, not in other location where Sentence is expected to be immutable
     * TODO make a distinct superclass between Mutable & ImmutableSentence for the different places Sentence are used (ex: Task vs. Concept)
     *
     * @return Truth value, null for question
     */
    public void discountConfidence() {
        truth.setConfidence(truth.getConfidence() * Global.DISCOUNT_RATE).setAnalytic(false);
    }


    final public boolean equalTerms(final Sentence s) {
        return term.equals(s.term);
    }

    final public boolean equalPunctuations(Sentence s) {
        return punctuation == s.punctuation;
    }

    public final boolean isEternal() {
        return stamp.isEternal();
    }

    public long getCreationTime() { return stamp.getCreationTime();    }
    public long getOccurrenceTime() {
        return stamp.getOccurrenceTime();
    }

    public boolean after(Sentence s, int duration) {
        return stamp.after(s.stamp, duration);
    }

    public boolean before(Sentence s, int duration) {
        return stamp.before(s.stamp, duration);
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
        if ((t == null) || (!(t instanceof Compound))) { //(t instanceof Interval) || (t instanceof Variable)
            return true;
        }

        if (t instanceof Statement) {
            Statement st = (Statement) t;

            /* A statement sentence is not allowed to have a independent variable as subj or pred"); */
            if (st.subjectOrPredicateIsIndependentVar())
                return true;

            if (Statement.invalidStatement(st.getSubject(), st.getPredicate()))
                return true;

        }


        //ok valid
        return false;
    }

    @Override
    public T getTerm() {
        return term;
    }

    @Override
    public TruthValue getTruth() {
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

    public Stamp getStamp() {
        return stamp;
    }

}
