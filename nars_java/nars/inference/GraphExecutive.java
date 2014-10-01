package nars.inference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import nars.core.Memory;
import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.entity.Stamp;
import nars.entity.Task;
import nars.entity.TruthValue;
import static nars.inference.Executive.isPlanTerm;
import nars.language.Conjunction;
import nars.language.Implication;
import nars.language.Interval;
import nars.language.Term;
import nars.operator.Operation;
import nars.util.graph.ImplicationGraph;
import nars.util.graph.ImplicationGraph.PostCondition;

public class GraphExecutive {

    public final Memory memory;
    public final ImplicationGraph implication;
    
    
    /** controls the relative weigting of edges and vertices for particle traversals */
    double conceptCostFactor = 0.5;
    double edgeCostFactor = 1.0 - conceptCostFactor;
    
    //for observation purposes, TODO enable/disable the maintenance of this
    public final Map<Term,Double> accumulatedTerm = new HashMap();
    public final Map<Sentence,Double> accumulatedSentence = new HashMap();
    
    
    public GraphExecutive(Memory memory, Executive exec) {
        super();

        this.memory = memory;
        this.implication = new ImplicationGraph(memory);
    }

    
    protected void accumulate(final Term t) {
        accumulatedTerm.put(t, accumulatedTerm.getOrDefault(t, new Double(0)) + 1);
    }
    protected void accumulate(final Sentence s) {
        accumulatedSentence.put(s, accumulatedTerm.getOrDefault(s, new Double(0)) + 1);
    }
    protected void accumulate(final Term t, final List<Sentence> path) {
        accumulate(t);
        for (Sentence s : path)
            accumulate(s);
    }
    /** returns maximum value */
    public double fadeAccumulatedTerms(final double rate) {
        double max = 0;
        for (final Map.Entry<Term, Double> e : accumulatedTerm.entrySet()) {
            double vv = e.getValue();
            if (vv > max) max = vv;
            e.setValue( vv * rate );
        }
        return max;
    }
    /** returns maximum value */
    public double fadeAccumulatedSentences(double rate) {
        double max = 0;
        for (final Map.Entry<Sentence, Double> e : accumulatedSentence.entrySet()) {
            double vv = e.getValue();
            if (vv > max) max = vv;
            e.setValue( vv * rate );
        }
        return max;
    }
    
    /** whether the Term is currently a valid goal for the implication graph to plan for */
    public boolean isPlannable(final Term goal) {
        PostCondition goalPostCondition = new PostCondition(goal);
        
        /** must be in the graph and have at least one incoming edge */
        if (implication.containsVertex(goalPostCondition)) {
            return implication.inDegreeOf(goalPostCondition) > 0;
        }
        return false;
    }

    
    public class ParticlePath implements Comparable<ParticlePath> {
        final public Term target;
        
        double activation = 0;
        Sentence[] shortestPath;
        double distance;
        
        public ParticlePath(final Term target, final List<Sentence> path, final double distance) {
            this.target = target;            
            addPath(path, distance);
        }
        
        public void addPath(final List<Sentence> p, final double dist) {
            if ((this.shortestPath == null) || (dist < distance)) {
                this.shortestPath = p.toArray(new Sentence[p.size()]);
                this.distance = dist;
            }
            accumulate(target, p);
        }

        @Override
        public final int compareTo(final ParticlePath o) {
            return Double.compare(o.activation, activation);
        }

        @Override
        public String toString() {
            return activation + "|" /*+ target */ + " <- " + Arrays.toString(shortestPath);
        }
        
    }
    
    public class ParticleActivation {
        private final ImplicationGraph graph;
        
        Map<Term, ParticlePath> termPaths = new HashMap();
        boolean avoidCycles = true;
        private int edgeDecisionPass = 0;
        private int edgeDecisionFailCyclical = 0;
        private int edgeDecisionFailInvalidVertex = 0;
        private int edgeDecisionFailInactiveEdge = 0;
        private int pathFailEmpty = 0;
        private int pathFailNoOperation = 0;
        private int pathsValid = 0;
        private int numIterations = 0;
        private TreeSet paths;

        public ParticleActivation(ImplicationGraph graph) {
            this.graph = graph;            
        }
        
        public SortedSet<ParticlePath> activate(final Term source, final boolean forward, int iterations, double distance) {

            //TODO cache pathways in the graph for faster traversal. must store source leading edge, destination(s) and their distances
            
            double particleActivation = 1.0 / iterations;

            List<Sentence> currentPath = new ArrayList();

            List<Sentence> nextEdges = new ArrayList();
            
            for (int i = 0; i < iterations; i++) {            

                numIterations++;
                
                currentPath.clear();

                double energy = distance;

                Term current = source;

                boolean choicesAvailable = false;
                boolean operationTraversed = false;

                while (energy > 0) {

                    Set<Sentence> graphEdges = forward ? 
                            graph.outgoingEdgesOf(current) : 
                            graph.incomingEdgesOf(current);
                    
                    nextEdges.clear();
                    double totalProb = 0;
                    //remove edges which loop to the target goal precondition OR postcondition
                    for (final Sentence s : graphEdges) {
                        Term etarget = forward ?
                                graph.getEdgeTarget(s) :
                                graph.getEdgeSource(s);
                        
                        if ((avoidCycles) && (etarget.equals(source))) {
                            edgeDecisionFailCyclical++;
                            continue;
                        }
                           
                        if (!validVertex(etarget)) {
                            edgeDecisionFailInvalidVertex++;
                            continue;
                        }
                                                
                        double ew = graph.getEdgeWeight(s);
                        if (ew >= ImplicationGraph.DEACTIVATED_EDGE_WEIGHT) {
                            edgeDecisionFailInactiveEdge++;
                            continue;
                        }

                        edgeDecisionPass++;
                        nextEdges.add(s);

                        totalProb += 1.0 / ew;

                        if (etarget instanceof Operation) {
                            operationTraversed = true;
                        }
                        
                    }


                    if (nextEdges.isEmpty()) {
                        //particle went as far as it can
                        break;
                    }                

                    Sentence nextEdge = null;
                    if (nextEdges.size() == 1) {
                        nextEdge = nextEdges.get(0);
                    }
                    else {
                        int numEdges = nextEdges.size();
                        
                        //choose edge; prob = 1/weight

                        //TODO disallow edge that completes cycle back to target or traversed edge?
                        //  probably an option to allow cycles
                        
                        double r = Memory.randomNumber.nextDouble() * totalProb;

                        choicesAvailable = true;

                        int j;
                        for (j = 0; j < numEdges; j++) {
                            nextEdge = nextEdges.get(j);
                            r -= 1.0 / graph.getEdgeWeight(nextEdge);
                            if (r <= 0) {
                                //selected the next Edge
                                break;
                            }
                        }

                    }

                    currentPath.add(nextEdge);

                    current = forward ? graph.getEdgeTarget(nextEdge) : graph.getEdgeSource(nextEdge)
                            ;
                    double nextConceptPriority = getEffectivePriority(memory, current);
                    
                    /** weight = distance = cost, for an edge */
                    double nextEdgeCost = graph.getEdgeWeight(nextEdge);
                    
                    /** cost = (1.0 - priority) of a concept, if it exists.  
                     * if it doesn't then cost is 1.0 */
                    double nextConceptCost = (1.0 - nextConceptPriority);

                    energy -= (edgeCostFactor * nextEdgeCost + 
                            conceptCostFactor * nextConceptCost);

                    
//                    if ((current == null)  /*|| (!graph.containsVertex(current)*/) {                  
//                        break;
//                    }
                }

                if (currentPath.isEmpty()) {
                    pathFailEmpty++;
                    continue;
                }
                if (!operationTraversed) {
                    pathFailNoOperation++;
                    continue;                        
                }

                ParticlePath ppath = termPaths.get(current);
                if (ppath == null) {                    
                    termPaths.put(current, 
                            ppath = new ParticlePath(source, currentPath, distance - energy));
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
                
                pathsValid++;

            }

            Collection<ParticlePath> paths = termPaths.values();
                 
            
            //normalize activations to maxValue=1.0
            double maxAct = 0;
            for (final ParticlePath p : paths)
                if (p.activation > maxAct)
                    maxAct = p.activation;
            if (maxAct > 0)
                for (final ParticlePath p : paths)
                    p.activation /= maxAct;            


            this.paths = new TreeSet(paths);
            return this.paths;
        }
        
        public String getStatus() {
                    
            return "iterations=" + numIterations + 
                    ", pathsFound=" + paths.size() +
                    ", pathsValid=" + pathsValid +
                    ", pathEmpty=" + pathFailEmpty +
                    ", pathNoOperations=" + pathFailNoOperation +
                    ", edgeDecisionPass=" + edgeDecisionPass +
                    ", edgeDecisionFailInactiveEdge=" + edgeDecisionFailInactiveEdge +
                    ", edgeDecisionFailInvalidVertex=" + edgeDecisionFailInvalidVertex +
                    ", edgeDecisionFailCyclical=" + edgeDecisionFailCyclical;
            
        }
        
        public boolean validVertex(final Term x) {
            return true;
        }
        //public void reset()
        
    }

    public static double getActualPriority(final Memory memory, final Term t) {
        double p;
        Concept nextConcept = memory.concept(t);
        if (nextConcept!=null)
            p = nextConcept.getPriority();
        else
            p = 0.0f;        
        return p;
    }

    public static double getEffectivePriority(final Memory memory, final Term current) {
        double nextConceptPriority;
        if (current instanceof Interval) {
            //default priority for intervals
            nextConceptPriority = 1.0f;
        }
        else if (current instanceof PostCondition) {
            //get the priority for the postcondition's actual concept
            nextConceptPriority = getActualPriority(memory, ((PostCondition)current).term[0]);
        }
        else {
            nextConceptPriority = getActualPriority(memory, current);
        }
        return nextConceptPriority;
    }

        
    public static class ParticlePlan implements Comparable<ParticlePlan> {
        public final Sentence[] path;
        public final List<Term> sequence;
        public final double distance;
        public final double activation;
        public final TruthValue truth;
        public final BudgetValue budget;

        public ParticlePlan(Memory memory, Sentence[] path, List<Term> sequence, double activation, double distance) {
            this.path = path;
            this.sequence = sequence;
            this.activation = activation;
            this.distance = distance;
            
            final float confidence = getMinConfidence();                
            truth = new TruthValue(1.0f, confidence);
            budget = BudgetFunctions.forward(truth, memory);
            budget.andPriority(confidence);
            
        }        

        public float getMinConfidence() {
            if (path.length == 0) return 0;
            
            float min = Float.MAX_VALUE;
            for (final Sentence s : path) {
                float c = s.truth.getConfidence();
                if (c < min)
                    min = c;
            }
            return min;
        }

        public double score() {
            return truth.getConfidence() * activation;
        }
        
        @Override public final int compareTo(final ParticlePlan o) {
            int i = Double.compare(o.score(), score());
            if ((i == 0) && (o!=this)) {
                return -1;
            }
            return i;
        }

        @Override
        public String toString() {
            return sequence + "(" + score() + ";"+ distance + ")";
        }
        
        
        
    }
    
    protected void particlePredict(final Term source, final double distance, final int particles) {
        ParticleActivation act = new ParticleActivation(implication);
        SortedSet<ParticlePath> paths = act.activate(source, true, particles, distance);
        if (!paths.isEmpty())
            System.out.println(source + " predicts: " + paths);
        
    }
    
    public TreeSet<ParticlePlan> particlePlan(final Term target, final double distance, final int particles) {
        PostCondition targetPost = new PostCondition(target);
        
        if (!implication.containsVertex(targetPost)) {
            //System.out.println("  plan for " + target + ": missing postCondition vertex");
            return null;
        }
        
        ParticleActivation act = new ParticleActivation(implication) {
            @Override public boolean validVertex(final Term x) {
                //additional restriction on path's vertices
                return !targetPost.equals(x);
            }            
        };
        
        SortedSet<ParticlePath> roots = act.activate(targetPost, false, particles, distance);
        //System.out.println("  plan: " + act.getStatus());
        

        if (roots == null) {            
            return null;
        }
//        System.out.println("Particle paths for " + target);
//        for (ParticlePath pp : roots) {
//            System.out.println("  " + pp);
//        }
        
        TreeSet<ParticlePlan> plans = new TreeSet();
        for (final ParticlePath pp : roots) {

            Sentence[] path = pp.shortestPath;
            
            if (path.length == 0)
                throw new RuntimeException("ParticlePath empty: " + pp);
            
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
                
                if (isPlanTerm(subj)) {                    
                    boolean isInterval = subj instanceof Interval;
                    if (!isInterval) {
                        nonIntervalAdded = true;
                        if (accumulatedDelay > 0) {
                            //TODO calculate a more fine-grained sequence of itnervals
                            //rather than just rounding to the nearest.
                            //ex: +2,+1 may be more accurate than a +3
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
                    //accumulate delay if the temporal rule involves time difference??
                    /*
                    if (nonIntervalAdded) { 
                        ////ignore prefix intervals                        
                        int temporal = (s.content).getTemporalOrder();
                        if (temporal == TemporalRules.ORDER_FORWARD) {                            
                            accumulatedDelay++;
                        }                        
                    }
                    */
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
            
            if (seq.isEmpty())
                continue;

            //System.out.println("  cause: " + Arrays.toString(path));
            ParticlePlan rp = new ParticlePlan(memory, path, seq, pp.activation, pp.distance);
            plans.add(rp);
        }
        
        return plans;
    } 
    
    protected void planTask(ParticlePlan plan, Task task, Term target, char punctuation) {        
        
        TruthValue truth = plan.truth;
        BudgetValue budget = plan.budget;
        
        Sentence[] path = plan.path;
        List<Term> seq = plan.sequence;
 
        
        Sentence currentEdge = path[path.length-1];

        Stamp stamp = Stamp.make(task.sentence.stamp, currentEdge.stamp, memory.getTime());
        
        //add all terms to derivation chain
        for(Term T : seq) {
            stamp.derivationChain.add(T); //todo: if too long kick out the first n terms
        }
        //todo: evidental base hm
        
        //memory.setTheNewStamp(stamp);

        //memory.setCurrentTask(task);
        
        //remove final element from path if it's equal to target
        /*if (seq.get(seq.size()-1).equals(target)) {
            seq.remove(seq.size()-1);
        }*/

        Term subj = seq.size() > 1 ?
            Conjunction.make(seq.toArray(new Term[seq.size()]), TemporalRules.ORDER_FORWARD, memory)
                :
            seq.get(0);
        
        
        //val=TruthFunctions.abduction(val, newEvent.sentence.truth);

        Term imp = Implication.make(subj, target, TemporalRules.ORDER_FORWARD, memory);

        if (imp == null) {
            throw new RuntimeException("Invalid implication: " + subj + " =\\> " + target);
        }
        
        
        Task newTask = new Task(new Sentence(imp, punctuation, truth, stamp), budget, task);
        
        if (memory.getRecorder().isActive())
               memory.getRecorder().append("Plan Add", newTask.toString());

        memory.derivedTask(newTask, false, true, null, null);
        
    }

   protected void plan(Concept c, Task task, Term target, int particles, double searchDistance, char punctuation, int maxTasks) {

        if (!implication.containsVertex(target))
            return;

        TreeSet<ParticlePlan> plans = particlePlan(target, searchDistance, particles);
        int n = 0;
        for (ParticlePlan p : plans) {
            planTask(p, task, target, punctuation);
            if (n++ == maxTasks)
                break;
        }
       
    }
    
//    protected void plan(Task task, Task __not_used_newEvent) {
//
//        Term t = task.getContent();
//        if (t == null) return;
//        
//        if ((t instanceof Implication) && (t.getTemporalOrder()!=TemporalRules.ORDER_NONE)) {
//            
//            //System.out.println("plan: task=" + task + " newEvent=" + __not_used_newEvent);
//
//            Implication i = (Implication) t;
//            Term target;
//            
//            //implication.add(task.sentence);
//
//            if (i.getTemporalOrder() == TemporalRules.ORDER_FORWARD) {
//                target = i.getPredicate();
//            } else {
//                //TODO reverse it
//                target = i.getSubject();
//            }
//
//            if (target != null) {
//                System.err.println("plan: " + target);
//                System.exit(1);
//                ///plan(task, target, ...);
//            }
//        }
//
////        System.out.println("Goals");        
////        for (Task t : tasks) {
////            System.out.println(t + " " + t.getParentBelief());
////            //System.out.println(getImplicationPath(t.getParentBelief()));
////        }
////        System.out.println();        
//    }

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

    /** TODO */
    public void reset() {
    }


//    public boolean isActionable(final Task newEvent, Memory mem) {
//        /*if(!((newEvent.isInput()) || (newEvent.getCause()!=null))) {
//         return false;
//         }*/
//
//        Term newcontent = newEvent.sentence.content;
//        if (newcontent instanceof Operation) {
//            Term pred = ((Operation) newcontent).getPredicate();
//            if (pred.equals(mem.getOperator("^want")) || pred.equals(mem.getOperator("^believe"))) {
//                return false;
//            }
//        }
//
//        plan(newEvent, (Task)null);
//
//        return true;
//    }
//
//    public boolean isActionable(final Task task, final Task newEvent) {
//
//        plan(task, newEvent);
//        return true;
//
//        /*
//         if (task.sentence.stamp.getOccurrenceTime() == Stamp.ETERNAL) {
//         return false;
//         }
//        
//         if (!task.sentence.isJudgment()) {
//         return false;
//         }
//         */
////        if ((newEvent == null)
////                || (rankBelief(newEvent.sentence) < rankBelief(task.sentence))) {
////            
////            return true;
////            
////            /*return 
////                ((shortTermMemory.isEmpty()                     
////                    ||                    
////                !equalSubTermsInRespectToImageAndProduct(
////                            shortTermMemory.getLast().getContent(),
////                            task.getContent()))
////            );
////                    */
////        }        
//        //return false;        
//    }

//    /** doesnt work yet and may not be necessary */
//
//    @Deprecated public static class CandidateSequenceRoot implements Comparable {
//        public final Term root;
//        public final double distance;
//
//        public CandidateSequenceRoot(Term root, double distance) {
//            this.root = root;
//            this.distance = distance;
//        }
//
//        @Override
//        public int compareTo(Object o) {
//            if (o instanceof CandidateSequenceRoot) {
//                CandidateSequenceRoot csr = (CandidateSequenceRoot)o;
//                return Double.compare(csr.distance, distance);
//            }
//            return -1;
//        }
//
//        @Override
//        public String toString() {
//            return root.toString() + "|" + distance;
//        }
//        
//        
//    }
//
//    
//    

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
