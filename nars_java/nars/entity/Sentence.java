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

import nars.core.Parameters;
import static nars.entity.Stamp.ETERNAL;
import nars.io.Symbols;
import nars.language.*;
import nars.inference.TruthFunctions;

/**
 * A Sentence is an abstract class, mainly containing a Term, a TruthValue, and
 * a Stamp.
 * <p>
 * It is used as the premises and conclusions of all inference rules.
 */
public class Sentence implements Cloneable {

    /**
     * The content of a Sentence is a Term
     */
    private Term content;
    /**
     * The punctuation also indicates the type of the Sentence: Judgment,
     * Question, or Goal
     */
    final public char punctuation;
    /**
     * The truth value of Judgment
     */
    final public TruthValue truth;
    /**
     * Partial record of the derivation path
     */
    protected Stamp stamp;

    /**
     * Whether the sentence can be revised
     */
    private boolean revisible;

    //caches the 'getKey()' result
    private String key;

    /**
     * Create a Sentence with the given fields
     *
     * @param content The Term that forms the content of the sentence
     * @param punctuation The punctuation indicating the type of the sentence
     * @param truth The truth value of the sentence, null for question
     * @param stamp The stamp of the sentence indicating its derivation time and
     * base
     */
    public Sentence(final Term content, final char punctuation, final TruthValue truth, final Stamp stamp) {
        this.content = content;
        this.content.renameVariables();
        this.punctuation = punctuation;
        this.truth = truth;
        this.stamp = stamp;
        this.revisible = !((content instanceof Conjunction) && Variable.containVarDep(content.getName()));
    }

    /**
     * To check whether two sentences are equal
     *
     * @param that The other sentence
     * @return Whether the two sentences have the same content
     */
    @Override
    public boolean equals(final Object that) {
        if (that instanceof Sentence) {
            final Sentence t = (Sentence) that;
            return content.equals(t.getContent()) && punctuation == t.getPunctuation() && truth.equals(t.getTruth()) && stamp.equals(t.getStamp());
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
        int hash = 5;
        hash = 67 * hash + (this.content != null ? this.content.hashCode() : 0);
        hash = 67 * hash + this.punctuation;
        hash = 67 * hash + (this.truth != null ? this.truth.hashCode() : 0);
        hash = 67 * hash + (this.stamp != null ? this.stamp.hashCode() : 0);
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
        assert content.equals(that.getContent()) && punctuation == that.getPunctuation();
        return (truth.equals(that.getTruth()) && stamp.equals(that.getStamp()));
    }

    /**
     * Clone the Sentence
     *
     * @return The clone
     */
    @Override
    public Object clone() {
        if (truth == null) {
            return new Sentence((Term) content.clone(), punctuation, null, (Stamp) stamp.clone());
        }
        return new Sentence((Term) content.clone(), punctuation, new TruthValue(truth), (Stamp) stamp.clone());
    }

    public Sentence projection(long targetTime, long currentTime) {
        TruthValue newTruth = new TruthValue(truth);
        Stamp newStamp = (Stamp) stamp.clone();
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
        if (eternalizing) {
            newStamp.setOccurrenceTime(Stamp.ETERNAL);
        }
        Sentence newSentence = new Sentence((Term) content.clone(), punctuation, newTruth, newStamp);
        return newSentence;
    }

    /**
     * Get the content of the sentence
     *
     * @return The content Term
     */
    public Term getContent() {
        return content;
    }

    public void setContent(final Term t) {
        content = t;
        key = null;
    }

    /**
     * Get the punctuation of the sentence
     *
     * @return The character '.' or '?'
     */
    public char getPunctuation() {
        return punctuation;
    }

    /**
     * Clone the content of the sentence
     *
     * @return A clone of the content Term
     */
    public Term cloneContent() {
        return (Term) content.clone();
    }

    /**
     * Get the truth value of the sentence
     *
     * @return Truth value, null for question
     */
    public TruthValue getTruth() {
        return truth;
    }

    /**
     * Get the stamp of the sentence
     *
     * @return The stamp
     */
    public Stamp getStamp() {
        return stamp;
    }

    /**
     * Distinguish Judgment from Goal ("instanceof Judgment" doesn't work)
     *
     * @return Whether the object is a Judgment
     */
    public boolean isJudgment() {
        return (punctuation == Symbols.JUDGMENT_MARK);
    }

    /**
     * Distinguish Question from Quest ("instanceof Question" doesn't work)
     *
     * @return Whether the object is a Question
     */
    public boolean isQuestion() {
        return (punctuation == Symbols.QUESTION_MARK);
    }

    public boolean containQueryVar() {
        return (content.getName().indexOf(Symbols.VAR_QUERY) >= 0);
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

    /**
     * Get a String representation of the sentence
     *
     * @return The String
     */
    @Override
    public String toString() {
        return toStringBrief();
    }

    /**
     * Get a String representation of the sentence, with 2-digit accuracy
     *
     * @return The String
     */
    public String toStringBrief() {
        return toKey() + " " + stamp.toString();
    }

    public void setStamp(Stamp stamp) {
        this.stamp = stamp;
    }

    /**
     * Get a String representation of the sentence for key of Task and TaskLink
     *
     * @return The String
     */
    public String toKey() {
        //key must be invalidated if content or truth change
        if (key == null) {
            final String contentToString = content.toString();
            final String occurrenceTimeString = stamp.getOccurrenceTimeString();
            final String truthString = truth != null ? truth.toStringBrief() : null;
            //final String stampString = stamp.toString();

            int stringLength = contentToString.length() + 1 + 1/* + stampString.length()*/;
            if (truth != null) {
                stringLength += occurrenceTimeString.length() + truthString.length();
            }

            final StringBuilder k = new StringBuilder(stringLength).append(contentToString)
                    .append(punctuation);

            if (truth != null) {
                k.append(' ').append(truthString);
            }
            if (occurrenceTimeString.length() > 0) {
                k.append(' ').append(occurrenceTimeString);
            }

            key = k.toString();

        }
        return key;
    }

// need a separate display method, where the occurenceTime is converted to tense,
    // according to the current time
    /**
     * Get a String representation of the sentence for display purpose
     *
     * @param currentTime Current time on the internal clock
     * @return The String
     */
    public String display(long currentTime) {
        final String contentToString = content.toString();
        String tenseString = stamp.getTense(currentTime);
        final String truthString = truth != null ? truth.toStringBrief() : null;
        //final String stampString = stamp.toString();

        int stringLength = contentToString.length() + tenseString.length() + 1 + 1/* + stampString.length()*/;
        if (truth != null) {
            stringLength += truthString.length();
        }

        final StringBuilder buffer = new StringBuilder(stringLength).append(contentToString)
                .append(punctuation).append(" ").append(tenseString);
        if (truth != null) {
            buffer.append(" ").append(truthString);
        }
        return buffer.toString();
    }

}
