package nars.rl;

import nars.Symbols;
import nars.nal.DirectProcess;
import nars.nal.Sentence;
import nars.nal.Task;
import nars.nal.Truth;
import nars.nal.concept.Concept;
import nars.nal.nal5.Implication;
import nars.nal.term.Compound;
import nars.nal.term.Term;
import nars.util.index.ConceptMatrix;
import nars.util.index.ConceptMatrixEntry;

/**
 * Represents a 'row' in the q-matrix
 */
public class QEntry<S extends Term, A extends Term> extends ConceptMatrixEntry<S,A,Implication,QEntry> {

    double dq = 0; //delta-Q; current q = q0 + dq, temporary
    double q0 = 0; //previous Q value, for comparing nar vs. QL influence
    double q = 0; //current q-value
    double e = 1; //eligibility trace

    //TODO modes: average, nar, q
    boolean defaultQMode = true;

    public QEntry(Concept c, ConceptMatrix matrix) {
        super(matrix, c);
    }


    public double getE() {
        return e;
    }

    public double getDQ() {
        return dq;
    }

    public void clearDQ() {

        q0 = q;

        q = q + dq;

        dq = 0;
    }

    public void updateE(final double mult, final double add) {
        e = e * mult + add;
    }

    public void addE(final double add) {
        e += add;
    }

    /**
     * adds a deltaQ divided by E (meaning it must be multiplied by the eligiblity trace before adding to the accumulator
     */
    public void addDQ(final double dq) {

        this.dq += dq;
    }



    /** q according to the concept's best belief / goal & its truth/desire */
    public double getQSentence(char implicationPunctuation) {

        Sentence s = implicationPunctuation == Symbols.GOAL ? concept.getStrongestGoal(true, true) : concept.getStrongestBelief();
        if (s == null) return 0f;

        return getQSentence(s);
    }

    /** gets the Q-value scalar from the best belief or goal of a state=/>action concept */
    public static double getQSentence(Sentence s) {

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

    public double getQ() { return q;    }

    public double getQ(Sentence sentence) {
        return getQSentence(sentence);
    }

    long lastCommit = -1;
    long commitEvery = 0;
    float lastFreq = -1;

    public void commitDirect(Task t) {
        DirectProcess.run(concept.memory, t);
    }

    /** inserts the belief directly into the table */
    public void commitFast(Task t) {
        //concept.beliefs.clear();

        //switch(t.punctuation) ..
        //concept.processJudgment(null, t);
        concept.processGoal(null, t);
    }

    /** input to NAR */
    public void commit(char punctuation, float qUpdateConfidence, float thresh) {

        clearDQ();

        double qq = getQ();
        double nq = qq;
        if (nq > 1d) nq = 1d;
        if (nq < -1d) nq = -1d;

        Term qt = concept.term;
        //System.out.println(qt + " qUpdate: " + Texts.n4(q) + " + " + dq + " -> " + " (" + Texts.n4(nq) + ")");

        float nextFreq = (float)((nq / 2f) + 0.5f);

        long now = concept.memory.time();

        if (lastFreq==-1 ||
                ((now - lastCommit >= commitEvery) && Math.abs(nextFreq - lastFreq) > thresh)) {

            Task t = concept.memory.task((Compound) qt).punctuation(

                    punctuation

            ).present().truth(nextFreq, qUpdateConfidence).get();

            commitDirect(t);

            lastFreq = nextFreq;
            lastCommit = now;
        }

    }


}
