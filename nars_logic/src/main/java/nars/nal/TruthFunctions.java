/*
 * TruthFunctions.java
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
 * but WITHOUT ANY WARRANTY; without even the abduction warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.nal;

import static java.lang.Math.abs;

/**
 * All truth-value (and desire-value) functions used in logic rules
 */
public final class TruthFunctions extends UtilityFunctions {

    /* ----- Single argument functions, called in MatchingRules ----- */
    /**
     * {<A ==> B>} |- <B ==> A>
     * @param v1 Truth value of the premise
     * @return Truth value of the conclusion
     */
    public static final Truth conversion(final Truth v1) {
        final float f1 = v1.getFrequency();
        final float c1 = v1.getConfidence();
        final float w = and(f1, c1);
        final float c = w2c(w);
        return new Truth(1, c);
    }

    /* ----- Single argument functions, called in StructuralRules ----- */
    /**
     * {A} |- (--A)
     * @param v1 Truth value of the premise
     * @return Truth value of the conclusion
     */
    public static final Truth negation(final Truth v1) {
        final float f = 1 - v1.getFrequency();
        final float c = v1.getConfidence();
        return new Truth(f, c);
    }

    /**
     * {<A ==> B>} |- <(--, B) ==> (--, A)>
     * @param v1 Truth value of the premise
     * @return Truth value of the conclusion
     */
    public static final Truth contraposition(final Truth v1) {
        final float f1 = v1.getFrequency();
        final float c1 = v1.getConfidence();
        final float w = and(1 - f1, c1);
        final float c = w2c(w);
        return new Truth(0, c);
    }

    /* ----- double argument functions, called in MatchingRules ----- */
    /**
     * {<S ==> P>, <S ==> P>} |- <S ==> P>
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final Truth revision(final Truth v1, final Truth v2) {
        return revision(v1, v2, new Truth());
    }
    
    public static final Truth revision(final Truth v1, final Truth v2, final Truth result) {
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
    public static final Truth deduction(final Truth v1, final Truth v2) {
        final float f1 = v1.getFrequency();
        final float f2 = v2.getFrequency();
        final float c1 = v1.getConfidence();
        final float c2 = v2.getConfidence();
        final float f = and(f1, f2);
        final float c = and(c1, c2, f);
        return new Truth(f, c);
    }

    /**
     * {M, <M ==> P>} |- P
     * @param v1 Truth value of the first premise
     * @param reliance Confidence of the second (analytical) premise
     * @return Truth value of the conclusion
     */
    public static final Truth deduction(final Truth v1, final float reliance) {
        final float f1 = v1.getFrequency();
        final float c1 = v1.getConfidence();
        final float c = and(f1, c1, reliance);
        return new Truth(f1, c, true);
    }

    /**
     * {<S ==> M>, <M <=> P>} |- <S ==> P>
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final Truth analogy(final Truth v1, final Truth v2) {
        final float f1 = v1.getFrequency();
        final float f2 = v2.getFrequency();
        final float c1 = v1.getConfidence();
        final float c2 = v2.getConfidence();
        final float f = and(f1, f2);
        final float c = and(c1, c2, f2);
        return new Truth(f, c);
    }

    /**
     * {<S <=> M>, <M <=> P>} |- <S <=> P>
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final Truth resemblance(final Truth v1, final Truth v2) {
        final float f1 = v1.getFrequency();
        final float f2 = v2.getFrequency();
        final float c1 = v1.getConfidence();
        final float c2 = v2.getConfidence();
        final float f = and(f1, f2);
        final float c = and(c1, c2, or(f1, f2));
        return new Truth(f, c);
    }

    /**
     * {<S ==> M>, <P ==> M>} |- <S ==> P>
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final Truth abduction(final Truth v1, final Truth v2) {
        if (v1.getAnalytic() || v2.getAnalytic()) {
            return new Truth(0.5f, 0f);
        }
        final float f1 = v1.getFrequency();
        final float f2 = v2.getFrequency();
        final float c1 = v1.getConfidence();
        final float c2 = v2.getConfidence();
        final float w = and(f2, c1, c2);
        final float c = w2c(w);
        return new Truth(f1, c);
    }

    /**
     * {M, <P ==> M>} |- P
     * @param v1 Truth value of the first premise
     * @param reliance Confidence of the second (analytical) premise
     * @return Truth value of the conclusion
     */
    public static final Truth abduction(final Truth v1, final float reliance) {
        if (v1.getAnalytic()) {
            return new Truth(0.5f, 0f);
        }
        final float f1 = v1.getFrequency();
        final float c1 = v1.getConfidence();
        final float w = and(c1, reliance);
        final float c = w2c(w);
        return new Truth(f1, c, true);
    }

    /**
     * {<M ==> S>, <M ==> P>} |- <S ==> P>
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final Truth induction(final Truth v1, final Truth v2) {
        return abduction(v2, v1);
    }

    /**
     * {<M ==> S>, <P ==> M>} |- <S ==> P>
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final Truth exemplification(final Truth v1, final Truth v2) {
        if (v1.getAnalytic() || v2.getAnalytic()) {
            return new Truth(0.5f, 0f);
        }
        final float f1 = v1.getFrequency();
        final float f2 = v2.getFrequency();
        final float c1 = v1.getConfidence();
        final float c2 = v2.getConfidence();
        final float w = and(f1, f2, c1, c2);
        final float c = w2c(w);
        return new Truth(1, c);
    }

    /**
     * {<M ==> S>, <M ==> P>} |- <S <=> P>
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final Truth comparison(final Truth v1, final Truth v2) {
        final float f1 = v1.getFrequency();
        final float f2 = v2.getFrequency();
        final float c1 = v1.getConfidence();
        final float c2 = v2.getConfidence();
        final float f0 = or(f1, f2);
        final float f = (f0 == 0) ? 0 : (and(f1, f2) / f0);
        final float w = and(f0, c1, c2);
        final float c = w2c(w);
        return new Truth(f, c);
    }

    /* ----- desire-value functions, called in SyllogisticRules ----- */
    /**
     * A function specially designed for desire value [To be refined]
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final Truth desireStrong(final Truth v1, final Truth v2) {
        final float f1 = v1.getFrequency();
        final float f2 = v2.getFrequency();
        final float c1 = v1.getConfidence();
        final float c2 = v2.getConfidence();
        final float f = and(f1, f2);
        final float c = and(c1, c2, f2);
        return new Truth(f, c);
    }

    /**
     * A function specially designed for desire value [To be refined]
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final Truth desireWeak(final Truth v1, final Truth v2) {
        final float f1 = v1.getFrequency();
        final float f2 = v2.getFrequency();
        final float c1 = v1.getConfidence();
        final float c2 = v2.getConfidence();
        final float f = and(f1, f2);
        final float c = and(c1, c2, f2, w2c(1.0f));
        return new Truth(f, c);
    }

    /**
     * A function specially designed for desire value [To be refined]
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final Truth desireDed(final Truth v1, final Truth v2) {
        final float f1 = v1.getFrequency();
        final float f2 = v2.getFrequency();
        final float c1 = v1.getConfidence();
        final float c2 = v2.getConfidence();
        final float f = and(f1, f2);
        final float c = and(c1, c2);
        return new Truth(f, c);
    }

    /**
     * A function specially designed for desire value [To be refined]
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final Truth desireInd(final Truth v1, final Truth v2) {
        final float f1 = v1.getFrequency();
        final float f2 = v2.getFrequency();
        final float c1 = v1.getConfidence();
        final float c2 = v2.getConfidence();
        final float w = and(f2, c1, c2);
        final float c = w2c(w);
        return new Truth(f1, c);
    }

    /* ----- double argument functions, called in CompositionalRules ----- */
    /**
     * {<M --> S>, <M <-> P>} |- <M --> (S|P)>
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final Truth union(final Truth v1, final Truth v2) {
        final float f1 = v1.getFrequency();
        final float f2 = v2.getFrequency();
        final float c1 = v1.getConfidence();
        final float c2 = v2.getConfidence();
        final float f = or(f1, f2);
        final float c = and(c1, c2);
        return new Truth(f, c);
    }

    /**
     * {<M --> S>, <M <-> P>} |- <M --> (S&P)>
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final Truth intersection(final Truth v1, final Truth v2) {
        final float f1 = v1.getFrequency();
        final float f2 = v2.getFrequency();
        final float c1 = v1.getConfidence();
        final float c2 = v2.getConfidence();
        final float f = and(f1, f2);
        final float c = and(c1, c2);
        return new Truth(f, c);
    }

    /**
     * {(||, A, B), (--, B)} |- A
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    static final Truth reduceDisjunction(final Truth v1, final Truth v2) {
        final Truth v0 = intersection(v1, negation(v2));
        return deduction(v0, 1f);
    }

    /**
     * {(--, (&&, A, B)), B} |- (--, A)
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final Truth reduceConjunction(final Truth v1, final Truth v2) {
        final Truth v0 = intersection(negation(v1), v2);
        return negation(deduction(v0, 1f));
    }

    /**
     * {(--, (&&, A, (--, B))), (--, B)} |- (--, A)
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final Truth reduceConjunctionNeg(final Truth v1, final Truth v2) {
        return reduceConjunction(v1, negation(v2));
    }

    /**
     * {(&&, <#x() ==> M>, <#x() ==> P>), S ==> M} |- <S ==> P>
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final Truth anonymousAnalogy(final Truth v1, final Truth v2) {
        final float f1 = v1.getFrequency();
        final float c1 = v1.getConfidence();
        final Truth v0 = new Truth(f1, w2c(c1));
        return analogy(v2, v0);
    }
    
    
    /** functions the same as TruthValue, but being a separate class,
     *  indicates it was the result of eternalization */
    public static final class EternalizedTruthValue extends Truth {
        public EternalizedTruthValue(final float f, final float c) {
            super(f, c);
        }        
    }
    
    /**
     * From one moment to eternal
     * @param v1 Truth value of the premise
     * @return Truth value of the conclusion
     */
    public static final EternalizedTruthValue eternalize(final Truth v1) {
        return new EternalizedTruthValue(v1.getFrequency(), eternalizedConfidence(v1.getConfidence()));
    }
    public static final float eternalizedConfidence(float conf) {
        return w2c(conf);
    }
    
    public static final float temporalProjection(final long sourceTime, final long targetTime, final long currentTime) {
        return abs(sourceTime - targetTime) / (float) (abs(sourceTime - currentTime) + abs(targetTime - currentTime));
    }
}
