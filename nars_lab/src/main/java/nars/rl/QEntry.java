package nars.rl;

import nars.nal.concept.Concept;
import nars.util.index.ConceptMatrix;

/**
 * Created by me on 5/2/15.
 */
public class QEntry extends ConceptMatrix.EntryValue {

    double dq = 0; //delta-Q, temporary
    double e = 0; //eligibility trace

    public QEntry(Concept c) {
        super(c);
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

}
