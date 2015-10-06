package nars.task;

import nars.budget.Budget;
import nars.nal.nal1.Inheritance;
import nars.premise.Premise;
import nars.task.stamp.Stamp;
import nars.term.Atom;
import nars.term.Compound;
import nars.term.Term;
import nars.truth.Truth;

/** dummy task useful for recording the known components of an aborted derivation */
public class PreTask extends DefaultTask {

    //HACK wrap the non-compound in a compound to form a task
    public PreTask(Term term, char punctuation, Truth truth, Budget b, long occurr, Premise reason) {
        super(term instanceof Compound ? ((Compound)term) :
                Inheritance.make(term, Atom.the("NON_COMPOUND")),
                punctuation, truth, b, reason.getTask(), reason.getBelief(), null);

        if (occurr!= Stamp.TIMELESS)
            setOccurrenceTime(occurr);

    }

}
