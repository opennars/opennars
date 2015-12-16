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
package nars.truth;

import nars.nal.UtilityFunctions;

import static java.lang.Math.abs;

/**
 * All truth-value (and desire-value) functions used in logic rules
 */
public final class TruthFunctions extends UtilityFunctions {

    /* ----- Single argument functions, called in MatchingRules ----- */
    /**
     * {<A ==> B>} |- <B ==> A>
     * @param t Truth value of the premise
     * @return Truth value of the conclusion
     */
    public static Truth conversion(Truth t) {
        float f1 = t.getFrequency();
        float c1 = t.getConfidence();
        float w = and(f1, c1);
        float c = w2c(w);
        return new DefaultTruth(1, c);
    }

    /* ----- Single argument functions, called in StructuralRules ----- */
    /**
     * {A} |- (--A)
     * @param t Truth value of the premise
     * @return Truth value of the conclusion
     */
    public static Truth negation(Truth v1) {
        float f = 1.0f - v1.getFrequency();
        float c = v1.getConfidence();
        return new DefaultTruth(f, c);

        /*

        if (t == null) return null;
        final float f = 1 - t.getFrequency();
        final float c = t.getConfidence();

        if (t.isAnalytic())
            return AnalyticTruth.get(f, c, t); //experimental: for cases where analytic is inverted, to preserve analytic state
        else
            return new DefaultTruth(f, c, t);
            */
    }


    /**
     * {<A ==> B>} |- <(--, B) ==> (--, A)>
     * @param v1 Truth value of the premise
     * @return Truth value of the conclusion
     */
    public static Truth contraposition(Truth v1) {
        float f1 = v1.getFrequency();
        float c1 = v1.getConfidence();
        float w = and(1 - f1, c1);
        float c = w2c(w);
        return new DefaultTruth(0, c);
    }

    /* ----- double argument functions, called in MatchingRules ----- */
    /**
     * {<S ==> P>, <S ==> P>} |- <S ==> P>
     * @param a Truth value of the first premise
     * @param b Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static Truth revision(Truth a, Truth b) {
        return revision(a, b, new DefaultTruth(0, 0));
    }

    static Truth revision(Truth a, Truth b, Truth result) {
        float f1 = a.getFrequency();
        float f2 = b.getFrequency();
        float w1 = c2w(a.getConfidence());
        float w2 = c2w(b.getConfidence());
        float w = w1 + w2;
        return result.set(
                (w1 * f1 + w2 * f2) / w,
                w2c(w)
        );
    }



    /**
     * {M, <M ==> P>} |- P
     * @param a Truth value of the first premise
     * @param reliance Confidence of the second (analytical) premise
     * @return AnalyticTruth value of the conclusion, because it is structural
     */
    public static AnalyticTruth deduction(Truth a, float reliance) {
        float f = a.getFrequency();
        float c = and(f, a.getConfidence(), reliance);
        return new AnalyticTruth(f, c);
    }
        /* ----- double argument functions, called in SyllogisticRules ----- */
    /**
     * {<S ==> M>, <M ==> P>} |- <S ==> P>
     * @param a Truth value of the first premise
     * @param b Truth value of the second premise
     * @return (non-Analytic) Truth value of the conclusion - normal truth because this is based on 2 premises
     */
    public static Truth deduction(Truth a, Truth b) {
        return deduction(a, b.getFrequency(), b.getConfidence());
    }

    /** assumes belief freq=1f */
    public static Truth deduction1(Truth a, float bC) {
        return deduction(a, 1f, bC);
    }

    public static Truth deduction(Truth a, float bF, float bC) {
        float f = and(a.getFrequency(), bF);
        float c = and(f, a.getConfidence(), bC);
        return new DefaultTruth(f, c);
    }

    /**
     * {<S ==> M>, <M <=> P>} |- <S ==> P>
     * @param a Truth value of the first premise
     * @param b Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static Truth analogy(Truth a, Truth b) {
        float f1 = a.getFrequency();
        float f2 = b.getFrequency();
        float c1 = a.getConfidence();
        float c2 = b.getConfidence();
        float f = and(f1, f2);
        float c = and(c1, c2, f2);
        return new DefaultTruth(f, c);
    }

    /**
     * {<S <=> M>, <M <=> P>} |- <S <=> P>
     * @param a Truth value of the first premise
     * @param b Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static Truth resemblance(Truth a, Truth b) {
        float f1 = a.getFrequency();
        float f2 = b.getFrequency();
        float c1 = a.getConfidence();
        float c2 = b.getConfidence();
        float f = and(f1, f2);
        float c = and(c1, c2, or(f1, f2));
        return new DefaultTruth(f, c);
    }

    /**
     * {<S ==> M>, <P ==> M>} |- <S ==> P>
     * @param a Truth value of the first premise
     * @param b Truth value of the second premise
     * @return Truth value of the conclusion, or null if either truth is analytic already
     */
    public static Truth abduction(Truth a, Truth b) {
        if (a.isAnalytic() || b.isAnalytic()) {
            return null;
        }
        float f1 = a.getFrequency();
        float f2 = b.getFrequency();
        float c1 = a.getConfidence();
        float c2 = b.getConfidence();
        float w = and(f2, c1, c2);
        float c = w2c(w);
        return new DefaultTruth(f1, c);
    }

    /**
     * {M, <P ==> M>} |- P
     * @param t Truth value of the first premise
     * @param reliance Confidence of the second (analytical) premise
     * @return Truth value of the conclusion
     */
    public static AnalyticTruth abduction(Truth t, float reliance) {
        if (t.isAnalytic()) {
            return null;
        }
        float f1 = t.getFrequency();
        float c1 = t.getConfidence();
        float w = and(c1, reliance);
        float c = w2c(w);
        return new AnalyticTruth(f1, c);
    }

    /**
     * {<M ==> S>, <M ==> P>} |- <S ==> P>
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static Truth induction(Truth v1, Truth v2) {
        return abduction(v2, v1);
    }

    /**
     * {<M ==> S>, <P ==> M>} |- <S ==> P>
     * @param a Truth value of the first premise
     * @param b Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static Truth exemplification(Truth a, Truth b) {
        if (a.isAnalytic() || b.isAnalytic()) {
            return null;
        }
        float f1 = a.getFrequency();
        float f2 = b.getFrequency();
        float c1 = a.getConfidence();
        float c2 = b.getConfidence();
        float w = and(f1, f2, c1, c2);
        float c = w2c(w);
        return new DefaultTruth(1, c);
    }

    /**
     * {<M ==> S>, <M ==> P>} |- <S <=> P>
     * @param a Truth value of the first premise
     * @param b Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static Truth comparison(Truth a, Truth b) {
        float f1 = a.getFrequency();
        float f2 = b.getFrequency();
        float c1 = a.getConfidence();
        float c2 = b.getConfidence();
        float f0 = or(f1, f2);
        float f = (f0 == 0) ? 0 : (and(f1, f2) / f0);
        float w = and(f0, c1, c2);
        float c = w2c(w);
        return new DefaultTruth(f, c);
    }

    /* ----- desire-value functions, called in SyllogisticRules ----- */
    /**
     * A function specially designed for desire value [To be refined]
     * @param a Truth value of the first premise
     * @param b Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static Truth desireStrong(Truth a, Truth b) {
        float f1 = a.getFrequency();
        float f2 = b.getFrequency();
        float c1 = a.getConfidence();
        float c2 = b.getConfidence();
        float f = and(f1, f2);
        float c = and(c1, c2, f2);
        return new DefaultTruth(f, c);
    }

    /**
     * A function specially designed for desire value [To be refined]
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static Truth desireWeak(Truth v1, Truth v2) {
        float f1 = v1.getFrequency();
        float f2 = v2.getFrequency();
        float c1 = v1.getConfidence();
        float c2 = v2.getConfidence();
        float f = and(f1, f2);
        float c = and(c1, c2, f2, w2c(1.0f));
        return new DefaultTruth(f, c);
    }

    /**
     * A function specially designed for desire value [To be refined]
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static Truth desireDed(Truth v1, Truth v2) {
        float f1 = v1.getFrequency();
        float f2 = v2.getFrequency();
        float c1 = v1.getConfidence();
        float c2 = v2.getConfidence();
        float f = and(f1, f2);
        float c = and(c1, c2);
        return new DefaultTruth(f, c);
    }

    /**
     * A function specially designed for desire value [To be refined]
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static Truth desireInd(Truth v1, Truth v2) {
        float f1 = v1.getFrequency();
        float f2 = v2.getFrequency();
        float c1 = v1.getConfidence();
        float c2 = v2.getConfidence();
        float w = and(f2, c1, c2);
        float c = w2c(w);
        return new DefaultTruth(f1, c);
    }

    /* ----- double argument functions, called in CompositionalRules ----- */
    /**
     * {<M --> S>, <M <-> P>} |- <M --> (S|P)>
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static Truth union(Truth v1, Truth v2) {
        float f1 = v1.getFrequency();
        float f2 = v2.getFrequency();
        float c1 = v1.getConfidence();
        float c2 = v2.getConfidence();
        float f = or(f1, f2);
        float c = and(c1, c2);
        return new DefaultTruth(f, c);
    }

    /**
     * {<M --> S>, <M <-> P>} |- <M --> (S&P)>
     * @param v1 Truth value of the first premise
     * @param v2 Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static Truth intersection(Truth v1, Truth v2) {
        float f1 = v1.getFrequency();
        float f2 = v2.getFrequency();
        float c1 = v1.getConfidence();
        float c2 = v2.getConfidence();
        float f = and(f1, f2);
        float c = and(c1, c2);
        return new DefaultTruth(f, c);
    }

    /**
     * {(||, A, B), (--, B)} |- A
     * @param a Truth value of the first premise
     * @param b Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static Truth reduceDisjunction(Truth a, Truth b) {
        return deduction(intersection(a, negation(b)), 1.0f);
    }

    /**
     * {(--, (&&, A, B)), B} |- (--, A)
     * @param a Truth value of the first premise
     * @param b Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static Truth reduceConjunction(Truth v1, Truth v2) {
        Truth v0 = intersection(negation(v1), v2);
        return negation(deduction(v0, 1.0f));

//        AnalyticTruth x = deduction(
//                intersection(negation(a), b),
//                1f
//        );
//        if (x!=null)
//            return x.negate();
//        else
//            return null;
    }

    /**
     * {(--, (&&, A, (--, B))), (--, B)} |- (--, A)
     * @param a Truth value of the first premise
     * @param b Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static Truth reduceConjunctionNeg(Truth a, Truth b) {
        return reduceConjunction(a, negation(b));
    }

    /**
     * {(&&, <#x() ==> M>, <#x() ==> P>), S ==> M} |- <S ==> P>
     * @param a Truth value of the first premise
     * @param b Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static Truth anonymousAnalogy(Truth a, Truth b) {
        float f1 = a.getFrequency();
        float c1 = a.getConfidence();
        Truth v0 = new DefaultTruth(f1, w2c(c1));
        return analogy(b, v0);
    }

    public static Truth decomposePositiveNegativeNegative(Truth a, Truth b) {
        float f1 = a.getFrequency();
        float c1 = a.getConfidence();
        float f2 = b.getFrequency();
        float c2 = b.getConfidence();

        float fn = and(f1,1-f2);
        return new DefaultTruth(1-fn, and(fn,c1,c2));
    }

    public static Truth decomposeNegativePositivePositive(Truth a, Truth b) {
        float f1 = a.getFrequency();
        float c1 = a.getConfidence();
        float f2 = b.getFrequency();
        float c2 = b.getConfidence();

        float f = and((1-f1),f2);
        return new DefaultTruth(f, and(f,c1,c2));
    }

    public static Truth decomposePositiveNegativePositive(Truth a, Truth b) {
        float f1 = a.getFrequency();
        float c1 = a.getConfidence();
        float f2 = b.getFrequency();
        float c2 = b.getConfidence();

        float f = and(f1,(1-f2));
        return new DefaultTruth(f, and(f,c1,c2));
    }

    public static Truth decomposeNegativeNegativeNegative(Truth a, Truth b) {
        float f1 = a.getFrequency();
        float c1 = a.getConfidence();
        float f2 = b.getFrequency();
        float c2 = b.getConfidence();

        float fn = and((1-f1),(1-f2));
        return new DefaultTruth(1-fn, and(fn,c1,c2));
    }

    public static Truth difference(Truth a, Truth b) {
        float f1 = a.getFrequency();
        float c1 = a.getConfidence();
        float f2 = b.getFrequency();
        float c2 = b.getConfidence();

        return new DefaultTruth(and(f1,(1-f2)), and(c1,c2));
    }

    public static ProjectedTruth eternalize(Truth t) {
        return eternalize(t.getFrequency(), t.getConfidence());
    }

    /**
     * From one moment to eternal
     * @param t Truth value of the premise
     * @return Truth value of the conclusion
     */
    public static ProjectedTruth eternalize(float freq, float conf) {
        return new ProjectedTruth(
                freq,
                eternalizedConfidence(conf)
        );
    }
    public static float eternalizedConfidence(float conf) {
        return w2c(conf);
    }
    
    public static float temporalProjection(long sourceTime, long targetTime, long currentTime) {
        return abs(sourceTime - targetTime) / (float) (abs(sourceTime - currentTime) + abs(targetTime - currentTime));
    }
}
