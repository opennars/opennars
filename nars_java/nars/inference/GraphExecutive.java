package nars.inference;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import nars.core.EventEmitter.Observer;
import nars.core.Events.ConceptGoalAdd;
import nars.core.Events.ConceptGoalRemove;
import nars.core.Events.CycleEnd;
import nars.core.Memory;
import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.entity.Stamp;
import nars.entity.Task;
import nars.entity.TruthValue;
import nars.io.buffer.PriorityBuffer;
import nars.language.Conjunction;
import nars.language.Implication;
import nars.language.Interval;
import nars.language.Term;
import nars.operator.Operation;
import nars.util.graph.ImplicationGraph;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.traverse.ClosestFirstIterator;

public class GraphExecutive implements Observer {

    public final ImplicationGraph implication;
    PriorityBuffer<Task> tasks;
    int numTasks = 32;
    private final Memory memory;

    public GraphExecutive(Memory memory) {
        super();

        this.memory = memory;
        tasks = new PriorityBuffer(new Comparator<Task>() {
            @Override
            public final int compare(final Task a, final Task b) {
                float bp = b.getPriority();
                float ap = a.getPriority();
                if (bp != ap) {
                    return Float.compare(bp, ap);
                } else {
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

    /**
     * Add plausibility estimation
     */
    public void decisionMaking(final Task task, final Concept concept) {
        tasks.add(task.clone());
        System.out.println("\n" + memory.getTime() + " decisionMaking: " + task + " from " + concept);
        plan(task, task.getContent());
        System.out.println("\n");
    }

    @Override
    public void event(Class event, Object[] a) {

        if (event == ConceptGoalAdd.class) {
            Task t = (Task) a[2];
            /*if (!t.isInput())*/ 
            {
            System.out.println("Goal add: " + a[0] + " " + a[1] + " " + t.budget.getPriority());
            tasks.add(t.clone());
            
            
            }
            
        } else if (event == ConceptGoalRemove.class) {
            Task t = (Task) a[2];
            System.out.println("Goal rem: " + a[0] + " " + a[1] + " " + t.budget);
            tasks.remove(t);
        } else if (event == CycleEnd.class) {
//            if (tasks.size() > 0) {
//                plan();
//            }
        }
    }

    protected List<Term> plan(Term target) {
        double maxRadius = 32;

        ClosestFirstIterator<Term, Sentence> cfi = new ClosestFirstIterator<Term, Sentence>(new EdgeReversedGraph(implication), target, maxRadius);

        //TODO use a sorted list of furthest candidates
        double furthestDistance = 0;
        Term furthestTerm = null;
        while (cfi.hasNext()) {
            Term v = cfi.next();

            double length = cfi.getShortestPathLength(v);
            if (length == 0) continue;
            
            //System.out.println("  --" + v + " dist=" + length + " spanEdge=" + cfi.getSpanningTreeEdge(v));
            
            if (length > furthestDistance) {
                furthestDistance = length;
                furthestTerm = v;
                
            }
            
            
        }

        //System.out.println("  from: " + furthestTerm + " via " + cfi.getSpanningTreeEdge(furthestTerm) + ", length=" + furthestDistance);

        //calculate path back to target: TODO make as method of a ClosestFirstIterator subclass
        List<Term> path = new ArrayList();
        Term current = furthestTerm;
        Sentence currentEdge = null;
        int operations = 0;
        while (current != target) {

            boolean isOperation = (current instanceof Operation);
            if (isOperation)
                operations++;

            //only include Operations and Intervals
            if (isOperation || (current instanceof Interval)) {
                path.add(current);
                //System.out.println(" ++ " + current);
            }
            else if (!current.equals(target)) {
                //transclude best subpath iff vertex has other preconditions
                Set<Sentence> preconditions = implication.incomingEdgesOf(current);
                for (Sentence s : preconditions) {
                    if (s!=currentEdge) {
                        //System.out.println("  precondition: " + current + " = " + s);
                        Term preconditionSource = implication.getEdgeSource(s);
                        List<Term> preconditionPlan = plan(preconditionSource);
                        path.addAll(preconditionPlan);
                        path.add(preconditionSource);
                        
                    }
                    
                }
                //path.add(current);
            }

            currentEdge = cfi.getSpanningTreeEdge(current);
            if (currentEdge == null) {
                //Should mean we have returned to target
                //System.err.println(current + " has no spanning edge to " + target);
                break;
            }
            current = implication.getEdgeTarget(currentEdge);
        }
        //path.add(current);

        if (operations == 0)
            return null;

        return path;
    }

    protected void plan(Task task, Term target) {

        if (!implication.containsVertex(target))
            return;

        List<Term> path = plan(target);
        if (path == null)
            return;
        
        //if (currentEdge != null) {
        //    memory.setTheNewStamp(Stamp.make(task.sentence.stamp, currentEdge.stamp, memory.getTime()));
        //} else {
            memory.setTheNewStamp(new Stamp(memory));
        //}

        memory.setCurrentTask(task);

        System.out.println(" -> Graph PATH: " + path + " -> " + target);

        Conjunction subj = (Conjunction) Conjunction.make(path.toArray(new Term[path.size()]), TemporalRules.ORDER_FORWARD, memory);
        TruthValue val = task.sentence.truth;
        //val=TruthFunctions.abduction(val, newEvent.sentence.truth);

        Term imp = Implication.make(subj, target, TemporalRules.ORDER_FORWARD, memory);

        BudgetValue bud = BudgetFunctions.forward(val, memory);
        
        System.out.println("  -> Graph OUT: " + imp);

        memory.doublePremiseTask(imp, val, bud);

        
    }
    
    protected void plan(Task task, Task __not_used_newEvent) {

        Term t = task.getContent();
        if (t == null) return;
        
        if ((t instanceof Implication) && (t.getTemporalOrder()!=TemporalRules.ORDER_NONE)) {
            
            System.out.println("plan: task=" + task + " newEvent=" + __not_used_newEvent);

            Implication i = (Implication) t;
            Term target;
            
            //implication.add(task.sentence);

            if (i.getTemporalOrder() == TemporalRules.ORDER_FORWARD) {
                target = i.getPredicate();
            } else {
                //TODO reverse it
                target = i.getSubject();
            }

            if (target != null) {

                plan(task, target);
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

        Term newcontent = newEvent.sentence.content;
        if (newcontent instanceof Operation) {
            Term pred = ((Operation) newcontent).getPredicate();
            if (pred.equals(mem.getOperator("^want")) || pred.equals(mem.getOperator("^believe"))) {
                return false;
            }
        }

        plan(newEvent, (Task)null);

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
