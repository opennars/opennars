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
package nars.nal;

import nars.Global;
import nars.Symbols;
import nars.io.Texts;
import nars.nal.term.Atom;
import nars.nal.term.Term;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.util.FastMath;

import java.io.Serializable;


abstract public interface Truth extends Cloneable, Serializable { // implements Cloneable {



    final static Term Truth_TRUE = Atom.get("TRUE");
    final static Term Truth_FALSE = Atom.get("FALSE");
    final static Term Truth_UNSURE = Atom.get("UNSURE");

    public static class DefaultTruth implements Truth {


        /** determines the internal precision used for TruthValue calculations.
         *  a value of 0.01 gives 100 truth value states between 0 and 1.0.
         *  other values may be used, for example, 0.02 for 50, 0.10 for 10, etc.
         *  Change at your own risk
         */
        //public static boolean TASK_LINK_UNIQUE_BY_INDEX = false;


        /**
         * The frequency factor of the truth value
         */
        private float frequency;
        /**
         * The confidence factor of the truth value
         */
        private float confidence;

        /**
         * Whether the truth value is derived from a definition
         */
        private boolean analytic = false;
        final float epsilon;


        public DefaultTruth() {
            this(0,0);
        }



        /**
         * Constructor with two ShortFloats
         *
         * @param f The frequency value
         * @param c The confidence value
         */
        public DefaultTruth(final float f, final float c) {
            this(f, c, false);
        }

        public DefaultTruth(final float f, final float c, final boolean b, float epsilon) {
            this.epsilon = epsilon;
            setFrequency(f);
            setConfidence(c);
            setAnalytic(b);
        }
        /**
         * Constructor with two ShortFloats
         *
         * @param f The frequency value
         * @param c The confidence value
         *
         */
        public DefaultTruth(final float f, final float c, final boolean b) {
            this(f, c, b, Global.TRUTH_EPSILON);
        }

        public DefaultTruth(final float f, final float c, float epsilon) {
            this(f, c, false, epsilon);
        }

        /**
         * Constructor with a TruthValue to clone
         *
         * @param v The truth value to be cloned
         */
        public DefaultTruth(final Truth v) {
            this(v.getFrequency(), v.getConfidence(), v.getAnalytic(), Global.TRUTH_EPSILON);
        }

        public DefaultTruth(final DefaultTruth v) {
            this(v.getFrequency(), v.getConfidence(), v.getAnalytic(), v.getEpsilon());
        }

        public DefaultTruth(char punctuation) {
            this.epsilon = Global.TRUTH_EPSILON;
            float c;
            switch(punctuation) {
                case Symbols.JUDGMENT: c = Global.DEFAULT_JUDGMENT_CONFIDENCE;  break;
                case Symbols.GOAL: c = Global.DEFAULT_GOAL_CONFIDENCE;  break;
                default:
                    throw new RuntimeException("Invalid punctuation " + punctuation + " for a TruthValue");
            }
            float f = 1;
            setFrequency(f);
            setConfidence(c);
        }

        public float getFrequency() {
            return frequency;
        }

        @Override
        public void setAnalytic() {
            this.analytic = true;
        }

        @Override
        public boolean getAnalytic() {
            return analytic;
        }

        public Truth setFrequency(float f) {
            if (f > 1.0f) f = 1.0f;
            if (f < 0f) f = 0f;
            //if ((f > 1.0f) || (f < 0f)) throw new RuntimeException("Invalid frequency: " + f); //f = 0f;

            final float e = getEpsilon();
            this.frequency = FastMath.round(f / e) * e;
            return this;
        }

        public float getEpsilon() {
            return epsilon;
        }

        @Override
        public float getConfidence() {
            return confidence;
        }


        public Truth setConfidence(float c) {
            //if ((c > 1.0f) || (c < 0f)) throw new RuntimeException("Invalid confidence: " + c);
            final float maxConf = getConfidenceMax();
            if (c > maxConf)  c = maxConf;
            if (c < 0) c = 0;

            final float e = getEpsilon();
            this.confidence = Math.round( c / e) * e;
            return this;
        }

        @Override public float getConfidenceMax() {
            return Global.MAX_CONFIDENCE;
        }

        @Override
        public Truth setAnalytic(final boolean a) {
            analytic = a;
            return this;
        }

        /**
         * Compare two truth values
         *
         * @param that The other TruthValue
         * @return Whether the two are equivalent
         */

        @Override
        public boolean equals(final Object that) {
            if (that == this) return true;
            if (that instanceof Truth) {
                final Truth t = ((Truth) that);

                final float e = getEpsilon();

                if (!isEqual(getFrequency(), t.getFrequency(), e))
                    return false;
                if (!isEqual(getConfidence(), t.getConfidence(), e))
                    return false;
                return true;
            }
            return false;
        }

        /**
         * The String representation of a TruthValue, as used internally by the system
         *
         * @return The String
         */
        @Override
        public String toString() {
            //return DELIMITER + frequency.toString() + SEPARATOR + confidence.toString() + DELIMITER;

            //1 + 6 + 1 + 6 + 1
            return name().toString();
        }
    }



    /**
     * Get the frequency value
     *
     * @return The frequency value
     */
    public float getFrequency();

    public Truth setFrequency(float f);

    /**
     * Get the confidence value
     *
     * @return The confidence value
     */
    public float getConfidence();


    /** max confidence value */
    public float getConfidenceMax();


    public Truth setConfidence(float c);


    /**
     * Get the isAnalytic flag
     *
     * @return The isAnalytic value
     */
    public boolean getAnalytic();

    /**
     * Set the isAnalytic flag
     */
    public void setAnalytic();

    /**
     * Calculate the expectation value of the truth value
     *
     * @return The expectation value
     */
    default public float getExpectation() {
        return expectation(getFrequency(), getConfidence());
    }

    public static float expectation(final float frequency, final float confidence) {
        return (confidence * (frequency - 0.5f) + 0.5f);
    }

    /**
     * Calculate the absolute difference of the expectation value and that of a
     * given truth value
     *
     * @param t The given value
     * @return The absolute difference
     */
    default public float getExpDifAbs(final Truth t) {
        return FastMath.abs(getExpectation() - t.getExpectation());
    }

    /**
     * Check if the truth value is negative
     *
     * @return True if the frequence is less than 1/2
     */
    default public boolean isNegative() {
        return getFrequency() < 0.5;
    }

    /** tests equivalence (according to epsilon precision) */
    public static boolean isEqual(final float a, final float b, final float epsilon) {
        final float he = epsilon/2f;
        if (a > b) return ((a - b) < he);
        else if ( a < b) return ((b - a) < he);
        return true;
    }
    


    /**
     * The hash code of a TruthValue
     * @return The hash code
     */
    static public int hash(Truth t) {
        int h = 0;
        h += Float.floatToRawIntBits(t.getFrequency());
        h += 31 * Float.floatToRawIntBits(t.getConfidence());
        return h;
    }

//    @Override
//    default public Truth clone() {
//        return new DefaultTruth(getFrequency(), getConfidence(), getAnalytic());
//    }



    public Truth setAnalytic(final boolean a);




    /**
     * A simplified String representation of a TruthValue, where each factor is
     * accruate to 1%
     */
    default public StringBuilder appendString(final StringBuilder sb, final boolean external) {
        /*String s1 = DELIMITER + frequency.toStringBrief() + SEPARATOR;
        String s2 = confidence.toStringBrief();
        if (s2.equals("1.00")) {
            return s1 + "0.99" + DELIMITER;
        } else {
            return s1 + s2 + DELIMITER;
        }*/
        
        sb.ensureCapacity(11);
        return sb
            .append(Symbols.TRUTH_VALUE_MARK)
            .append(Texts.n2(getFrequency()))
            .append(Symbols.VALUE_SEPARATOR)
            .append(Texts.n2(getConfidence()))
            .append(Symbols.TRUTH_VALUE_MARK);
    }

    default public String toStringExternal1() {
        return new StringBuilder(5)
                .append(Symbols.TRUTH_VALUE_MARK)
                .append(Texts.n1(getFrequency()))
                .append(Symbols.VALUE_SEPARATOR)
                .append(Texts.n1(getConfidence()))
                .append(Symbols.TRUTH_VALUE_MARK).toString();
    }


    default public CharSequence name() {
        //1 + 4 + 1 + 4 + 1
        StringBuilder sb =  new StringBuilder();
        return appendString(sb, false);
    }

    /** output representation */
    default public CharSequence toStringExternal() {
        //return name().toString();
        StringBuilder sb =  new StringBuilder();
        return appendString(sb, true);
    }

    
    
    /** displays the truth value as a short string indicating degree of true/false */
    default public String toTrueFalseString() {
        //TODO:
        //  F,f,~,t,T
        return null;
    }

    /** displays the truth value as a short string indicating degree of yes/no */
    default public String toYesNoString() {
        //TODO
        // N,n,~,y,Y
        return null;
    }

    
    default public Term toWordTerm() {
        float e = getExpectation();
        float t = Global.DEFAULT_CREATION_EXPECTATION;
        if (e > t) {
            return Truth_TRUE;
        }
        if (e < 1 - t) {
            return Truth_FALSE;
        }
        return Truth_UNSURE;
    }

    default public Truth set(float frequency, float confidence) {
        setFrequency(frequency);
        setConfidence(confidence);
        return this;
    }


    /** indicates an implementation has, or is associated with a specific TruthValue */
    public interface Truthable {
        public Truth getTruth();
    }
    
    public enum TruthComponent {
        Frequency, Confidence, Expectation
    }
    
    default public float getComponent(TruthComponent c) {
        switch (c) {
            case Frequency: return getFrequency();
            case Confidence: return getConfidence();
            case Expectation: return getExpectation();                
        }
        return Float.NaN;
    }
    
    /** provides a statistics summary (mean, min, max, variance, etc..) of a particular TruthValue component across a given list of Truthables (sentences, TruthValue's, etc..).  null values in the iteration are ignored */
    public static DescriptiveStatistics statistics(Iterable<? extends Truthable> t, TruthComponent component) {
        DescriptiveStatistics d = new DescriptiveStatistics();
        for (Truthable x : t) {            
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
     * TODO make a distinct superclass between Mutable & ImmutableSentence for the different places Sentence are used (ex: Task vs. Concept)
     *
     * @return Truth value, null for question
     */
    default public void discountConfidence() {
        setConfidence(getConfidence() * Global.DISCOUNT_RATE).setAnalytic(false);
    }
}
