package nars.operator.app;

import nars.core.AbstractPlugin;
import nars.core.Events;
import nars.core.NAR;
import nars.core.Parameters;
import nars.logic.entity.Sentence;
import nars.logic.entity.Task;
import nars.logic.nal7.TemporalRules;
import nars.logic.reason.ImmediateProcess;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

import static nars.logic.Terms.equalSubTermsInRespectToImageAndProduct;
import static nars.operator.app.plan.MultipleExecutionManager.isInputOrTriggeredOperation;

/**
 * Short-term memory Event Induction.  Empties task buffer when plugin is (re)started.
 */
public class STMInduction extends AbstractPlugin {

    public final Deque<Task> stm;
    int stmSize;

    public STMInduction() {
        this.stmSize = 1;
        stm = Parameters.THREADS == 1 ? new ArrayDeque() : new ConcurrentLinkedDeque<>();
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
            ImmediateProcess n = (ImmediateProcess) args[1];
            inductionOnSucceedingEvents(t, n);
        }
    }

    public int getStmSize() {
        return stmSize;
    }

    boolean inductionOnSucceedingEvents(final Task newEvent, ImmediateProcess nal) {

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


        for (Task stmLast : stm) {




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
