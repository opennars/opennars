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
import nars.task.stamp.Stamp;

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
    public static final Truth conversion(final Truth t) {
        final float f1 = t.getFrequency();
        final float c1 = t.getConfidence();
        final float w = and(f1, c1);
        final float c = w2c(w);
        return new DefaultTruth(1, c);
    }

    /* ----- Single argument functions, called in StructuralRules ----- */
    /**
     * {A} |- (--A)
     * @param t Truth value of the premise
     * @return Truth value of the conclusion
     */
    public static final Truth negation(final Truth v1) {
        final float f = 1f - v1.getFrequency();
        final float c = v1.getConfidence();
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
    public static final Truth contraposition(final Truth v1) {
        final float f1 = v1.getFrequency();
        final float c1 = v1.getConfidence();
        final float w = and(1 - f1, c1);
        final float c = w2c(w);
        return new DefaultTruth(0, c);
    }

    /* ----- double argument functions, called in MatchingRules ----- */
    /**
     * {<S ==> P>, <S ==> P>} |- <S ==> P>
     * @param a Truth value of the first premise
     * @param b Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final Truth revision(final Truth a, final Truth b) {
        return revision(a, b, new DefaultTruth(0, 0));
    }

    static final Truth revision(final Truth a, final Truth b, final Truth result) {
        final float f1 = a.getFrequency();
        final float f2 = b.getFrequency();
        final float w1 = c2w(a.getConfidence());
        final float w2 = c2w(b.getConfidence());
        final float w = w1 + w2;
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
    public static final AnalyticTruth deduction(final Truth a, final float reliance) {
        final float f = a.getFrequency();
        final float c = and(f, a.getConfidence(), reliance);
        return new AnalyticTruth(f, c);
    }
        /* ----- double argument functions, called in SyllogisticRules ----- */
    /**
     * {<S ==> M>, <M ==> P>} |- <S ==> P>
     * @param a Truth value of the first premise
     * @param b Truth value of the second premise
     * @return (non-Analytic) Truth value of the conclusion - normal truth because this is based on 2 premises
     */
    public static final Truth deduction(final Truth a, final Truth b) {
        final float f = and(a.getFrequency(), b.getFrequency());
        final float c = and(f, a.getConfidence(), b.getConfidence());
        return new DefaultTruth(f, c);
    }

    /**
     * {<S ==> M>, <M <=> P>} |- <S ==> P>
     * @param a Truth value of the first premise
     * @param b Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final Truth analogy(final Truth a, final Truth b) {
        final float f1 = a.getFrequency();
        final float f2 = b.getFrequency();
        final float c1 = a.getConfidence();
        final float c2 = b.getConfidence();
        final float f = and(f1, f2);
        final float c = and(c1, c2, f2);
        return new DefaultTruth(f, c);
    }

    /**
     * {<S <=> M>, <M <=> P>} |- <S <=> P>
     * @param a Truth value of the first premise
     * @param b Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final Truth resemblance(final Truth a, final Truth b) {
        final float f1 = a.getFrequency();
        final float f2 = b.getFrequency();
        final float c1 = a.getConfidence();
        final float c2 = b.getConfidence();
        final float f = and(f1, f2);
        final float c = and(c1, c2, or(f1, f2));
        return new DefaultTruth(f, c);
    }

    /**
     * {<S ==> M>, <P ==> M>} |- <S ==> P>
     * @param a Truth value of the first premise
     * @param b Truth value of the second premise
     * @return Truth value of the conclusion, or null if either truth is analytic already
     */
    public static final Truth abduction(final Truth a, final Truth b) {
        if (a.isAnalytic() || b.isAnalytic()) {
            return null;
        }
        final float f1 = a.getFrequency();
        final float f2 = b.getFrequency();
        final float c1 = a.getConfidence();
        final float c2 = b.getConfidence();
        final float w = and(f2, c1, c2);
        final float c = w2c(w);
        return new DefaultTruth(f1, c);
    }

    /**
     * {M, <P ==> M>} |- P
     * @param t Truth value of the first premise
     * @param reliance Confidence of the second (analytical) premise
     * @return Truth value of the conclusion
     */
    public static final AnalyticTruth abduction(final Truth t, final float reliance) {
        if (t.isAnalytic()) {
            return null;
        }
        final float f1 = t.getFrequency();
        final float c1 = t.getConfidence();
        final float w = and(c1, reliance);
        final float c = w2c(w);
        return new AnalyticTruth(f1, c);
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
     * @param a Truth value of the first premise
     * @param b Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final Truth exemplification(final Truth a, final Truth b) {
        if (a.isAnalytic() || b.isAnalytic()) {
            return null;
        }
        final float f1 = a.getFrequency();
        final float f2 = b.getFrequency();
        final float c1 = a.getConfidence();
        final float c2 = b.getConfidence();
        final float w = and(f1, f2, c1, c2);
        final float c = w2c(w);
        return new DefaultTruth(1, c);
    }

    /**
     * {<M ==> S>, <M ==> P>} |- <S <=> P>
     * @param a Truth value of the first premise
     * @param b Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final Truth comparison(final Truth a, final Truth b) {
        final float f1 = a.getFrequency();
        final float f2 = b.getFrequency();
        final float c1 = a.getConfidence();
        final float c2 = b.getConfidence();
        final float f0 = or(f1, f2);
        final float f = (f0 == 0) ? 0 : (and(f1, f2) / f0);
        final float w = and(f0, c1, c2);
        final float c = w2c(w);
        return new DefaultTruth(f, c);
    }

    /* ----- desire-value functions, called in SyllogisticRules ----- */
    /**
     * A function specially designed for desire value [To be refined]
     * @param a Truth value of the first premise
     * @param b Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final Truth desireStrong(final Truth a, final Truth b) {
        final float f1 = a.getFrequency();
        final float f2 = b.getFrequency();
        final float c1 = a.getConfidence();
        final float c2 = b.getConfidence();
        final float f = and(f1, f2);
        final float c = and(c1, c2, f2);
        return new DefaultTruth(f, c);
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
        return new DefaultTruth(f, c);
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
        return new DefaultTruth(f, c);
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
        return new DefaultTruth(f1, c);
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
        return new DefaultTruth(f, c);
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
        return new DefaultTruth(f, c);
    }

    /**
     * {(||, A, B), (--, B)} |- A
     * @param a Truth value of the first premise
     * @param b Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final Truth reduceDisjunction(final Truth a, final Truth b) {
        return deduction(intersection(a, negation(b)), 1f);
    }

    /**
     * {(--, (&&, A, B)), B} |- (--, A)
     * @param a Truth value of the first premise
     * @param b Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final Truth reduceConjunction(final Truth v1, final Truth v2) {
        final Truth v0 = intersection(negation(v1), v2);
        return negation(deduction(v0, 1f));

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
    public static final Truth reduceConjunctionNeg(final Truth a, final Truth b) {
        return reduceConjunction(a, negation(b));
    }

    /**
     * {(&&, <#x() ==> M>, <#x() ==> P>), S ==> M} |- <S ==> P>
     * @param a Truth value of the first premise
     * @param b Truth value of the second premise
     * @return Truth value of the conclusion
     */
    public static final Truth anonymousAnalogy(final Truth a, final Truth b) {
        final float f1 = a.getFrequency();
        final float c1 = a.getConfidence();
        final Truth v0 = new DefaultTruth(f1, w2c(c1));
        return analogy(b, v0);
    }

    public static final Truth decomposePositiveNegativeNegative(final Truth a, final Truth b) {
        final float f1 = a.getFrequency();
        final float c1 = a.getConfidence();
        final float f2 = b.getFrequency();
        final float c2 = b.getConfidence();

        final float fn = and(f1,1-f2);
        return new DefaultTruth(1-fn, and(fn,c1,c2));
    }

    public static final Truth decomposeNegativePositivePositive(final Truth a, final Truth b) {
        final float f1 = a.getFrequency();
        final float c1 = a.getConfidence();
        final float f2 = b.getFrequency();
        final float c2 = b.getConfidence();

        final float f = and((1-f1),f2);
        return new DefaultTruth(f, and(f,c1,c2));
    }

    public static final Truth decomposePositiveNegativePositive(final Truth a, final Truth b) {
        final float f1 = a.getFrequency();
        final float c1 = a.getConfidence();
        final float f2 = b.getFrequency();
        final float c2 = b.getConfidence();

        final float f = and(f1,(1-f2));
        return new DefaultTruth(f, and(f,c1,c2));
    }

    public static final Truth decomposeNegativeNegativeNegative(final Truth a, final Truth b) {
        final float f1 = a.getFrequency();
        final float c1 = a.getConfidence();
        final float f2 = b.getFrequency();
        final float c2 = b.getConfidence();

        final float fn = and((1-f1),(1-f2));
        return new DefaultTruth(1-fn, and(fn,c1,c2));
    }

    public static final Truth difference(final Truth a, final Truth b) {
        final float f1 = a.getFrequency();
        final float c1 = a.getConfidence();
        final float f2 = b.getFrequency();
        final float c2 = b.getConfidence();

        return new DefaultTruth(and(f1,(1-f2)), and(c1,c2));
    }

    /**
     * From one moment to eternal
     * @param t Truth value of the premise
     * @return Truth value of the conclusion
     */
    public static final ProjectedTruth eternalize(final Truth t) {
        return new ProjectedTruth(
                t.getFrequency(),
                eternalizedConfidence(t.getConfidence()),
                Stamp.ETERNAL
        );
    }
    public static final float eternalizedConfidence(float conf) {
        return w2c(conf);
    }
    
    public static final float temporalProjection(final long sourceTime, final long targetTime, final long currentTime) {
        return abs(sourceTime - targetTime) / (float) (abs(sourceTime - currentTime) + abs(targetTime - currentTime));
    }
}
