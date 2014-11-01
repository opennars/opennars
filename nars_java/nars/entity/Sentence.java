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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import nars.core.Memory;
import nars.core.NAR;
import nars.core.Parameters;
import nars.inference.TruthFunctions;
import nars.io.Symbols;
import nars.io.Texts;
import nars.language.CompoundTerm;
import nars.language.Conjunction;
import nars.language.Inheritance;
import nars.language.Product;
import nars.language.Statement;
import nars.language.Term;
import nars.language.Variable;
import nars.operator.Operation;
import nars.operator.Operator;

/**
 * A Sentence is an abstract class, mainly containing a Term, a TruthValue, and
 * a Stamp.
 * <p>
 * It is used as the premises and conclusions of all inference rules.
 */
public class Sentence<T extends Term> implements Cloneable {

    /**
     * The content of a Sentence is a Term
     */
    public final T content;
    
    /**
     * The punctuation also indicates the type of the Sentence: 
     * Judgment, Question, Goal, or Quest.
     * Represented by characters: '.', '?', '!', or '@'
     */
    public final char punctuation;
    
    /**
     * The truth value of Judgment, or desire value of Goal     
     */
    public TruthValue truth;
    
    /**
     * Partial record of the derivation path
     */
    public final Stamp stamp;

    /**
     * Whether the sentence can be revised
     */
    private boolean revisible;

    /** caches the 'getKey()' result */
    private CharSequence key;

    private final int hash;
    
    /**
     * Create a Sentence with the given fields
     *
     * @param content The Term that forms the content of the sentence
     * @param punctuation The punctuation indicating the type of the sentence
     * @param truth The truth value of the sentence, null for question
     * @param stamp The stamp of the sentence indicating its derivation time and
     * base
     */
    public Sentence(final T _content, final char punctuation, final TruthValue truth, final Stamp stamp) {
        
        this.punctuation = punctuation;
        this.truth = truth;
        this.stamp = stamp;
        this.revisible = !((_content instanceof Conjunction) && _content.hasVarDep());
            
        if (_content.hasVar() && (_content instanceof CompoundTerm)) {
            this.content = (T)((CompoundTerm)_content).cloneDeepVariables();
            if (this.content == null) {
                ((CompoundTerm)_content).cloneDeepVariables();
                throw new RuntimeException("clone deep should never return null: " + _content);
            }
            final CompoundTerm c = (CompoundTerm)content;
            
            List<Variable> vars = new ArrayList(); //may contain duplicates, list for efficiency
            Map<String,String> rename = new HashMap();
            
            
            c.recurseTerms(new Term.TermVisitor() {
                
                @Override public void visit(final Term t) {
                    if (t instanceof Variable) {                        
                        Variable v = ((Variable)t);
                        
                        if (!v.getScope().equals(v)) {
                            //reserve the name of the already scoped variable
                            //rename.put(v.name().toString(), v.name().toString());        
                            
                            //rescope cloned copy
                            v.setScope(c, v.name());
                        }
                                     
                        vars.add(v);
                    }
                }            
            });
            for (Variable v : vars) {
                //check if scope already applied (ie. if there was a duplicate variable since
                //it used a list not a set
                //if ((!v.getScope().equals(v))) continue;
                
                String n;
                String vname = v.name().toString();
                
                n = rename.get(v.name().toString());
                /*if ((n!=null) && (n.equals(v.name()))) {
                    //rename this one to avoid overwriting a previously scoped variable
                    vname = v.name() + "_";
                    n = null;
                }*/
                if (n==null) {                            
                    //type + id

                    n = String.valueOf(v.getType()) + (1+rename.size());
                    
                    /*n = CharBuffer.allocate(4).append(v.getType()).
                            append(String.valueOf( rename.size() + 1)).compact();*/

                    rename.put(vname, n);
                }    

                v.setScope(c, n);                
            }
                                   
            ((CompoundTerm)content).invalidateName();
            
        }
        else {
            this.content = _content;
        }
        
    
        if (isUniqueByOcurrenceTime())
            this.hash = Objects.hash( content, punctuation, truth, stamp.getOccurrenceTime());
        else 
            this.hash = Objects.hash( content, punctuation, truth );
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
            
            if (!content.equals(t.content)) return false;
                    
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
        //assert content.equals(content) && punctuation == that.punctuation;
        return (truth.equals(that.truth) && stamp.equals(that.stamp));
    }

    /**
     * Clone the Sentence
     *
     * @return The clone
     */
    @Override
    public Sentence clone() {
        return clone(content);
    }
    
    
    public Sentence clone(boolean makeEternal) {
        Sentence clon = clone(content);
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
        
        TruthValue newTruth = new TruthValue(truth);
        
        boolean eternalizing = false;
        
        if (stamp.getOccurrenceTime() != Stamp.ETERNAL) {
            newTruth = TruthFunctions.eternalization(truth);
            eternalizing = true;
            if (targetTime != Stamp.ETERNAL) {
                long occurrenceTime = stamp.getOccurrenceTime();
                float factor = TruthFunctions.temporalProjection(occurrenceTime, targetTime, currentTime);
                float projectedConfidence = factor * truth.getConfidence();
                if (projectedConfidence > newTruth.getConfidence()) {
                    newTruth = new TruthValue(truth.getFrequency(), projectedConfidence);
                    eternalizing = false;
                }
            }
        }
        
        
        Stamp newStamp = eternalizing ? stamp.cloneWithNewOccurrenceTime(Stamp.ETERNAL) : stamp.clone();
        
        return new Sentence(content, punctuation, newTruth, newStamp);
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
        return content.hasVarQuery();
    }

    public boolean getRevisible() {
        return revisible;
    }

    public void setRevisible(final boolean b) {
        revisible = b;
    }

    public int getTemporalOrder() {
        return content.getTemporalOrder();
    }
    
    public long getOccurenceTime() {
        return stamp.getOccurrenceTime();
    }
    
    public Operator getOperator() {
        if (content instanceof Operation) {
             return (Operator) ((Statement) content).getPredicate();
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
            final CharSequence contentName = content.name();
            
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
    
        CharSequence contentName = content.name();
        
        final long t = nar.memory.time();

        final String tenseString = ((punctuation == Symbols.JUDGMENT_MARK) || (punctuation == Symbols.QUESTION_MARK)) ? stamp.getTense(t, nar.memory.param.duration.get()) : "";
        final CharSequence truthString = (truth != null) ? truth.toStringExternal() : null;
 
        CharSequence stampString = showStamp ? stamp.name() : null;
        
        int stringLength = contentName.length() + tenseString.length() + 1 + 1;
                
        if (truth != null)
            stringLength += truthString.length()+1;
        
        if (showStamp)
            stringLength += stampString.length()+1;
        
        
        final StringBuilder buffer = new StringBuilder(stringLength).
                    append(contentName).append(punctuation);
        
        if (tenseString.length() > 0)
            buffer.append(' ').append(tenseString);
        
        if (truth != null)
            buffer.append(' ').append(truthString);
        
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

    public Term toTerm(final Memory mem) {
        String opName;
        switch (punctuation) {
            case Symbols.JUDGMENT_MARK:
                opName = "^believe";
                break;
            case Symbols.GOAL_MARK:
                opName = "^want";
                break;
            case Symbols.QUESTION_MARK:
                opName = "^wonder";
                break;
            case Symbols.QUEST_MARK:
                opName = "^assess";
                break;
            default:
                return null;
        }
        Term opTerm = mem.getOperator(opName);
        int size=(truth==null ? 1 : 2);
        Term[] arg = new Term[size];
        arg[0]=content;
        if (truth != null) {
            String word = truth.toWord();
            arg[1]=new Term(word);
        }
        
        Term operation = Inheritance.make(new Product(arg), opTerm);
        return operation;
    }

    final public boolean equalsContent(final Sentence s2) {
        return content.equals(s2.content);
    }

    public boolean isEternal() {
        return getOccurenceTime() == Stamp.ETERNAL;
    }

}
