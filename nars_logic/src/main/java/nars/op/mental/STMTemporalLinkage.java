package nars.op.mental;

import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.concept.Concept;
import nars.nal.Deriver;
import nars.nal.RuleMatch;
import nars.nar.Default;
import nars.process.TaskProcess;
import nars.process.TaskBeliefProcess;
import nars.task.Task;
import nars.term.Compound;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Short-term memory Event Induction.  Empties task buffer when plugin is (re)started.
 */
public class STMTemporalLinkage {

    public final Deque<Task> stm;
    //private final Deriver deriver;
    //int stmSize;
    //public static STMTemporalLinkage I=null;

    final private static String id = STMTemporalLinkage.class.getSimpleName();

    @Override
    public final String toString() {
        return id;
    }

    public STMTemporalLinkage(NAR nar, Deriver deriver) {

        //this.deriver = deriver;
        //this.stmSize = 1;
        stm = Global.THREADS == 1 ? new ArrayDeque() : new ConcurrentLinkedDeque<>();
        //I=this; //hm there needs to be a way to query plugins from the NAR/NAL object like in 1.6.x, TODO find out


        nar.memory.eventTaskProcess.on(n -> {
            if (!n.getTask().isDeleted())
                inductionOnSucceedingEvents(n, false);
        });
        nar.memory.eventReset.on(n -> {
            stm.clear();
        });

    }

    public static boolean isInputOrTriggeredOperation(final Task newEvent, Memory mem) {
        if (newEvent.isInput()) return true;
        //if (Tense.containsMentalOperator(newEvent)) return true;
        return false;
    }

//    public int getStmSize() {
//        return stmSize;
//    }

    public boolean inductionOnSucceedingEvents(TaskProcess nal, boolean anticipation) {

        final Task currentTask = nal.getTask();

        int stmSize = nal.memory().shortTermMemoryHistory.intValue();


//        if (!currentTask.isTemporalInductable() && !anticipation) { //todo refine, add directbool in task
//            return false;
//        }

        if (currentTask.isEternal() || (!isInputOrTriggeredOperation(currentTask, nal.memory()) && !anticipation)) {
            return false;
        }

        //new one happened and duration is already over, so add as negative task
        //nal.emit(Events.EventBasedReasoningEvent.class, currentTask, nal);

        //final long now = nal.memory.time();



        int numToRemoveFromBeginning = Math.max(0, stm.size() - stmSize);

        /** current task's... */
        final Compound term = currentTask.getTerm();
        final Concept concept = nal.nar.concept(term);
        if (concept == null)
            return false;

        Iterator<Task> ss = stm.iterator();

        while (ss.hasNext()) {

            Task previousTask = ss.next();


            if (numToRemoveFromBeginning > 0) {
                ss.remove();
                numToRemoveFromBeginning--;
            }
            else {
                if (!previousTask.isDeleted()) {
                    Concept previousConcept = nal.nar.concept(previousTask.getTerm());

                    if (previousConcept != null) {
                        //allow inference between these concepts, temporally justified

                        //allow budget flow from one event to the other
                        nal.link(previousConcept, currentTask);
                        nal.link(concept, previousTask);

                        //also allow direct event based inference between these events:
                        Default nar = (Default) nal.nar;
                        TaskBeliefProcess tbp = new TaskBeliefProcess(nal.nar, currentTask, previousTask);
                        nar.getDeriver().run(tbp, nar.core.derivedTasksBuffer::add);
                    }
                }
            }
        }

        int a = stm.size();

        stm.add(currentTask);

        return true;
    }

}
