package nars.op.mental;

import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.concept.Concept;
import nars.nal.Deriver;
import nars.nal.nal7.Tense;
import nars.process.TaskProcess;
import nars.task.Task;
import nars.term.Compound;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

import static nars.term.Terms.equalSubTermsInRespectToImageAndProduct;

/**
 * Short-term memory Event Induction.  Empties task buffer when plugin is (re)started.
 */
public class STMTemporalLinkage {

    public final Deque<Task> stm;
    private final Deriver deriver;
    int stmSize;
    //public static STMTemporalLinkage I=null;




    public STMTemporalLinkage(NAR nar, Deriver deriver) {

        this.deriver = deriver;
        this.stmSize = 1;
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
        if (Tense.containsMentalOperator(newEvent)) return true;
        return false;
    }

//    public int getStmSize() {
//        return stmSize;
//    }


    public boolean inductionOnSucceedingEvents(TaskProcess nal, boolean anticipation) {

        final Task currentTask = nal.getTask();

        stmSize = nal.memory().shortTermMemoryHistory.get();

//        if (!currentTask.isTemporalInductable() && !anticipation) { //todo refine, add directbool in task
//            return false;
//        }

        if (Tense.isEternal(currentTask.getOccurrenceTime()) || (!isInputOrTriggeredOperation(currentTask, nal.memory()) && !anticipation)) {
            return false;
        }

        //new one happened and duration is already over, so add as negative task
        //nal.emit(Events.EventBasedReasoningEvent.class, currentTask, nal);

        //final long now = nal.memory.time();


        Iterator<Task> ss = stm.iterator();

        int numToRemoveFromBeginning = stm.size() - stmSize;

        /** current task's... */
        final Compound term = currentTask.getTerm();
        final Concept concept = nal.nar.concept(term);
        if (concept == null)
            return false;

        while (ss.hasNext()) {

            Task stmLast = ss.next();


            if (!equalSubTermsInRespectToImageAndProduct(term, stmLast.getTerm())) {
                continue;
            }


            if (numToRemoveFromBeginning > 0) {
                ss.remove();
            }
        }


        //iterate on a copy because temporalInduction seems like it sometimes calls itself recursively and this will cause a concurrent modification exception otherwise
        Task[] stmCopy = stm.toArray(new Task[stm.size()]);

        //RuleMatch m = matchers.get();

        for (Task previousTask : stmCopy) {


            //help me out seh, why doesnt this work? ^^
            //nal.nar.concept(currentTask.getTerm()).link(previousTask);
            //nal.nar.concept(previousTask.getTerm()).link(currentTask);
            //nal.setCurrentTask(currentTask);

            if (!previousTask.isDeleted()) {
                Concept previousConcept = nal.nar.concept(previousTask.getTerm());
                if (previousConcept!=null) {
                    nal.link(previousConcept, currentTask);
                    nal.link(concept, previousTask);
                }
            }


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
        while (stm.size() > stmSize) {
            stm.removeFirst();
        }
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
