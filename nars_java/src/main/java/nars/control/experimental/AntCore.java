/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.control.experimental;

import javolution.context.ConcurrentContext;
import nars.Memory;
import nars.Global;
import nars.budget.Budget;
import nars.nal.*;
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
    
    public final Random random = Memory.randomNumber;

    public final Deque<Ant> ants = new ArrayDeque();
    
    /** prevents Ants from moving to a concept in which another Ant occupies */
    public final HashMap<Term,Ant> occupied = new HashMap();
    
    float cycleSpeed;
    float conceptVisitDelivery;

    @Override
    public double conceptMass() {
        //TODO implement
        return 0;
    }

    public AntCore(int numAnts, float cycleSpeed, int maxConcepts) {
        super(maxConcepts);
                
        this.cycleSpeed = cycleSpeed;
        this.conceptVisitDelivery = 1.0f / numAnts;

        for (int i = 0; i < numAnts; i++) {
            Ant a = new Ant(    ((1+i) / ((double)(1+numAnts))), true, true );
            ants.add(a);
        }
    }

    @Override public void init(Memory m) {
        super.init(m);
        concepts.setTargetActivated((int) (ants.size() * 0.1f));
    }

    @Deprecated final Deque<Task> tasks = new ArrayDeque();

    @Override
    public void addTask(Task t) {
        tasks.addLast(t);
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
                if (t.aboveThreshold()) {
                    run.add(new DirectProcess(memory, t));
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
        Concept concept = null;
        
        boolean traverseTermLinks = true;
        boolean traverseTaskLinks = true;
        boolean allowLoops = false;
        double randomConceptProbability = 0.01;
        
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

            if (concept!=null)
                occupied.remove(concept.getTerm());

            concept = null;
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

                if (c == concept) {
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

            concept = c;

        }
        
        boolean inConcept() { return (link==null); } 
        boolean inLink() { return (link!=null); }
        
        /** returns how many tasks were queued */
        public int cycle(float dt, List<Runnable> queue) {
 
            int queueBefore = queue.size();
            
            boolean c = inConcept();
            boolean l = inLink();
            
            
            if ((c) && (concept!=null)) {
                               
                onConcept(concept, eta, queue);
                
                eta -= dt * speed;
                
                if (eta < 0) {
                    leaveConcept(nextLink(), queue);
                }
                
            }
            else if (l) {
                
                onLink(link, eta, queue);

                eta -= dt * speed;
                
                if (eta < 0) {
                    enterConcept(concept, queue);
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
                
                Sentence taskSentence = taskLink.getTarget().sentence;
                Sentence parentSentence = taskLink.getTarget().parentBelief;
                Sentence bestSolution = taskLink.getTarget().getBestSolution();
                
                Concept t = null;
                if (taskSentence!=null)
                    t = concept(taskSentence.term);               
                if ((t == null) || (t == concept)) {
                    if (bestSolution!=null) {
                        t = concept(bestSolution.term);
                    }
                }
                if ((t == null) || (t == concept)) {
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
            
            if (random.nextDouble() < randomConceptProbability) {
                return null;
            }
            
            if ((traverseTermLinks) && (!traverseTaskLinks)) {
                ii = concept.termLinks;
            }
            else if ((!traverseTermLinks) && (traverseTaskLinks)) {
                ii = concept.taskLinks;
            }
            else {
                if (random.nextInt() % 2 == 0) {
                    ii = concept.termLinks;
                }
                else
                    ii = concept.taskLinks;
            }
            return (TLink)Item.selectRandomByPriority(ii);
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
            int i = (int)(links.size() * Math.random());
            return links.get(i);
        }
        */
        
        void enterConcept(Concept c, List<Runnable> queue) {
            Concept previous = concept;
            
            concept = c;
            
            if ((c == null) || ((!allowLoops) && previous.equals(c)))  {
                goRandomConcept(queue);
                return;                
            }
                        
            //tlink remains the same
            eta = concept.getPriority();     
            onConcept(c, eta, queue);
        }
        
        void leaveConcept(TLink viaLink, List<Runnable> queue) {
            if (viaLink == null) {
                goRandomConcept(queue);
                return;
            }
            else {
                if (viaLink instanceof TermLink) {
                    concept.termLinks.putBack((TermLink)viaLink, memory.param.cycles(memory.param.termLinkForgetDurations), memory);
                }
                else if (viaLink instanceof TaskLink) {
                    concept.taskLinks.putBack((TaskLink)viaLink, memory.param.cycles(memory.param.taskLinkForgetDurations), memory);
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
            if (concept!=null)
                occupied.remove(concept.getTerm());

            if (occupied.containsKey(ct)) {
                return concept = null;
            }

            Concept nextC = conceptualize(delivery, ct, false);
            if (nextC == null)
                return concept = null;

            occupied.put(nextC.getTerm(), this);

            return concept = nextC;
        }

        @Override
        public String toString() {
            return "   {" + 
                    (link!=null ? ((Item)link).name() : null)  + " <<< " + 
                    (concept!=null ? concept.name() : null) +
                    " | " + eta + ' ' + (inConcept() ? "concept" : (inLink() ? "tlink" : "")) + '}';
        }

                
    }
    
}
