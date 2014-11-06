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
package nars.entity;

import nars.core.Parameters;
import static nars.core.Parameters.TRUTH_EPSILON;
import nars.io.Symbols;
import nars.io.Texts;
import nars.language.Term;


public class TruthValue implements Cloneable { // implements Cloneable {


    final static Term Truth_TRUE = new Term("TRUE");
    final static Term Truth_FALSE = new Term("FALSE");
    final static Term Truth_UNSURE = new Term("UNSURE");
    
    /**
     * The character that marks the two ends of a truth value
     */
    private static final char DELIMITER = Symbols.TRUTH_VALUE_MARK;
    /**
     * The character that separates the factors in a truth value
     */
    private static final char SEPARATOR = Symbols.VALUE_SEPARATOR;
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

    public TruthValue() {
        this(0,0);
    }
    
    /**
     * Constructor with two ShortFloats
     *
     * @param f The frequency value
     * @param c The confidence value
     */
    public TruthValue(final float f, final float c) {
        this(f, c, false);
    }

    /**
     * Constructor with two ShortFloats
     *
     * @param f The frequency value
     * @param c The confidence value
     *
     */
    public TruthValue(final float f, final float c, final boolean b) {
        setFrequency(f);                
        setConfidence(c);        
        setAnalytic(b);
    }

    /**
     * Constructor with a TruthValue to clone
     *
     * @param v The truth value to be cloned
     */
    public TruthValue(final TruthValue v) {
        frequency = v.getFrequency();
        confidence = v.getConfidence();
        analytic = v.getAnalytic();
    }

    /**
     * Get the frequency value
     *
     * @return The frequency value
     */
    public float getFrequency() {
        //return Math.round(frequency * TRUTH_PRECISION) / TRUTH_PRECISION; 
        return frequency;
    }

    /**
     * Get the confidence value
     *
     * @return The confidence value
     */
    public float getConfidence() {
        //return Math.round(confidence * TRUTH_PRECISION) / TRUTH_PRECISION; 
        return confidence;
    }

    public TruthValue setFrequency(final float f) {
        this.frequency = f;
        return this;
    }
    
    public TruthValue setConfidence(float c) {
        this.confidence = (c < Parameters.MAX_CONFIDENCE) ? c : Parameters.MAX_CONFIDENCE;
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
        return (confidence * (frequency - 0.5f) + 0.5f);
    }

    /**
     * Calculate the absolute difference of the expectation value and that of a
     * given truth value
     *
     * @param t The given value
     * @return The absolute difference
     */
    public float getExpDifAbs(final TruthValue t) {
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

    public static boolean isEqual(final float a, final float b, float epsilon) {
        float d = Math.abs(a - b);
        return (d < epsilon);
    }
    
    /**
     * Compare two truth values
     *
     * @param that The other TruthValue
     * @return Whether the two are equivalent
     */
    @Override
    public boolean equals(final Object that) { 
        if (that instanceof TruthValue) {
            final TruthValue t = ((TruthValue) that);
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
     *
     * @return The hash code
     */
    @Override
    public int hashCode() {
        return (int) (getExpectation() * 37);
    }

    @Override
    public TruthValue clone() {
        return new TruthValue(frequency, confidence, getAnalytic());
    }
    
    
    public TruthValue setAnalytic(final boolean a) {
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
            .append(DELIMITER)
            .append(Texts.n2(frequency))
            .append(SEPARATOR)
            .append(Texts.n2(confidence))
            .append(DELIMITER);        
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
        float t = Parameters.DEFAULT_CREATION_EXPECTATION;
        if (e > t) {
            return Truth_TRUE;
        }
        if (e < 1 - t) {
            return Truth_FALSE;
        }
        return Truth_UNSURE;
    }
    
}
