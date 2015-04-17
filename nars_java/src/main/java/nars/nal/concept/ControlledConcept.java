package nars.nal.concept;

import nars.Memory;
import nars.budget.Bag;
import nars.budget.Budget;
import nars.io.Symbols;
import nars.nal.TruthValue;
import nars.nal.stamp.Stamp;
import nars.nal.term.Compound;
import nars.nal.tlink.TaskLink;
import nars.nal.tlink.TermLink;
import nars.nal.tlink.TermLinkKey;

/**
 * Concept which allows control over a singular (max: 1) belief and/or goal state.
 */
public class ControlledConcept extends AxiomaticConcept {

    public ControlledConcept(Compound t, Budget b, Memory m, Bag<TermLinkKey, TermLink> ttaskLinks, Bag<String, TaskLink> ttermLinks) {
        super(t, b, m, ttaskLinks, ttermLinks);
    }

    public void setEternal(TruthValue t, char punctuation, Budget b) {
        set(t, punctuation, memory.time(), Stamp.ETERNAL, b);
    }

    public void setPresent(TruthValue t, char punctuation, Budget b) {
        set(t, punctuation, memory.time(), memory.time(), b);
    }

    public void set(TruthValue t, char punctuation, long creationTime, long occurrenceTime, Budget b) {
        if ((punctuation == Symbols.QUESTION) || (punctuation == Symbols.QUEST))
            throw new RuntimeException("Invalid punctuation: " + punctuation);

        clearAxioms(punctuation == Symbols.JUDGMENT, punctuation == Symbols.GOAL);

        addAxiom(memory.newTask((Compound) getTerm()).
                punctuation(punctuation).truth(t).budget(b).time(creationTime, occurrenceTime).get(), true);
    }
}
