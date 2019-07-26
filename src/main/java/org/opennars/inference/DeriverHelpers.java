/*
 * The MIT License
 *
 * Copyright 2019 The OpenNARS authors.
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

import org.opennars.entity.Sentence;
import org.opennars.language.*;

/**
 * helper methods for the deriver
 */
public class DeriverHelpers {
    /**
     *
     * @param termCode code of the type of the term, can be copula if it is a statement
     * @param left
     * @param right
     * @return
     */
    public static Term makeBinary(String termCode, Term left, Term right) {
        if (termCode.equals("&/")) {
            return Conjunction.make(new Term[]{left, right}, TemporalRules.ORDER_FORWARD);
        }
        else if (termCode.equals("&|")) {
            return Conjunction.make(new Term[]{left, right}, TemporalRules.ORDER_CONCURRENT);
        }
        else if(termCode.equals("=/>")) {
            return new Implication(new Term[]{left, right}, TemporalRules.ORDER_FORWARD);
        }
        else if(termCode.equals("</>")) {
            return Equivalence.make(left, right, TemporalRules.ORDER_FORWARD);
        }
        else if(termCode.equals("<|>")) {
            return Equivalence.make(left, right, TemporalRules.ORDER_CONCURRENT);
        }
        else if(termCode.equals("=\\>")) {
            return new Implication(new Term[]{left, right}, TemporalRules.ORDER_BACKWARD);
        }
        else if(termCode.equals("=|>")) {
            return new Implication(new Term[]{left, right}, TemporalRules.ORDER_CONCURRENT);
        }
        else {
            throw new RuntimeException("NOT IMPLEMENTED!"); // TODO< chose an exception which we can throw >
        }
    }

    public static Term make(String termCode, Term... args) {
        if (termCode.equals("&/")) {
            return Conjunction.make(args, TemporalRules.ORDER_FORWARD);
        }
        else {
            throw new RuntimeException("NOT IMPLEMENTED!"); // TODO< chose an exception which we can throw >
        }
    }

    /**
     * implements special handling for derivation of =/>
     * @param defaultConclusion is the conclusion without special handling
     * @param subj
     * @param pred
     * @return
     */
    public static Term derivePredImplConclusionTerm(Term defaultConclusion, Sentence subj, Sentence pred) {
        Term conclusionTerm = defaultConclusion;

        if (pred.term instanceof Implication && pred.term.getTemporalOrder() == TemporalRules.ORDER_FORWARD) {
            boolean isPredASeqPredImpl = ((Implication)pred.term).getSubject() instanceof Conjunction && ((Implication)pred.term).getSubject().getTemporalOrder() == TemporalRules.ORDER_FORWARD;
            if (!isPredASeqPredImpl) {
                // has form <(a, +t) =/> b> =/> c

                Term event0 = subj.term;
                long interval = pred.stamp.getOccurrenceTime() - subj.stamp.getOccurrenceTime();
                Term event1 = ((Implication)pred.term).getSubject();
                Term event2 = ((Implication)pred.term).getPredicate();

                Term conj = Conjunction.make(new Term[]{event0, new Interval(interval), event1}, TemporalRules.ORDER_FORWARD);
                Term seq = Implication.make(conj, event2, TemporalRules.ORDER_FORWARD);

                return seq;
            }

            int here2 = 5;
        }




        return conclusionTerm;
    }

    public static Term deriveRetroImplConclusionTerm(Term defaultConclusion, Sentence subj, Sentence pred) {
        Term conclusionTerm = defaultConclusion;

        if (pred.term instanceof Implication && pred.term.getTemporalOrder() == TemporalRules.ORDER_FORWARD) {
            return null; // disallow because it is not legal
        }

        return conclusionTerm;
    }

    // computes the overall interval time of a seq
    public static long calcSeqTime(Term term) {
        if (!(term instanceof Conjunction)) {
            return 0; // time of non-Conjuction is zero
        }

        Conjunction seq = (Conjunction)term;
        assert seq.getTemporalOrder() == TemporalRules.ORDER_FORWARD;

        long intervalSum = 0;
        for(final Term iComponent : seq.term) {
            if (iComponent instanceof Interval) {
                intervalSum += ((Interval)iComponent).time;
            }
        }
        return intervalSum;
    }
}
