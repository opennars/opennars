package nars.inference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
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
    
    
    float searchDepth = 64;
    int particles = 512;

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
        
        plan(task, task.getContent(), searchDepth, '!');
        
    }

    @Override
    public void event(Class event, Object[] a) {

        if (event == ConceptGoalAdd.class) {
            Task t = (Task) a[2];
            /*if (!t.isInput())*/ 
            {
            //System.out.println("Goal add: " + a[0] + " " + a[1] + " " + t.budget.getPriority());
            tasks.add(t.clone());
            
            //EXPERIMENTAL
            if (implication.containsVertex(t.getContent()))
                plan(t, t.getContent(), searchDepth, '.');
            }
            
        } else if (event == ConceptGoalRemove.class) {
            Task t = (Task) a[2];
            //System.out.println("Goal rem: " + a[0] + " " + a[1] + " " + t.budget);
            tasks.remove(t);
        } else if (event == CycleEnd.class) {
//            if (tasks.size() > 0) {
//                plan();
//            }
        }
    }

    protected List<Term> plan(Term target, double distance, int particles)  {        
                
        List<Term> p = planParticle(target, distance, particles);
        
        if (!p.isEmpty())
            System.out.println("plan: " + target);
        return p;
    }
    
    
    public static boolean validPlanComponent(final Term t) {
        return ((t instanceof Interval) || (t instanceof Operation));
    }
    
    public static class CandidateSequenceRoot implements Comparable {
        public final Term root;
        public final double distance;

        public CandidateSequenceRoot(Term root, double distance) {
            this.root = root;
            this.distance = distance;
        }

        @Override
        public int compareTo(Object o) {
            if (o instanceof CandidateSequenceRoot) {
                CandidateSequenceRoot csr = (CandidateSequenceRoot)o;
                return Double.compare(csr.distance, distance);
            }
            return -1;
        }

        @Override
        public String toString() {
            return root.toString() + "|" + distance;
        }
        
        
    }

    protected List<Term> planExhaustive(Term target, double remainingDistance, List<Term> parentPath, double[] distResult) {
        
        if (remainingDistance <= 0)
            return Collections.EMPTY_LIST;
                
        ClosestFirstIterator<Term, Sentence> cfi = new ClosestFirstIterator<Term, Sentence>(new EdgeReversedGraph(implication), target, remainingDistance);

        
        if (parentPath == null)
            parentPath = Collections.EMPTY_LIST;
                
        SortedSet<CandidateSequenceRoot> roots = new TreeSet();
        
        while (cfi.hasNext()) {
            Term v = cfi.next();

            double length = cfi.getShortestPathLength(v);
            if (length == 0) continue;
                        
            
            //dont settle for 1-edge hop from target, we need further
            if (implication.getEdgeTarget( cfi.getSpanningTreeEdge(v) ).equals(target) ) {
                //System.out.println(v + " " + cfi.getSpanningTreeEdge(v) + " ==? " + target);
                continue;
            }
            
            if ((!v.equals(target)) /*&& (!parentPath.contains(v))*/) {
                //ignore intervals as roots
                if (!(v instanceof Interval))
                    roots.add(new CandidateSequenceRoot(v, length));                
            }
        }
        if (roots.isEmpty())
            return Collections.EMPTY_LIST;
        
        double initialRemainingDistance = remainingDistance;
        
        
        for (final CandidateSequenceRoot csroot : roots) {
            final Term root = csroot.root;
            if (root == target) continue;
            
            remainingDistance = initialRemainingDistance - csroot.distance;
            if (remainingDistance < 0)
                continue;
            
            //Calculate path back to target

            List<Term> path = new ArrayList();
            Term current = root;
            Sentence currentEdge = null;
            int operations = 0;

            while (current != target) {

                boolean isOperation = (current instanceof Operation);
                if (isOperation)
                    operations++;

                //only include Operations and Intervals
                if (isOperation || (current instanceof Interval)) {
                    path.add(current);
                }
                //but if it's something else, we need to transclude it because it may indicate other  necessary preconditions
                else if ((!current.equals(target))) {

                    //Transclude best subpath iff vertex has other preconditions

                    /*if (implication.outgoingEdgesOf(current).size() > 1) {
                        //ignore a preconditon with a postcondition
                        continue;
                    }*/

                    //TODO should the precondition branches be sorted, maybe shortest first?

                    boolean goodPreconditions = true;
                    Set<Sentence> preconditions = implication.incomingEdgesOf(current);
                    for (Sentence s : preconditions) {
                        if (!s.equals(currentEdge)) {
                            //System.out.println("  precondition: " + current + " = " + s);
                            Term preconditionSource = implication.getEdgeSource(s);

                            if (parentPath!=null) {
                                /*if (!parentPath.contains(preconditionSource))*/ {
                                    if (!preconditionSource.equals(target) ) {
                                        List<Term> preconditionPlan = null;
                                        try {
                                            double[] d = new double[1];
                                            preconditionPlan = planExhaustive(preconditionSource, remainingDistance, path, d);

                                            if (!((preconditionPlan.size() == 0) || (preconditionPlan == null))) {
                                                if (remainingDistance - d[0] > 0) {
                                                    if (!preconditionPlan.contains(preconditionSource)) {                                                    path.addAll(preconditionPlan);
                                                        if (validPlanComponent(preconditionSource))
                                                            path.add(preconditionSource);
                                                    }
                                                    remainingDistance -= d[0];
                                                }
                                                else {
                                                    //ignore this condition sequence because it would exceed the search distance                                
                                                    System.out.println("  excess subpath: " + remainingDistance + " " + d[0] + " " + preconditionPlan);
                                                    goodPreconditions = false;
                                                    break;

                                                }
                                            }
                                        }
                                        catch (Throwable e) {

                                            System.err.println(e + " "  +target + " " + path + " " + preconditionSource + " " + parentPath);
                                            System.err.println("   " + preconditionPlan);
                                            new Window("Implications", new JGraphXGraphPanel(memory.executive.graph.implication)).show(500,500);
                                            try {
                                                System.in.read();
                                            } catch (IOException ex) {
                                                Logger.getLogger(GraphExecutive.class.getName()).log(Level.SEVERE, null, ex);
                                            }

                                        }
                                        
                                    }

                                }
                            }

                        }

                    }                
                    
                    if (!goodPreconditions)
                        break;
                }

                currentEdge = cfi.getSpanningTreeEdge(current);
                if (currentEdge == null) {
                    //Should mean we have returned to target
                    break;
                }
                current = implication.getEdgeTarget(currentEdge);
            }

            if (operations == 0)
                continue;
            if (path.size() < 2)
                continue;


            System.out.println(path + " " + root + " in " + roots);
            distResult[0] = initialRemainingDistance - remainingDistance;
            return path;
        }
        
        return Collections.EMPTY_LIST;
    }
    
    
    
    
    public static class ParticlePath implements Comparable<ParticlePath> {
        double activation = 0;
        Term target;
        Sentence[] path;
        
        public ParticlePath(Term target, List<Sentence> path) {
            this.target = target;
            this.path = path.toArray(new Sentence[path.size()]);
        }

        @Override
        public int compareTo(ParticlePath o) {
            return Double.compare(o.activation, activation);
        }

        @Override
        public String toString() {
            return activation + "|" /*+ target */ + " <- " + Arrays.toString(path);
        }

        public List<Term> getSequence() {
            if (path.length == 0) return Collections.EMPTY_LIST;
            
            int operations = 0;
            
            List<Term> seq = new ArrayList(path.length);
                        
            //Calculate path back to target
            Implication imp = null;
            for (int i = path.length-1; i >=0; i--) {
                Sentence s = path[i];
                Term t = s.content;
                
                if (t instanceof Implication) {
                    imp = (Implication)t;
                    Term subj = subj = imp.getSubject();
                    if (GraphExecutive.validPlanComponent(subj))
                        seq.add(subj);
                    if (subj instanceof Operation)
                        operations++;
                }
                else {
                    System.err.println("Unknown type: " + t + " in sequence generation of " + this);
                }                    
            }
            
            //the last (first) predicate should be the target
//            if (imp!=null) {
//                Term pred = imp.getPredicate();
//                if (GraphExecutive.validPlanComponent(pred))
//                    seq.add(pred);
//                if (pred instanceof Operation)
//                    operations++;
//            }
             
            
            if (operations == 0)
                return Collections.EMPTY_LIST;
            
            return seq;
        }
        
        
        
    }
    
    protected List<Term> planParticle(Term target, double distance, int iterations) {
        
        //TODO can this be continuous but decay the activation
        Map<Term, ParticlePath> termPaths = new HashMap();
        
                
        SortedSet<ParticlePath> roots = new TreeSet();

                
        double particleActivation = 1.0 / iterations;
        
        List<Sentence> currentPath = new ArrayList(8);            
        
        for (int i = 0; i < iterations; i++) {            
        
            currentPath.clear();
            
            double energy = distance;
            Term current = target;
            
            
            while (energy > 0) {
                
                Set<Sentence> incomingEdges = implication.incomingEdgesOf(current);
                if (incomingEdges.isEmpty()) {
                    break;
                }                
                
                Sentence nextEdge = null;
                if (incomingEdges.size() == 1)
                    nextEdge = incomingEdges.iterator().next();
                else {
                    //choose edge; prob = 1/weight
                    
                    //TODO disallow edge that completes cycle back to target or traversed edge?
                    //  probably an option to allow cycles
                    
                    double totalProb = 0;
                    for (Sentence s : incomingEdges) {
                        totalProb += 1.0 / implication.getEdgeWeight(s);                        
                    }                    
                    double r = Math.random() * totalProb;
                    
                    for (Sentence s : incomingEdges) {
                        nextEdge = s;
                        r -= 1.0 / implication.getEdgeWeight(s);
                        if (r <= 0) {
                            break;
                        }                            
                    }
                }
                
                energy -= implication.getEdgeWeight(nextEdge);
                
                currentPath.add(nextEdge);
                
                current = implication.getEdgeSource(nextEdge);
            }
            
            if (currentPath.isEmpty())
                continue;
            
            ParticlePath source = termPaths.get(current);
            if (source == null) {
                source = new ParticlePath(target, currentPath);
                termPaths.put(current, source);
            }
            
            source.activation += particleActivation;
        }
        
        roots.addAll(termPaths.values());

//        System.out.println("Particle paths for " + target);
//        for (ParticlePath pp : roots) {
//            System.out.println("  " + pp);
//        }

        
        for (final ParticlePath pp : roots) {
                        
            List<Term> path = pp.getSequence();

            if (path.size() < 2)
                continue;

            return path;
        }
        
        return Collections.EMPTY_LIST;
    }

    protected void plan(Task task, Term target, double distance, char punctuation) {

        if (!implication.containsVertex(target))
            return;

        List<Term> path = plan(target, distance, particles);
        if (path == null)
            return;
        if (path.size() < 2) 
            return;

        System.out.println("@" + memory.getTime() + " plan: " + task);
        
        
        //final incoming edge of the path, for the stamp for derivation
        Term ultimateTerm = path.get(path.size()-1);
        Sentence currentEdge = implication.getEdge(path.get(path.size()-2), ultimateTerm);
        
        if (currentEdge == null) {
            
            //choose one sentence involving the final edge
            currentEdge = implication.edgesOf(ultimateTerm).iterator().next();
            if (currentEdge == null) {
                System.err.println("No leading edge for path: " + path);
                return;
            }
        }
        
            
        Stamp stamp = Stamp.make(task.sentence.stamp, currentEdge.stamp, memory.getTime());
        //memory.setTheNewStamp(stamp);

        //memory.setCurrentTask(task);
        
        //remove final element from path if it's equal to target
        if (path.get(path.size()-1).equals(target)) {
            path.remove(path.size()-1);
        }

        Term subj = path.size() > 1 ?
            (Conjunction) Conjunction.make(path.toArray(new Term[path.size()]), TemporalRules.ORDER_FORWARD, memory)
                :
            path.get(0);
        
        
        //System.out.println(" -> Graph PATH: " + subj + " =\\> " + target);

        TruthValue val = task.sentence.truth;
        //val=TruthFunctions.abduction(val, newEvent.sentence.truth);

        Term imp = Implication.make(subj, target, TemporalRules.ORDER_FORWARD, memory);

        if (imp == null)
            return;
        
        BudgetValue bud = BudgetFunctions.forward(val, memory);
        
        System.out.println("  -> Graph OUT: " + imp);

        //memory.doublePremiseTask(imp, val, bud);
        memory.inputTask(
                new Task(new Sentence(imp, punctuation, val, stamp), bud)
        );

        
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
                System.err.println("plan: " + target);
                System.exit(1);
                ///plan(task, target, ...);
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
