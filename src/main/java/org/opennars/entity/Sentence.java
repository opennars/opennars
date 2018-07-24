/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.opennars.entity;

import org.opennars.inference.TemporalRules;
import org.opennars.inference.TruthFunctions;
import org.opennars.inference.TruthFunctions.EternalizedTruthValue;
import org.opennars.io.Symbols;
import org.opennars.io.Texts;
import org.opennars.language.*;
import org.opennars.main.Nar;
import org.opennars.main.MiscFlags;

import java.io.Serializable;
import java.util.*;
import org.opennars.main.Parameters;
import org.opennars.storage.Memory;

/**
 * A Sentence is used as the premises and conclusions of all inference rules.
 */
public class Sentence<T extends Term> implements Cloneable, Serializable {

    public boolean producedByTemporalInduction=false;

    /**
     * The content of a Sentence is a Term
     */
    public final T term;
    
    /**
     * The punctuation indicates the type of the Sentence:
     * Judgment '.', Question '?', Goal '!', or Quest '@'
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

    /**
     * caches the 'getKey()' result
     */
    private CharSequence key;

    private final int hash;
    
    
    public Sentence(final T term, final char punctuation, final TruthValue newTruth, final Stamp newStamp) {
        this(term, punctuation, newTruth, newStamp, true);
    }
    
    /**
     * Create a Sentence with the given fields
     *
     * @param _content The Term that forms the content of the sentence
     * @param punctuation The punctuation indicating the type of the sentence
     * @param truth The truth value of the sentence, null for question
     * @param stamp The stamp of the sentence indicating its derivation time and
     * base
     */
    private Sentence(T _content, final char punctuation, final TruthValue truth, final Stamp stamp, final boolean normalize) {
        
        //cut interval at end for sentence in serial conjunction, and inbetween for parallel
        if(punctuation!=Symbols.TERM_NORMALIZING_WORKAROUND_MARK) {
            if(_content instanceof Conjunction) {
                final Conjunction c=(Conjunction)_content;
                if(c.getTemporalOrder()==TemporalRules.ORDER_FORWARD) {
                    if(c.term[c.term.length-1] instanceof Interval) {
                        long time=0; 
                        //refined:
                        int u = 0;
                        while(c.term.length-1-u >= 0 && c.term[c.term.length-1-u] instanceof Interval) {
                            time += ((Interval)c.term[c.term.length-1-u]).time;
                            u++;
                        }
                        
                        final Term[] term2=new Term[c.term.length-u];
                        System.arraycopy(c.term, 0, term2, 0, term2.length);
                        _content=(T) Conjunction.make(term2, c.getTemporalOrder(), c.isSpatial);
                        //ok we removed a part of the interval, we have to transform the occurence time of the sentence back
                        //accordingly
                        
                        if(!c.isSpatial && stamp!=null && stamp.getOccurrenceTime() != Stamp.ETERNAL)
                            stamp.setOccurrenceTime(stamp.getOccurrenceTime()-time);
                    }
                    if(c.term[0] instanceof Interval) {
                        long time=0; 
                        //refined:
                        int u = 0;
                        while(u < c.term.length && (c.term[u] instanceof Interval)) {
                            time += ((Interval)c.term[u]).time;
                            u++;
                        }
                        
                        final Term[] term2=new Term[c.term.length-u];
                        System.arraycopy(c.term, u, term2, 0, term2.length);
                        _content=(T) Conjunction.make(term2, c.getTemporalOrder(), c.isSpatial);
                        //ok we removed a part of the interval, we have to transform the occurence time of the sentence back
                        //accordingly
                        
                        if(!c.isSpatial && stamp!=null && stamp.getOccurrenceTime() != Stamp.ETERNAL)
                            stamp.setOccurrenceTime(stamp.getOccurrenceTime()+time);
                    }
                }
            }
        }
        
        this.punctuation = punctuation;

        if( truth != null ) {
            if (_content instanceof Implication || _content instanceof Equivalence) {
                if (((Statement) _content).getSubject().hasVarIndep() && !((Statement) _content).getPredicate().hasVarIndep())
                    truth.setConfidence(0.0f);
                if (((Statement) _content).getPredicate().hasVarIndep() && !((Statement) _content).getSubject().hasVarIndep())
                    truth.setConfidence(0.0f); //TODO:
            } else if (_content instanceof Interval && punctuation != Symbols.TERM_NORMALIZING_WORKAROUND_MARK) {
                truth.setConfidence(0.0f); //do it that way for now, because else further inference is interrupted.
                if (MiscFlags.DEBUG)
                    throw new IllegalStateException("Sentence content must not be Interval: " + _content + punctuation + " " + stamp);
            }

            if ((!isQuestion() && !isQuest()) && (truth == null) && punctuation != Symbols.TERM_NORMALIZING_WORKAROUND_MARK) {
                throw new IllegalStateException("Judgment and Goal sentences require non-null truth value");
            }

            if (_content.subjectOrPredicateIsIndependentVar() && punctuation != Symbols.TERM_NORMALIZING_WORKAROUND_MARK) {
                truth.setConfidence(0.0f); //do it that way for now, because else further inference is interrupted.
                if (MiscFlags.DEBUG)
                    throw new IllegalStateException("A statement sentence is not allowed to have a independent variable as subj or pred");
            }

            if (MiscFlags.DEBUG && MiscFlags.DEBUG_INVALID_SENTENCES && punctuation != Symbols.TERM_NORMALIZING_WORKAROUND_MARK) {
                if (!Term.valid(_content)) {
                    truth.setConfidence(0.0f);
                    if (MiscFlags.DEBUG) {
                        System.err.println("Invalid Sentence term: " + _content);
                        Thread.dumpStack();
                    }
                }
            }
        }
        
        if ((isQuestion() || isQuest()) && punctuation!=Symbols.TERM_NORMALIZING_WORKAROUND_MARK && !stamp.isEternal()) {
            stamp.setEternal();
            //throw new IllegalStateException("Questions and Quests require eternal tense");
        }
        
        this.truth = truth;
        this.stamp = stamp;
        this.revisible = _content instanceof Implication || _content instanceof Equivalence || !(_content.hasVarDep());

        T newTerm = null;
        if( _content instanceof CompoundTerm)
            newTerm = (T)((CompoundTerm)_content).cloneDeepVariables();
        
        //Variable name normalization
        //TODO move this to Concept method, like cloneNormalized()
        if ( newTerm != null && normalize && _content.hasVar() && (!((CompoundTerm)_content).isNormalized() ) ) {
            
            this.term = newTerm;
            final CompoundTerm c = (CompoundTerm)term;
            final List<Variable> vars = new ArrayList(); //may contain duplicates, list for efficiency

            c.recurseSubtermsContainingVariables((t, parent) -> {
                if (t instanceof Variable) {
                    final Variable v = ((Variable) t);
                    vars.add(v);
                }
            });

            final Map<CharSequence, CharSequence> rename = new HashMap();
            boolean renamed = false;

            for (final Variable v : vars) {
                CharSequence vname = v.name();
                if (!v.hasVarIndep())
                    vname = vname + " " + v.getScope().name();
                CharSequence n = rename.get(vname);
                if (n == null) {
                    //type + id
                    rename.put(vname, n = Variable.getName(v.getType(), rename.size() + 1));
                    if (!n.equals(vname))
                        renamed = true;
                }

                v.setScope(c, n);
            }

            if (renamed) {
                c.invalidateName();

                if (MiscFlags.DEBUG && MiscFlags.DEBUG_INVALID_SENTENCES) {
                    if (!Term.valid(c)) {
                        final CompoundTerm.UnableToCloneException ntc = new CompoundTerm.UnableToCloneException("Invalid term discovered after normalization: " + c + " ; prior to normalization: " + _content);
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
    
        if (isNotTermlinkNormalizer())
            this.hash = Objects.hash(term, punctuation, truth, stamp.getOccurrenceTime());
        else 
            this.hash = Objects.hash(term, punctuation, truth );
    }

    protected boolean isNotTermlinkNormalizer() {
        return punctuation != Symbols.TERM_NORMALIZING_WORKAROUND_MARK;
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
            if (isNotTermlinkNormalizer()) {
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
            
            if(term.term_indices != null && t.term.term_indices != null) {
                for(int i=0;i<term.term_indices.length;i++) {
                    if(term.term_indices[i] != t.term.term_indices[i]) {
                        return false; //position or scale was different
                    }
                }
            }

            return stamp.equals(t.stamp, false, true, true);
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
     * Clone the Sentence
     *
     * @return The clone
     */
    @Override
    public Sentence clone() {
        return clone(term);
    }

    public Sentence clone(final boolean makeEternal) {
        final Sentence clon = clone(term);
        if(clon.stamp.getOccurrenceTime()!=Stamp.ETERNAL && makeEternal) {
            //change occurence time of clone
            clon.stamp.setEternal();
        }
        return clon;
    }

    /** Clone with a different Term */    
    public final Sentence clone(final Term t) {
        return new Sentence(
            t,
            punctuation,
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
    public Sentence projection(final long targetTime, final long currentTime, Memory mem) {
            
        final TruthValue newTruth = projectionTruth(targetTime, currentTime, mem);
        final boolean eternalizing = (newTruth instanceof EternalizedTruthValue);
                
        final Stamp newStamp = eternalizing ? stamp.cloneWithNewOccurrenceTime(Stamp.ETERNAL) :
                                        stamp.cloneWithNewOccurrenceTime(targetTime);
        
        return new Sentence(
            term,
            punctuation,
            newTruth,
            newStamp,
            false);
    }

    
    public TruthValue projectionTruth(final long targetTime, final long currentTime, Memory mem) {
        TruthValue newTruth = null;
                        
        if (!stamp.isEternal()) {
            newTruth = TruthFunctions.eternalize(truth, mem.narParameters);
            if (targetTime != Stamp.ETERNAL) {
                final long occurrenceTime = stamp.getOccurrenceTime();
                final float factor = TruthFunctions.temporalProjection(occurrenceTime, targetTime, currentTime, mem.narParameters);
                final float projectedConfidence = factor * truth.getConfidence();
                if (projectedConfidence > newTruth.getConfidence()) {
                    newTruth = new TruthValue(truth.getFrequency(), projectedConfidence, mem.narParameters);
                }
            }
        }
        
        if (newTruth == null) newTruth = truth.clone();
        
        return newTruth;
    }

    /**
     * Recognize a Judgment
     * @return Whether the object is a Judgment
     */
    public boolean isJudgment() {
        return (punctuation == Symbols.JUDGMENT_MARK);
    }
    
    /**
     * Recognize a Question
     * @return Whether the object is a Question
     */
    public boolean isQuestion() {
        return (punctuation == Symbols.QUESTION_MARK);
    }
    
    /**
     * Recognize a Goal
     * @return Whether the object is a Goal
     */
    public boolean isGoal() {
        return (punctuation == Symbols.GOAL_MARK);
    }
    
    /**
     * Recognize a Quest
     * @return Whether the object is a Quest
     */
    public boolean isQuest() {
        return (punctuation == Symbols.QUEST_MARK);
    }    

    /**
     * Revisible?
     */
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

            String conv = "";
            if(term.term_indices != null) {
                conv = " [i,j,k,l]=[";
                for(int i = 0; i<4; i++) { //skip min sizes
                    conv += String.valueOf(term.term_indices[i])+",";
                }
                conv = conv.substring(0, conv.length()-1) + "]";
            }
            
            //suffix = [punctuation][ ][truthString][ ][occurenceTimeString]
            final StringBuilder suffix = new StringBuilder(stringLength).append(punctuation).append(conv);

            if (truth != null) {
                suffix.append(' ');
                truth.appendString(suffix, false);
            }
            if ((showOcurrenceTime) && (stamp!=null)) {
                suffix.append(' ');
                stamp.appendOcurrenceTime(suffix);
            }

            key = Texts.yarn( 
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
    public CharSequence toString(final Nar nar, final boolean showStamp) {
    
        final CharSequence contentName = term.name();
        
        final long t = nar.time();

        final long diff=stamp.getOccurrenceTime()-nar.time();
        final long diffabs = Math.abs(diff);
        
        String timediff = "";
        if(diffabs < nar.narParameters.DURATION) {
            timediff = "|";
        }
        else {
            final Long Int = diffabs;
            timediff = diff>0 ? "+"+String.valueOf(Int) : "-"+String.valueOf(Int);
        }
        
        if(MiscFlags.TEST_RUNNING) {
            timediff = "!"+String.valueOf(stamp.getOccurrenceTime());
        }
        
        String tenseString = ":"+timediff+":"; //stamp.getTense(t, nar.memory.getDuration());
        if(stamp.getOccurrenceTime() == Stamp.ETERNAL)
            tenseString="";
        
        final CharSequence stampString = showStamp ? stamp.name() : null;
        
        int stringLength = contentName.length() + tenseString.length() + 1 + 1;
                
        if (truth != null)
            stringLength += 11;
        
        if (showStamp)
            stringLength += stampString.length()+1;
        
        String conv = "";
        if(term.term_indices != null) {
            conv = " [i,j,k,l]=[";
            for(int i = 0; i<4; i++) { //skip min sizes
                conv += String.valueOf(term.term_indices[i])+",";
            }
            conv = conv.substring(0, conv.length()-1) + "]";
        }
        
        final StringBuilder buffer = new StringBuilder(stringLength).
                    append(contentName).append(punctuation).append(conv);
        
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
    public void discountConfidence(Parameters narParameters) {
        truth.setConfidence(truth.getConfidence() * narParameters.DISCOUNT_RATE).setAnalytic(false);
    }

    public boolean isEternal() {
        return stamp.isEternal();
    }
    
    public T getTerm() {
        return term;
    }

    public TruthValue getTruth() {
        return truth;
    }
}
