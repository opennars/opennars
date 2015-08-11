package nars.nar.experimental;

import com.google.common.collect.Iterators;
import com.google.common.util.concurrent.AtomicDouble;
import com.gs.collections.impl.map.mutable.primitive.ObjectFloatHashMap;
import nars.*;
import nars.bag.Bag;
import nars.bag.impl.CacheBag;
import nars.bag.impl.CurveBag;
import nars.bag.impl.GuavaCacheBag;
import nars.budget.Budget;
import nars.budget.BudgetFunctions;
import nars.budget.ItemAccumulator;
import nars.budget.ItemComparator;
import nars.clock.CycleClock;
import nars.concept.*;
import nars.cycle.AbstractCycle;
import nars.io.DefaultPerception;
import nars.io.Perception;
import nars.io.out.TextOutput;
import nars.link.*;
import nars.meter.NARTrace;
import nars.nal.LogicPolicy;
import nars.nar.Default;
import nars.premise.Premise;
import nars.process.CycleProcess;
import nars.process.TaskProcess;
import nars.process.concept.TableDerivations;
import nars.task.Sentence;
import nars.task.Task;
import nars.term.Compound;
import nars.term.Term;
import nars.term.transform.TermVisitor;

import java.util.Iterator;
import java.util.function.Consumer;

/**
 * "Adaptive Logic And Neural Networks" Spiking continuous-time model
 * designed by TonyLo
 */
public class Alann extends NARSeed {

    static final AtomicDouble activationThreshold = new AtomicDouble(0.05);
    final int MAX_CONCEPTS = 128 * 1024;
    final int subcyclesPerCycle = 3;
    static final float spikeDecay = 0.9f;
    static final float defaultBeliefPriority = 0.1f;
    private double conceptActivationThreshold = 0.05f; //this will be automatically tuned by a busy metric
    final int commandsPerCycle = 1;

    public Alann() {
        setClock(new CycleClock());
    }

    public static void main(String[] args) {
        //temporary testing shell

        Global.DEBUG = true;
        Global.EXIT_ON_EXCEPTION = true;

        Global.DEFAULT_JUDGMENT_PRIORITY = defaultBeliefPriority;

        NAR n = new NAR(new Alann());
        TextOutput.out(n);
        NARTrace.out(n);

        n.input("<x --> y>.\n" +
                "<y --> z>.\n" +
                "<x --> z>?\n");

        n.frame(8);

    }

    @Override
    protected Memory newMemory(Param narParam, LogicPolicy policy) {
        Memory m = super.newMemory(narParam, policy);
        m.on(this); //default conceptbuilder
        return m;
    }

    @Override
    public CycleProcess newCycleProcess() {
        return new AlannCycle();
    }

    @Override
    protected CacheBag<Term, Concept> newIndex() {
        return new GuavaCacheBag<>();
    }

    @Override
    protected int getMaximumNALLevel() {
        return 8;
    }

    @Override
    public LogicPolicy getLogicPolicy() {
        return Default.newPolicy(new TableDerivations());
    }

    @Override
    public Perception newPerception() {
        return new DefaultPerception();
    }


    @Override
    public Concept newConcept(final Term t, final Budget b, final Memory m) {
        return new AlannConcept(t, b, m);
    }



    public static class AlannConcept extends AbstractConcept {

        final ArrayListBeliefTable beliefs;

        final TermLinkBuilder termLinkBuilder;

        final ObjectFloatHashMap<TermLink> termLinks = new ObjectFloatHashMap<>();

        float activation = 0;
        Task lastTask;

        public AlannConcept(Term term, Budget budget, Memory memory) {
            super(term, budget, memory);
            beliefs = new ArrayListBeliefTable(4, new BeliefTable.BeliefConfidenceAndCurrentTime(this));
            termLinkBuilder = new TermLinkBuilder(this);

            if (termLinkBuilder.templates()!=null) {
                for (TermLinkTemplate tlt : termLinkBuilder.templates())
                    termLinks.put(termLinkBuilder.out(tlt), 0);
            }
        }

        @Override
        public boolean processBelief(Premise nal, Task task) {
            System.out.println(this + " processBelief " + task);

            final TaskLink taskLink = new TaskLink(task, task.getBudget());


            believe(taskLink);

            if (task.isInput()) {
                //trigger spike event (spike const * truth.confidence)
                onSpike(taskLink, 1f);
            }


            return false;
        }

        @Override
        public Task processQuestion(Premise nal, Task task) {
            System.out.println(this + " processQuestion " + task);


            final TaskLink taskLink = new TaskLink(task, task.getBudget());
            onSpike(taskLink, 1f);

            lastTask = task;


            return null;
        }

        protected void believe(TaskLink beliefTask) {
            final Task theTask = beliefTask.getTask();
            BeliefTable.Ranker belifeRanker = new BeliefTable.Ranker() {

                @Override
                public float rank(Task t, float bestToBeat) {
                    return t.getTruth().getConfidence();
                }
            };

            if (beliefs.add(theTask, belifeRanker, this, null) == theTask) {
                inferLocal(beliefTask);
                System.out.println(this + " added belief " + theTask);
            }
        }

        /**  Local Inference on belief (revision/Choice/Decision) */
        protected void inferLocal(TaskLink t) {

            //TODO ???



        }


        /*onTask ( task)
        {
            // just store/replace the task in the concept
        }*/

        @Override
        public boolean processGoal(Premise nal, Task task) {
            System.out.println(this + " processGoal " + task);
            lastTask = task;
            return false;
        }


        protected void forget() {
            //TODO make sure # cycles relative to duration are appropriately used
            /*switch (param.forgetting) {
                case Iterative:
                    BudgetFunctions.forgetIterative(x.budget, forgetCycles, relativeThreshold);
                    break;
                case Periodic:*/
            BudgetFunctions.forgetPeriodic(this, memory.param.conceptForgetDurations.floatValue(), (float) 0, memory.time());
        }
        protected void forget(TermLink tl) {
            //TODO make sure # cycles relative to duration are appropriately used
            /*switch (param.forgetting) {
                case Iterative:
                    BudgetFunctions.forgetIterative(x.budget, forgetCycles, relativeThreshold);
                    break;
                case Periodic:*/
            BudgetFunctions.forgetPeriodic(tl, memory.param.termLinkForgetDurations.floatValue(), (float) 0, memory.time());
        }
        protected void forget(TaskLink tl) {
            //TODO make sure # cycles relative to duration are appropriately used
            /*switch (param.forgetting) {
                case Iterative:
                    BudgetFunctions.forgetIterative(x.budget, forgetCycles, relativeThreshold);
                    break;
                case Periodic:*/
            BudgetFunctions.forgetPeriodic(tl, memory.param.termLinkForgetDurations.floatValue(), (float) 0, memory.time());
        }

        protected void onSpike(TaskLink taskLink, float multiplier) {
            // adjust activation level for decay
            //forget();



            float conf =
                    taskLink.getTask().isQuestOrQuestion() ?
                            1f :
                            taskLink.getTask().getTruth().getConfidence();
            float spikeActivation = taskLink.getPriority() * conf * multiplier;

            // if activation > threshold then this.Activate()
            if (getPriority() > activationThreshold.floatValue()) {

                taskLink.addPriority(spikeActivation);
                taskLink.orDurability(getDurability());
                taskLink.orQuality(getQuality());

                for (TermLink tl : termLinks.keySet()) {

                    tl.addPriority(spikeActivation);
                    tl.orDurability(getDurability());
                    tl.orQuality(getQuality());
                    activate(taskLink, tl, multiplier);

                }
            }
            else {
                //System.out.println(this + " at " + toBudgetString());

                //accumulate difference
                addPriority(spikeActivation);
            }



        }

        /** not used currently */
        public void forEachSubterm(TermVisitor consumeTerm) {

            final Term t = getTerm();
            if (t instanceof Compound) {
                Compound c = (Compound)t;
                c.recurseTerms(consumeTerm);
            }

        }

        public void forEachBelief(Consumer<Task> beliefConsumer) {
            beliefs.forEach(beliefConsumer);
        }

        protected void activate(TaskLink taskLink, TermLink termLink, float multiplier) {


            System.out.println(this + " activated by " + taskLink + "," + termLink + " @ " + getPriority() );

            //reset activationLevel because it has fired



            final Term thisTerm = getTerm();

            //for each outbound link

                Term term = termLink.getTerm();

                if (term.equals(thisTerm)) return;

                System.out.println("  spiking: " + term);

                AlannConcept c = (AlannConcept)memory.conceptualize(term, taskLink.getBudget());

                //trigger spike event (spike const * truth.confidence)
                c.onSpike(taskLink, multiplier * spikeDecay);

            //for each activeBelief
            //  activeBeliefs = getActivatedBeliefs() // belief links with active src and destination
            forEachBelief(b -> {
                /*
                new_tasks = do inference (lastTaskRx, activeBelief)
                for each newtask
                addTask(newTask)
                */

                //TODO

            });

            //setPriority(0);
            forget();
        }




        // the following should not be necessary and will be removed:


        @Override
        public Bag<Sentence, TaskLink> getTaskLinks() {
            return null;
        }

        @Override
        public Bag<TermLinkKey, TermLink> getTermLinks() {
            return null;
        }

        @Override
        public TaskLink activateTaskLink(TaskLinkBuilder taskLinkBuilder) {
            return null;
        }

        @Override
        public boolean linkTerms(Budget budgetRef, boolean b) {
            return false;
        }

        @Override
        public TermLink activateTermLink(TermLinkBuilder termLinkBuilder) {
            return null;
        }

        @Override
        public void updateLinks() {

        }

        @Override
        public boolean link(Task currentTask) {
            return false;
        }

        @Override
        public TermLinkBuilder getTermLinkBuilder() {
            return null;
        }

        @Override
        public TermLink nextTermLink(TaskLink taskLink) {
            return null;
        }

        @Override
        public BeliefTable getBeliefs() {
            return beliefs;
        }

        @Override
        public BeliefTable getGoals() {
            return null;
        }

        @Override
        public TaskTable getQuestions() {
            return null;
        }

        @Override
        public TaskTable getQuests() {
            return null;
        }



    }

//    static class AlannConceptProcess extends ConceptProcess {
//
//        public AlannConceptProcess(Memory memory, Concept concept, TaskLink taskLink) {
//            super(memory, concept, taskLink);
//        }
//
//        @Override
//        protected void afterDerive() {
//            super.afterDerive();
//
//            if (derived!=null && !derived.isEmpty())
//                System.out.println(this + " derived " + derived);
//        }
//
//        public void run(TermLink tl) {
//
//            beforeDerive();
//
//            derive();
//
//            if (tl != null)
//                processTerm(tl);
//
//            afterDerive();
//
//        }
//
//        @Override
//        protected void processTerms() {
//            //nothing
//        }
//    }

    class AlannCycle extends AbstractCycle {

        /**
         * holds original (user-input) goals and question tasks
         */
        protected final ItemAccumulator<Task> commands = new ItemAccumulator(new ItemComparator.Plus());

        Iterator<Task> commandCycle;

        /**
         * this is temporary if it can be is unified with the concept's main index
         */
        Bag<Term, Concept> concepts;


        @Override
        public boolean accept(Task t) {

            if (t.isInput() && !t.isJudgment()) {
                //match against commands or goals and adjust as necessary
                return commands.add(t);
            } else {
                return add(t);
            }

            /*
            {
                Update UI as required
                decrement priority of derived tasks
                Create new concepts if required
                trigger onTask event for respective concepts
            }
            */

        }

        public double getConceptActivationThreshold() {
            return conceptActivationThreshold;
        }

        @Override
        public void reset(Memory m, Perception p) {
            super.reset(m, p);

            if (concepts == null)
                concepts = new CurveBag(memory.random, MAX_CONCEPTS);
            else
                concepts.clear();

            commands.clear();
            commandCycle = Iterators.cycle(commands.items);
        }

        /**
         * an input belief, or a derived task of any punctuation
         */
        protected boolean add(Task t) {
            Concept c = memory.conceptualize(t.getTerm(), t.getBudget());
            if (c == null)
                return false;

            TaskProcess.run(memory, t);

            return true;
        }

        @Override
        public int size() {
            return concepts.size();
        }

        @Override
        public void conceptPriorityHistogram(double[] bins) {
            concepts.getPriorityHistogram(bins);
        }

        /** forever (slow cycle) “cycle” */
        @Override public void cycle() {



            // it needs to be thought of as a parallel system
            // if you process it sequentially then you have to store all the spikes for each cycle for each concept
            // However, if you think of it an an event based model then it is much easier
            // So concepts are, ideally, independent processing elements that receive events (spike/tasks)
            // It does not really translate to a loop.
            // So no ‘real’ cycles


            /*
                inject tasks and goals into respective concepts only questions and goals

                insert task into each relevant concept and replace existing as necessary
            */

            inputNextPerception();


            if (commandCycle.hasNext()) {
                for (int i = 0; i < commandsPerCycle; i++) {
                    Task nextCommand = commandCycle.next();
                    add(nextCommand);
                    //TaskProcess.run(memory, nextCommand);
                }
            }

            // for fast cycle in 1 to n “subcycle”
            for (int i = 0; i < subcyclesPerCycle; i++)
                subcycle();

        }

        protected void subcycle() {
            /*insert input tasks into respective concepts */

            for (Concept c : concepts) {

                AlannConcept a = (AlannConcept)c;

                //if level > threshold {
                /*if (a.active())*/ {

                    a.forEachBelief(b -> {

                        /*
                        send spike to all outbound belief links modulating for truth.confidence
                        reset activation
                        do inference on all beliefs that have a matching activated concept on the other end of the link
                        send generated (derived?) tasks to relevant concepts
                        */

                        float bconf = b.getTruth().getConfidence();

                        a.onSpike(new TaskLink(b, b.getBudget()), bconf);

                    });

                }

                /*





                }

                */
            }

        }


        @Override
        public Concept conceptualize(final Term term, Budget budget, boolean createIfMissing) {
            return (AlannConcept)conceptualize(term, budget, createIfMissing, memory.time(), concepts);
        }


        @Override
        public Concept nextConcept() {
            return null;
        }

        @Override
        public boolean reprioritize(Term term, float newPriority) {
            return false;
        }

        @Override
        public Concept remove(Concept c) {
            return concepts.remove(c.getTerm());
        }

        @Override
        protected void on(Concept c) {

        }

        @Override
        protected void off(Concept c) {

        }

        @Override
        protected boolean active(Term t) {
            return concepts.get(t)!=null;
        }

        @Override
        public Iterator<Concept> iterator() {
            return concepts.iterator();
        }
    }
}
