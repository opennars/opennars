//package nars.process;
//
//import nars.Memory;
//import nars.bag.Bag;
//import nars.concept.Concept;
//import nars.term.Term;
//import nars.util.event.Active;
//
///**
// * iteratively processes a concept bag and the bags of fired concepts to improve the accuracy of budget dynamics
// *
// *
// * according to a supplied percentage of additional items (concepts, termlinks, tasklinks, ...etc)
// * which have their priority reduced by forgetting each cycle.
// *
// * forgetting an item can be applied as often
// * as possible since it is governed by rate over time.  however we can
// * afford to update item priority less frequently than every cycle.
// * an accuracy of 1.0 means to process approximatley all concepts every cycle,
// * while an accuracy of 0.5 would mean to process approximately half.
// *
// * since the items selected for update are determined by bag selection,
// * higher priority items will tend to get updated more frequently.
// *
// * an estimate for average "error" due to latency can be calculated
// * in terms of # of items, forgetting rate, and the accuracy rate.
// * more accuracy = lower error because concepts are more likely to receive forget sooner
// *
// * a lower bound on accuracy is when the expected latency exceeds the forgetting time,
// * in which case the forgetting will have been applied some amount of time past
// * when it would have completed its forget descent.
//*/
//public class BagForgettingEnhancer extends Active {
//
//
//    public BagForgettingEnhancer(Memory memory, Bag<Term, Concept> bag, float conceptDepth, float termLinkDepth, float taskLinkDepth) {
//
//        super(
//                memory.eventConceptProcess.on(cp -> {
//
//                    Concept c = cp.getConcept();
//
//
////                    c.getTermLinks().forgetNext(
////                            memory.termLinkForgetDurations,
////                            termLinkDepth,
////                            memory);
////
////
////                    c.getTaskLinks().forgetNext(
////                            memory.taskLinkForgetDurations,
////                            taskLinkDepth,
////                            memory);
//
//                }),
//
//                memory.eventCycleEnd.on(mm -> {
////                    bag.forgetNext(
////                            memory.conceptForgetDurations,
////                            conceptDepth,
////                            memory)
//                })
//        );
//
//    }
//
//
//}
