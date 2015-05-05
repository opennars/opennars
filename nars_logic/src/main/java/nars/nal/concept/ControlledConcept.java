package nars.nal.concept;

import nars.Memory;
import nars.bag.Bag;
import nars.budget.Budget;
import nars.Symbols;
import nars.nal.Sentence;
import nars.nal.Task;
import nars.nal.Truth;
import nars.nal.stamp.Stamp;
import nars.nal.term.Compound;
import nars.nal.tlink.TaskLink;
import nars.nal.tlink.TermLink;
import nars.nal.tlink.TermLinkKey;

/**
 * Concept which allows control over a singular (max: 1) belief and/or goal state.
 */
public class ControlledConcept extends AxiomaticConcept {

    public ControlledConcept(Compound t, Budget b, Memory m, Bag<TermLinkKey, TermLink> ttaskLinks, Bag<Sentence, TaskLink> ttermLinks) {
        super(t, b, m, ttaskLinks, ttermLinks);
    }

    public void setEternal(boolean reset, Truth t, char punctuation, Budget b) {
        set(reset, t, punctuation, memory.time(), Stamp.ETERNAL, b);
    }

    public void setPresent(boolean reset, Truth t, char punctuation, Budget b) {
        set(reset, t, punctuation, memory.time(), memory.time(), b);
    }

    public void set(boolean reset, Truth t, char punctuation, long creationTime, long occurrenceTime, Budget b) {
        if ((punctuation == Symbols.QUESTION) || (punctuation == Symbols.QUEST))
            throw new RuntimeException("Invalid punctuation: " + punctuation);

        if (reset)
            clearAxioms(punctuation == Symbols.JUDGMENT, punctuation == Symbols.GOAL);

        Task tt = memory.newTask((Compound) getTerm()).
                punctuation(punctuation).truth(t).budget(b).time(creationTime, occurrenceTime).get();
        addAxiom(tt, true);

    }
}
