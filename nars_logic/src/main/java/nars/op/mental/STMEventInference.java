package nars.op.app;

import nars.AbstractMemory;
import nars.Events;
import nars.Global;
import nars.NAR;
import nars.concept.Concept;
import nars.event.NARReaction;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.meta.RuleMatch;
import nars.nal.Deriver;
import nars.premise.Premise;
import nars.process.AbstractPremise;
import nars.process.NAL;
import nars.process.TaskProcess;
import nars.task.Task;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

import static nars.nal.nal7.TemporalRules.containsMentalOperator;
import static nars.term.Terms.equalSubTermsInRespectToImageAndProduct;

/**
 * Short-term memory Event Induction.  Empties task buffer when plugin is (re)started.
 */
public class STMEventInference extends NARReaction {

    public final Deque<Task> stm;
    private final Deriver deriver;
    int stmSize;
    //public static STMEventInference I=null;

    /** use a separate matching context in case this is invoked by a Deriver process so as not to interrupt it */
    static final ThreadLocal<RuleMatch> matchers = Deriver.newThreadLocalRuleMatches();


    public STMEventInference(NAR nar, Deriver deriver) {
        super(nar);
        this.deriver = deriver;
        this.stmSize = 1;
        stm = Global.THREADS == 1 ? new ArrayDeque() : new ConcurrentLinkedDeque<>();
        //I=this; //hm there needs to be a way to query plugins from the NAR/NAL object like in 1.6.x, TODO find out


        nar.memory.eventTaskProcess.on(n -> {
            inductionOnSucceedingEvents(n, false);
        });
    }

    @Override
    public Class[] getEvents() {
        return new Class[]{ Events.ResetStart.class};
    }


    @Override
    public void event(Class event, Object[] args) {

        if (event == Events.ResetStart.class) {
            stm.clear();
        }
    }

    public static boolean isInputOrTriggeredOperation(final Task newEvent, AbstractMemory mem) {
        if (newEvent.isInput()) return true;
        if (containsMentalOperator(newEvent)) return true;
        return newEvent.getCause() != null;
    }

    public int getStmSize() {
        return stmSize;
    }


    public boolean inductionOnSucceedingEvents(TaskProcess nal, boolean anticipation) {

        final Task currentTask = nal.getTask();

        stmSize = nal.memory.param.shortTermMemoryHistory.get();

        if (currentTask == null || (!currentTask.isTemporalInductable() && !anticipation)) { //todo refine, add directbool in task
            return false;
        }

        if (currentTask.isEternal() || (!isInputOrTriggeredOperation(currentTask, nal.memory) && !anticipation)) {
            return false;
        }

        //new one happened and duration is already over, so add as negative task
        nal.emit(Events.EventBasedReasoningEvent.class, currentTask, nal);

        //final long now = nal.memory.time();


        Iterator<Task> ss = stm.iterator();

        int numToRemoveFromBeginning = stm.size() - stmSize;

        while (ss.hasNext()) {

            Task stmLast = ss.next();


            if (!equalSubTermsInRespectToImageAndProduct(currentTask.getTerm(), stmLast.getTerm())) {
                continue;
            }


            if (numToRemoveFromBeginning > 0) {
                ss.remove();
            }
        }


        //iterate on a copy because temporalInduction seems like it sometimes calls itself recursively and this will cause a concurrent modification exception otherwise
        Task[] stmCopy = stm.toArray(new Task[stm.size()]);

        RuleMatch m = matchers.get();

        for (Task previousTask : stmCopy) {

            //nal.setCurrentTask(currentTask);

            //nal.setBelief(previousTask);

            //if(currentTask.getPriority()>Parameters.TEMPORAL_INDUCTION_MIN_PRIORITY)
            //TemporalRules.temporalInduction(currentTask, previousTask,
                    //nal.newStamp(currentTask.sentence, previousTask.sentence),
            //        nal);
            final Premise premise = new STMPremise(nal, previousTask);
            ///final Task task, final Sentence belief, Term beliefterm,
            //tLink.getTask(), belief, bLink.getTerm(),


            m.start(premise);

            final Task task = premise.getTask();

            if (task.isJudgment() || task.isGoal()) {

                deriver.forEachRule(m);

                //TODO also allow backward inference by traversing
            }

            m.clear();

        }

        ////for this heuristic, only use input events & task effects of operations
        ////if(currentTask.getPriority()>Parameters.TEMPORAL_INDUCTION_MIN_PRIORITY) {
        //stmLast = currentTask;
        ////}
        while (stm.size() > stmSize) {
            stm.removeFirst();
        }
        stm.add(currentTask);

        return true;
    }


    public static class STMPremise extends AbstractPremise {

        private final Task previousTask;
        private final NAL parent;

        //deriver.reason(new STMPremise(currentTask, previousTask.getSentence(), previousTask.getTerm())
        public STMPremise(NAL parent, Task previousTask) {
            super(parent.getMemory());
            this.parent = parent;
            this.previousTask = previousTask;
        }


        @Override
        public Concept getConcept() {
            return getMemory().concept(getTask().getTerm());
        }

        @Override
        public Task getBelief() {
            return previousTask;
        }

        @Override
        public TermLink getTermLink() {
            return parent.getTermLink();
        }

        @Override
        public TaskLink getTaskLink() {
            return parent.getTaskLink();
        }

        @Override
        public Task getTask() {
            return parent.getTask();
        }
    }
}
