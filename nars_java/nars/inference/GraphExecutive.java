package nars.inference;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import nars.core.EventEmitter.Observer;
import nars.core.Events.ConceptGoalAdd;
import nars.core.Events.ConceptGoalRemove;
import nars.core.Events.CycleEnd;
import nars.core.Memory;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.io.buffer.PriorityBuffer;
import nars.language.Implication;
import nars.language.Term;
import nars.operator.Operation;
import nars.util.graph.ImplicationGraph;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.traverse.ClosestFirstIterator;



public class GraphExecutive implements Observer {

    ImplicationGraph implication;
    PriorityBuffer<Task> tasks;
    int numTasks = 32;
    private final Memory memory;
    
    public GraphExecutive(Memory memory) {        
        super();
        
        this.memory = memory;
        tasks = new PriorityBuffer(new Comparator<Task>() {
            @Override public final int compare(final Task a, final Task b) {
                float bp = b.getPriority();
                float ap = a.getPriority();
                if (bp!=ap)
                    return Float.compare(bp, ap);
                else {
                    float ad = a.getDurability();
                    float bd = b.getDurability();
                    return Float.compare(bd, ad);
                }
                     
            }            
        }, numTasks);
                
        implication = new ImplicationGraph(memory);
        memory.event.on(CycleEnd.class, this);
        memory.event.on(ConceptGoalAdd.class, this);
        memory.event.on(ConceptGoalRemove.class, this);
    }

    /** Add plausibility estimation */
    public void decisionMaking(final Task task, final Concept concept) {    
        tasks.add(task.clone());
        System.out.println("decisionMaking: " + task + " from " + concept);
    }
    
    @Override
    public void event(Class event, Object[] a) {
        
        if (event == ConceptGoalAdd.class) {
            Task t = (Task)a[2];
            /*if (!t.isInput())*/ {
                System.out.println("Goal add: " + a[0] + " " + a[1] +  " " + t.budget.getPriority());
                tasks.add(t.clone());
            }
        }
        else if (event == ConceptGoalRemove.class) {
            Task t = (Task)a[2];
            System.out.println("Goal rem: " + a[0] + " " + a[1] +  " " + t.budget);
            tasks.remove(t);
        }
        else if (event == CycleEnd.class) {
//            if (tasks.size() > 0) {
//                plan();
//            }
        }
    }
    
    protected void plan(Task task, Task newEvent) {
        System.out.println("plan: task=" + task + " newEvent=" + newEvent);        
                
        //implication.add(task.sentence);
        
        Term t = task.getContent();
        if ((t!=null) && (t instanceof Implication)) {
            Implication i = (Implication)t;
            Term target;
            if (i.getTemporalOrder() == TemporalRules.ORDER_FORWARD) {
                target = i.getPredicate();
            }
            else {
                //TODO reverse it
                target = i.getSubject();
            }
            
            if (target!=null) {
                double maxRadius = 16;
                
                //Traverse graph from target
                System.out.println("TRAVERSING to : " + target);
                ClosestFirstIterator<Term,Sentence> cfi = new ClosestFirstIterator<Term,Sentence>(new EdgeReversedGraph(implication), target, maxRadius);
                
                //TODO use a sorted list of furthest candidates
                double furthestDistance = 0;
                Term furthestTerm = null;
                while (cfi.hasNext()) {
                    Term v = cfi.next();
                    
                    double length = cfi.getShortestPathLength(v);
                    if (length > furthestDistance) {
                        furthestDistance = length;
                        furthestTerm = v;
                    }
                    
                }
                
                System.out.println("  from: " + furthestTerm + " via " + cfi.getSpanningTreeEdge(furthestTerm) + ", length=" + furthestDistance);
                
                //calculate path back to target: TODO make as method of a ClosestFirstIterator subclass
                List<Term> path = new ArrayList();
                Term current = furthestTerm;
                while (current!=target) {
                    path.add(current);
                    current = implication.getEdgeTarget(cfi.getSpanningTreeEdge(current));
                }
                path.add(current);
                
                System.out.println("Path=" + path);
                
            
            }
        }
        
//        System.out.println("Goals");        
//        for (Task t : tasks) {
//            System.out.println(t + " " + t.getParentBelief());
//            //System.out.println(getImplicationPath(t.getParentBelief()));
//        }
//        System.out.println();        
    }
            
    
//    public String getImplicationPath(Sentence s) {
//        Term t = s.content;
//        if (t instanceof Implication) {
//            return getImplicationPath(((Implication)t).getPredicate());                   
//        }
//        else {
//            return getImplicationPath(t);
//        }
//        //return "";
//    }
//    public String getImplicationPath(Term t) {
//        KShortestPaths ksp = new KShortestPaths(implication, t, 5);
//        return ksp.getPaths(t).toString();
//        
//    }
//    

    public void reset() {
    }

    public void manageExecution() {
    }

    public boolean isActionable(final Task newEvent, Memory mem) {
        /*if(!((newEvent.isInput()) || (newEvent.getCause()!=null))) {
            return false;
        }*/
        
        Term newcontent=newEvent.sentence.content;
        if(newcontent instanceof Operation) {
            Term pred=((Operation)newcontent).getPredicate();
            if(pred.equals(mem.getOperator("^want")) || pred.equals(mem.getOperator("^believe"))) {
                return false;
            }
        }
                
        plan(newEvent, null);
        
        return true;
    }

    public boolean isActionable(final Task task, final Task newEvent) {
                
        plan(task, newEvent);        
        return true;
        
        /*
        if (task.sentence.stamp.getOccurrenceTime() == Stamp.ETERNAL) {
            return false;
        }
        
        if (!task.sentence.isJudgment()) {
            return false;
        }
        */
            
        
//        if ((newEvent == null)
//                || (rankBelief(newEvent.sentence) < rankBelief(task.sentence))) {
//            
//            return true;
//            
//            /*return 
//                ((shortTermMemory.isEmpty()                     
//                    ||                    
//                !equalSubTermsInRespectToImageAndProduct(
//                            shortTermMemory.getLast().getContent(),
//                            task.getContent()))
//            );
//                    */
//        }        
        
        //return false;        
    }

    public boolean planShortTerm(Task newEvent, Memory aThis) {
        
        return true;
    }
    
}
