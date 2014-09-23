package nars.inference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import nars.core.EventEmitter.Observer;
import nars.core.Events.ConceptGoalAdd;
import nars.core.Events.ConceptGoalRemove;
import nars.core.Events.CycleEnd;
import nars.core.Memory;
import nars.entity.BudgetValue;
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
import nars.util.graph.ImplicationGraph.PostCondition;

public class GraphExecutive implements Observer {

    public final ImplicationGraph implication;
    PriorityBuffer<Task> tasks;
    int numTasks = 32;
    private final Memory memory;
    
    
    float searchDepth = 16;
    int particles = 64;

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


    @Override
    public void event(Class event, Object[] a) {

        if (event == ConceptGoalAdd.class) {
            Task t = (Task) a[2];
            
            tasks.add(t.clone());
            
            Term c = t.getContent();
            if (!(c instanceof Operation)) {
                System.out.println("Goal: " + t);
                if (implication.containsVertex(t.getContent())) {                    
                    plan(t, t.getContent(), searchDepth, '!');
                }
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

    
    
    
    
    public static class ParticlePath implements Comparable<ParticlePath> {
        final public Term target;
        
        double activation = 0;
        Sentence[] path;
        double distance;
        
        public ParticlePath(final Term target, final List<Sentence> path, final double distance) {
            this.target = target;
            this.distance = Double.MAX_VALUE;
            addPath(path, distance);
        }
        
        public void addPath(final List<Sentence> path, final double dist) {
            if (dist < distance) {
                this.path = path.toArray(new Sentence[path.size()]);
                this.distance = dist;
            }
        }

        @Override
        public final int compareTo(final ParticlePath o) {
            return Double.compare(o.activation, activation);
        }

        @Override
        public String toString() {
            return activation + "|" /*+ target */ + " <- " + Arrays.toString(path);
        }
        
    }
    
    public static class ParticleActivation {
        private final ImplicationGraph graph;
        
        Map<Term, ParticlePath> termPaths = new HashMap();

        public ParticleActivation(ImplicationGraph graph) {
            this.graph = graph;            
        }
        
        public SortedSet<ParticlePath> activate(final Term source, final boolean forward, int iterations, double distance) {

            SortedSet<ParticlePath> roots = new TreeSet();
            
            double particleActivation = 1.0 / iterations;

            List<Sentence> currentPath = new ArrayList(8);            

            for (int i = 0; i < iterations; i++) {            

                currentPath.clear();

                double energy = distance;
                Term current = source;

                boolean choicesAvailable = false;

                while (energy > 0) {

                    Set<Sentence> _nextEdges = forward ? 
                            graph.outgoingEdgesOf(current) : 
                            graph.incomingEdgesOf(current);
                    
                    Set<Sentence> nextEdges = new HashSet(_nextEdges);


                    //remove edges which loop to the target goal precondition OR postcondition
                    List<Sentence> toRemove = new LinkedList();
                    for (final Sentence s : nextEdges) {
                        Term etarget = graph.getEdgeSource(s);
                        if (etarget.equals(source)/* || etarget.equals(target)*/) {
                            toRemove.add(s);
                        }
                    }
                    nextEdges.removeAll(toRemove);


                    if (nextEdges.isEmpty()) {
                        break;
                    }                

                    Sentence nextEdge = null;
                    if (nextEdges.size() == 1) {
                        nextEdge = nextEdges.iterator().next();                    
                    }
                    else {
                        //choose edge; prob = 1/weight

                        //TODO disallow edge that completes cycle back to target or traversed edge?
                        //  probably an option to allow cycles

                        double totalProb = 0;
                        for (Sentence s : nextEdges) {
                            totalProb += 1.0 / graph.getEdgeWeight(s);
                        }                    
                        double r = Math.random() * totalProb;

                        for (Sentence s : nextEdges) {
                            nextEdge = s;
                            r -= 1.0 / graph.getEdgeWeight(s);
                            if (r <= 0) {
                                break;
                            }                            
                        }

                        choicesAvailable = true;
                    }

                    double weight = graph.getEdgeWeight(nextEdge);                

                    energy -= weight;

                    currentPath.add(nextEdge);

                    current = forward ? graph.getEdgeTarget(nextEdge) : graph.getEdgeSource(nextEdge);

                    if ((current == null) || (!graph.containsVertex(current)))
                        break;
                }

                if (currentPath.isEmpty())
                    continue;                        

                ParticlePath ppath = termPaths.get(current);
                if (ppath == null) {
                    ppath = new ParticlePath(source, currentPath, distance - energy);
                    termPaths.put(current, ppath);
                }
                else {
                    ppath.addPath(currentPath, distance - energy);
                }

                if (choicesAvailable) {                
                    ppath.activation += particleActivation;
                }
                else {
                    //we've found the only path, so it gets all activation and we dont need to iterate any further
                    ppath.activation = 1;
                    break;
                }

            }

            //normalize activations to maxValue=1.0
            double maxAct = 0;
            for (ParticlePath p : termPaths.values())
                if (p.activation > maxAct)
                    maxAct = p.activation;
            if (maxAct > 0)
                for (ParticlePath p : termPaths.values())
                    p.activation /= maxAct;            


            roots.addAll(termPaths.values());
            
            return roots;
        }
                
        //public void reset()
        
    }
    
    public static class ParticlePlan {
        Sentence[] path;
        List<Term> sequence;
        public double distance;
        private final double activation;

        public ParticlePlan(Sentence[] path, List<Term> sequence, double activation, double distance) {
            this.path = path;
            this.sequence = sequence;
            this.activation = activation;
            this.distance = distance;
        }        
    }
    
    protected void predictParticle(final Term source, final double distance, final int iterations) {
        
    }
    
    protected ParticlePlan planParticle(final Term target, final double distance, final int iterations) {
        PostCondition targetPost = new PostCondition(target);
        
        if (!implication.containsVertex(targetPost))
            return null;
        
        ParticleActivation act = new ParticleActivation(implication);
        SortedSet<ParticlePath> roots = act.activate(targetPost, false, particles, distance);
        

//        System.out.println("Particle paths for " + target);
//        for (ParticlePath pp : roots) {
//            System.out.println("  " + pp);
//        }
        
        for (final ParticlePath pp : roots) {

            Sentence[] path = pp.path;
            
            if (path.length == 0) continue;
            
            int operations = 0;
            
            List<Term> seq = new ArrayList(path.length);
                        
            //Calculate path back to target
            Implication imp;
            boolean nonIntervalAdded = false;
            long accumulatedDelay = 0;
            for (int i = path.length-1; i >=0; i--) {
                Sentence s = path[i];
                Term t = s.content;
                
                if (!(t instanceof Implication))
                    throw new RuntimeException("Unknown type: " + t + " in sequence generation of " + this);
                imp = (Implication)t;
                Term subj = imp.getSubject();
                
                if (validPlanComponent(subj)) {                    
                    boolean isInterval = subj instanceof Interval;
                    if (!isInterval) {
                        nonIntervalAdded = true;
                        if (accumulatedDelay > 0) {
                            seq.add( Interval.intervalTime(accumulatedDelay, memory)  );
                            accumulatedDelay = 0;
                        }
                        seq.add(subj);
                    }
                    else {
                        //prevent prefix intervals
                        if (nonIntervalAdded) {
                            Interval in = (Interval)subj;
                            long time = in.getTime(memory);
                            accumulatedDelay += time;
                        }
                    }                    
                }
                else {
                    if (nonIntervalAdded) { ////prevent prefix intervals                        
                        accumulatedDelay++;
                    }
                }
                
                if (subj instanceof Operation)
                    operations++;
            }            
                    
            if (operations == 0)
                continue;
            
            if (accumulatedDelay > 0) {
                //add suffix delay
                seq.add( Interval.intervalTime(accumulatedDelay, memory)  );                
            }
            
            if (seq.size() < 2)
                continue;

            System.out.println("  cause: " + Arrays.toString(path));

            return new ParticlePlan(path, seq, pp.activation, pp.distance);
        }
        
        return null;
    }

    protected void plan(Task task, Term target, double searchDistance, char punctuation) {

        if (!implication.containsVertex(target))
            return;

        ParticlePlan plan = planParticle(target, searchDistance, particles);
        if (plan == null)
            return;
        Sentence[] path = plan.path;
        List<Term> seq = plan.sequence;
        if (seq.size() < 1) 
            return;
        
        Sentence currentEdge = path[path.length-1];

        Stamp stamp = Stamp.make(task.sentence.stamp, currentEdge.stamp, memory.getTime());
        //memory.setTheNewStamp(stamp);

        //memory.setCurrentTask(task);
        
        //remove final element from path if it's equal to target
        if (seq.get(seq.size()-1).equals(target)) {
            seq.remove(seq.size()-1);
        }

        Term subj = seq.size() > 1 ?
            (Conjunction) Conjunction.make(seq.toArray(new Term[seq.size()]), TemporalRules.ORDER_FORWARD, memory)
                :
            seq.get(0);
        
        
        //System.out.println(" -> Graph PATH: " + subj + " =\\> " + target);

        double planDistance = Math.min(plan.distance, searchDistance);
        double confidence = plan.activation; // * planDistance/searchDistance;
        TruthValue val = new TruthValue(1.0f, (float)confidence);
        
        //val=TruthFunctions.abduction(val, newEvent.sentence.truth);

        Term imp = Implication.make(subj, target, TemporalRules.ORDER_FORWARD, memory);

        if (imp == null) {
            throw new RuntimeException("Invalid implication: " + subj + " =\\> " + target);
        }
        
        BudgetValue bud = BudgetFunctions.forward(val, memory);
        
        Task t = new Task(new Sentence(imp, punctuation, val, stamp), bud);
        
        System.out.println("  -> Plan: " + t);

        //memory.doublePremiseTask(imp, val, bud);
        memory.inputTask(t);

        
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

//    /** doesnt work yet and may not be necessary */
//    @Deprecated protected List<Term> planExhaustive(Term target, double remainingDistance, List<Term> parentPath, double[] distResult) {
//        
//        if (remainingDistance <= 0)
//            return Collections.EMPTY_LIST;
//                
//        ClosestFirstIterator<Term, Sentence> cfi = new ClosestFirstIterator<Term, Sentence>(new EdgeReversedGraph(implication), target, remainingDistance);
//
//        
//        if (parentPath == null)
//            parentPath = Collections.EMPTY_LIST;
//                
//        SortedSet<CandidateSequenceRoot> roots = new TreeSet();
//        
//        while (cfi.hasNext()) {
//            Term v = cfi.next();
//
//            double length = cfi.getShortestPathLength(v);
//            if (length == 0) continue;
//                        
//            
//            //dont settle for 1-edge hop from target, we need further
//            if (implication.getEdgeTarget( cfi.getSpanningTreeEdge(v) ).equals(target) ) {
//                //System.out.println(v + " " + cfi.getSpanningTreeEdge(v) + " ==? " + target);
//                continue;
//            }
//            
//            if ((!v.equals(target)) /*&& (!parentPath.contains(v))*/) {
//                //ignore intervals as roots
//                if (!(v instanceof Interval))
//                    roots.add(new CandidateSequenceRoot(v, length));                
//            }
//        }
//        if (roots.isEmpty())
//            return Collections.EMPTY_LIST;
//        
//        double initialRemainingDistance = remainingDistance;
//        
//        
//        for (final CandidateSequenceRoot csroot : roots) {
//            final Term root = csroot.root;
//            if (root == target) continue;
//            
//            remainingDistance = initialRemainingDistance - csroot.distance;
//            if (remainingDistance < 0)
//                continue;
//            
//            //Calculate path back to target
//
//            List<Term> path = new ArrayList();
//            Term current = root;
//            Sentence currentEdge = null;
//            int operations = 0;
//
//            while (current != target) {
//
//                boolean isOperation = (current instanceof Operation);
//                if (isOperation)
//                    operations++;
//
//                //only include Operations and Intervals
//                if (isOperation || (current instanceof Interval)) {
//                    path.add(current);
//                }
//                //but if it's something else, we need to transclude it because it may indicate other  necessary preconditions
//                else if ((!current.equals(target))) {
//
//                    //Transclude best subpath iff vertex has other preconditions
//
//                    /*if (implication.outgoingEdgesOf(current).size() > 1) {
//                        //ignore a preconditon with a postcondition
//                        continue;
//                    }*/
//
//                    //TODO should the precondition branches be sorted, maybe shortest first?
//
//                    boolean goodPreconditions = true;
//                    Set<Sentence> preconditions = implication.incomingEdgesOf(current);
//                    for (Sentence s : preconditions) {
//                        if (!s.equals(currentEdge)) {
//                            //System.out.println("  precondition: " + current + " = " + s);
//                            Term preconditionSource = implication.getEdgeSource(s);
//
//                            if (parentPath!=null) {
//                                /*if (!parentPath.contains(preconditionSource))*/ {
//                                    if (!preconditionSource.equals(target) ) {
//                                        List<Term> preconditionPlan = null;
//                                        try {
//                                            double[] d = new double[1];
//                                            preconditionPlan = planExhaustive(preconditionSource, remainingDistance, path, d);
//
//                                            if (!((preconditionPlan.size() == 0) || (preconditionPlan == null))) {
//                                                if (remainingDistance - d[0] > 0) {
//                                                    if (!preconditionPlan.contains(preconditionSource)) {                                                    path.addAll(preconditionPlan);
//                                                        if (validPlanComponent(preconditionSource))
//                                                            path.add(preconditionSource);
//                                                    }
//                                                    remainingDistance -= d[0];
//                                                }
//                                                else {
//                                                    //ignore this condition sequence because it would exceed the search distance                                
//                                                    System.out.println("  excess subpath: " + remainingDistance + " " + d[0] + " " + preconditionPlan);
//                                                    goodPreconditions = false;
//                                                    break;
//
//                                                }
//                                            }
//                                        }
//                                        catch (Throwable e) {
//
//                                            System.err.println(e + " "  +target + " " + path + " " + preconditionSource + " " + parentPath);
//                                            System.err.println("   " + preconditionPlan);
//                                            new Window("Implications", new JGraphXGraphPanel(memory.executive.graph.implication)).show(500,500);
//                                            try {
//                                                System.in.read();
//                                            } catch (IOException ex) {
//                                                Logger.getLogger(GraphExecutive.class.getName()).log(Level.SEVERE, null, ex);
//                                            }
//
//                                        }
//                                        
//                                    }
//
//                                }
//                            }
//
//                        }
//
//                    }                
//                    
//                    if (!goodPreconditions)
//                        break;
//                }
//
//                currentEdge = cfi.getSpanningTreeEdge(current);
//                if (currentEdge == null) {
//                    //Should mean we have returned to target
//                    break;
//                }
//                current = implication.getEdgeTarget(currentEdge);
//            }
//
//            if (operations == 0)
//                continue;
//            if (path.size() < 2)
//                continue;
//
//
//            System.out.println(path + " " + root + " in " + roots);
//            
//            distResult[0] = initialRemainingDistance - remainingDistance;
//            return path;
//        }
//        
//        return Collections.EMPTY_LIST;
//    }

}
