package nars.util.attention;

import nars.Global;
import nars.Memory;
import nars.bag.Bag;
import nars.concept.Concept;
import nars.term.Term;
import nars.util.event.Active;

/**
 iteratively processes a concept bag and the bags of fired concepts to improve the accuracy of budget dynamics
 */
public class BagForgettingEnhancer extends Active {


    public BagForgettingEnhancer(Memory memory, Bag<Term, Concept> bag) {

        super(
                memory.eventConceptProcess.on(cp -> {

                    Concept c = cp.getConcept();

                    if (Global.TERMLINK_FORGETTING_EXTRA_DEPTH > 0)
                        c.getTermLinks().forgetNext(
                                memory.termLinkForgetDurations,
                                Global.TERMLINK_FORGETTING_EXTRA_DEPTH,
                                memory);


                    if (Global.TASKLINK_FORGETTING_EXTRA_DEPTH > 0)
                        c.getTaskLinks().forgetNext(
                                memory.taskLinkForgetDurations,
                                Global.TASKLINK_FORGETTING_EXTRA_DEPTH,
                                memory);

                }),

                memory.eventCycleEnd.on(mm -> {

                    bag.forgetNext(
                            memory.conceptForgetDurations,
                            Global.CONCEPT_FORGETTING_EXTRA_DEPTH,
                            memory);

                })
        );

    }


}
