package nars.concept;

import nars.Memory;
import nars.nal.nal7.Temporal;
import nars.premise.Premise;
import nars.task.Task;

import static nars.nal.nal1.LocalRules.getRevision;
import static nars.nal.nal1.LocalRules.revisible;

/**
 * Stores beliefs ranked in a sorted ArrayList, with strongest beliefs at lowest indexes (first iterated)
 */
public class ArrayListBeliefTable extends ArrayListTaskTable implements BeliefTable {


    /** warning this will create a 0-capacity table,
     * rejecting all attempts at inputs.  either use the
     * other constructor or change capacity after construction. */
    public ArrayListBeliefTable() {
        this(0);
    }

    public ArrayListBeliefTable(int cap) {
        super(cap);
    }



//    @Override
//    public Task top(boolean hasQueryVar, long now, long occTime, Truth truth) {
//        throw new RuntimeException("not supposed to be called");
//    }


    @Override
    public Task top(final boolean eternal, final boolean temporal) {

        if (isEmpty())
            return null;

        final Task[] tasks = getCachedNullTerminatedArray();

        if (eternal && temporal) {
            return tasks[0];
        } else if (eternal ^ temporal) {
            ///final int n = size();

            Task t;
            for (int i = 0; null!=(t = tasks[i++]); ) {
                final boolean tEtern = Temporal.isEternal(t.getOccurrenceTime());
                if (eternal && tEtern) return t;
                if (temporal && !tEtern) return t;
            }
        }

        return null;
    }


    @Override
    public final Task top(Ranker r) {

        float s = Float.NEGATIVE_INFINITY;
        Task b = null;

        final Task[] tasks = getCachedNullTerminatedArray();

        Task t;
        for (int i = 0; null!=(t = tasks[i++]); ) {
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


    /**
     * merges an input task with this belief table.
     * ordinarily this should never return null.
     * it will return the best matching old or new (input or
     * revised here) belief corresponding to the input.
     *
     * the input will either be added or not depending
     * on its relation to the table's contents.
     *
     * @param input
     * @param ranking
     * @param c
     * @param nal
     * @return
     */
    @Override
    public Task add(final Task input, BeliefTable.Ranker ranking, Concept c, Premise nal) {

        /**
         * involves 3 potentially unique tasks:
         * input, strongest, revised (created here and returned)
         */

        final Memory memory = c.getMemory();


        long now = memory.time();

        if (isEmpty()) {
            add(input);
            return input;
        }


        boolean added = tryAdd(input, ranking, nal.memory());




//            if (ranking == null) {
//                //just return thie top item if no ranker is provided
//                return table.top();
//            }


        Task top = top(input, now);


        if (!added) {
            return top;
        }

        if (input.isDeleted()) {
            return top;
        }

        if (top != null) {

            if (top == input) {

                //the same task instance existed here already
                //bounce
                return input;

            } else if (input.equivalentTo(top, false, false, true, true, false)) {
                //equal but different instances; discard the new one

                /*if (!t.isInput() && t.isJudgment()) {
                    strongest.decPriority(0);    // duplicated task
                }   // else: activated belief*/


                //activate the strongest belief?
                //strongest.getBudget().mergePlus( input.getBudget() );

                //removing like this seems that it causes problems elsewhere
                //memory.remove(input, "Duplicate Existed"); //"has no effect" on belief/desire, etc

                nal.updateBelief(top);

                return top;
            }

            if (revisible(input, top)) {


                Task revised = getRevision(input, top, false, nal);
                if (revised != null && !input.equals(revised)) {

                    nal.updateBelief(revised);


                    /*boolean addedRevised =
                        tryAdd(revised, ranking, nal.memory());*/
                    //if (addedRevised) {

                        //input the new task to memory here?
                        nal.memory().eventDerived.emit(revised);
                        //nal.nar().input(revised);
                    //}

                    return revised;
                }

            }

        }

        /** choose between strongest and input (may be the same) */
        return (top !=null) ? top : input;
    }



    @Override public final boolean tryAdd(Task input, Ranker r, Memory memory) {

        float rankInput = r.rank(input);    // for the new isBelief


        final int siz = data.size();

        boolean atCapacity = (capacity == siz);


        //if (Global.DEBUG) { //seems necessary?
        handleDeleted();
        //}

        final Task[] tasks = getCachedNullTerminatedArray();

        int i = 0;
        if (tasks!=null) {

            for (Task b; null != (b = tasks[i++]); ) {
                if (b == input) return false;

                if (b.equals(input)) {
                    //these should be preventable earlier
                    memory.remove(input, "Duplicate");
                    return false;
                }

                float existingRank = r.rank(b, rankInput);
                boolean inputGreater = (Float.isNaN(existingRank) || rankInput > existingRank);
                if (inputGreater) {
                    //item will be inserted at this index
                    break;
                }
            }
            i--; //-1 is correct since after the above for loop it will be 1 ahead
        }


        if (atCapacity) {
            if (i == siz) {
                //reached the end of the list and there is no room to add at the end
                memory.remove(input, "Unbelievable/Undesirable");
                return false;
            } else {
                Task removed = remove(siz - 1);
                memory.remove(removed, "Forgotten");
                add(i, input);
                return true;
            }
        } else {
            add(i, input);
            return true;
        }
    }

    private void handleDeleted() {
        if (data == null) return;

        for (int i = 0; i < data.size(); i++) {
            Task dt = data.get(i);
            if (dt == null)
                throw new RuntimeException("wtf");
            if (dt.isDeleted()) {
                //throw new RuntimeException("deleted tasks should not be present in belief tables: " + dt);
                remove(i);
                i--;
            }
        }
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
