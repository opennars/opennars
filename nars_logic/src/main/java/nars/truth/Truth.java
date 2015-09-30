/*
 * TruthValue.java
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
package nars.truth;

import nars.Global;
import nars.Symbols;
import nars.io.Texts;
import nars.task.Sentence;
import nars.task.stamp.Stamp;
import nars.term.Atom;
import nars.term.Term;
import nars.util.data.Util;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/** scalar (1D) truth value "frequency", stored as a floating point value */
public interface Truth extends MetaTruth<Float> {



    Term Truth_TRUE = Atom.the("TRUE");
    Term Truth_FALSE = Atom.the("FALSE");
    Term Truth_UNSURE = Atom.the("UNSURE");


    /**
     * Get the frequency value
     *
     * @return The frequency value
     */
    float getFrequency();

    Truth setFrequency(float f);




    /**
     * Get the isAnalytic flag
     *
     * @return The isAnalytic value
     */
    boolean isAnalytic();





    /**
     * Calculate the expectation value of the truth value
     *
     * @return The expectation value
     */
    default float getExpectation() {
        return getExpectationPositive();
    }

    /** expectation inverse, ie. the expectation of freq=1 */
    default float getExpectationPositive() {
        return expectation(getFrequency(), getConfidence());
    }


    /** expectation inverse, ie. the expectation of freq=0  */
    default float getExpectationNegative() {
        return expectation(1f-getFrequency(), getConfidence());
    }

    static float expectation(final float frequency, final float confidence) {
        return (confidence * (frequency - 0.5f) + 0.5f);
    }

    /**
     * Calculate the absolute difference of the expectation value and that of a
     * given truth value
     *
     * @param t The given value
     * @return The absolute difference
     */
    default float getExpDifAbs(final Truth t) {
        return Math.abs(getExpectation() - t.getExpectation());
    }

    /**
     * Check if the truth value is negative
     *
     * @return True if the frequence is less than 1/2
     */
    default boolean isNegative() {
        return getFrequency() < 0.5;
    }


    /**
     * The hash code of a TruthValue
     * @return The hash code
     */
    static int hash(final Truth t) {
        final int discreteness = (int)(1.0f / DefaultTruth.DEFAULT_TRUTH_EPSILON);

        //assuming discreteness is below 2^15:
        final int freqHash = Util.hash(t.getFrequency(), discreteness);
        final int confHash = Util.hash(t.getConfidence(), discreteness);

        final int hash = freqHash | (confHash << 16) ;

        return hash * Util.PRIME2;
//        return (1 + ) * Util.PRIME2 +
//                     Util.hash(t.getConfidence(), discreteness);

//        return Float.floatToRawIntBits(t.getFrequency()) +
//         31 * Float.floatToRawIntBits(t.getConfidence());
    }

    //    @Override
//    default public Truth clone() {
//        return new DefaultTruth(getFrequency(), getConfidence(), getAnalytic());
//    }




    @Override
    default StringBuilder appendString(final StringBuilder sb) {
        return appendString(sb, 2);
    }


    /**
     * A simplified String representation of a TruthValue, where each factor is
     * accruate to 1%
     */
    default StringBuilder appendString(final StringBuilder sb, final int decimals) {
        /*String s1 = DELIMITER + frequency.toStringBrief() + SEPARATOR;
        String s2 = confidence.toStringBrief();
        if (s2.equals("1.00")) {
            return s1 + "0.99" + DELIMITER;
        } else {
            return s1 + s2 + DELIMITER;
        }*/
        
        sb.ensureCapacity(3 + 2 * (2 + decimals) );
        return sb
            .append(Symbols.TRUTH_VALUE_MARK)
            .append(Texts.n(getFrequency(), decimals))
            .append(Symbols.VALUE_SEPARATOR)
            .append(Texts.n(getConfidence(), decimals))
            .append(Symbols.TRUTH_VALUE_MARK);
    }




    
    /** displays the truth value as a short string indicating degree of true/false */
    default String toTrueFalseString() {
        //TODO:
        //  F,f,~,t,T
        return null;
    }

    /** displays the truth value as a short string indicating degree of yes/no */
    default String toYesNoString() {
        //TODO
        // N,n,~,y,Y
        return null;
    }

    
    default Term toWordTerm(float trueExpectationThreshold) {
        float e = getExpectation();
        if (e > trueExpectationThreshold) {
            return Truth_TRUE;
        }
        if (e < 1 - trueExpectationThreshold) {
            return Truth_FALSE;
        }
        return Truth_UNSURE;
    }

    default Truth set(final float frequency, final float confidence) {
        setFrequency(frequency);
        setConfidence(confidence);
        return this;
    }

    /** negation that modifies the truth instance itself */
    default Truth negate() {
        //final float f = 1 - getFrequency();
        return setFrequency(1f - getFrequency());
    }

    default float projectionQuality(Sentence s, long targetTime, long currentTime, boolean problemHasQueryVar) {
        float freq = getFrequency();
        float conf = getConfidence();

        if (!s.isEternal() && (targetTime != s.getOccurrenceTime())) {
            conf = TruthFunctions.eternalizedConfidence(conf);
            if (targetTime != Stamp.ETERNAL) {
                long occurrenceTime = s.getOccurrenceTime();
                float factor = TruthFunctions.temporalProjection(occurrenceTime, targetTime, currentTime);
                float projectedConfidence = factor * s.getConfidence();
                if (projectedConfidence > conf) {
                    conf = projectedConfidence;
                }
            }
        }

        if (problemHasQueryVar) {
            return Truth.expectation(freq, conf) / s.getTerm().complexity();
        } else {
            return conf;
        }

    }



    enum TruthComponent {
        Frequency, Confidence, Expectation
    }
    
    default float getComponent(TruthComponent c) {
        switch (c) {
            case Frequency: return getFrequency();
            case Confidence: return getConfidence();
            case Expectation: return getExpectation();                
        }
        return Float.NaN;
    }
    
    /** provides a statistics summary (mean, min, max, variance, etc..) of a particular TruthValue component across a given list of Truthables (sentences, TruthValue's, etc..).  null values in the iteration are ignored */
    static DescriptiveStatistics statistics(Iterable<? extends Truthed> t, TruthComponent component) {
        DescriptiveStatistics d = new DescriptiveStatistics();
        for (Truthed x : t) {
            Truth v = x.getTruth();
            if (v!=null)
                d.addValue(v.getComponent(component));
        }
        return d;
    }

    /**
     * Get the truth value (or desire value) of the sentence
     *
     * Should only be used in Concept's sentences, not in other location where Sentence is expected to be immutable
     *
     * @return Truth value, null for question
     */
    default AnalyticTruth discountConfidence() {
        return new AnalyticTruth(getFrequency(), getConfidence() * Global.DISCOUNT_RATE);
    }


    /** use getFrequency() when possible because this may box the result as a non-primitive */
    @Override
    default Float value() { return getFrequency(); }


    /** use setFrequency(v) when possible because this may box the result as a non-primitive */
    @Override
    default void setValue(Float v) { setFrequency(v); }

}
