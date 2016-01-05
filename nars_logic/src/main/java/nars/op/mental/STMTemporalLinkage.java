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
import nars.task.DefaultTask;
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
                inductionOnSucceedingEvents(n);
        });
        nar.memory.eventReset.on(n -> {
            stm.clear();
        });

    }

//    public int getStmSize() {
//        return stmSize;
//    }

    public boolean inductionOnSucceedingEvents(TaskProcess nal) {

        final Task currentTask = nal.getTask();

        int stmSize = nal.memory().shortTermMemoryHistory.intValue();

        if (currentTask.isEternal()) { //this is for events only
            return false;
        }

        if(!(currentTask.isInput() || ((DefaultTask)currentTask).getIsIntrospectiveEvent())) {
            return false; //we allow only these events which are input or introspective
        }


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
                        //allow budget flow from one event concept to the other and justify inference between them
                        nal.link(previousConcept, currentTask);

                        //also allow direct event based inference between these events:
                        if(previousTask.isJudgment()) { //but only if the second premise plays the role of a belief
                            Default nar = (Default) nal.nar;
                            TaskBeliefProcess tbp = new TaskBeliefProcess(nal.nar, currentTask, previousTask);
                            nar.getDeriver().run(tbp, nar.core.derivedTasksBuffer::add);
                        }
                    }
                }
            }
        }

        int a = stm.size();

        stm.add(currentTask);

        return true;
    }

}
