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
package org.opennars.inference;

import static java.lang.Math.abs;
import org.opennars.main.Parameters;
import org.opennars.entity.TruthValue;

/**
 * All truth-value (and desire-value) functions used in inference rules 
 */
public final class TruthFunctions extends UtilityFunctions {

    /* ----- Single argument functions, called in MatchingRules ----- */
    /**
     * {<A ==> B>} |- <B ==> A>
     * @param v1 Truth value of the premise
     * @return Truth value of the conclusion
     */
    static final TruthValue conversion(final TruthValue v1) {
        final float f1 = v1.getFrequency();
        final float c1 = v1.getConfidence();
        final float w = and(f1, c1);
        final float c = w2c(w);
        return new TruthValue(1, c);
    }

    /* ----- Single argument functions, called in StructuralRules ----- */
    /**
     * {A} |- (--A)
     * @param v1 Truth value of the premise
     * @return Truth value of the conclusion
     */
    public static final TruthValue negation(final TruthValue v1) {
        final float f = 1 - v1.getFrequency();
        final float c = v1.getConfidence();
        return new TruthValue(f, c);
    }

    /**
     * {<A ==> B>} |- <(--, B) ==> (--, A)>
     * @param v1 Truth value of the premise
     * @return Truth value of the conclusion
     */
    static final TruthValue contraposition(final TruthValue v1) {
        final float f1 = v1.getFrequency();
        final float c1 = v1.getConfidence();
        final float w = and(1 - f1, c1);
        final float c = w2c(w);
        return new TruthValue(0, c);
    }

    /* ----- double argument functions, called in MatchingRules ----- */
    /**
     * {<S ==> P>, <S ==> P>} |- <S ==> P>
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final TruthValue revision(final TruthValue v1, final TruthValue v2) {
        return revision(v1, v2, new TruthValue());
    }
    
    static final TruthValue revision(final TruthValue v1, final TruthValue v2, final TruthValue result) {
        final float f1 = v1.getFrequency();
        final float f2 = v2.getFrequency();
        final float w1 = c2w( v1.getConfidence() );
        final float w2 = c2w( v2.getConfidence() );
        final float w = w1 + w2;
        result.setFrequency( (w1 * f1 + w2 * f2) / w );
        result.setConfidence( w2c(w) );
        return result;
    }
    
    /* ----- double argument functions, called in SyllogisticRules ----- */
    /**
     * {<S ==> M>, <M ==> P>} |- <S ==> P>
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final TruthValue deduction(final TruthValue v1, final TruthValue v2) {
        final float f1 = v1.getFrequency();
        final float f2 = v2.getFrequency();
        final float c1 = v1.getConfidence();
        final float c2 = v2.getConfidence();
        final float f = and(f1, f2);
        final float c = and(c1, c2, f);
        return new TruthValue(f, c);
    }

    /**
     * {M, <M ==> P>} |- P
     * @param v1 Truth value of the first premise
     * @param reliance Confidence of the second (analytical) premise
     * @return Truth value of the conclusion
     */
    public static final TruthValue deduction(final TruthValue v1, final float reliance) {
        final float f1 = v1.getFrequency();
        final float c1 = v1.getConfidence();
        final float c = and(f1, c1, reliance);
        return new TruthValue(f1, c, true);
    }

    /**
     * {<S ==> M>, <M <=> P>} |- <S ==> P>
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    static final TruthValue analogy(final TruthValue v1, final TruthValue v2) {
        final float f1 = v1.getFrequency();
        final float f2 = v2.getFrequency();
        final float c1 = v1.getConfidence();
        final float c2 = v2.getConfidence();
        final float f = and(f1, f2);
        final float c = and(c1, c2, f2);
        return new TruthValue(f, c);
    }

    /**
     * {<S <=> M>, <M <=> P>} |- <S <=> P>
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    static final TruthValue resemblance(final TruthValue v1, final TruthValue v2) {
        final float f1 = v1.getFrequency();
        final float f2 = v2.getFrequency();
        final float c1 = v1.getConfidence();
        final float c2 = v2.getConfidence();
        final float f = and(f1, f2);
        final float c = and(c1, c2, or(f1, f2));
        return new TruthValue(f, c);
    }

    /**
     * {<S ==> M>, <P ==> M>} |- <S ==> P>
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final TruthValue abduction(final TruthValue v1, final TruthValue v2) {
        if (v1.getAnalytic() || v2.getAnalytic()) {
            return new TruthValue(0.5f, 0f);
        }
        final float f1 = v1.getFrequency();
        final float f2 = v2.getFrequency();
        final float c1 = v1.getConfidence();
        final float c2 = v2.getConfidence();
        final float w = and(f2, c1, c2);
        final float c = w2c(w);
        return new TruthValue(f1, c);
    }

    /**
     * {M, <P ==> M>} |- P
     * @param v1 Truth value of the first premise
     * @param reliance Confidence of the second (analytical) premise
     * @return Truth value of the conclusion
     */
    static final TruthValue abduction(final TruthValue v1, final float reliance) {
        if (v1.getAnalytic()) {
            return new TruthValue(0.5f, 0f);
        }
        final float f1 = v1.getFrequency();
        final float c1 = v1.getConfidence();
        final float w = and(c1, reliance);
        final float c = w2c(w);
        return new TruthValue(f1, c, true);
    }

    /**
     * {<M ==> S>, <M ==> P>} |- <S ==> P>
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final TruthValue induction(final TruthValue v1, final TruthValue v2) {
        return abduction(v2, v1);
    }

    /**
     * {<M ==> S>, <P ==> M>} |- <S ==> P>
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    static final TruthValue exemplification(final TruthValue v1, final TruthValue v2) {
        if (v1.getAnalytic() || v2.getAnalytic()) {
            return new TruthValue(0.5f, 0f);
        }
        final float f1 = v1.getFrequency();
        final float f2 = v2.getFrequency();
        final float c1 = v1.getConfidence();
        final float c2 = v2.getConfidence();
        final float w = and(f1, f2, c1, c2);
        final float c = w2c(w);
        return new TruthValue(1, c);
    }

    /**
     * {<M ==> S>, <M ==> P>} |- <S <=> P>
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final TruthValue comparison(final TruthValue v1, final TruthValue v2) {
        final float f1 = v1.getFrequency();
        final float f2 = v2.getFrequency();
        final float c1 = v1.getConfidence();
        final float c2 = v2.getConfidence();
        final float f0 = or(f1, f2);
        final float f = (f0 == 0) ? 0 : (and(f1, f2) / f0);
        final float w = and(f0, c1, c2);
        final float c = w2c(w);
        return new TruthValue(f, c);
    }

    /* ----- desire-value functions, called in SyllogisticRules ----- */
    /**
     * A function specially designed for desire value [To be refined]
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final TruthValue desireStrong(final TruthValue v1, final TruthValue v2) {
        final float f1 = v1.getFrequency();
        final float f2 = v2.getFrequency();
        final float c1 = v1.getConfidence();
        final float c2 = v2.getConfidence();
        final float f = and(f1, f2);
        final float c = and(c1, c2, f2);
        return new TruthValue(f, c);
    }

    /**
     * A function specially designed for desire value [To be refined]
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    static final TruthValue desireWeak(final TruthValue v1, final TruthValue v2) {
        final float f1 = v1.getFrequency();
        final float f2 = v2.getFrequency();
        final float c1 = v1.getConfidence();
        final float c2 = v2.getConfidence();
        final float f = and(f1, f2);
        final float c = and(c1, c2, f2, w2c(1.0f));
        return new TruthValue(f, c);
    }

    /**
     * A function specially designed for desire value [To be refined]
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final TruthValue desireDed(final TruthValue v1, final TruthValue v2) {
        final float f1 = v1.getFrequency();
        final float f2 = v2.getFrequency();
        final float c1 = v1.getConfidence();
        final float c2 = v2.getConfidence();
        final float f = and(f1, f2);
        final float c = and(c1, c2);
        return new TruthValue(f, c);
    }

    /**
     * A function specially designed for desire value [To be refined]
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    static final TruthValue desireInd(final TruthValue v1, final TruthValue v2) {
        final float f1 = v1.getFrequency();
        final float f2 = v2.getFrequency();
        final float c1 = v1.getConfidence();
        final float c2 = v2.getConfidence();
        final float w = and(f2, c1, c2);
        final float c = w2c(w);
        return new TruthValue(f1, c);
    }

    /* ----- double argument functions, called in CompositionalRules ----- */
    /**
     * {<M --> S>, <M <-> P>} |- <M --> (S|P)>
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final TruthValue union(final TruthValue v1, final TruthValue v2) {
        final float f1 = v1.getFrequency();
        final float f2 = v2.getFrequency();
        final float c1 = v1.getConfidence();
        final float c2 = v2.getConfidence();
        final float f = or(f1, f2);
        final float c = and(c1, c2);
        return new TruthValue(f, c);
    }

    /**
     * {<M --> S>, <M <-> P>} |- <M --> (S&P)>
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final TruthValue intersection(final TruthValue v1, final TruthValue v2) {
        final float f1 = v1.getFrequency();
        final float f2 = v2.getFrequency();
        final float c1 = v1.getConfidence();
        final float c2 = v2.getConfidence();
        final float f = and(f1, f2);
        final float c = and(c1, c2);
        return new TruthValue(f, c);
    }

    /**
     * {(||, A, B), (--, B)} |- A
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    static final TruthValue reduceDisjunction(final TruthValue v1, final TruthValue v2) {
        final TruthValue v0 = intersection(v1, negation(v2));
        return deduction(v0, 1f);
    }

    /**
     * {(--, (&&, A, B)), B} |- (--, A)
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    static final TruthValue reduceConjunction(final TruthValue v1, final TruthValue v2) {
        final TruthValue v0 = intersection(negation(v1), v2);
        return negation(deduction(v0, 1f));
    }

    /**
     * {(--, (&&, A, (--, B))), (--, B)} |- (--, A)
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    static final TruthValue reduceConjunctionNeg(final TruthValue v1, final TruthValue v2) {
        return reduceConjunction(v1, negation(v2));
    }

    /**
     * {(&&, <#x() ==> M>, <#x() ==> P>), S ==> M} |- <S ==> P>
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    static final TruthValue anonymousAnalogy(final TruthValue v1, final TruthValue v2) {
        final float f1 = v1.getFrequency();
        final float c1 = v1.getConfidence();
        final TruthValue v0 = new TruthValue(f1, w2c(c1));
        return analogy(v2, v0);
    }
    
    
    /** functions the same as TruthValue, but being a separate class,
     *  indicates it was the result of eternalization */
    public static final class EternalizedTruthValue extends TruthValue {
        public EternalizedTruthValue(final float f, final float c) {
            super(f, c);
        }        
    }
    
    /**
     * From one moment to eternal
     * @param v1 Truth value of the premise
     * @return Truth value of the conclusion
     */
    public static final EternalizedTruthValue eternalize(final TruthValue v1) {
        final float f1 = v1.getFrequency();
        final float c1 = v1.getConfidence();
        final float c = w2c(c1);
        return new EternalizedTruthValue(f1, c);
    }
    
    public static final float temporalProjection(final long sourceTime, final long targetTime, final long currentTime) {
        double a = 100000.0 * Parameters.projectionDecay.get(); //projection less strict as we changed in v2.0.0  10000.0 slower decay than 100000.0
        return 1.0f - abs(sourceTime - targetTime) / (float) (abs(sourceTime - currentTime) + abs(targetTime - currentTime) + a);
    }
}
