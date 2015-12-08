/*
 * Task.java
 *
 * Copyright (C) 2008  Pei Wang
 *
 * This file is part of Open-NARS.
 *
 * Open-NARS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Open-NARS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.task;

import nars.Global;
import nars.Memory;
import nars.Symbols;
import nars.budget.Budget;
import nars.budget.Itemized;
import nars.concept.Concept;
import nars.nal.nal7.Tense;
import nars.term.compound.Compound;
import nars.truth.DefaultTruth;
import nars.truth.Truth;
import nars.truth.TruthFunctions;
import nars.truth.Truthed;

import java.lang.ref.Reference;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static nars.Global.dereference;
import static nars.nal.nal7.Tense.ORDER_BACKWARD;
import static nars.nal.nal7.Tense.ORDER_FORWARD;

/**
 * A task to be processed, consists of a Sentence and a BudgetValue.
 * A task references its parent and an optional causal factor (usually an Operation instance).  These are implemented as WeakReference to allow forgetting via the
 * garbage collection process.  Otherwise, Task ancestry would grow unbounded,
 * violating the assumption of insufficient resources (AIKR).
 * <p>
 * TODO decide if the Sentence fields need to be Reference<> also
 */
public interface Task extends Sentence,
        Itemized<Sentence>, Truthed, Comparable {


    static void getExplanation(Task task, int indent, StringBuilder sb) {
        //TODO StringBuilder

        for (int i = 0; i < indent; i++)
            sb.append("  ");


        task.appendTo(sb);

        /*List l = task.getLog();
        if (l!=null)
            sb.append(" log=").append(l);*/

        if (task.getBestSolution() != null) {
            if (!task.getTerm().equals(task.getBestSolution().getTerm())) {
                sb.append(" solution=");
                task.getBestSolution().appendTo(sb);
            }
        }

        Task pt = task.getParentTask();
        Task pb = task.getParentBelief();
//        if (pb != null) {
//            if (pt != null && pb.equals(pt)) {
//
//            } else {
//                sb.append(" parentBelief=");
//                task.getParentBelief().appendTo(sb);
//            }
//        }
        sb.append('\n');

        if (pt != null) {
            sb.append("  PARENT ");
            getExplanation(pt, indent+1, sb);
        }
        if (pb != null) {
            sb.append("  BELIEF ");
            getExplanation(pb, indent+1, sb);
        }
    }

    static Set<Truthed> getSentences(Iterable<Task> tasks) {


        int size;

        if (tasks instanceof Collection)
            size = ((Collection)tasks).size();
        else
            size = 2;

        Set<Truthed> s = Global.newHashSet(size);
        for (Task t : tasks)
            s.add(t);
        return s;
    }

    default Task getTask() { return this; }

    /**
     * Sets the perceived temporal duration of the Task,
     * in cycles.  This corresponds to how long the Task
     * seems to the Memory (ie. Memory.duration()), so it
     * serves as a default duration when the task's term
     * does not specify one (as in the case of a
     * Sequence or Parallel).
     *
     */
    void setDuration(int l);


    /**
     * Get the parent task of a task.
     * It is not guaranteed to remain because it is
     * stored as a Soft or Weak reference so that
     * task ancestry does not grow uncontrollably;
     *
     * instead, we rely on the JVM garbage collector
     * to serve as an enforcer of AIKR
     *
     * @return The task from which the task is derived, or
     * null if it has been forgotten
     */
    default Task getParentTask() {
        return dereference(getParentTaskRef());
    }

    Reference<Task> getParentTaskRef();


    Task getParentBelief();

    Reference<Task> getParentBeliefRef();


    /**
     * Check whether different aspects of sentence are equivalent to another one
     *
     * @param that The other judgment
     * @return Whether the two are equivalent
     */
    boolean equivalentTo(final Task that, final boolean punctuation, final boolean term, final boolean truth, final boolean stamp, final boolean creationTime);

    /** called when a Concept processes this Task */
    void onConcept(final Concept/*<T>*/ equivalentInstance);

    default <X extends Compound> MutableTask<X> solution(X t, char newPunc, Truth newTruth, long newOcc, Task question, Memory memory) {

        MutableTask tt = new MutableTask<>(t, newPunc)
            .truth(newTruth)
            .budget(getPriority(), getDurability(), getQuality())
            .time(memory.time(), newOcc);

        tt.setParents(getParentTaskRef(), getParentBeliefRef());


        tt.setEvidence(getEvidence());
        //tt.log(getLog());
        tt.log(new Solution(question));
        return tt;
    }


    final class Solution extends AtomicReference<Task> {
        Solution(Task referent) {
            super(referent);
        }

        @Override
        public String toString() {
            return "Solved: " + get();
        }
    }


    Task getBestSolution();

    Reference<Task> getBestSolutionRef();



    default StringBuilder toString(/**@Nullable*/ Memory memory) {
        return appendTo(null, memory);
    }

    @Override
    default StringBuilder appendTo(StringBuilder sb, /**@Nullable*/ Memory memory) {
        if (sb == null) sb = new StringBuilder();
        return appendTo(sb, memory, false);
    }

    @Override @Deprecated
    default StringBuilder appendTo(StringBuilder buffer, /**@Nullable*/ final Memory memory, final boolean showStamp) {
        final boolean notCommand = getPunctuation()!=Symbols.COMMAND;
        return appendTo(buffer, memory, true, showStamp && notCommand,
                notCommand, //budget
                showStamp //log
        );
    }

    @Override
    default StringBuilder appendTo(StringBuilder buffer, /**@Nullable*/ final Memory memory, final boolean term, final boolean showStamp, boolean showBudget, boolean showLog) {


        String contentName;
        if (term && getTerm()!=null) {
            contentName = getTerm().toString();
        }
        else contentName = "";

        final CharSequence tenseString;
        if (memory!=null) {
            tenseString = getTense(memory.time(), memory.duration());
        }
        else {
            //TODO dont bother craeting new StringBuilder and calculating the entire length etc.. just append it to a reusable StringReader?
            appendOccurrenceTime(
                    (StringBuilder)(tenseString = new StringBuilder()));
        }


        CharSequence stampString = showStamp ? stampAsStringBuilder() : null;

        int stringLength = contentName.length() + tenseString.length() + 1 + 1;

        if (getTruth() != null)
            stringLength += 11;

        if (showStamp)
            stringLength += stampString.length()+1;

        /*if (showBudget)*/
        //"$0.8069;0.0117;0.6643$ "
        stringLength += 1 + 6 + 1 + 6 + 1 + 6 + 1  + 1;

        String finalLog;
        if (showLog) {
            Object ll = getLogLast();

            finalLog = (ll!=null ? ll.toString() : null);
            if (finalLog!=null)
                stringLength += finalLog.length()+1;
            else
                showLog = false;
        }
        else
            finalLog = null;


        if (buffer == null)
            buffer = new StringBuilder(stringLength);
        else
            buffer.ensureCapacity(stringLength);


        if (showBudget) {
            getBudget().toBudgetStringExternal(buffer).append(' ');
        }

        buffer.append(contentName).append(getPunctuation());

        if (tenseString.length() > 0)
            buffer.append(' ').append(tenseString);

        if (getTruth()!= null) {
            buffer.append(' ');
            getTruth().appendString(buffer, 2);
        }

        if (showStamp)
            buffer.append(' ').append(stampString);

        if (showLog) {
            buffer.append(' ').append(finalLog);
        }

        return buffer;
    }

    default Object getLogLast() {
        final List<String> log = getLog();
        if (log ==null || log.isEmpty()) return null;
        return log.get(log.size()-1);
    }


    default boolean hasParent(Task t) {
        if (getParentTask() == null)
            return false;
        Task p = getParentTask();
        do {
            Task n = p.getParentTask();
            if (n == null) break;
            if (n.equals(t))
                return true;
            p = n;
        } while (true);
        return false;
    }

    default Task getRootTask() {
        if (getParentTask() == null) {
            return null;
        }
        Task p = getParentTask();
        do {
            Task n = p.getParentTask();
            if (n == null) break;
            p = n;
        } while (true);
        return p;
    }


    default String getExplanation() {
        StringBuilder sb = new StringBuilder();
        return getExplanation(sb).toString();
    }

    default StringBuilder getExplanation(StringBuilder temporary) {
        temporary.setLength(0);
        getExplanation(this, 0, temporary);
        return temporary;
    }


//    /**
//     * Get a String representation of the Task
//     *
//     * @return The Task as a String
//     */
//    @Override
//    public String toStringLong() {
//        final StringBuilder s = new StringBuilder();
//        s.append(super.toString()).append(' ').append(sentence.stamp.name());
//
//        Task pt = getParentTask();
//        if (pt != null) {
//            s.append("  \n from task: ").append(pt.toStringExternal());
//            if (parentBelief != null) {
//                s.append("  \n from belief: ").append(parentBelief.toString());
//            }
//        }
//        if (bestSolution != null) {
//            s.append("  \n solution: ").append(bestSolution.toString());
//        }
//        return s.toString();
//    }


//    /** returns the goal term for this task, which may be either the predicate of a forward implication,
//     * an operation.  if neither, returns null      */
//    public Term getGoalTerm() {
//        Term t = getContent();
//        if (t instanceof Implication) {
//            Implication i = (Implication)t;
//            if (i.getTemporalOrder() == TemporalRules.ORDER_FORWARD)
//                return i.getPredicate();
//            else if (i.getTemporalOrder() == TemporalRules.ORDER_BACKWARD) {
//                throw new RuntimeException("Term getGoal reversed");
//            }
//        }
//        else if (t instanceof Operation)
//            return t;
//        else if (Executive.isSequenceConjunction(t))
//            return t;
//        
//        return null;
//    }
//

    default Truth getDesire() {
        return getTruth();
    }

    /**
     * signaling that the Task has ended or discarded
     * @return if it was already deleted, will immediately return false.
     */
    @Override
    boolean delete();

//    default void logUnrepeated(String reason) {
//        if (getLog()!=null &&
//                getLog().get(getLog().size()-1).equals(reason))
//            return;
//        log(reason);
//    }

    /** append a log entry */
    void log(Object entry);

    /** append log entries */
    void log(List entries);

    /** get the recorded log entries */
    List getLog();


    //TODO make a Source.{ INPUT, SINGLE, DOUBLE } enum

    /** is double-premise */
    boolean isDouble();

    /** is single premise */
    boolean isSingle();

    /**
     * Check if a Task is a direct input,
     * or if its origin has been forgotten or never known
     */
    default boolean isInput() {
        return getParentTask() == null;
    }


    /**
     * a task is considered amnesiac (origin not rememebered) if its parent task has been forgotten (garbage collected via a soft/weakref)
     */
    default boolean isAmnesiac() {
        return !isInput() && getParentTask() == null;
    }


    /** if unnormalized, returns a normalized version of the task,
     *  null if not normalizable
     */
    Task normalize(final Memory memory);


    default void ensureValidParentTaskRef() {
        if ((getParentTaskRef() != null && getParentTask() == null))
            throw new RuntimeException("parentTask must be null itself, or reference a non-null Task");
    }



//    default Task projectTask(final long targetTime, final long currentTime) {
//
//        final ProjectedTruth t = projection(targetTime, currentTime);
//
//        return clone(getTerm(), t, t.getTargetTime());
//    }





    @Override
    default int getTemporalOrder() {
        return getTerm().getTemporalOrder();
    }

    void setTruth(Truth t);
    void discountConfidence();


    void setBestSolution(Task belief, Memory memory);


    @Override
    boolean isDeleted();


    /** normalize a collection of tasks to each other
     * so that the aggregate budget sums to a provided
     * normalization amount.
     * @param derived
     * @param premisePriority the total value that the derivation group should reach, effectively a final scalar factor determined by premise parent and possibly existing belief tasks
     * @return the input collection, unmodified (elements may be adjusted individually)
     */
    static void normalizeCombined(final Iterable<Task> derived, final float premisePriority) {


        final float totalDerivedPriority = Budget.prioritySum(derived);
        final float factor = Math.min(
                    premisePriority/totalDerivedPriority,
                    1.0f //limit to only diminish
                );

        if (Float.isNaN(factor))
            throw new RuntimeException("NaN");

        derived.forEach(t -> t.getBudget().mulPriority(factor));
    }

    static void normalize(final Iterable<Task> derived, final float premisePriority) {
        derived.forEach(t -> t.getBudget().mulPriority(premisePriority));
    }

    static Task command(Compound op) {
        //TODO use lightweight CommandTask impl without all the logic metadata
        return new MutableTask(op, Symbols.COMMAND);
    }

    default boolean isEternal() {
        return getOccurrenceTime()== Tense.ETERNAL;
    }


    default StringBuilder appendOccurrenceTime(final StringBuilder sb) {
        final long oc = getOccurrenceTime();
        final long ct = getCreationTime();

        /*if (oc == Stamp.TIMELESS)
            throw new RuntimeException("invalid occurrence time");*/
        if (ct == Tense.ETERNAL)
            throw new RuntimeException("invalid creation time");

        //however, timeless creation time means it has not been perceived yet

        if (oc == Tense.ETERNAL) {
            if (ct == Tense.TIMELESS) {
                sb.append(":-:");
            } else {
                sb.append(':').append(Long.toString(ct)).append(':');
            }

        } else if (oc == Tense.TIMELESS) {
            sb.append("N/A");

        } else {
            int estTimeLength = 8; /* # digits */
            sb.ensureCapacity(estTimeLength);

            sb.append(Long.toString(ct));

            long OCrelativeToCT = (oc - ct);
            if (OCrelativeToCT >= 0)
                sb.append('+'); //+ sign if positive or zero, negative sign will be added automatically in converting the int to string:
            sb.append(OCrelativeToCT);

        }

        return sb;
    }

    default String getTense(final long currentTime, final int duration) {

        if (Tense.isEternal(getOccurrenceTime())) {
            return "";
        }

        switch (Tense.order(currentTime, getOccurrenceTime(), duration)) {
            case ORDER_FORWARD:
                return Symbols.TENSE_FUTURE;
            case ORDER_BACKWARD:
                return Symbols.TENSE_PAST;
            default:
                return Symbols.TENSE_PRESENT;
        }
    }

    default CharSequence stampAsStringBuilder() {

        final long[] ev = getEvidence();
        final int len = ev != null ? ev.length : 0;
        final int estimatedInitialSize = 8 + (len * 3);

        final StringBuilder buffer = new StringBuilder(estimatedInitialSize);
        buffer.append(Symbols.STAMP_OPENER);

        if (getCreationTime() == Tense.TIMELESS) {
            buffer.append('?');
        } else if (!Tense.isEternal(getOccurrenceTime())) {
            appendOccurrenceTime(buffer);
        } else {
            buffer.append(getCreationTime());
        }
        buffer.append(Symbols.STAMP_STARTER).append(' ');
        for (int i = 0; i < len; i++) {

            buffer.append(Long.toString(ev[i], 36));
            if (i < (len - 1)) {
                buffer.append(Symbols.STAMP_SEPARATOR);
            }
        }

        buffer.append(Symbols.STAMP_CLOSER); //.append(' ');

        //this is for estimating an initial size of the stringbuffer
        //System.out.println(baseLength + " " + derivationChain.size() + " " + buffer.baseLength());

        return buffer;


    }


    /** creates a new child task (has this task as its parent) */
    default MutableTask spawn(Compound content, char punc) {
        return new MutableTask(content, punc);
    }

    default long getOccurrenceTime() {
        return Tense.ETERNAL;
    }

    default long start() { return getOccurrenceTime(); }
    default long end() {
        return start() + duration();
    }
    default int duration() {
        return 0;
    }



    //projects the truth to a certain time, covering all 4 cases as discussed in
    //https://groups.google.com/forum/#!searchin/open-nars/task$20eteneral/open-nars/8KnAbKzjp4E/rBc-6V5pem8J
    default DefaultTruth projection(final long targetTime, final long currentTime) {

        final Truth currentTruth = getTruth();
        long occurrenceTime = getOccurrenceTime();

        boolean eternal = targetTime == Tense.ETERNAL;
        boolean tenseEternal = Tense.isEternal(occurrenceTime);
        if (eternal && tenseEternal) {
            return new DefaultTruth(currentTruth);                 //target and itself is eternal so return the truth of itself
        }
        else if(!eternal && tenseEternal) {
            return new DefaultTruth(currentTruth);                 //target is not eternal but itself is,
        }                                                                        //note: we don't need to project since itself holds for every moment.
        else if (eternal && !tenseEternal) { //target is eternal, but ours isnt, so we need to eternalize it
            return TruthFunctions.eternalize(currentTruth);
        }
        else {
            //ok last option is that both are tensed, in this case we need to project to the target time
            //but since also eternalizing is valid, we use the stronger one.
            DefaultTruth eternalTruth = TruthFunctions.eternalize(currentTruth);
            float factor = TruthFunctions.temporalProjection(targetTime, occurrenceTime, currentTime);
            float projectedConfidence = factor * currentTruth.getConfidence();

            if(projectedConfidence > eternalTruth.getConfidence()) {
                return new DefaultTruth(currentTruth.getFrequency(), projectedConfidence);
            }
            else {
                return eternalTruth;
            }
        }
    }

    /** calculates projection truth quality without creating new TruthValue instances */
    default float projectionTruthQuality(long targetTime, long currentTime, boolean problemHasQueryVar) {
        return projectionTruthQuality(getTruth(), targetTime, currentTime, problemHasQueryVar);
    }

    /** calculates projection truth quality without creating new TruthValue instances */
    default float projectionTruthQuality(final Truth t, long targetTime, long currentTime, boolean problemHasQueryVar) {
        return t.projectionQuality(this, targetTime, currentTime, problemHasQueryVar);
    }

}
