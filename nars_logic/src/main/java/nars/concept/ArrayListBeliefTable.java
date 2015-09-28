package nars.concept;

import nars.Memory;
import nars.NAR;
import nars.premise.Premise;
import nars.process.ConceptProcess;
import nars.task.Task;

import static nars.nal.nal1.LocalRules.revisibleTermsAlreadyEqual;
import static nars.nal.nal1.LocalRules.tryRevision;

/**
 * Stores beliefs ranked in a sorted ArrayList, with strongest beliefs at lowest indexes (first iterated)
 */
public class ArrayListBeliefTable extends ArrayListTaskTable implements BeliefTable {

    private Ranker rank;

    public ArrayListBeliefTable() {
        super();
        rank = null;
    }

    public ArrayListBeliefTable(int cap, Ranker rank) {
        super(cap);
        this.rank = rank;
    }

    @Override
    public final Ranker getRank() {
        return rank;
    }

//    @Override
//    public Task top(boolean hasQueryVar, long now, long occTime, Truth truth) {
//        throw new RuntimeException("not supposed to be called");
//    }


    @Override
    public Task top(final boolean eternal, final boolean temporal) {
        if (isEmpty()) {
            return null;
        } else if (eternal && temporal) {
            return get(0);
        } else if (eternal ^ temporal) {
            final int n = size();
            for (int i = 0; i < n; i++) {
                Task t = get(i);
                if (eternal && t.isEternal()) return t;
                if (temporal && !t.isEternal()) return t;
            }
        }

        return null;
    }


    @Override
    public Task top(Ranker r) {

        float s = Float.NEGATIVE_INFINITY;
        Task b = null;

        final int n = size();
        for (int i = 0; i < n; i++) {
            Task t = get(i);

            float x = r.rank(t, s);
            if (x > s) {
                s = x;
                b = t;
            }
        }

        return b;
    }

//    /**
//     * Select a belief to interact with the given task in logic
//     * <p/>
//     * get the first qualified one
//     * <p/>
//     * only called in RuleTables.rule
//     *
//     * @return The selected isBelief
//     */
////    @Override
//    public Task match(final Task task, long now) {
//        if (isEmpty()) return null;
//
//        long occurrenceTime = task.getOccurrenceTime();
//
//        final int b = size();
//
//        if (task.isEternal()) {
//            Task eternal = top(true, false);
//
//        }
//        else {
//
//        }
//
//        for (final Task belief : this) {
//
//            //if (task.sentence.isEternal() && belief.isEternal()) return belief;
//
//
//            return belief;
//        }
//
//
//        Task projectedBelief = belief.projectTask(occurrenceTime, now);
//
//        //TODO detect this condition before constructing Task
//        if (projectedBelief.getOccurrenceTime()!=belief.getOccurrenceTime()) {
//            //belief = nal.derive(projectedBelief); // return the first satisfying belief
//            return projectedBelief;
//        }
//
//        return null;
//    }

//    @Override
//    public Task project(Task t, long now) {
//        Task closest = topRanked();
//        if (closest == null) return null;
//        return closest.projectTask(t.getOccurrenceTime(), now);
//    }
    @Override
    public Task add(Task input, Ranker ranking, Concept c, Premise nal) {

        Task revised = input;

        final Memory memory = c.getMemory();


        long now = memory.time();

        if (isEmpty()) {
            add(input);
            return input;
        } else {

            if (ranking == null) {
                //just return thie top item if no ranker is provided
                return top();
            }


            Task existing = top(input, now);


            if (existing != null) {

                //equal instance, or equal truth and stamp:
                if ((existing == input) || input.equivalentTo(existing, false, false, true, true, false)) {

                        /*if (!t.isInput() && t.isJudgment()) {
                            existing.decPriority(0);    // duplicated task
                        }   // else: activated belief*/

                    if (input != existing)
                        memory.remove(input, "Ineffectual"); //"has no effect" on belief/desire, etc

                    return null;

                } else if (revisibleTermsAlreadyEqual(input, existing)) {


                    if (nal != null) {
                        revised = tryRevision(input, existing, false, nal);
                        if (revised != null) {
                            if (nal instanceof ConceptProcess) {
                                ((ConceptProcess) nal).setBelief(revised);
                            }
                        }
                        if (revised == null) revised = input; /* set to original value */
                    }

                }

            }

            add(input, ranking, memory);

            if (!input.equals(revised)) {
                addRevised(revised, ranking, nal.nar());
            }


            return revised;
        }


//        if (size()!=preSize)
//            c.onTableUpdated(goalOrJudgment.getPunctuation(), preSize);
//
//        if (removed != null) {
//            if (removed == goalOrJudgment) return false;
//
//            m.emit(eventRemove, this, removed.sentence, goalOrJudgment.sentence);
//
//            if (preSize != table.size()) {
//                m.emit(eventAdd, this, goalOrJudgment.sentence);
//            }
//        }

        //the new task was not added, so remove it


    }

    private void addRevised(Task revised, Ranker r, NAR nar) {
        /** insert into array table */
        add(revised, r, nar.memory);

        /** input derived task */
        nar.input(revised);
        //ALTERNATELY: TaskProcess.run(nar, revised);

    }

    private boolean add(Task input, Ranker r, Memory memory) {
        float rankInput = r.rank(input);    // for the new isBelief


        final int siz = size();

        boolean atCapacity = (cap == siz);

        int i;
        for (i = 0; i < siz; i++) {
            Task b = get(i);
            float existingRank = r.rank(b, rankInput);
            boolean inputGreater = (Float.isNaN(existingRank) && rankInput >= existingRank);
            if (inputGreater) {
                //item will be inserted at this index
                break;
            }
        }

        if (atCapacity) {
            if (i == siz) {
                //reached the end of the list and there is no room to add at the end
                memory.remove(input, "Unbelievable/Undesirable");
                return true;
            } else {
                Task removed = remove(siz - 1);
                memory.remove(removed, "Forgotten");
                add(i, input);
            }
        } else {
            add(i, input);
        }
        return false;
    }

//TODO provide a projected belief
//
//
//
//        //first create a projected
//
//
//        /*if (t.sentence == belief.sentence) {
//            return false;
//        }*/
//
//        if (belief.sentence.equalStamp(t.sentence, true, false, true)) {
////                if (task.getParentTask() != null && task.getParentTask().sentence.isJudgment()) {
////                    //task.budget.decPriority(0);    // duplicated task
////                }   // else: activated belief
//
//            getMemory().removed(belief, "Duplicated");
//            return false;
//        } else if (revisible(belief.sentence, t.sentence)) {
//            //final long now = getMemory().time();
//
////                if (nal.setTheNewStamp( //temporarily removed
////                /*
////                if (equalBases(first.getBase(), second.getBase())) {
////                return null;  // do not merge identical bases
////                }
////                 */
////                //        if (first.baseLength() > second.baseLength()) {
////                new Stamp(newStamp, oldStamp, memory.time()) // keep the order for projection
////                //        } else {
////                //            return new Stamp(second, first, time);
////                //        }
////                ) != null) {
//
//            //TaskSeed projectedBelief = t.projection(nal.memory, now, task.getOccurrenceTime());
//
//
//            //Task r = t.projection(nal.memory, now, newBelief.getOccurrenceTime());
//
//            //Truth r = t.projection(now, newBelief.getOccurrenceTime());
//                /*
//                if (projectedBelief.getOccurrenceTime()!=t.getOccurrenceTime()) {
//                }
//                */
//
//
//
//            Task revised = tryRevision(belief, t, false, nal);
//            if (revised != null) {
//                belief = revised;
//                nal.setCurrentBelief(revised);
//            }
//
//        }
//

//        if (!addToTable(belief, getBeliefs(), getMemory().param.conceptBeliefsMax.get(), Events.ConceptBeliefAdd.class, Events.ConceptBeliefRemove.class)) {
//            //wasnt added to table
//            getMemory().removed(belief, "Insufficient Rank"); //irrelevant
//            return false;
//        }
//    }

//    @Override
//    public Task addGoal(Task goal, Concept c) {
//        if (goal.equalStamp(t, true, true, false)) {
//            return false; // duplicate
//        }
//
//        if (revisible(goal.sentence, oldGoal)) {
//
//            //nal.setTheNewStamp(newStamp, oldStamp, memory.time());
//
//
//            //Truth projectedTruth = oldGoal.projection(now, task.getOccurrenceTime());
//                /*if (projectedGoal!=null)*/
//            {
//                // if (goal.after(oldGoal, nal.memory.param.duration.get())) { //no need to project the old goal, it will be projected if selected anyway now
//                // nal.singlePremiseTask(projectedGoal, task.budget);
//                //return;
//                // }
//                //nal.setCurrentBelief(projectedGoal);
//
//                Task revisedTask = tryRevision(goal, oldGoalT, false, nal);
//                if (revisedTask != null) { // it is revised, so there is a new task for which this function will be called
//                    goal = revisedTask;
//                    //return true; // with higher/lower desire
//                } //it is not allowed to go on directly due to decision making https://groups.google.com/forum/#!topic/open-nars/lQD0no2ovx4
//
//                //nal.setCurrentBelief(revisedTask);
//            }
//        }
//    }


    //    public static float rankBeliefConfidence(final Sentence judg) {
//        return judg.getTruth().getConfidence();
//    }
//
//    public static float rankBeliefOriginal(final Sentence judg) {
//        final float confidence = judg.truth.getConfidence();
//        final float originality = judg.getOriginality();
//        return or(confidence, originality);
//    }


//    boolean addToTable(final Task goalOrJudgment, final List<Task> table, final int max, final Class eventAdd, final Class eventRemove, Concept c) {
//        int preSize = table.size();
//
//        final Memory m = c.getMemory();
//
//        Task removed = addToTable(goalOrJudgment, table, max, c);
//
//        if (size()!=preSize)
//            c.onTableUpdated(goalOrJudgment.getPunctuation(), preSize);
//
//        if (removed != null) {
//            if (removed == goalOrJudgment) return false;
//
//            m.emit(eventRemove, this, removed.sentence, goalOrJudgment.sentence);
//
//            if (preSize != table.size()) {
//                m.emit(eventAdd, this, goalOrJudgment.sentence);
//            }
//        }
//
//        return true;
//    }


}
