package nars.nal.concept;

import nars.Memory;
import nars.budget.Bag;
import nars.budget.Budget;
import nars.nal.term.Term;
import nars.nal.tlink.TaskLink;
import nars.nal.tlink.TermLink;
import nars.nal.tlink.TermLinkKey;

/**
 * Created by me on 4/17/15.
 */
public class DefaultConcept extends Concept {

    /**
     * Constructor, called in Memory.getConcept only
     *
     * @param term      A term corresponding to the concept
     * @param b
     * @param taskLinks
     * @param termLinks
     * @param memory    A reference to the memory
     */
    public DefaultConcept(Term term, Budget b, Bag<String, TaskLink> taskLinks, Bag<TermLinkKey, TermLink> termLinks, Memory memory) {
        super(term, b, taskLinks, termLinks, memory);
    }

}
