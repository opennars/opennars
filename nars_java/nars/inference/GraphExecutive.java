package nars.inference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import nars.gui.Window;
import nars.gui.output.JGraphXGraphPanel;
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
        plan(task, task.getContent());
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
        return plan(target, null);
    }
    
    protected List<Term> plan(Term target, List<Term> parentPath) {
        
        double maxRadius = 8;

        ClosestFirstIterator<Term, Sentence> cfi = new ClosestFirstIterator<Term, Sentence>(new EdgeReversedGraph(implication), target, maxRadius);

        if (parentPath == null)
            parentPath = Collections.EMPTY_LIST;
        
        //TODO use a sorted list of furthest candidates
        
        double furthestDistance = 0;
        Term furthestTerm = null;
        
        while (cfi.hasNext()) {
            Term v = cfi.next();

            double length = cfi.getShortestPathLength(v);
            if (length == 0) continue;
            
            //System.out.println("  --" + v + " dist=" + length + " spanEdge=" + cfi.getSpanningTreeEdge(v));
            
            if ((length > furthestDistance) && (!v.equals(target)) && (!parentPath.contains(v))) {
                furthestDistance = length;
                furthestTerm = v;
                
            }
            
                        
        }
        
        if (furthestTerm == null)
            return Collections.EMPTY_LIST;
        if (furthestTerm == target)
            return Collections.EMPTY_LIST;


        //Calculate path back to target
        List<Term> path = new ArrayList();
        Term current = furthestTerm;
        Sentence currentEdge = null;
        int operations = 0;
        Set<Term> preconditionsSatisfied = new HashSet();
        
        while (current != target) {

            boolean isOperation = (current instanceof Operation);
            if (isOperation)
                operations++;

            //only include Operations and Intervals
            if (isOperation || (current instanceof Interval)) {
                path.add(current);
            }
            //but if it's something else, we need to transclude it because it may indicate other  necessar ypreconditions
            else if ((!current.equals(target))) {
                
                //Transclude best subpath iff vertex has other preconditions
                
                //TODO should the precondition branches be sorted, maybe shortest first?
                
                Set<Sentence> preconditions = implication.incomingEdgesOf(current);
                for (Sentence s : preconditions) {
                    if (!s.equals(currentEdge)) {
                        //System.out.println("  precondition: " + current + " = " + s);
                        Term preconditionSource = implication.getEdgeSource(s);
                        
                        if (parentPath!=null) {
                            if (parentPath.contains(preconditionSource))
                                continue;
                        }
                        
                        if (!preconditionSource.equals(target) ) {
                            try {
                                List<Term> preconditionPlan = plan(preconditionSource, path);
                                if ((preconditionPlan.size() == 0) || (preconditionPlan == null))
                                    continue;
                                path.addAll(preconditionPlan);
                                path.add(preconditionSource);                        
                                preconditionsSatisfied.add(preconditionSource);
                            }
                            catch (Throwable e) {
                                
                                System.err.println(e + " "  +target + " " + furthestTerm + " " + furthestDistance + " " + preconditionSource + " " + preconditionsSatisfied);
                                new Window("Implications", new JGraphXGraphPanel(memory.executive.graph.implication)).show(500,500);
                                try {
                                    System.in.read();
                                } catch (IOException ex) {
                                    Logger.getLogger(GraphExecutive.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                    
                                    //System.exit(1);
                                    //return Collections.EMPTY_LIST;
                                
                            }
                        }
                    }
                    
                }                
            }

            currentEdge = cfi.getSpanningTreeEdge(current);
            if (currentEdge == null) {
                //Should mean we have returned to target
                break;
            }
            current = implication.getEdgeTarget(currentEdge);
        }

        if (operations == 0)
            return Collections.EMPTY_LIST;

        return path;
    }

    protected void plan(Task task, Term target) {

        if (!implication.containsVertex(target))
            return;

        List<Term> path = plan(target);
        if (path == null)
            return;
        if (path.size() < 2) 
            return;

        System.out.println("\n@" + memory.getTime() + " plan: " + task);
        
        //final incoming edge of the path, for getting the stamp
        Sentence currentEdge = implication.getEdge(path.get(path.size()-2), path.get(path.size()-1));
        
        if (currentEdge == null) {
            System.err.println("No leading edge for path: " + path);
            return;
        }
            
        memory.setTheNewStamp(Stamp.make(task.sentence.stamp, currentEdge.stamp, memory.getTime()));

        memory.setCurrentTask(task);

        System.out.println(" -> Graph PATH: " + path + " -> " + target);

        Conjunction subj = (Conjunction) Conjunction.make(path.toArray(new Term[path.size()]), TemporalRules.ORDER_FORWARD, memory);
        TruthValue val = task.sentence.truth;
        //val=TruthFunctions.abduction(val, newEvent.sentence.truth);

        Term imp = Implication.make(subj, target, TemporalRules.ORDER_FORWARD, memory);

        BudgetValue bud = BudgetFunctions.forward(val, memory);
        
        //System.out.println("  -> Graph OUT: " + imp);

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
