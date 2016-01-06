package nars.op.mental;

import nars.Global;
import nars.NAR;
import nars.concept.Concept;
import nars.task.Task;
import nars.term.compound.Compound;

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

    private static final String id = STMTemporalLinkage.class.getSimpleName();
    private final NAR nar;

    @Override
    public final String toString() {
        return id;
    }

    public STMTemporalLinkage(NAR nar) {

        //this.deriver = deriver;
        //this.stmSize = 1;
        stm = Global.THREADS == 1 ? new ArrayDeque() : new ConcurrentLinkedDeque<>();
        //I=this; //hm there needs to be a way to query plugins from the NAR/NAL object like in 1.6.x, TODO find out


        nar.memory.eventTaskProcess.on(n -> {
            if (!n.getTask().getDeleted())
                inductionOnSucceedingEvents(n, false);
        });
        nar.memory.eventReset.on(n -> stm.clear());
        this.nar = nar;

    }

    public static boolean isInputOrTriggeredOperation(Task newEvent) {
        if (newEvent.isInput()) return true;
        //if (Tense.containsMentalOperator(newEvent)) return true;
        return false;
    }

//    public int getStmSize() {
//        return stmSize;
//    }


    public boolean inductionOnSucceedingEvents(Task currentTask, boolean anticipation) {


        int stmSize = nar.memory.shortTermMemoryHistory.intValue();


//        if (!currentTask.isTemporalInductable() && !anticipation) { //todo refine, add directbool in task
//            return false;
//        }

        if (currentTask.isEternal() || (!isInputOrTriggeredOperation(currentTask) && !anticipation)) {
            return false;
        }

        //new one happened and duration is already over, so add as negative task
        //nal.emit(Events.EventBasedReasoningEvent.class, currentTask, nal);

        //final long now = nal.memory.time();



        int numToRemoveFromBeginning = Math.max(0, stm.size() - stmSize);

        /** current task's... */
        Compound term = currentTask.term();
        Concept concept = nar.concept(term);
        if (concept == null)
            return false;

        Iterator<Task> ss = stm.iterator();

        while (ss.hasNext()) {

            Task previousTask = ss.next();

            /*if (!equalSubTermsInRespectToImageAndProduct(term, t.getTerm())) {
                continue;
            }*/

            if (numToRemoveFromBeginning > 0) {
                numToRemoveFromBeginning--;
            }
            else {
                if (!previousTask.getDeleted()) {
                    concept.crossLink(currentTask, previousTask, 1f, nar);
                }
            }

            ss.remove();

           /* continue;
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

            m.clear();*/

        }

        ////for this heuristic, only use input events & task effects of operations
        ////if(currentTask.getPriority()>Parameters.TEMPORAL_INDUCTION_MIN_PRIORITY) {
        //stmLast = currentTask;
        ////}
//        while (stm.size() > stmSize) {
//            stm.removeFirst();
//        }
        stm.add(currentTask);

        return true;
    }


//    public static class STMPremise extends AbstractPremise {
//
//        private final Task previousTask;
//        private final NAL parent;
//
//        //deriver.reason(new STMPremise(currentTask, previousTask.getSentence(), previousTask.getTerm())
//        public STMPremise(NAL parent, Task previousTask) {
//            super(parent.nar());
//            this.parent = parent;
//            this.previousTask = previousTask;
//        }
//
//
//        @Override
//        public Concept getConcept() {
//            return nar().concept(getTask().getTerm());
//        }
//
//        @Override
//        public Task getBelief() {
//            return previousTask;
//        }
//
//        @Override
//        public TermLink getTermLink() {
//            return parent.getTermLink();
//        }
//
//        @Override
//        public TaskLink getTaskLink() {
//            return parent.getTaskLink();
//        }
//
//        @Override
//        public Task getTask() {
//            return parent.getTask();
//        }
//    }
}
