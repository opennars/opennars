/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.model.cycle.experimental;

import javolution.context.ConcurrentContext;
import nars.Memory;
import nars.Global;
import nars.budget.Budget;
import nars.nal.*;
import nars.nal.process.TaskProcess;
import nars.nal.term.Termed;
import nars.nal.concept.Concept;
import nars.nal.tlink.TLink;
import nars.nal.tlink.TaskLink;
import nars.nal.tlink.TermLink;
import nars.nal.term.Term;

import java.util.*;

/**
 * Multiprocessor-capable NAR controller.
 * 
 * Named Ant-core because the activity pattern resembles a collection of 
 * simulated Ants navigating through the concept network
 */
public class AntCore extends ConceptWaveCore {

    public final Deque<Ant> ants = new ArrayDeque();
    
    /** prevents Ants from moving to a concept in which another Ant occupies */
    public final HashMap<Term,Ant> occupied = new HashMap();

    @Deprecated final Deque<Task> tasks = new ArrayDeque();

    float cycleSpeed;
    float conceptVisitDelivery;


    public AntCore(int numAnts, float cycleSpeed, int maxConcepts) {
        super(maxConcepts);
                
        this.cycleSpeed = cycleSpeed;
        this.conceptVisitDelivery = 1.0f / numAnts;

        for (int i = 0; i < numAnts; i++) {
            Ant a = new Ant(    ((1+i) / ((double)(1+numAnts))), true, true );
            ants.add(a);
        }
    }

    @Override
    public void reset(Memory m, boolean delete) {
        super.reset(m, delete);
        concepts.setTargetActivated((int) (ants.size() * 0.1f));
    }
    @Override
    public boolean addTask(Task t) {
        tasks.addLast(t);
        return true;
    }

    @Override
    public synchronized void cycle() {
        
        int numNew, numNovel, numConcepts = 0, other;

        memory.perceiveNext(memory.param.inputsMaxPerCycle.get());

        if (!tasks.isEmpty()) {
            int maxNewTasks = Math.min(tasks.size(), ants.size());
            for (int i = 0; i < maxNewTasks && !tasks.isEmpty(); ) {
                Task t = tasks.removeFirst();
                if (t == null) break;

                TaskProcess tp = TaskProcess.get(memory, t);
                if (tp != null) {
                    run.add(new TaskProcess(memory, t));
                    i++;
                }
            }
        }

        memory.runNextTasks(); //probably want to batch this with the next tasks below.. but this will work for now
        
        for (Ant a : ants) {            
            numConcepts += a.cycle(cycleSpeed, run);
        }

        /*
        long t = memory.time();
        if (t % 10 == 0)
            System.out.println(t+": "+ run.size() + "[" + numNew + "|" + numNovel + "|" + numConcepts + "|" + other);
        */

        if (run.isEmpty()) return;

        if (Global.THREADS == 1) {
            for (Runnable r : run) r.run();
        }
        else {
            final ConcurrentContext ctx = ConcurrentContext.enter();
            ctx.setConcurrency(Global.THREADS);
            try {
                for (Runnable r : run) ctx.execute(r);
            } finally {
                ctx.exit();
            }
        }

        run.clear();        
        
    }

    @Override
    public Concept conceptualize(Budget budget, Term term, boolean createIfMissing) {
        Concept c = super.conceptualize(budget, term, createIfMissing);
        /*if (c!=null) {
            immediate.add(c);
        }*/
        return c;
    }

    @Override
    public Concept remove(Concept c) {
        return concepts.remove(c.getTerm());
    }

    public boolean ensureAntsOccupyUniqueConcepts() {
        int numConcepts = occupied.size();
        int uniqueOccupants = Global.newHashSet(occupied.values()).size();
        boolean fair = numConcepts == uniqueOccupants;
        if (!fair) {
            System.err.println("occupied concepts = " + numConcepts + ", unique registered ants = " + uniqueOccupants + ", total ants = " + ants.size());
            for (Map.Entry<Term, Ant> e : occupied.entrySet()) {
                System.err.println(e.getValue() + " @ " + e.getKey());
            }
            System.err.println(occupied);
            throw new RuntimeException("Ants violated occupation rules");
        }
        return true;
    }
    
    
    
    /** intelligent visitor */
    public class Ant {
        
        TLink link = null;
        Concept c = null;
        
        boolean traverseTermLinks = true;
        boolean traverseTaskLinks = true;
        boolean allowLoops = false;
        float randomConceptProbability = 0.01f;
        
        /**
         * ETA = estimated time of arrival
         * 1.0: start at source concept, <= 0.0: reached target concept
         */
        double eta; 
        
        /**
         * position units per cycle time
         */
        double speed; 

        public Ant(double speed, boolean traverseTermLinks, boolean traverseTaskLinks) {
            this.traverseTermLinks = traverseTermLinks;
            this.traverseTaskLinks = traverseTaskLinks;
            this.speed = speed;                        
        }
                
        synchronized void goRandomConcept(List<Runnable> queue) {

            if (c !=null)
                occupied.remove(c.getTerm());

            c = null;
            link = null;

            //check if there are enough remaining concepts to transition to */
            if (concepts.size() - occupied.size() - 1 /* an extra one to include this concept) */ <= 0)
                return;

            Concept c;

            boolean validDestination = false;

            int maxTries = 2;
            int tries = 0;
            do {

                c = concepts.pop();

                if (c == null)
                    return;

                if (c == this.c) {
                    //no need to move to self
                }
                else {
                    Ant occupier = occupied.get(c.getTerm());
                    //if ((occupier!=null) || (occupier!=null && this==occupier)) {
                    if ((occupier!=null) /*|| (occupier!=null && this==occupier)*/) {
                        //occupied, or it's me
                    }
                    else {
                        validDestination = true;
                    }
                }

                concepts.putBack(c, memory.param.cycles(memory.param.conceptForgetDurations), memory);

                if (tries++ == maxTries)
                    return;

            } while (!validDestination);



            occupied.put(c.getTerm(), this);

            this.c = c;

        }
        
        boolean inConcept() { return (link==null); } 
        boolean inLink() { return (link!=null); }
        
        /** returns how many tasks were queued */
        public int cycle(float dt, List<Runnable> queue) {
 
            int queueBefore = queue.size();
            
            boolean c = inConcept();
            boolean l = inLink();
            
            
            if ((c) && (this.c !=null)) {
                               
                onConcept(this.c, eta, queue);
                
                eta -= dt * speed;
                
                if (eta < 0) {
                    leaveConcept(nextLink(), queue);
                }
                
            }
            else if (l) {
                
                onLink(link, eta, queue);

                eta -= dt * speed;
                
                if (eta < 0) {
                    enterConcept(this.c, queue);
                }
                
            }
            else {
                goRandomConcept(queue);                
            }
            
            int queueAfter = queue.size();
            return queueAfter - queueBefore;
        }

        protected void fireConcept(Concept c, List<Runnable> queue) {
//            queue.add(new FireConcept(memory, t, 1) {
//                @Override public void beforeFinish() {                }
//            });

        }
        void onConcept(Concept c, double progress, List<Runnable> queue) {
            //TODO fire concept
            //fireConcept(c, queue);
        }
        
        void onLink(TLink l, double progress, List<Runnable> queue) {
            if (l instanceof TaskLink) {
                TaskLink taskLink = (TaskLink)l;
                
                Sentence taskSentence = taskLink.getTask().sentence;
                Sentence parentSentence = taskLink.getTask().parentBelief;
                Sentence bestSolution = taskLink.getTask().getBestSolution();
                
                Concept t = null;
                if (taskSentence!=null)
                    t = concept(taskSentence.term);
                if ((t == null) || (t == c)) {
                    if (bestSolution!=null) {
                        t = concept(bestSolution.term);
                    }
                }
                if ((t == null) || (t == c)) {
                    if (parentSentence!=null) {
                        t = concept(parentSentence.term);
                    }
                }
                
                if (t!=null) {
                    fireConcept(t, queue);
                }

            }
        }
        
        
        /** return null to be relocated to a random concept */
        public TLink nextLink() {
            Iterable<? extends Item> ii;
            
            if (memory.random.nextFloat() < randomConceptProbability) {
                return null;
            }
            
            if ((traverseTermLinks) && (!traverseTaskLinks)) {
                ii = c.getTermLinks();
            }
            else if ((!traverseTermLinks) && (traverseTaskLinks)) {
                ii = c.getTaskLinks();
            }
            else {
                if (memory.random.nextInt() % 2 == 0) {
                    ii = c.getTermLinks();
                }
                else
                    ii = c.getTaskLinks();
            }
            return (TLink)Item.selectRandomByPriority(memory, ii);
        }
        
        /*
        public TLink randomLink() {
            List<TLink> links = new ArrayList();
            
            if (traverseTermLinks)
                links.addAll(concept.termLinks.values());
            if (traverseTaskLinks)
                links.addAll(concept.taskLinks.values());           
            
            if (links.isEmpty()) return null;
            
            //TODO weighted probability selection
            //TODO use Memory.random instance not Math.random
            int i = (int)(links.size() * Memory.randomNumber.nextDouble()s);
            return links.get(i);
        }
        */
        
        void enterConcept(Concept c, List<Runnable> queue) {
            Concept previous = this.c;
            
            this.c = c;
            
            if ((c == null) || ((!allowLoops) && previous.equals(c)))  {
                goRandomConcept(queue);
                return;                
            }
                        
            //tlink remains the same
            eta = this.c.getPriority();
            onConcept(c, eta, queue);
        }
        
        void leaveConcept(TLink viaLink, List<Runnable> queue) {
            if (viaLink == null) {
                goRandomConcept(queue);
                return;
            }
            else {
                if (viaLink instanceof TermLink) {
                    c.getTermLinks().putBack((TermLink) viaLink, memory.param.cycles(memory.param.termLinkForgetDurations), memory);
                }
                else if (viaLink instanceof TaskLink) {
                    c.getTaskLinks().putBack((TaskLink) viaLink, memory.param.cycles(memory.param.taskLinkForgetDurations), memory);
                }

                eta = viaLink.getPriority();
                link = viaLink;
                Termed target = viaLink.getTarget();
                viaLink = null;
                if (goNextConcept(target, new Budget(getConceptVisitDelivery(), 0.5f, 0.5f)) == null)
                    return;

                onLink(link, eta, queue);
            }



        }
        
        public float getConceptVisitDelivery() {
            return (float)(speed * conceptVisitDelivery);
        }                
        
        protected synchronized Concept goNextConcept(Termed x, Budget delivery) {

            Term ct = x.getTerm();
            if (c !=null)
                occupied.remove(c.getTerm());

            if (occupied.containsKey(ct)) {
                return c = null;
            }

            Concept nextC = conceptualize(delivery, ct, false);
            if (nextC == null)
                return c = null;

            occupied.put(nextC.getTerm(), this);

            return c = nextC;
        }

        @Override
        public String toString() {
            return "   {" + 
                    (link!=null ? ((Item)link).name() : null)  + " <<< " + 
                    (c !=null ? c.name() : null) +
                    " | " + eta + ' ' + (inConcept() ? "concept" : (inLink() ? "tlink" : "")) + '}';
        }

                
    }
    
}
