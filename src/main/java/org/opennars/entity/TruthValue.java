/* 
 * The MIT License
 *
 * Copyright 2018 The OpenNARS authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.opennars.entity;

import org.opennars.io.Symbols;
import org.opennars.io.Texts;
import org.opennars.language.Term;

import java.io.Serializable;
import org.opennars.main.Parameters;

/**
 * Truth is a tuple of frequency and confidence as defined by NARS theory
 *
 * @author Pei Wang
 * @author Patrick Hammer
 */
public class TruthValue implements Cloneable, Serializable { // implements Cloneable {


    final static Term Truth_TRUE = new Term("TRUE");
    final static Term Truth_FALSE = new Term("FALSE");
    final static Term Truth_UNSURE = new Term("UNSURE");
    
    /**
     * character that marks the two ends of a truth value
     */
    private static final char DELIMITER = Symbols.TRUTH_VALUE_MARK;
    /**
     * character that separates the factors in a truth value
     */
    private static final char SEPARATOR = Symbols.VALUE_SEPARATOR;
    /**
     * frequency factor of the truth value
     */
    private float frequency;
    /**
     * confidence factor of the truth value
     */
    private float confidence;
    /**
     * Whether the truth value is derived from a definition
     */
    private boolean analytic = false;
    
    private Parameters narParameters;

    /**
     * @param narParameters parameters of the reasoner
     */
    public TruthValue(Parameters narParameters) {
        this(0,0, narParameters);
    }
    
    /**
     * Constructor
     *
     * @param f frequency value
     * @param c confidence value
     * @param narParameters parameters of the reasoner
     */
    public TruthValue(final float f, final float c, Parameters narParameters) {
        this(f, c, false, narParameters);
    }

    /**
     * Constructor
     *
     * @param f frequency value
     * @param c confidence value
     * @param isAnalytic is the truth value an analytic one?
     * @param narParameters parameters of the reasoner
     */
    public TruthValue(final float f, final float c, final boolean isAnalytic, Parameters narParameters) {
        this.narParameters = narParameters;
        setFrequency(f);                
        setConfidence(c);        
        setAnalytic(isAnalytic);
    }

    /**
     * Constructor with a TruthValue to clone
     *
     * @param v truth value to be cloned
     */
    public TruthValue(final TruthValue v) {
        narParameters = v.narParameters;
        frequency = v.getFrequency();
        confidence = v.getConfidence();
        analytic = v.getAnalytic();
    }

    /**
     * returns the frequency value
     *
     * @return frequency value
     */
    public float getFrequency() {
        return frequency;
    }

    /**
     * returns the confidence value
     *
     * @return confidence value
     */
    public float getConfidence() {
        return confidence;
    }

    public TruthValue setFrequency(final float f) {
        this.frequency = f;
        return this;
    }
    
    public TruthValue setConfidence(final float c) {
        float max_confidence = 1.0f - this.narParameters.TRUTH_EPSILON;
        this.confidence = (c < max_confidence) ? c : max_confidence;
        return this;
    }
    
    /**
     * @return is it a analytic truth value?
     */
    public boolean getAnalytic() {
        return analytic;
    }

    /**
     * Set it to analytic truth
     */
    public void setAnalytic() {
        analytic = true;
    }

    /**
     * Calculate the expectation value of the truth value
     *
     * @return expectation value
     */
    public float getExpectation() {
        return (confidence * (frequency - 0.5f) + 0.5f);
    }

    /**
     * Calculate the absolute difference of the expectation value and that of a
     * given truth value
     *
     * @param t given value
     * @return absolute difference
     */
    public float getExpDifAbs(final TruthValue t) {
        return Math.abs(getExpectation() - t.getExpectation());
    }

    /**
     * Check if the truth value is negative
     *
     * @return True if the frequency is less than 1/2
     */
    public boolean isNegative() {
        return getFrequency() < 0.5;
    }

    public static boolean isEqual(final float a, final float b, final float epsilon) {
        final float d = Math.abs(a - b);
        return (d < epsilon);
    }
    
    /**
     * Compare two truth values
     *
     * @param that other TruthValue
     * @return Whether the two are equivalent
     */
    @Override
    public boolean equals(final Object that) { 
        if (that instanceof TruthValue) {
            final TruthValue t = (TruthValue)that;
            return
                isEqual(getFrequency(), t.getFrequency(), this.narParameters.TRUTH_EPSILON) &&
                isEqual(getConfidence(), t.getConfidence(), this.narParameters.TRUTH_EPSILON);
        }
        return false;
    }

    /**
     * The hash code of a TruthValue
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return ((int)(0xFFFF * this.frequency) << 16) | (int)(0xFFFF * this.confidence);
    }

    @Override
    public TruthValue clone() {
        return new TruthValue(frequency, confidence, getAnalytic(), this.narParameters);
    }
    
    
    public TruthValue setAnalytic(final boolean a) {
        analytic = a;
        return this;
    }

    /**
     * A simplified String representation of a TruthValue
     */
    public StringBuilder appendString(final StringBuilder sb, final boolean external) {        
        sb.ensureCapacity(11);
        return sb
            .append(DELIMITER)
            .append(Texts.n2(frequency))
            .append(SEPARATOR)
            .append(Texts.n2(confidence))
            .append(DELIMITER);        
    }

    public CharSequence name() {
        final StringBuilder sb =  new StringBuilder();
        return appendString(sb, false);
    }

    /** output representation */
    public CharSequence toStringExternal() {
        final StringBuilder sb =  new StringBuilder();
        return appendString(sb, true);
    }
    /**
     * Returns a String representation of a TruthValue, as used internally by the system
     *
     * @return String representation
     */
    @Override
    public String toString() {
        return name().toString();
    }
    
    public Term toWordTerm() {
        final float e = getExpectation();
        final float t = this.narParameters.DEFAULT_CREATION_EXPECTATION;
        if (e > t) {
            return Truth_TRUE;
        }
        if (e < 1 - t) {
            return Truth_FALSE;
        }
        return Truth_UNSURE;
    }
    
    public static TruthValue fromWordTerm(Parameters narParameters, Term term) {
        if(term.equals(Truth_TRUE)) {
            return new TruthValue(1.0f, narParameters.DEFAULT_JUDGMENT_CONFIDENCE, narParameters);
        } else if(term.equals(Truth_FALSE)) {
            return new TruthValue(0.0f, narParameters.DEFAULT_JUDGMENT_CONFIDENCE, narParameters);
        } else if(term.equals(Truth_UNSURE)) {
            return new TruthValue(0.5f, narParameters.DEFAULT_JUDGMENT_CONFIDENCE / 2.0f, narParameters);
        } else {
            return null;
        }
    }

    public TruthValue set(final float frequency, final float confidence) {
        setFrequency(frequency);
        setConfidence(confidence);
        return this;
    }
}
