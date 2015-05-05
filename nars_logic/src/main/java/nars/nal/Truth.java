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

import static nars.Global.TRUTH_EPSILON;


public class Truth implements Cloneable { // implements Cloneable {


    final static Term Truth_TRUE = Atom.get("TRUE");
    final static Term Truth_FALSE = Atom.get("FALSE");
    final static Term Truth_UNSURE = Atom.get("UNSURE");

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

    public Truth() {
        this(0,0);
    }
    
    /**
     * Constructor with two ShortFloats
     *
     * @param f The frequency value
     * @param c The confidence value
     */
    public Truth(final float f, final float c) {
        this(f, c, false);
    }

    /**
     * Constructor with two ShortFloats
     *
     * @param f The frequency value
     * @param c The confidence value
     *
     */
    public Truth(final float f, final float c, final boolean b) {
        setFrequency(f);                
        setConfidence(c);        
        setAnalytic(b);
    }

    /**
     * Constructor with a TruthValue to clone
     *
     * @param v The truth value to be cloned
     */
    public Truth(final Truth v) {
        setFrequency(v.getFrequency());
        setConfidence(v.getConfidence());
        setAnalytic(v.getAnalytic());
    }

    public Truth(char punctuation) {
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

    /**
     * Get the frequency value
     *
     * @return The frequency value
     */
    public float getFrequency() {
        return frequency;
    }

    /**
     * Get the confidence value
     *
     * @return The confidence value
     */
    public float getConfidence() {
        return confidence;
    }

    public Truth setFrequency(float f) {
        if (f > 1.0f) f = 1.0f;
        if (f < 0f) f = 0f;
        //if ((f > 1.0f) || (f < 0f)) throw new RuntimeException("Invalid frequency: " + f); //f = 0f;

        this.frequency = Math.round( f / TRUTH_EPSILON) * TRUTH_EPSILON;
        return this;
    }
    
    public Truth setConfidence(float c) {
        //if ((c > 1.0f) || (c < 0f)) throw new RuntimeException("Invalid confidence: " + c);
        if (c > Global.MAX_CONFIDENCE)  c = Global.MAX_CONFIDENCE;
        if (c < 0) c = 0;
        this.confidence = Math.round( c / TRUTH_EPSILON) * TRUTH_EPSILON;
        return this;
    }
    
    /**
     * Get the isAnalytic flag
     *
     * @return The isAnalytic value
     */
    public boolean getAnalytic() {
        return analytic;
    }

    /**
     * Set the isAnalytic flag
     */
    public void setAnalytic() {
        analytic = true;
    }

    /**
     * Calculate the expectation value of the truth value
     *
     * @return The expectation value
     */
    public float getExpectation() {
        return expectation(frequency, confidence);
    }

    public static final float expectation(final float frequency, final float confidence) {
        return (confidence * (frequency - 0.5f) + 0.5f);
    }

    /**
     * Calculate the absolute difference of the expectation value and that of a
     * given truth value
     *
     * @param t The given value
     * @return The absolute difference
     */
    public float getExpDifAbs(final Truth t) {
        return Math.abs(getExpectation() - t.getExpectation());
    }

    /**
     * Check if the truth value is negative
     *
     * @return True if the frequence is less than 1/2
     */
    public boolean isNegative() {
        return getFrequency() < 0.5;
    }

    public static boolean isEqual(final float a, final float b, final float epsilon) {
        if (a > b) return ((a - b) < epsilon/2f);
        else if ( a < b) return ((b - a) < epsilon/2f);
        return true;
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
            if (!isEqual(getFrequency(), t.getFrequency(), TRUTH_EPSILON))
                return false;
            if (!isEqual(getConfidence(), t.getConfidence(), TRUTH_EPSILON))
                return false;
            return true;
        }
        return false;
    }

    /**
     * The hash code of a TruthValue
     * TODO this is not accurate, must use both freq and conf otherwise there is missing information
     * @return The hash code
     */
    @Override
    public int hashCode() {
        int h = 0;
        h += Float.floatToRawIntBits(frequency);
        h += 31 * Float.floatToRawIntBits(confidence);
        if (analytic)
            h += 31;
        return h;
    }

    @Override
    public Truth clone() {
        return new Truth(frequency, confidence, getAnalytic());
    }
    
    
    public Truth setAnalytic(final boolean a) {
        analytic = a;
        return this;
    }

    

    /**
     * A simplified String representation of a TruthValue, where each factor is
     * accruate to 1%
     */
    public StringBuilder appendString(final StringBuilder sb, final boolean external) {
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
            .append(Texts.n2(frequency))
            .append(Symbols.VALUE_SEPARATOR)
            .append(Texts.n2(confidence))
            .append(Symbols.TRUTH_VALUE_MARK);
    }

    public String toStringExternal1() {
        return new StringBuilder(5)
                .append(Symbols.TRUTH_VALUE_MARK)
                .append(Texts.n1(frequency))
                .append(Symbols.VALUE_SEPARATOR)
                .append(Texts.n1(confidence))
                .append(Symbols.TRUTH_VALUE_MARK).toString();
    }


    public CharSequence name() {
        //1 + 4 + 1 + 4 + 1
        StringBuilder sb =  new StringBuilder();
        return appendString(sb, false);
    }

    /** output representation */
    public CharSequence toStringExternal() {
        //return name().toString();
        StringBuilder sb =  new StringBuilder();
        return appendString(sb, true);
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

    
    
    /** displays the truth value as a short string indicating degree of true/false */
    public String toTrueFalseString() {        
        //TODO:
        //  F,f,~,t,T
        return null;
    }
    /** displays the truth value as a short string indicating degree of yes/no */
    public String toYesNoString() {        
        //TODO
        // N,n,~,y,Y
        return null;
    }

    
    public Term toWordTerm() {
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

    public Truth set(float frequency, float confidence) {
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
    
    public float getComponent(TruthComponent c) {
        switch (c) {
            case Frequency: return frequency;
            case Confidence: return confidence;
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
}
