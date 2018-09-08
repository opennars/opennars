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
package org.opennars.inference;

import org.opennars.entity.TruthValue;

import static java.lang.Math.abs;
import org.opennars.main.Parameters;

/**
 * All truth-value (and desire-value) functions used in inference rules
 *
 * @author Pei Wang
 * @author Patrick Hammer
 * @author Robert Wünsche
 */
public final class TruthFunctions extends UtilityFunctions {
    public enum EnumType {
        DESIREDED,
        DESIREIND,
        DESIREWEAK,
        DESIRESTRONG,
        COMPARISON,
        ANALOGY,
        ANONYMOUSANALOGY,
        DEDUCTION,
        EXEMPLIFICATION,
        ABDUCTION,
        RESEMBLENCE,
        REDUCECONJUNCTION,
        REDUCEDISJUNCTION,
        REDUCEDISJUNCTIONREV,
        REDUCECONJUNCTIONNEG,
    }

    /**
     * lookup the truth function and compute the value
     * @param type truth-function
     * @param a truth value of the first premise
     * @param b truth value of the second premise
     * @return truth value as computed by the truth-function
     */
    public static TruthValue lookupTruthFunctionAndCompute(final EnumType type, final TruthValue a, final TruthValue b, final Parameters narParameters) {
        switch(type) {
            case DESIREDED: return desireDed(a, b, narParameters);
            case DESIREIND: return desireInd(a, b, narParameters);
            case DESIREWEAK: return desireWeak(a, b, narParameters);
            case DESIRESTRONG: return desireStrong(a, b, narParameters);
            case COMPARISON: return comparison(a, b, narParameters);
            case ANALOGY: return analogy(a, b, narParameters);
            case ANONYMOUSANALOGY: return anonymousAnalogy(a, b, narParameters);
            case DEDUCTION: return deduction(a, b, narParameters);
            case EXEMPLIFICATION: return exemplification(a, b, narParameters);
            case ABDUCTION: return abduction(a, b, narParameters);
            case RESEMBLENCE: return resemblance(a, b, narParameters);
            case REDUCECONJUNCTION: return reduceConjunction(a, b, narParameters);
            case REDUCEDISJUNCTION: return reduceDisjunction(a, b, narParameters);
            case REDUCEDISJUNCTIONREV: return reduceDisjunction(b, a, narParameters);
            case REDUCECONJUNCTIONNEG: return reduceConjunctionNeg(a, b, narParameters);
            default: throw new IllegalArgumentException("Encountered unimplemented case!"); // internal error
        }
    }

    /**
     * lookup the truth function and compute the value - for two truth functions which are decided by flag
     * @param flag which type to choose
     * @param typeTrue truth-function for the case when the flag is true
     * @param typeFalse truth-function for the case when the flag is false
     * @param a truth value of the first premise
     * @param b truth value of the second premise
     * @return truth value as computed by the truth-function
     */
    public static TruthValue lookupTruthFunctionByBoolAndCompute(final boolean flag, final EnumType typeTrue, final EnumType typeFalse, final TruthValue a, final TruthValue b, Parameters narParameters) {
        final EnumType type = flag ? typeTrue : typeFalse;
        return lookupTruthFunctionAndCompute(type, a, b, narParameters);
    }

    /**
     * lookup the truth function by the first boolean which is true or return null if no boolean is true
     * @param values tuples of boolean conditional values and their corresponding truth function
     * @param a truth value of the first premise
     * @param b truth value of the second premise
     * @return truth value as computed by the truth-function or null if no boolean value was true
     */
    public static TruthValue lookupTruthOrNull(final TruthValue a, final TruthValue b, Parameters narParameters, final Object... values) {
        final int numberOfTuples = (values.length) / 2;

        for(int idx = 0; idx < numberOfTuples; idx++) {
            final boolean v = (boolean)values[idx*2];
            if( v ) {
                final EnumType type = (EnumType)values[idx*2+1];
                return lookupTruthFunctionAndCompute(type, a, b, narParameters);
            }
        }

        return null;
    }


    /* ----- Single argument functions, called in MatchingRules ----- */
    /**
     * {&lt;A ==&gt; B&gt;} |- &lt;B ==&gt; A&gt;
     * @param v1 Truth value of the premise
     * @return Truth value of the conclusion
     */
    public static final TruthValue conversion(final TruthValue v1, Parameters narParameters) {
        final float f1 = v1.getFrequency();
        final float c1 = v1.getConfidence();
        final float w = and(f1, c1);
        final float c = w2c(w, narParameters);
        return new TruthValue(1, c, narParameters);
    }

    /* ----- Single argument functions, called in StructuralRules ----- */
    /**
     * {A} |- (--A)
     * @param v1 Truth value of the premise
     * @return Truth value of the conclusion
     */
    public static final TruthValue negation(final TruthValue v1, Parameters narParameters) {
        final float f = 1 - v1.getFrequency();
        final float c = v1.getConfidence();
        return new TruthValue(f, c, narParameters);
    }

    /**
     * {&lt;A ==&gt; B&gt;} |- &lt;(--, B) ==&gt; (--, A)&gt;
     * @param v1 Truth value of the premise
     * @return Truth value of the conclusion
     */
    public static final TruthValue contraposition(final TruthValue v1, Parameters narParameters) {
        final float f1 = v1.getFrequency();
        final float c1 = v1.getConfidence();
        final float w = and(1 - f1, c1);
        final float c = w2c(w, narParameters);
        return new TruthValue(0, c, narParameters);
    }

    /* ----- double argument functions, called in MatchingRules ----- */
    /**
     * {&lt;S ==&gt; P&gt;, &lt;S ==&gt; P&gt;} |- &lt;S ==&gt; P&gt;
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final TruthValue revision(final TruthValue v1, final TruthValue v2, Parameters narParameters) {
        return revision(v1, v2, new TruthValue(narParameters), narParameters);
    }
    
    private static final TruthValue revision(final TruthValue v1, final TruthValue v2, final TruthValue result, Parameters narParameters) {
        final float f1 = v1.getFrequency();
        final float f2 = v2.getFrequency();
        final float w1 = c2w( v1.getConfidence(), narParameters );
        final float w2 = c2w( v2.getConfidence(), narParameters );
        final float w = w1 + w2;
        result.setFrequency( (w1 * f1 + w2 * f2) / w );
        result.setConfidence( w2c(w, narParameters) );
        return result;
    }
    
    /* ----- double argument functions, called in SyllogisticRules ----- */
    /**
     * {&lt;S ==&gt; M&gt;, &lt;M ==&gt; P&gt;} |- &lt;S ==&gt; P&gt;
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final TruthValue deduction(final TruthValue v1, final TruthValue v2, Parameters narParameters) {
        final float f1 = v1.getFrequency();
        final float f2 = v2.getFrequency();
        final float c1 = v1.getConfidence();
        final float c2 = v2.getConfidence();
        final float f = and(f1, f2);
        final float c = and(c1, c2, f);
        return new TruthValue(f, c, narParameters);
    }

    /**
     * {M, &lt;M ==&gt; P&gt;} |- P
     * @param v1 Truth value of the first premise
     * @param reliance Confidence of the second (analytical) premise
     * @return Truth value of the conclusion
     */
    public static final TruthValue deduction(final TruthValue v1, final float reliance, Parameters narParameters) {
        final float f1 = v1.getFrequency();
        final float c1 = v1.getConfidence();
        final float c = and(f1, c1, reliance);
        return new TruthValue(f1, c, true, narParameters);
    }

    /**
     * {&lt;S ==&gt; M&gt;, &lt;M &lt;=&gt; P&gt;} |- &lt;S ==&gt; P&gt;
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final TruthValue analogy(final TruthValue v1, final TruthValue v2, Parameters narParameters) {
        final float f1 = v1.getFrequency();
        final float f2 = v2.getFrequency();
        final float c1 = v1.getConfidence();
        final float c2 = v2.getConfidence();
        final float f = and(f1, f2);
        final float c = and(c1, c2, f2);
        return new TruthValue(f, c, narParameters);
    }

    /**
     * {&lt;S &lt;=&gt; M&gt;, &lt;M &lt;=&gt; P&gt;} |- &lt;S &lt;=&gt; P&gt;
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final TruthValue resemblance(final TruthValue v1, final TruthValue v2, Parameters narParameters) {
        final float f1 = v1.getFrequency();
        final float f2 = v2.getFrequency();
        final float c1 = v1.getConfidence();
        final float c2 = v2.getConfidence();
        final float f = and(f1, f2);
        final float c = and(c1, c2, or(f1, f2));
        return new TruthValue(f, c, narParameters);
    }

    /**
     * {&lt;S ==&gt; M&gt;, &lt;P ==&gt; M&gt;} |- &lt;S ==&gt; P&gt;
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final TruthValue abduction(final TruthValue v1, final TruthValue v2, Parameters narParameters) {
        if (v1.getAnalytic() || v2.getAnalytic()) {
            return new TruthValue(0.5f, 0f, narParameters);
        }
        final float f1 = v1.getFrequency();
        final float f2 = v2.getFrequency();
        final float c1 = v1.getConfidence();
        final float c2 = v2.getConfidence();
        final float w = and(f2, c1, c2);
        final float c = w2c(w, narParameters);
        return new TruthValue(f1, c, narParameters);
    }

    /**
     * {M, &lt;P ==&gt; M&gt;} |- P
     * @param v1 Truth value of the first premise
     * @param reliance Confidence of the second (analytical) premise
     * @return Truth value of the conclusion
     */
    public static final TruthValue abduction(final TruthValue v1, final float reliance, Parameters narParameters) {
        if (v1.getAnalytic()) {
            return new TruthValue(0.5f, 0f, narParameters);
        }
        final float f1 = v1.getFrequency();
        final float c1 = v1.getConfidence();
        final float w = and(c1, reliance);
        final float c = w2c(w, narParameters);
        return new TruthValue(f1, c, true, narParameters);
    }

    /**
     * {&lt;M ==&gt; S&gt;, &lt;M ==&gt; P&gt;} |- &lt;S ==&gt; P&gt;
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final TruthValue induction(final TruthValue v1, final TruthValue v2, Parameters narParameters) {
        return abduction(v2, v1, narParameters);
    }

    /**
     * {&lt;M ==&gt; S&gt;, &lt;P ==&gt; M&gt;} |- &lt;S ==&gt; P&gt;
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final TruthValue exemplification(final TruthValue v1, final TruthValue v2, Parameters narParameters) {
        if (v1.getAnalytic() || v2.getAnalytic()) {
            return new TruthValue(0.5f, 0f, narParameters);
        }
        final float f1 = v1.getFrequency();
        final float f2 = v2.getFrequency();
        final float c1 = v1.getConfidence();
        final float c2 = v2.getConfidence();
        final float w = and(f1, f2, c1, c2);
        final float c = w2c(w, narParameters);
        return new TruthValue(1, c, narParameters);
    }

    /**
     * {&lt;M ==&gt; S&gt;, &lt;M ==&gt; P&gt;} |- &lt;S &lt;=&gt; P&gt;
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final TruthValue comparison(final TruthValue v1, final TruthValue v2, Parameters narParameters) {
        final float f1 = v1.getFrequency();
        final float f2 = v2.getFrequency();
        final float c1 = v1.getConfidence();
        final float c2 = v2.getConfidence();
        final float f0 = or(f1, f2);
        final float f = (f0 == 0) ? 0 : (and(f1, f2) / f0);
        final float w = and(f0, c1, c2);
        final float c = w2c(w, narParameters);
        return new TruthValue(f, c, narParameters);
    }

    /* ----- desire-value functions, called in SyllogisticRules ----- */
    /**
     * A function specially designed for desire value [To be refined]
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final TruthValue desireStrong(final TruthValue v1, final TruthValue v2, Parameters narParameters) {
        final float f1 = v1.getFrequency();
        final float f2 = v2.getFrequency();
        final float c1 = v1.getConfidence();
        final float c2 = v2.getConfidence();
        final float f = and(f1, f2);
        final float c = and(c1, c2, f2);
        return new TruthValue(f, c, narParameters);
    }

    /**
     * A function specially designed for desire value [To be refined]
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final TruthValue desireWeak(final TruthValue v1, final TruthValue v2, Parameters narParameters) {
        final float f1 = v1.getFrequency();
        final float f2 = v2.getFrequency();
        final float c1 = v1.getConfidence();
        final float c2 = v2.getConfidence();
        final float f = and(f1, f2);
        final float c = and(c1, c2, f2, w2c(1.0f, narParameters));
        return new TruthValue(f, c, narParameters);
    }

    /**
     * A function specially designed for desire value [To be refined]
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final TruthValue desireDed(final TruthValue v1, final TruthValue v2, Parameters narParameters) {
        final float f1 = v1.getFrequency();
        final float f2 = v2.getFrequency();
        final float c1 = v1.getConfidence();
        final float c2 = v2.getConfidence();
        final float f = and(f1, f2);
        final float c = and(c1, c2);
        return new TruthValue(f, c, narParameters);
    }

    /**
     * A function specially designed for desire value [To be refined]
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final TruthValue desireInd(final TruthValue v1, final TruthValue v2, Parameters narParameters) {
        final float f1 = v1.getFrequency();
        final float f2 = v2.getFrequency();
        final float c1 = v1.getConfidence();
        final float c2 = v2.getConfidence();
        final float w = and(f2, c1, c2);
        final float c = w2c(w, narParameters);
        return new TruthValue(f1, c, narParameters);
    }

    /* ----- double argument functions, called in CompositionalRules ----- */
    /**
     * {&lt;M --&gt; S&gt;, &lt;M &gt; P&gt;} |- &lt;M --&gt; (S|P)&gt;
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final TruthValue union(final TruthValue v1, final TruthValue v2, Parameters narParameters) {
        final float f1 = v1.getFrequency();
        final float f2 = v2.getFrequency();
        final float c1 = v1.getConfidence();
        final float c2 = v2.getConfidence();
        final float f = or(f1, f2);
        final float c = and(c1, c2);
        return new TruthValue(f, c, narParameters);
    }

    /**
     * {&lt;M --&gt; S&gt;, &lt;M &lt;-&gt; P&gt;} |- &lt;M --&gt; (S&amp;P)&gt;
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final TruthValue intersection(final TruthValue v1, final TruthValue v2, Parameters narParameters) {
        final float f1 = v1.getFrequency();
        final float f2 = v2.getFrequency();
        final float c1 = v1.getConfidence();
        final float c2 = v2.getConfidence();
        final float f = and(f1, f2);
        final float c = and(c1, c2);
        return new TruthValue(f, c, narParameters);
    }

    /**
     * {(||, A, B), (--, B)} |- A
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final TruthValue reduceDisjunction(final TruthValue v1, final TruthValue v2, Parameters narParameters) {
        final TruthValue v0 = intersection(v1, negation(v2, narParameters), narParameters);
        return deduction(v0, 1f, narParameters);
    }

    /**
     * {(--, (&amp;&amp;, A, B)), B} |- (--, A)
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final TruthValue reduceConjunction(final TruthValue v1, final TruthValue v2, Parameters narParameters) {
        final TruthValue v0 = intersection(negation(v1, narParameters), v2, narParameters);
        return negation(deduction(v0, 1f, narParameters), narParameters);
    }

    /**
     * {(--, (&amp;&amp;, A, (--, B))), (--, B)} |- (--, A)
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final TruthValue reduceConjunctionNeg(final TruthValue v1, final TruthValue v2, Parameters narParameters) {
        return reduceConjunction(v1, negation(v2, narParameters), narParameters);
    }

    /**
     * {(&amp;&amp;, &lt;#x() ==&gt; M&gt;, &lt;#x() ==&gt; P&gt;), S ==&gt; M} |- &lt;S ==&gt; P&gt;
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final TruthValue anonymousAnalogy(final TruthValue v1, final TruthValue v2, Parameters narParameters) {
        final float f1 = v1.getFrequency();
        final float c1 = v1.getConfidence();
        final TruthValue v0 = new TruthValue(f1, w2c(c1, narParameters), narParameters);
        return analogy(v2, v0, narParameters);
    }
    
    
    /** Indicates the result of eternalization
     *
     * Implements the same functionality like TruthValue */
    public static final class EternalizedTruthValue extends TruthValue {
        public EternalizedTruthValue(final float f, final float c, Parameters narParameters) {
            super(f, c, narParameters);
        }        
    }
    
    /**
     * From one moment to eternal
     * @param v1 Truth value of the premise
     * @return Truth value of the conclusion
     */
    public static final EternalizedTruthValue eternalize(final TruthValue v1, Parameters narParameters) {
        final float f1 = v1.getFrequency();
        final float c1 = v1.getConfidence();
        final float c = w2c(c1, narParameters);
        return new EternalizedTruthValue(f1, c, narParameters);
    }
    
    public static final float temporalProjection(final long sourceTime, final long targetTime, final long currentTime, Parameters param) {
        final double a = 100000.0 * param.PROJECTION_DECAY; //projection less strict as we changed in v2.0.0  10000.0 slower decay than 100000.0
        return 1.0f - abs(sourceTime - targetTime) / (float) (abs(sourceTime - currentTime) + abs(targetTime - currentTime) + a);
    }
}
