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
package nars.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import nars.core.NAR;
import nars.core.Parameters;
import nars.entity.TruthValue.Truthable;
import nars.inference.TemporalRules;
import nars.inference.TruthFunctions;
import nars.inference.TruthFunctions.EternalizedTruthValue;
import nars.io.Symbols;
import nars.io.Texts;
import nars.language.CompoundTerm;
import nars.language.Conjunction;
import nars.language.Interval;
import nars.language.Interval.AtomicDuration;
import nars.language.Statement;
import nars.language.Term;
import nars.language.Terms.Termable;
import nars.language.Variable;
import nars.operator.Operation;
import nars.operator.Operator;

/**
 * A Sentence is an abstract class, mainly containing a Term, a TruthValue, and
 * a Stamp.
 * <p>
 * It is used as the premises and conclusions of all inference rules.
 */
public class Sentence<T extends Term> implements Cloneable, Termable, Truthable {

    public boolean producedByTemporalInduction=false;

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

    /** caches the 'getKey()' result */
    private transient CharSequence key;

    private final int hash;
    
    
    public Sentence(T term, char punctuation, TruthValue newTruth, Stamp newStamp) {
        this(term, punctuation, newTruth, newStamp, true);
    }
    
    /**
     * Create a Sentence with the given fields
     *
     * @param content The Term that forms the content of the sentence
     * @param punctuation The punctuation indicating the type of the sentence
     * @param truth The truth value of the sentence, null for question
     * @param stamp The stamp of the sentence indicating its derivation time and
     * base
     */
    private Sentence(T _content, final char punctuation, final TruthValue truth, final Stamp stamp, boolean normalize) {
        
        //cut interval at end for sentence in serial conjunction, and inbetween for parallel
        if(_content instanceof Conjunction) {
            Conjunction c=(Conjunction)_content;
            if(c.getTemporalOrder()==TemporalRules.ORDER_FORWARD) {
                if(c.term[c.term.length-1] instanceof Interval) {
                    Term[] term2=new Term[c.term.length-1];
                    for(int i=0;i<c.term.length-1;i++) {
                        term2[i]=c.term[i];
                    }
                    _content=(T) Conjunction.make(term2, c.getTemporalOrder());
                    //ok we removed a part of the interval, we have to transform the occurence time of the sentence back
                    //accordingly
                    long time=Interval.magnitudeToTime(((Interval)c.term[c.term.length-1]).magnitude,new AtomicDuration(Parameters.DURATION));
                    if(stamp!=null)
                        stamp.setOccurrenceTime(stamp.getOccurrenceTime()-time);
                }
            }
        }
        
        this.punctuation = punctuation;
        
        if (_content instanceof Interval)
        {
            truth.setConfidence(0.0f); //do it that way for now, because else further inference is interrupted.
            if(Parameters.DEBUG)
                throw new RuntimeException("Sentence content must not be Interval: " + _content + punctuation + " " + stamp);
        }
        
        if ( (!isQuestion() && !isQuest()) && (truth == null) ) {            
            throw new RuntimeException("Judgment and Goal sentences require non-null truth value");
        }
        
        if(_content.subjectOrPredicateIsIndependentVar()) {
            truth.setConfidence(0.0f); //do it that way for now, because else further inference is interrupted.
            if(Parameters.DEBUG)
                throw new RuntimeException("A statement sentence is not allowed to have a independent variable as subj or pred");
        }
        
        if (Parameters.DEBUG && Parameters.DEBUG_INVALID_SENTENCES) {
            if (!Term.valid(_content)) {
                CompoundTerm.UnableToCloneException ntc = new CompoundTerm.UnableToCloneException("Invalid Sentence term: " + _content);
                ntc.printStackTrace();
                throw ntc;
            }
        }
        
        
        if ((isQuestion() || isQuest()) && !stamp.isEternal()) {
            stamp.setEternal();
            //throw new RuntimeException("Questions and Quests require eternal tense");
        }
        
        this.truth = truth;
        this.stamp = stamp;
        this.revisible = !((_content instanceof Conjunction) && _content.hasVarDep());
            
        
        //Variable name normalization
        //TODO move this to Concept method, like cloneNormalized()
        if (normalize && _content.hasVar() && (_content instanceof CompoundTerm) && (!((CompoundTerm)_content).isNormalized() ) ) {
            
            this.term = (T)((CompoundTerm)_content).cloneDeepVariables();
            
            final CompoundTerm c = (CompoundTerm)term;
            
            List<Variable> vars = new ArrayList(); //may contain duplicates, list for efficiency
            
            c.recurseSubtermsContainingVariables(new Term.TermVisitor() {                
                @Override public void visit(final Term t, final Term parent) {
                    if (t instanceof Variable) {
                        Variable v = ((Variable)t);                        
                        vars.add(v);
                    }                    
                }            
            });
            
            Map<CharSequence,CharSequence> rename = new HashMap();            
            boolean renamed = false;
            
            for (final Variable v : vars) {
                
                CharSequence vname = v.name();
                if (!v.hasVarIndep())
                    vname = vname + " " + v.getScope().name();                                
                
                CharSequence n = rename.get(vname);                
                
                if (n==null) {                            
                    //type + id
                    rename.put(vname, n = Variable.getName(v.getType(), rename.size()+1));
                    if (!n.equals(vname))
                        renamed = true;
                }    

                v.setScope(c, n);                
            }
            
            if (renamed) {
                c.invalidateName();

                if (Parameters.DEBUG && Parameters.DEBUG_INVALID_SENTENCES) {
                    if (!Term.valid(c)) {
                        CompoundTerm.UnableToCloneException ntc = new CompoundTerm.UnableToCloneException("Invalid term discovered after normalization: " + c + " ; prior to normalization: " + _content);
                        ntc.printStackTrace();
                        throw ntc;
                    }
                }
                
            }
            
            c.setNormalized(true);            
            
            
        }
        else {
            this.term = _content;
        }
        
    
        if (isUniqueByOcurrenceTime())
            this.hash = Objects.hash(term, punctuation, truth, stamp.getOccurrenceTime());
        else 
            this.hash = Objects.hash(term, punctuation, truth );
    }

    

    protected boolean isUniqueByOcurrenceTime() {
        return ((punctuation == Symbols.JUDGMENT_MARK) || (punctuation == Symbols.QUESTION_MARK));
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
            final Sentence t = (Sentence) that;
            //return getKey().equals(t.getKey());
            
            if (hash!=t.hash) return false;
            
            if (punctuation!=t.punctuation) return false;
            if (isUniqueByOcurrenceTime()) {
                if (stamp.getOccurrenceTime()!=t.stamp.getOccurrenceTime()) return false;
            }                
            
            if (truth==null) {
                if (t.truth!=null) return false;
            }
            else if (t.truth==null) {
                return false;
            }
            else if (!truth.equals(t.truth)) return false;            
            
            if (!term.equals(t.term)) return false;
                    
            return true;
        }
        return false;
    }

    /**
     * To produce the hashcode of a sentence
     *
     * @return A hashcode
     */
    @Override
    public int hashCode() {
        return hash;
    }

    /**
     * Check whether the judgment is equivalent to another one
     * <p>
     * The two may have different keys
     *
     * @param that The other judgment
     * @return Whether the two are equivalent
     */
    public boolean equivalentTo(final Sentence that) {
        if (Parameters.DEBUG) {
            if ((!term.equals(term)) || (punctuation != that.punctuation)) {
                throw new RuntimeException("invalid comparison for Sentence.equivalentTo");
            }
        }
        return (truth.equals(that.truth) && stamp.equals(that.stamp,false,false,true,true));
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
    
    
    public Sentence clone(boolean makeEternal) {
        Sentence clon = clone(term);
        if(clon.stamp.getOccurrenceTime()!=Stamp.ETERNAL && makeEternal) {
            //change occurence time of clone
            clon.stamp.setEternal();
        }
        return clon;
    }

    /** Clone with a different Term */    
    public final Sentence clone(final Term t) {
        return new Sentence(t, punctuation, 
                truth!=null ? new TruthValue(truth) : null, 
                stamp.clone());
    }

    /**
      * project a judgment to a difference occurrence time
      *
      * @param targetTime The time to be projected into
      * @param currentTime The current time as a reference
      * @return The projected belief
      */    
    public Sentence projection(final long targetTime, final long currentTime) {
            
        TruthValue newTruth = projectionTruth(targetTime, currentTime);
        
        boolean eternalizing = (newTruth instanceof EternalizedTruthValue);
                
        Stamp newStamp = eternalizing ? stamp.cloneWithNewOccurrenceTime(Stamp.ETERNAL) : 
                                        stamp.cloneWithNewOccurrenceTime(targetTime);
        
        return new Sentence(term, punctuation, newTruth, newStamp, false);
    }

    
    public TruthValue projectionTruth(final long targetTime, final long currentTime) {
        TruthValue newTruth = null;
                        
        if (!stamp.isEternal()) {
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


//    /**
//     * Clone the content of the sentence
//     *
//     * @return A clone of the content Term
//     */
//    public Term cloneContent() {
//        return content.clone();
//    }
//


    /**
     * Recognize a Judgment
     *
     * @return Whether the object is a Judgment
     */
    public boolean isJudgment() {
        return (punctuation == Symbols.JUDGMENT_MARK);
    }

    /**
     * Recognize a Question
     *
     * @return Whether the object is a Question
     */
    public boolean isQuestion() {
        return (punctuation == Symbols.QUESTION_MARK);
    }

    public boolean isGoal() {
        return (punctuation == Symbols.GOAL_MARK);
    }
 
    public boolean isQuest() {
        return (punctuation == Symbols.QUEST_MARK);
    }    
    
    public boolean containQueryVar() {
        return term.hasVarQuery();
    }

    public boolean getRevisible() {
        return revisible;
    }

    public void setRevisible(final boolean b) {
        revisible = b;
    }

    public int getTemporalOrder() {
        return term.getTemporalOrder();
    }
    
    public long getOccurenceTime() {
        return stamp.getOccurrenceTime();
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

 
    /**
     * Get a String representation of the sentence for key of Task and TaskLink
     *
     * @return The String
     */
    public CharSequence getKey() {
        //key must be invalidated if content or truth change
        if (key == null) {
            final CharSequence contentName = term.name();
            
            final boolean showOcurrenceTime = ((punctuation == Symbols.JUDGMENT_MARK) || (punctuation == Symbols.QUESTION_MARK));
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
                suffix.append(' ');
                stamp.appendOcurrenceTime(suffix);
            }

            key = Texts.yarn(Parameters.ROPE_TERMLINK_TERM_SIZE_THRESHOLD, 
                    contentName,//.toString(), 
                    suffix); //.toString());
            //key = new FlatCharArrayRope(StringUtil.getCharArray(k));

        }
        return key;
    }

    /**
     * Get a String representation of the sentence for display purpose
     *
     * @return The String
     */
    public CharSequence toString(NAR nar, boolean showStamp) {
    
        CharSequence contentName = term.name();
        
        final long t = nar.memory.time();

        final String tenseString = stamp.getTense(t, nar.memory.getDuration());
        
        
        CharSequence stampString = showStamp ? stamp.name() : null;
        
        int stringLength = contentName.length() + tenseString.length() + 1 + 1;
                
        if (truth != null)
            stringLength += 11;
        
        if (showStamp)
            stringLength += stampString.length()+1;
        
        
        final StringBuilder buffer = new StringBuilder(stringLength).
                    append(contentName).append(punctuation);
        
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
     * @return Truth value, null for question
     */
    public void discountConfidence() {
        truth.setConfidence(truth.getConfidence() * Parameters.DISCOUNT_RATE).setAnalytic(false);
    }


    final public boolean equalsContent(final Sentence s2) {
        return term.equals(s2.term);
    }

    public boolean isEternal() {
        return stamp.isEternal();
    }

    public boolean after(Sentence s, int duration) {
        return stamp.after(s.stamp, duration);
    }
    public boolean before(Sentence s, int duration) {
        return stamp.before(s.stamp, duration);
    }

    public long getCreationTime() {
        return stamp.getCreationTime();
    }

    public static final class ExpectationComparator implements Comparator<Sentence> {
        final static ExpectationComparator the = new ExpectationComparator();
        @Override public int compare(final Sentence b, final Sentence a) {
            return Float.compare(a.truth.getExpectation(), b.truth.getExpectation());
        }
    }
    public static final class ConfidenceComparator implements Comparator<Sentence> {
        final static ExpectationComparator the = new ExpectationComparator();
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
    
    /** performs some (but not exhaustive) tests on a term to determine some cases where it is invalid as a sentence content */
    public static final boolean invalidSentenceTerm(final Term T) {
        if (!(T instanceof CompoundTerm)) {
            return true;
        }
        if (T instanceof Statement) {
            Statement st = (Statement) T;
            if (Statement.invalidStatement(st.getSubject(), st.getPredicate()))
                return true;
            if (st.getSubject().equals(st.getPredicate())) {
                return true;
            }
        }
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
    
    
    
}
