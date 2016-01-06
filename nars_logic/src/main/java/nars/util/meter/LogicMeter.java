package nars.util.meter;

import nars.Memory;
import nars.budget.BudgetedHandle;
import nars.concept.Concept;
import nars.util.event.FrameReaction;
import nars.util.meter.event.DoubleMeter;
import nars.util.meter.event.HitMeter;

import java.util.function.Consumer;

/**
 * Logic/reasoning sensors
 * <p>
 * TODO make a distinction between ValueMeter and IncrementingValueMeter for
 * accumulating multiple cycless data into one frame's aggregate
 * <p>
 * TODO add the remaining meter types for NARS data structures (ex: Concept metrics)
 */
public class LogicMeter extends FrameReaction {



    public final Memory m;

    //public final HitMeter TASK_PROCESS = new HitMeter("task.immediate_process");

    //public final HitMeter TASKLINK_FIRE = new HitMeter("tasklink.fire");

    public final DoubleMeter CONCEPTS_ACTIVE = new DoubleMeter("concepts.active");
    public final DoubleMeter CONCEPTS_TOTAL = new DoubleMeter("concepts.total");
//    public final DoubleMeter TERMLINK_MASS_CONCEPT_MEAN = new DoubleMeter("termlink.mass.concept_mean");
//    public final DoubleMeter TERMLINK_MASS_MEAN = new DoubleMeter("termlink.mass.mean");
//    public final DoubleMeter TASKLINK_MASS_CONCEPT_MEAN = new DoubleMeter("tasklink.mass.concept_mean");
//    public final DoubleMeter TASKLINK_MASS_MEAN = new DoubleMeter("tasklink.mass.mean");


    public final DoubleMeter CONCEPT_BELIEF_COUNT = new DoubleMeter("concept.belief.count");
    public final DoubleMeter CONCEPT_QUESTION_COUNT = new DoubleMeter("concept.question.count");
    public final HitMeter TERM_LINK_TRANSFORM = new HitMeter("concept.termlink.transform");

    //SummaryStatistics inputPriority = new SummaryStatistics();
    //public final DoubleMeter INPUT_PRIORITY_SUM = new DoubleMeter("input.pri.sum");

    /**
     * triggered for each StructuralRules.contraposition().
     * counts invocation and records complexity of statement parameter
     */
    public final HitMeter CONTRAPOSITION = new HitMeter("rule.contraposition");


    public final HitMeter TASK_ADD_NEW = new HitMeter("task.new.add");
    //public final HitMeter TASK_DERIVED = new HitMeter("task.derived");
    public final HitMeter TASK_EXECUTED = new HitMeter("task.executed");
    public final HitMeter TASK_ADD_NOVEL = new HitMeter("task.novel.add");

    public final HitMeter CONCEPT_NEW = new HitMeter("concept.new");

    /** when a concept is remembered (different than created) */
    public final HitMeter CONCEPT_REMEMBER = new HitMeter("concept.remember");
    //@Deprecated public final HitMeter CONCEPT_FORGET = new HitMeter("concept.forget");

    public final HitMeter JUDGMENT_PROCESS = new HitMeter("judgment.process");
    public final HitMeter GOAL_PROCESS = new HitMeter("goal.process");
    public final HitMeter QUESTION_PROCESS = new HitMeter("question.process");


    //public final HitMeter BELIEF_REVISION = new HitMeter("rule.belief.revised");
    public final HitMeter DED_SECOND_LAYER_VARIABLE_UNIFICATION_TERMS = new HitMeter("rule.ded2ndunifterms");
    public final HitMeter DED_SECOND_LAYER_VARIABLE_UNIFICATION = new HitMeter("rule.ded2ndunif");
    public final HitMeter DED_CONJUNCTION_BY_QUESTION = new HitMeter("rule.dedconjbyquestion");
    public final HitMeter ANALOGY = new HitMeter("rule.analogy");


    //public final DoubleMeter DERIVATION_LATENCY = new DoubleMeter("rule.derivation.latency");
    public final DoubleMeter SOLUTION_BEST = new DoubleMeter("task.solution.best");


    /*
    public final DoubleMeter PLAN_GRAPH_IN_DELAY_MAGNITUDE = new DoubleMeter("plan.graph.add#delay_magnitude");
    public final DoubleMeter PLAN_GRAPH_IN_OPERATION = new DoubleMeter("plan.graph.add#operation");
    public final DoubleMeter PLAN_GRAPH_IN_OTHER = new DoubleMeter("plan.graph.add#other");
    public final DoubleMeter PLAN_GRAPH_EDGE = new DoubleMeter("plan.graph.edge");
    public final DoubleMeter PLAN_GRAPH_VERTEX = new DoubleMeter("plan.graph.vertex");
    public final DoubleMeter PLAN_TASK_PLANNED = new DoubleMeter("plan.task.planned");
    public final DoubleMeter PLAN_TASK_EXECUTABLE = new DoubleMeter("plan.task.executable");
    */

    //public final ValueMeter TASK_INPUT = new ValueMeter("task.input");

    //private double conceptVariance;
    //private double[] conceptHistogram;


    public LogicMeter(Memory m) {
        super(m);
        this.m = m;
    }

    @Override
    public void onFrame() {
        conceptMeter.reset();
        //m.getConcepts().forEach(conceptMeter);
        conceptMeter.commit(m);
    }

    public static class ItemMeter<I extends BudgetedHandle> implements Consumer<I> {

        double prioritySum = 0;
        double prioritySumSq = 0;
        static final int histogramBins = 4;
        double[] histogram = new double[histogramBins];
        int count;
        private double mean;
        private double variance;

//        SummaryStatistics
//                termLinkMassPerConcept = new SummaryStatistics(),
//                termLinkMass = new SummaryStatistics(),
//                taskLinkMassPerConcept = new SummaryStatistics(),
//                taskLinkMass = new SummaryStatistics();

        public void reset() {
            count = 0;
            prioritySum = prioritySumSq = 0;
//            termLinkMassPerConcept.clear();
//            termLinkMass.clear();
//            taskLinkMassPerConcept.clear();
//            taskLinkMass.clear();
        }

        @Override
        public void accept(I c) {

            double p = c.getPriority();

            //TODO totalGoals...
            //TODO totalQuests...

            prioritySum += p;
            prioritySumSq += p * p;

            //noinspection IfStatementWithTooManyBranches
            if (p > 0.75) {
                histogram[0]++;
            } else if (p > 0.5) {
                histogram[1]++;
            } else if (p > 0.25) {
                histogram[2]++;
            } else {
                histogram[3]++;
            }

            count++;

        }

        public void commit() {
            double mean, variance;
            if (count > 0) {
                mean = prioritySum / count;

                //http://en.wikipedia.org/wiki/Algorithms_for_calculating_variance
                variance = (prioritySumSq - ((prioritySum * prioritySum) / count)) / (count - 1);
                for (int i = 0; i < histogram.length; i++) {
                    histogram[i] /= count;
                }
            } else {
                mean = variance = 0;
            }
            this.mean = mean;
            this.variance = variance;
        }

        /** average priority */
        public double getMean() {
            return mean;
        }

        /** variance in priority */
        public double getVariance() {
            return variance;
        }

        /** priority histogram */
        public double[] getHistogram() {
            return histogram;
        }
    }

    public class ConceptMeter implements Consumer<Concept> {

        int count = 0;
        int totalQuestions = 0;
        int totalBeliefs = 0;

        public void reset() {
            totalQuestions = totalBeliefs = 0;
            count = 0;
            //inputPriority.clear();
        }

        @Override
        public void accept(Concept c) {
            if (c == null) return;


            if (c.hasQuestions())
                totalQuestions += c.getQuestions().size();

            if (c.hasBeliefs())
                totalBeliefs += c.getBeliefs().size();


//            float termLinksMass = c.getTermLinks().mass();
//            int numTermLinks = c.getTermLinks().size();
//            float taskLinksMass = c.getTaskLinks().mass();
//            int numTaskLinks = c.getTaskLinks().size();
//
//            termLinkMassPerConcept.addValue(termLinksMass);
//            termLinkMass.addValue((numTermLinks > 0) ? termLinksMass / numTermLinks : 0);
//            taskLinkMassPerConcept.addValue(taskLinksMass);
//            taskLinkMass.addValue((numTaskLinks > 0) ? taskLinksMass / numTaskLinks : 0);

        }



        public void commit(Memory m) {


            CONCEPTS_ACTIVE.set(count);
            CONCEPTS_TOTAL.set(m.index.size());
//            TERMLINK_MASS_CONCEPT_MEAN.set(termLinkMassPerConcept.getMean());
//            TERMLINK_MASS_MEAN.set(termLinkMass.getMean());
//            TASKLINK_MASS_CONCEPT_MEAN.set(taskLinkMassPerConcept.getMean());
//            TASKLINK_MASS_MEAN.set(taskLinkMass.getMean());
            CONCEPT_BELIEF_COUNT.set(totalBeliefs);
            CONCEPT_QUESTION_COUNT.set(totalQuestions);

            //INPUT_PRIORITY_SUM.set(inputPriority.getSum());


            //TODO
            /*
            setConceptPriorityMean(mean);
            setConceptPriorityVariance(variance);
            setConceptPriorityHistogram(histogram);
            */
        }
    }

    public final ConceptMeter conceptMeter = new ConceptMeter();




//    @Override
//    public void commit(Memory memory) {
//        super.commit(memory);
//        
//        put("concept.count", conceptNum);
//        
//        put("concept.pri.mean", conceptPriorityMean);
//        put("concept.pri.variance", conceptVariance);
//        
//        //in order; 0= top 25%, 1 = 50%..75%.., etc
//        for (int n = 0; n < conceptHistogram.length; n++)
//            put("concept.pri.histo#" + n, conceptHistogram[n]);
//        
//        put("concept.belief.mean", conceptNum > 0 ? ((double)conceptBeliefsSum)/conceptNum : 0);
//        put("concept.question.mean", conceptNum > 0 ? ((double)conceptQuestionsSum)/conceptNum : 0);
//        
//        put("task.novel.total", memory.novelTasks.size());
//        //put("memory.newtasks.total", memory.newTasks.size()); //redundant with output.tasks below
//
//        //TODO move to EmotionState
//        put("emotion.happy", memory.emotion.happy());
//        put("emotion.busy", memory.emotion.busy());
//
//        
//        {
//            //DataSet rule = TASKLINK_REASON.get();
//            put("rule.fire.tasklink.pri.mean", TASKLINK_FIRE.mean());
//            put("rule.fire.tasklinks", TASKLINK_FIRE.getHits());
//            
//            putHits(TERM_LINK_SELECT);
//            
//            //only makes commit as a mean, since it occurs multiple times during a cycle
//            put("rule.tasktermlink.pri.mean", TERM_LINK_SELECT.mean());
//        }
//        {
//            putValue(TASK_INPUT);
//        }
//        {            
//            putHits(CONTRAPOSITION);
//            
//            //put("rule.contrapositions.complexity.mean", CONTRAPOSITION.get().mean());
//            
//            putHits(BELIEF_REVISION);
//            put("rule.ded_2nd_layer_variable_unification_terms", DED_SECOND_LAYER_VARIABLE_UNIFICATION_TERMS.getHits());
//            put("rule.ded_2nd_layer_variable_unification", DED_SECOND_LAYER_VARIABLE_UNIFICATION.getHits());
//            put("rule.ded_conjunction_by_question", DED_CONJUNCTION_BY_QUESTION.getHits());
//            
//            putHits(ANALOGY);
//        }
//        {
//            DataSet d = DERIVATION_LATENCY.get();
//            double min = d.min();
//            if (!Double.isFinite(min)) min = 0;
//            double max = d.max();
//            if (!Double.isFinite(max)) max = 0;
//            
//            put(DERIVATION_LATENCY.name() + ".min", min);
//            put(DERIVATION_LATENCY.name() + ".max", max);
//            put(DERIVATION_LATENCY.name() + ".mean", d.mean());
//        }
//        {
//            putHits(TASK_ADD_NEW);
//            putHits(TASK_ADD_NOVEL);            
//            put("task.derived", TASK_DERIVED.getHits());
//            
//            put("task.pri.mean#added", TASK_ADD_NEW.getReset().mean());
//            put("task.pri.mean#derived", TASK_DERIVED.getReset().mean());
//            put("task.pri.mean#executed", TASK_EXECUTED.getReset().mean());
//            
//            put("task.executed", TASK_EXECUTED.getHits());
//            
//            put("task.immediate.process", TASK_IMMEDIATE_PROCESS.getHits());
//            //put("task.immediate_processed.pri.mean", TASK_IMMEDIATE_PROCESS.get().mean());
//        }
//        {
//            put("task.link_to", LINK_TO_TASK.getHits());
//            put("task.process#goal", GOAL_PROCESS.getHits());
//            put("task.process#judgment", JUDGMENT_PROCESS.getHits());
//            put("task.process#question", QUESTION_PROCESS.getHits());            
//        }
//        
//        
//        putHits(SHORT_TERM_MEMORY_UPDATE);
//        
//        {
//            putHits(SOLUTION_BEST);
//            put("task.solved.best.pri.mean", SOLUTION_BEST.get().mean());
//        }
//        
//        
//        {
//            
//            put("plan.graph#edge", PLAN_GRAPH_EDGE.getValue());
//            put("plan.graph#vertex", PLAN_GRAPH_VERTEX.getValue());
//            
//            put("plan.graph.add#other", PLAN_GRAPH_IN_OTHER.getHits());
//            put("plan.graph.add#operation", PLAN_GRAPH_IN_OPERATION.getHits());
//            put("plan.graph.add#interval", PLAN_GRAPH_IN_DELAY_MAGNITUDE.getHits());
//            put("plan.graph.in.delay_magnitude.mean", PLAN_GRAPH_IN_DELAY_MAGNITUDE.getReset().mean());
//
//            put("plan.task#executable", PLAN_TASK_EXECUTABLE.getReset().sum());
//            put("plan.task#planned", PLAN_TASK_PLANNED.getReset().sum());
//
//        }
//    }
//    
//    public void putValue(final ValueMeter s) {
//        put(s.getName(), s.getValue());
//    }
//    public void putHits(final ValueMeter s) {
//        put(s.getName(), s.getHits());
//    }
//    public void putMean(final ValueMeter s) {
//        put(s.getName(), s.get().mean());
//    }


//    public void setConceptPriorityMean(double conceptPriorityMean) {
//        this.conceptPriorityMean = conceptPriorityMean;
//    }
//
////    public void setConceptPrioritySum(double conceptPrioritySum) {
////        this.conceptPrioritySum = conceptPrioritySum;
////    }
//
//
//    public void setConceptPriorityVariance(double variance) {
//        this.conceptVariance = variance;
//    }
//
//    public void setConceptPriorityHistogram(double[] histogram) {
//        this.conceptHistogram = histogram;
//    }


}
