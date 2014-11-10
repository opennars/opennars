/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.core.control;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import nars.core.Memory;
import nars.core.Parameters;
import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.entity.ConceptBuilder;
import nars.entity.Item;
import nars.entity.Sentence;
import nars.entity.TLink;
import nars.entity.Task;
import nars.entity.TaskLink;
import nars.entity.TermLink;
import nars.language.Term;

/**
 *
 * @author me
 */
public class AntAttention extends WaveAttention {

    public final Deque<Ant> ants = new ArrayDeque();
    
    float cycleSpeed;
    float conceptVisitDelivery = 0.5f;
        
    public AntAttention(int numAnts, float cycleSpeed, int maxConcepts, ConceptBuilder conceptBuilder) {
        super(maxConcepts, conceptBuilder);
                
        this.cycleSpeed = cycleSpeed;

        for (int i = 0; i < numAnts; i++) {
            Ant a = new Ant(    ((1+i) / ((double)(1+numAnts))), true, true );
            ants.add(a);
        }
    }

    @Override public void init(Memory m) {
        super.init(m);
        concepts.setTargetActivated( (int)(ants.size() * 0.1f * concepts.getCapacity()) );
    }

    @Override
    public void cycle() {
        run.clear();
        
        memory.processNewTasks(newTaskPriority, run);
                
        memory.processNovelTasks(novelTaskPriority, run);
        
        
        
        for (Ant a : ants) {
            a.cycle(cycleSpeed, run);                 
        }
        //System.out.println(ants);
        //System.out.println(run);
        
            
        
        memory.run(run, Parameters.THREADS);
        
    }
    
    
    
    
    /** intelligent visitor */
    public class Ant {
        
        TLink link = null;        
        Concept concept = null;
        
        boolean traverseTermLinks = true;
        boolean traverseTaskLinks = true;
        boolean allowLoops = false;
        
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
                
        void randomConcept(List<Runnable> queue) {
            
            
            concept = null;
            link = null;
            
            Concept c = concepts.takeNext();
            
            if (c == null) {
                return;
            }
                                    
            concepts.putBack(c, memory.param.cycles(memory.param.conceptForgetDurations), memory);
            
            concept = c;

        }
        
        boolean inConcept() { return (link==null); } 
        boolean inLink() { return (link!=null); }
        
        void cycle(float dt, List<Runnable> queue) {
 
        
            boolean c = inConcept();
            boolean l = inLink();
            
            
            if ((c) && (concept!=null)) {
                               
                onConcept(concept, eta, queue);
                
                eta -= dt * speed;
                
                if (eta < 0) {
                    leaveConcept(randomLink(), queue);
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
                randomConcept(queue);                
            }
            
        }
        
        void onConcept(Concept c, double progress, List<Runnable> queue) {
            queue.add(new FireConcept(memory, c, 1) {                    
                @Override public void onFinished() {                }
            });
        }
        
        void onLink(TLink l, double progress, List<Runnable> queue) {
            if (l instanceof TaskLink) {
                TaskLink taskLink = (TaskLink)l;
                
                Sentence taskSentence = taskLink.getTarget().sentence;
                Sentence parentSentence = taskLink.getTarget().parentBelief;
                Sentence bestSolution = taskLink.getTarget().getBestSolution();
                
                Concept t = null;
                if (taskSentence!=null)
                    t = concept(taskSentence.content);               
                if ((t == null) || (t == concept)) {
                    if (bestSolution!=null) {
                        t = concept(bestSolution.content);
                    }
                }
                if ((t == null) || (t == concept)) {
                    if (parentSentence!=null) {
                        t = concept(parentSentence.content);
                    }
                }
                
                if (t!=null) {                    
                    queue.add(new FireConcept(memory, t, 1) {                    
                        @Override public void onFinished() {                }
                    });        
                }

            }
        }
        
        public TLink randomLink() {
            Iterable<? extends Item> ii;
            if ((traverseTermLinks) && (!traverseTaskLinks)) {
                ii = concept.termLinks;
            }
            else if ((!traverseTermLinks) && (traverseTaskLinks)) {
                ii = concept.taskLinks;
            }
            else {
                if (Math.random() % 2 == 0) {
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
            int i = (int)(links.size() * Math.random());
            return links.get(i);
        }
        */
        
        void enterConcept(Concept c, List<Runnable> queue) {
            Concept previous = concept;
            
            concept = c;
            
            if ((c == null) || ((!allowLoops) && previous.equals(c)))  {
                randomConcept(queue);
                return;                
            }
                        
            //link remains the same
            eta = concept.getPriority();     
            onConcept(c, eta, queue);
        }
        
        void leaveConcept(TLink viaLink, List<Runnable> queue) {
            if (viaLink == null) {
                randomConcept(queue);
                return;
            }
            
            if (viaLink instanceof TermLink) {
                concept.termLinks.putBack((TermLink)viaLink, memory.param.cycles(memory.param.termLinkForgetDurations), memory);                               
            }
            else if (viaLink instanceof TaskLink) {
                concept.taskLinks.putBack((TaskLink)viaLink, memory.param.cycles(memory.param.taskLinkForgetDurations), memory);        
            }
            
            eta = viaLink.getPriority();
            link = viaLink;
                        
            concept = getConcept(viaLink.getTarget(), new BudgetValue(getConceptVisitDelivery(), 0.5f, 0.5f));
            
            onLink(link, eta, queue);
        }
        
        public float getConceptVisitDelivery() {
            return (float)(speed * conceptVisitDelivery);
        }                
        
        protected Concept getConcept(Object x, BudgetValue delivery) {
            if (x instanceof Term) {
                return conceptualize(delivery, (Term)x, false);
            }
            else if (x instanceof Task) {
                Task t = (Task)x;
                return conceptualize(delivery, t.getContent(), false);
            }
            return null;
        }

        @Override
        public String toString() {
            return "   {" + 
                    (link!=null ? ((Item)link).name() : null)  + " <<< " + 
                    (concept!=null ? concept.name() : null) +
                    " | " + eta + " " + (inConcept() ? "concept" : (inLink() ? "link" : "")) + "}";            
        }

        
        
    }
    
}
