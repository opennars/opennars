package nars.operate.app;

import nars.Events;
import nars.Global;
import nars.NAR;
import nars.nal.DirectProcess;
import nars.nal.Sentence;
import nars.nal.Task;
import nars.nal.nal7.TemporalRules;
import nars.operate.AbstractOperator;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

import static nars.nal.Terms.equalSubTermsInRespectToImageAndProduct;
import static nars.operate.app.plan.MultipleExecutionManager.isInputOrTriggeredOperation;

/**
 * Short-term memory Event Induction.  Empties task buffer when plugin is (re)started.
 */
public class STMInduction extends AbstractOperator {

    public final Deque<Task> stm;
    int stmSize;

    public STMInduction() {
        this.stmSize = 1;
        stm = Global.THREADS == 1 ? new ArrayDeque() : new ConcurrentLinkedDeque<>();
    }

    @Override
    public Class[] getEvents() {
        return new Class[]{Events.TaskImmediateProcessed.class};
    }

    @Override
    public void onEnabled(NAR n) {
        stm.clear();
    }

    @Override
    public void onDisabled(NAR n) {

    }

    @Override
    public void event(Class event, Object[] args) {
        if (event == Events.TaskImmediateProcessed.class) {
            Task t = (Task) args[0];
            DirectProcess n = (DirectProcess) args[1];
            inductionOnSucceedingEvents(t, n);
        }
    }

    public int getStmSize() {
        return stmSize;
    }

    boolean inductionOnSucceedingEvents(final Task newEvent, DirectProcess nal) {

        stmSize = nal.memory.param.shortTermMemoryHistory.get();

        if (newEvent.budget == null || !newEvent.isParticipatingInTemporalInduction()) { //todo refine, add directbool in task
            return false;
        }

        //new one happened and duration is already over, so add as negative task
        nal.emit(Events.InduceSucceedingEvent.class, newEvent, nal);

        if (newEvent.sentence.isEternal() || !isInputOrTriggeredOperation(newEvent, nal.memory)) {
            return false;
        }

        //final long now = nal.memory.time();


        Iterator<Task> ss = stm.iterator();

        int numToRemoveFromBeginning = stm.size() - stmSize;

        while (ss.hasNext()) {

            Task stmLast = ss.next();


            if (!equalSubTermsInRespectToImageAndProduct(newEvent.sentence.term, stmLast.sentence.term)) {
                continue;
            }


            if (numToRemoveFromBeginning > 0) {
                ss.remove();
            }
        }


        //iterate on a copy because temporalInduction seems like it sometimes calls itself recursively and this will cause a concurrent modification exception otherwise
        Task[] stmCopy = stm.toArray(new Task[stm.size()]);

        for (Task stmLast : stmCopy) {




            //nal.setCurrentTask(newEvent);

            Sentence previousBelief = stmLast.sentence;
            //nal.setCurrentBelief(previousBelief);

            Sentence currentBelief = newEvent.sentence;

            //if(newEvent.getPriority()>Parameters.TEMPORAL_INDUCTION_MIN_PRIORITY)
            TemporalRules.temporalInduction(currentBelief, previousBelief,
                    nal.newStamp(newEvent.sentence, stmLast.sentence),
                    nal);
        }

        ////for this heuristic, only use input events & task effects of operations
        ////if(newEvent.getPriority()>Parameters.TEMPORAL_INDUCTION_MIN_PRIORITY) {
        //stmLast = newEvent;
        ////}
        while (stm.size() > stmSize) {
            stm.removeFirst();
        }
        stm.add(newEvent);

        return true;
    }

}
