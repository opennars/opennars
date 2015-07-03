package nars.concept;

import nars.Global;
import nars.Memory;
import nars.process.NAL;
import nars.task.Sentence;
import nars.task.Task;
import nars.term.Compound;
import nars.truth.Truth;

import java.util.List;

import static nars.nal.UtilityFunctions.or;
import static nars.nal.nal1.LocalRules.revisible;
import static nars.nal.nal1.LocalRules.tryRevision;

/**
 * Created by me on 7/2/15.
 */
public class ArrayListBeliefTable extends ArrayListTaskTable implements BeliefTable {

    public ArrayListBeliefTable(int cap) {
        super(cap);
    }

    public Task add(Task input, Concept c) {



        /*if (input.sentence == belief.sentence) {
            return false;
        }*/

        if (belief.sentence.equalStamp(input.sentence, true, false, true)) {
//                if (task.getParentTask() != null && task.getParentTask().sentence.isJudgment()) {
//                    //task.budget.decPriority(0);    // duplicated task
//                }   // else: activated belief

            getMemory().removed(belief, "Duplicated");
            return false;
        } else if (revisible(belief.sentence, input.sentence)) {
            //final long now = getMemory().time();

//                if (nal.setTheNewStamp( //temporarily removed
//                /*
//                if (equalBases(first.getBase(), second.getBase())) {
//                return null;  // do not merge identical bases
//                }
//                 */
//                //        if (first.baseLength() > second.baseLength()) {
//                new Stamp(newStamp, oldStamp, memory.time()) // keep the order for projection
//                //        } else {
//                //            return new Stamp(second, first, time);
//                //        }
//                ) != null) {

            //TaskSeed projectedBelief = input.projection(nal.memory, now, task.getOccurrenceTime());


            //Task r = input.projection(nal.memory, now, newBelief.getOccurrenceTime());

            //Truth r = input.projection(now, newBelief.getOccurrenceTime());
                /*
                if (projectedBelief.getOccurrenceTime()!=input.getOccurrenceTime()) {
                }
                */



            Task revised = tryRevision(belief, input, false, nal);
            if (revised != null) {
                belief = revised;
                nal.setCurrentBelief(revised);
            }

        }


//        if (!addToTable(belief, getBeliefs(), getMemory().param.conceptBeliefsMax.get(), Events.ConceptBeliefAdd.class, Events.ConceptBeliefRemove.class)) {
//            //wasnt added to table
//            getMemory().removed(belief, "Insufficient Rank"); //irrelevant
//            return false;
//        }
    }

    @Override
    public Task addGoal(Task goal, Concept c) {
        if (goal.equalStamp(input, true, true, false)) {
            return false; // duplicate
        }

        if (revisible(goal.sentence, oldGoal)) {

            //nal.setTheNewStamp(newStamp, oldStamp, memory.time());


            //Truth projectedTruth = oldGoal.projection(now, task.getOccurrenceTime());
                /*if (projectedGoal!=null)*/
            {
                // if (goal.after(oldGoal, nal.memory.param.duration.get())) { //no need to project the old goal, it will be projected if selected anyway now
                // nal.singlePremiseTask(projectedGoal, task.budget);
                //return;
                // }
                //nal.setCurrentBelief(projectedGoal);

                Task revisedTask = tryRevision(goal, oldGoalT, false, nal);
                if (revisedTask != null) { // it is revised, so there is a new task for which this function will be called
                    goal = revisedTask;
                    //return true; // with higher/lower desire
                } //it is not allowed to go on directly due to decision making https://groups.google.com/forum/#!topic/open-nars/lQD0no2ovx4

                //nal.setCurrentBelief(revisedTask);
            }
        }
    }


    /**
     * Determine the rank of a judgment by its quality and originality (stamp
     * baseLength), called from Concept
     *
     * @param s The judgment to be ranked
     * @return The rank of the judgment, according to truth value only
     */
    public float rank(final Task s, final long now) {
        return rankBeliefConfidenceTime(s, now);
    }

    public static float rankBeliefConfidenceTime(final Sentence judg, long now) {
        float c = judg.getTruth().getConfidence();
        if (!judg.isEternal()) {
            float dur = judg.getDuration();
            float durationsToNow = Math.abs(judg.getOccurrenceTime() - now) / dur;

            float ageFactor = 1.0f / (1.0f + durationsToNow * Global.rankDecayPerTimeDuration);
            c *= ageFactor;
        }
        return c;
    }

    public static float rankBeliefConfidence(final Sentence judg) {
        return judg.getTruth().getConfidence();
    }

    public static float rankBeliefOriginal(final Sentence judg) {
        final float confidence = judg.truth.getConfidence();
        final float originality = judg.getOriginality();
        return or(confidence, originality);
    }


    @Override public Task<Compound> top() {
        if (isEmpty()) return null;
        return get(0); //TODO use a ranking order consistent with ArrayListTaskTable, where the oldest item that gets FIFO'd out is in position 0
    }

    /**
     * Select a belief to interact with the given task in logic
     * <p>
     * get the first qualified one
     * <p>
     * only called in RuleTables.rule
     *
     * @param task The selected task
     * @return The selected isBelief
     */
    Task match(final NAL nal, final Task task) {
        if (isEmpty()) return null;

        final long now = nal.time();
        long occurrenceTime = task.getOccurrenceTime();

        final int b = size();

        for (final Task belief : this) {

            //if (task.sentence.isEternal() && belief.isEternal()) return belief;

            Task projectedBelief = belief.projection(nal.memory, occurrenceTime, now);

            //TODO detect this condition before constructing Task
            if (projectedBelief.getOccurrenceTime()!=belief.getOccurrenceTime()) {
                //belief = nal.derive(projectedBelief); // return the first satisfying belief
                return projectedBelief;
            }

            return belief;
        }

        return null;
    }


    boolean addToTable(final Task goalOrJudgment, final List<Task> table, final int max, final Class eventAdd, final Class eventRemove, Concept c) {
        int preSize = table.size();

        final Memory m = c.getMemory();

        Task removed = addToTable(goalOrJudgment, table, max, c);

        if (size()!=preSize)
            c.onTableUpdated(goalOrJudgment.getPunctuation(), preSize);

        if (removed != null) {
            if (removed == goalOrJudgment) return false;

            m.emit(eventRemove, this, removed.sentence, goalOrJudgment.sentence);

            if (preSize != table.size()) {
                m.emit(eventAdd, this, goalOrJudgment.sentence);
            }
        }

        return true;
    }

    /**
     * Add a new belief (or goal) into the table Sort the beliefs/goals by
     * rank, and remove redundant or low rank one
     *
     * @param newSentence The judgment to be processed
     * @param table       The table to be revised
     * @param capacity    The capacity of the table
     * @return whether table was modified
     */
    public Task addToTable(final Task newSentence, final List<Task> table, final int capacity, Concept c) {

        final Memory memory = c.getMemory();


        long now = memory.time();

        float rank1 = rank(newSentence, now);    // for the new isBelief

        float rank2;
        int i;

        //int originalSize = table.size();


        //TODO decide if it's better to iterate from bottom up, to find the most accurate replacement index rather than top
        for (i = 0; i < table.size(); i++) {
            Task existing = table.get(i);

            rank2 = rank(existing, now);

            if (rank1 >= rank2) {
                if (newSentence.sentence.equivalentTo(existing, false, false, true, true, false)) {
                    //System.out.println(" ---------- Equivalent Belief: " + newSentence + " == " + judgment2);
                    return newSentence;
                }
                table.add(i, newSentence);
                break;
            }
        }

        if (table.size() == capacity) {
            // no change
            return null;
        }

        Task removed = null;

        final int ts = table.size();
        if (ts > capacity) {
            removed = table.remove(ts - 1);
        } else if (i == table.size()) { // branch implies implicit table.size() < capacity
            table.add(newSentence);
            //removed = nothing
        }

        return removed;
    }




}
