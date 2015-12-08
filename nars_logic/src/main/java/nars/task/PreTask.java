package nars.task;

import nars.Premise;
import nars.budget.Budget;
import nars.nal.nal1.Inheritance;
import nars.nal.nal7.Tense;
import nars.term.Term;
import nars.term.atom.Atom;
import nars.term.compound.Compound;
import nars.truth.Truth;

/** dummy task useful for recording the known components of an aborted derivation */
public class PreTask extends AbstractTask {

    //HACK wrap the non-compound in a compound to form a task
    public PreTask(Term term, char punctuation, Truth truth, Budget b, long occurr, Premise reason) {
        super(term instanceof Compound ? ((Compound)term) :
                Inheritance.make(term, Atom.the("NON_COMPOUND")),
                punctuation, truth, b, reason.getTask(), reason.getBelief(), null);

        if (occurr!= Tense.TIMELESS)
            setOccurrenceTime(occurr);

    }

}
