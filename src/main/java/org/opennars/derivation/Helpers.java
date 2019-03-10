package org.opennars.derivation;

import org.opennars.inference.TemporalRules;
import org.opennars.language.Conjunction;
import org.opennars.language.Implication;
import org.opennars.language.Term;

public class Helpers {
    public static Term convertFromSeqToSeqImpl(Term term) {
        if (!(term instanceof Conjunction) || ((Conjunction) term).temporalOrder != TemporalRules.ORDER_FORWARD) {
            return null;
        }

        Conjunction conj = (Conjunction)term;

        Term[] implSeq = new Term[conj.term.length-1];
        for(int i=0;i<conj.term.length-1;i++) {
            implSeq[i] = conj.term[i];
        }

        Term lastTerm = conj.term[conj.term.length-1];

        return Implication.make(Conjunction.make(implSeq, TemporalRules.ORDER_FORWARD), lastTerm, TemporalRules.ORDER_FORWARD);
    }
}
