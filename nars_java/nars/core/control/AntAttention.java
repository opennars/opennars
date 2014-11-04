/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.core.control;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
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
import nars.storage.CurveBag;

/**
 *
 * @author me
 */
public class AntAttention extends WaveAttention {

    Deque<Ant> ants = new ArrayDeque();
    float cycleSpeed = 1.0f;
    
    public AntAttention(int maxConcepts, ConceptBuilder conceptBuilder) {
        super(maxConcepts, conceptBuilder);
        
        this.concepts = new CurveBag(1000, true);
        
        //int numAnts = maxConcepts / 50;
        int numAnts = 1;
        for (int i = 0; i < numAnts; i++) {
            Ant a = new Ant(    ((1+i) / ((double)(1+numAnts)))   );
            ants.add(a);
        }
    }
    
    

    @Override
    public void cycle() {
        run.clear();
        
        memory.processNewTasks(newTaskPriority, run);
                
        memory.processNovelTasks(novelTaskPriority, run);
        
        
        
        for (Ant a : ants) {
            a.cycle(cycleSpeed, run);                 
        }
        System.out.println(ants);
        System.out.println(run);
        
            
        
        memory.run(run, Parameters.THREADS);
        
    }
    
    
    
    
    /** intelligent visitor */
    public class Ant {
        
        TLink link = null;
        Concept source = null;
        Concept target = null;
        
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

        public Ant(double speed) {
            this.speed = speed;                        
        }
        
        void randomConcept(List<Runnable> queue) {
            
            
            source = null;
            target = null;
            link = null;
            
            Concept c = concepts.takeNext();
            if (c == null) {
                System.out.println("NOTHING: " + concepts.size());
                return;
            }
            
            System.out.println("RANDOM: " + c);
                        
            concepts.putBack(c, memory.param.conceptForgetDurations.getCycles(), memory);
            
            target = c;

        }
        
        boolean inConcept() { return (source==null) && (target != null); } 
        boolean inLink() { return (source!=null) && (target != null); }
        
        void cycle(float dt, List<Runnable> queue) {
 
        
            boolean c = inConcept();
            boolean l = inLink();
            
            
            if (c) {
                               
                System.out.println("FIRE SOURCE=" + target);
                onConcept(target, eta, queue);
                
                eta -= dt * speed;
                
                if (eta < 0) {
                    leaveConcept(randomLink(), queue);
                }
                
            }
            else if (l) {
                
                onLink(link, eta, queue);

                eta -= dt * speed;
                
                if (eta < 0) {
                    enterConcept(target, queue);
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
                if ((t == null) || (t == target)) {
                    if (bestSolution!=null) {
                        t = concept(bestSolution.content);
                    }
                }
                if ((t == null) || (t == target)) {
                    if (parentSentence!=null) {
                        t = concept(parentSentence.content);
                    }
                }
                
                if (t!=null) {
                    System.out.println("tasklink: " + taskLink + " bestsolution=" + t);
                    queue.add(new FireConcept(memory, t, 1) {                    
                        @Override public void onFinished() {                }
                    });        
                }

            }
        }
        
        public TLink randomLink() {
            List<TLink> links = new ArrayList();
            if (traverseTermLinks)
                links.addAll(target.termLinks.values());
            if (traverseTaskLinks)
                links.addAll(target.taskLinks.values());           
            
            if (links.isEmpty()) return null;
            
            //TODO weighted probability selection
            int i = (int)(links.size() * Math.random());
            return links.get(i);
        }
        
        void enterConcept(Concept c, List<Runnable> queue) {
            Concept previous = source;
            
            source = null;
            target = c;
            
            if ((c == null) || ((!allowLoops) && previous.equals(c)))  {
                randomConcept(queue);
                return;                
            }
                        
            //link remains the same
            eta = target.getPriority();     
            onConcept(c, eta, queue);
        }
        
        void leaveConcept(TLink viaLink, List<Runnable> queue) {
            if (viaLink == null)
                randomConcept(queue);
            
            if (viaLink instanceof TermLink) {
                target.termLinks.putBack((TermLink)viaLink, memory.param.beliefForgetDurations.getCycles(), memory);                               
            }
            else if (viaLink instanceof TaskLink) {
                target.taskLinks.putBack((TaskLink)viaLink, memory.param.taskForgetDurations.getCycles(), memory);        
            }
            
            eta = viaLink.getPriority();
            link = viaLink;
            
            source = target;                        
            target = getConcept(viaLink.getTarget(), new BudgetValue(getConceptVisitDelivery(), 0.5f, 0.5f));
            /*if (target!=null)
                System.out.println("  concept: " + viaLink.getTarget() + " -> " + target);*/
            onLink(link, eta, queue);
        }
        
        public float getConceptVisitDelivery() {
            return 0.5f;
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
            return "{" + 
                    (source!=null ? source.name() : null) + 
                    " >>>> " + (link!=null ? ((Item)link).name() : null)  + " >>>> " + 
                    (target!=null ? target.name() : null) +
                    " | " + eta + " " + (inConcept() ? "concept" : (inLink() ? "link" : "")) + "}";            
        }
        
        
    }
    
}
