package nars.rl;

import nars.Symbols;
import nars.nal.Sentence;
import nars.nal.Truth;
import nars.nal.concept.Concept;
import nars.nal.nal5.Implication;
import nars.nal.term.Term;
import nars.util.index.ConceptMatrix;
import nars.util.index.ConceptMatrixEntry;

/**
 * Represents a 'row' in the q-matrix
 */
public class QEntry<S extends Term, A extends Term> extends ConceptMatrixEntry<S,A,Implication,QEntry> {

    double dq = 0; //delta-Q; current q = q0 + dq, temporary
    double q0 = 0; //previous Q value, for comparing nar vs. QL influence

    double e = 0; //eligibility trace

    public QEntry(Concept c, ConceptMatrix matrix) {
        super(matrix, c);
    }


    public double getE() {
        return e;
    }

    public double getDQ() {
        return dq;
    }

    public double clearDQ(final double thresh) {
        if (Math.abs(dq) < thresh) {
            //just keep accumulating for next cycles
            return 0;
        }

        double d = dq;
        dq = 0;
        return d;
    }

    public void updateE(final double mult, final double add) {
        e = e * mult + add;
    }

    /**
     * adds a deltaQ divided by E (meaning it must be multiplied by the eligiblity trace before adding to the accumulator
     */
    public void addDQ(final double dqDivE) {
        dq += dqDivE * e;
    }


    public double commit() {
        double nextQ = q0 + dq;

        dq = 0;
        return q0 = nextQ;
    }

    /** q according to the concept's best belief / goal & its truth/desire */
    public double getQNar(char implicationPunctuation) {


        Sentence s = implicationPunctuation == Symbols.GOAL ? concept.getStrongestGoal(true, true) : concept.getStrongestBelief();
        if (s == null) return 0f;

        return getQNar(s);
    }

    /** gets the Q-value scalar from the best belief or goal of a state=/>action concept */
    public static double getQNar(Sentence s) {

        Truth t = s.truth;
        if (t == null) return 0f;

        //TODO try expectation

        return ((t.getFrequency() - 0.5f) * 2.0f); // (t.getFrequency() - 0.5f) * 2f * t.getConfidence();
    }

    /** current delta */
    public double getDq() {
        return dq;
    }

    /** previous q-value */
    public double getQ0() {
        return q0;
    }

    public double getQ() {
        return q0 + dq;
    }
}
