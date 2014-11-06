package nars.inference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import nars.core.Memory;
import nars.core.Parameters;
import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.entity.Stamp;
import nars.entity.Task;
import nars.entity.TruthValue;
import static nars.inference.Executive.isPlanTerm;
import nars.io.Texts;
import nars.language.Conjunction;
import nars.language.Implication;
import nars.language.Interval;
import nars.language.Term;
import nars.operator.Operation;
import nars.util.graph.ImplicationGraph;
import nars.util.graph.ImplicationGraph.Cause;
import nars.util.graph.ImplicationGraph.PostCondition;

public class GraphExecutive {

    public final Memory memory;
    public final ImplicationGraph implication;
    

    final int maxConsecutiveIntervalTerms = 1;
        
    /** controls the relative weigting of edges and vertices for particle traversals */
    double conceptExpectationFactor = 1.0;
    double causeRelevanceFactor = 1.0;
    double conceptPriorityFactor = 1.0;

    double minEdgeCost = 1.0;
    double costPerDelayMagniutde = 0.25;
    
    
    public GraphExecutive(Memory memory, Executive exec) {
        super();

        this.memory = memory;
        this.implication = new ImplicationGraph(memory);
    }

    
    protected void accumulate(final Term t, final Cause[] path) {        
        for (final Cause s : path)
            s.addActivity(1.0);
    }
    
    /** returns maximum value */
    public double fadeAccumulatedSentences(double rate) {
        double max = 0;
        for (final Cause c: implication.edgeSet()) {
            double vv = c.multActivity(rate);
            if (vv > max) max = vv;
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
        final public Term goal;
        
        Cause[] bestPath;
        double distance;
        double score;
        
        public ParticlePath(final Term goal, final List<Cause> path, final double cost) {
            this.goal = goal;            
            addPath(path, cost);
        }
        
        public void addPath(final List<Cause> p, final double cost) {
                
            
            double pathScore = 0;
            for (Cause c : p)
                pathScore += c.getTruth().getExpectation();
            
            if ((this.bestPath == null) || (score < pathScore)) {
                this.bestPath = p.toArray(new Cause[p.size()]);
                this.distance = cost;
                this.score = pathScore;                
            }
            
        }

        @Override
        public final int compareTo(final ParticlePath o) {
            return Double.compare(o.score(), score());
        }

        @Override
        public String toString() {
            return "[" + Texts.n4((float)score()) + "|" + Texts.n4((float)distance) + "] "/*+ target */ + " <- " + Arrays.toString(bestPath);
        }

        /** can be used to favor the total activation, or short distnce, or combinations of other factors  */
        private double score() {
            return score;
        }
        
    }
    
    public class ParticleActivation {
        private final ImplicationGraph graph;
        
        /** caches sentence costs while traversing */
        transient public final Map<Cause, Double> sentenceCosts = new HashMap();
        
        public final Map<Term, ParticlePath> termPaths = new HashMap();
        
        TreeSet<ParticlePath> paths;
        
        final boolean avoidCycles = true;
        private int edgeDecisionPass = 0;
        private int edgeDecisionFailCyclical = 0;
        private int edgeDecisionFailInvalidVertex = 0;
        private int edgeDecisionFailInactiveEdge = 0;
        private int pathFailEmpty = 0;
        private int pathFailNoOperation = 0;
        private int pathsValid = 0;
        private int numIterations = 0;
        private final Term goal, goalPost;
        
       
        /** optional required source vertex to reach */
        private final Term source;
        
        final int initialEdgeCosts = 512;
        List<Cause> possibleEdge = new ArrayList();
        double[] possibleEdgeCost = new double[initialEdgeCosts];

        public ParticleActivation(ImplicationGraph graph, final Term goal, final Term goalPost) {
            this(graph, goal, goalPost, null);
        }
                
        public ParticleActivation(ImplicationGraph graph, final Term goal, final Term goalPost, final Term source) {
            this.graph = graph;
            this.goal = goal;
            this.goalPost = goalPost;
            this.source = source;
        }
        

        final public void addEdgeCost(final Cause ca, final double c) {
            possibleEdge.add(ca);            
            final int nes = possibleEdge.size();
            if (nes == possibleEdgeCost.length) {
                double[] n = new double[nes*2];
                System.arraycopy(possibleEdgeCost, 0, n, 0, possibleEdgeCost.length);
                possibleEdgeCost = n;
            }
            possibleEdgeCost[nes-1] = c;
        }
        
        public SortedSet<ParticlePath> activate(final boolean forward, int iterations, double distance) {

            //TODO cache pathways in the graph for faster traversal. must store source leading edge, destination(s) and their distances
            
            List<Cause> currentPath = new ArrayList();
            
                                   
            sentenceCosts.clear();
            
            for (int i = 0; i < iterations; i++) {            

                numIterations++;
                
                currentPath.clear();

                double energy = distance;

                Term currentVertex = goalPost;

                boolean choicesAvailable = false;
                boolean operationTraversed = false;

                
                while (energy > 0) {
 
                    if (!graph.containsVertex(currentVertex))
                        break;
                    
                    Set<Cause> graphEdges = forward ? 
                            graph.outgoingEdgesOf(currentVertex) : 
                            graph.incomingEdgesOf(currentVertex);
                    
                    possibleEdge.clear();                    
                    
                    Cause currentSentence = null;
                    
                    double totalProb = 0;
                    
                    //remove edges which loop to the target goal precondition OR postcondition
                    for (final Cause s : graphEdges) {
                        Term etarget = forward ? s.effect : s.cause;
                        
                       
                        if ((avoidCycles) && (etarget == goalPost)) {
                            edgeDecisionFailCyclical++;
                            continue;
                        }
                           
                        if (!validVertex(etarget)) {
                            edgeDecisionFailInvalidVertex++;
                            continue;
                        }
                                    
                        
                        double ew = getTraversalCost(s, etarget);
                        
                        //ignore if this edge will cost more energy than allowed
                        if (ew > energy)
                            continue;
                        
                        currentSentence = s;
                        totalProb += 1.0 / ew;
                                                
                        addEdgeCost(currentSentence, ew);

                        edgeDecisionPass++;


                        if (etarget instanceof Operation) {
                            operationTraversed = true;
                        }
                        
                    }
                    
                    if (possibleEdge.isEmpty()) {
                        //particle went as far as it can
                        break;
                    }                

                    Cause nextEdge;
                    int nextEdgeIndex;
                    if (possibleEdge.size() == 1) {
                        nextEdgeIndex = 0;
                    }
                    else {
                        choicesAvailable = true;
                        nextEdgeIndex = chooseEdge(totalProb);
                    }

                    nextEdge = possibleEdge.get(nextEdgeIndex);
                    
                    currentPath.add(nextEdge);

                    energy -= possibleEdgeCost[nextEdgeIndex];

                    currentVertex = forward ? nextEdge.effect : nextEdge.cause;

                    if ((source!=null) && (nextEdge.cause.equals(source)))
                        break;

                }

                if (currentPath.isEmpty()) {
                    pathFailEmpty++;
                    continue;
                }
                                
                if ((source!=null) && (!currentPath.get(currentPath.size()-1).cause.equals(source))) {
                    //this path is invalid because it doesnt terminate at the required source postcondition
                    continue;
                }

                if (!operationTraversed) {
                    pathFailNoOperation++;
                    continue;                        
                }

                ParticlePath ppath = termPaths.get(currentVertex);
                if (ppath == null) {                    
                    termPaths.put(currentVertex, 
                            ppath = new ParticlePath(goal, currentPath, distance - energy));
                }
                else {
                    ppath.addPath(currentPath, distance - energy);
                }

                pathsValid++;

                if (!choicesAvailable) {                
                    //we've found the only path, so and we dont need to iterate any further
                    break;
                }
                

            }

            this.paths = new TreeSet(termPaths.values());
            
            for (ParticlePath p : this.paths) {
                accumulate(p.goal, p.bestPath);
                //System.out.println("  " + p);
            }
            return this.paths;
        }
        
        /** total cost of a traversal, which includes the edge cost and the target vertex cost. any value > 1 */
        public double getTraversalCost(final Cause s, final Term nextTerm) {
            Double d = sentenceCosts.get(s);
            if (d!=null)
                return d;
            
            
            double conceptExpectation, conceptPriority, delayCost = 0;
            if (nextTerm instanceof Interval) {
                Interval i = (Interval)nextTerm;                
                conceptExpectation = 1.0;
                conceptPriority = 1.0;
                delayCost = i.magnitude * costPerDelayMagniutde;
            }
            else if (nextTerm instanceof Operation) {
                conceptExpectation = 1.0;
                conceptPriority = 1.0;                
            }
            else {
                if (conceptExpectationFactor > 0)
                    conceptExpectation = getEffectiveExpectation(memory, nextTerm);
                else
                    conceptExpectation = 0;
                
                if (conceptPriorityFactor > 0)
                    conceptPriority = getEffectivePriority(memory, nextTerm);
                else
                    conceptPriority = 0;
            }

            double causeRelevancy = causeRelevanceFactor > 0 ? getCauseRelevancy(s, goal) : 0.0;

            double c = causeRelevanceFactor * (1.0 - causeRelevancy) + 
                       conceptExpectationFactor * (1.0 - conceptExpectation) +
                       conceptPriorityFactor * (1.0 - conceptPriority);
                        
            c/= (causeRelevanceFactor + conceptExpectationFactor + conceptPriorityFactor);
            
            c+= minEdgeCost;
            
            c+= delayCost;
                        
            //System.out.println("  s " + s + " >> " + nextTerm + " : " + " = " + c);

            sentenceCosts.put(s, c);
            return c;
        }
        
        
        /** choose a sentence according to a random probability 
         * where lower cost = higher probability.  */
        public int chooseEdge(double totalProb) {            
            //TODO disallow edge that completes cycle back to target or traversed edge?
            //  probably an option to allow cycles

            double r = Memory.randomNumber.nextDouble() * totalProb;


            final int pes = possibleEdge.size();
            int i;
            for (i = 0; i < pes; i++) {
                double v = possibleEdgeCost[i];

                double edgeProb = 1.0 / v;
                r -= edgeProb;

                if (r <= 0) {
                    //selected the next Edge
                    return i;
                }
            }
            
            return i-1;
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
    
    
    /** returns (no relevancy) 0..1.0 (high relevancy) */
    public double getCauseRelevancy(final Cause c, final Term goal) {
//        //how desired is the implication?
//        Concept w=memory.concept(c.getImplication().getPredicate());
//        if(w!=null && w.getDesire()!=null) {
//            return Math.max(0.0, Math.min(1.0, c.getTruth().getExpectation()*w.getDesire().getExpectation()));
//        }
//        return Math.max(0.0, Math.min(1.0, c.getTruth().getExpectation()));//+c.getRelevancy(goal)));//getCauseRelevancy(c) * c.getRelevancy(goal);    
        
        if (c.getTruth().getFrequency() < 0.25f) {
            System.err.println(c + " has inverse truth which could be used to negatively weight results");
        }
        
        return c.getTruth().getExpectation();
    }
    
    /** returns (no relevancy) 0..1.0 (high relevancy) */
    public double getCauseRelevancy(final Cause c) {
        return getCauseRelevancy(c, null);
    }    

    public static double getActualPriority(final Memory memory, final Term t) {
        double p;
        Concept c = memory.concept(t);
        if ((c!=null) && (!c.beliefs.isEmpty())) {
            //Sentence bestBelief = c.beliefs.get(0);
            //if (bestBelief!=null)
            //    return c.getPriority() * bestBelief.truth.getExpectation();            
            return c.getPriority(); //it is not expectation cause the goal could be to make a judgement true but also false
        }

        //System.err.println("No Concept priority available for " + t);
        
        //Probably an input term, so make it high priority
        return 1;
    }
    
    /** between 0...1 */
    public static float getActualExpectation(final Memory memory, final Term t) {
        double p;
        
        Concept c = memory.concept(t);
        if ((c!=null) && (!c.beliefs.isEmpty())) {
            Sentence bestBelief = c.beliefs.get(0);
            if (bestBelief!=null)
                return bestBelief.truth.getExpectation();                   
        }
        
        //System.err.println("No Concept confidence available for " + t);
        
        //if no concept confidence is available, assume 0
        return 0f;
    }

    /** returns 0..1.0, 1.0 being highest priority, 0 = no priority */
    public static double getEffectivePriority(final Memory memory, final Term current) {
        double p;
        
        //default priority for intervals
        if ((current instanceof Interval) || (current instanceof Operation))  
            p = 1.0f;
        
        //get the priority for the postcondition's actual concept
        else if (current instanceof PostCondition)            
            p = getActualPriority(memory, ((PostCondition)current).term[0]);        
        else 
            p = getActualPriority(memory, current);
        
        return p;
    }

   public static float getEffectiveExpectation(final Memory memory, final Term current) {        
        //get the priority for the postcondition's actual concept
        if (current instanceof PostCondition)
            return getActualExpectation(memory, ((PostCondition)current).term[0]);
        else
            return getActualExpectation(memory, current);      
    }    
        

    
    
    public class ParticlePlan implements Comparable<ParticlePlan> {

        public final Cause[] path;
        public final List<Term> sequence;
        public final double distance;
        public final double pathScore;
        public final TruthValue truth;
        public final BudgetValue budget;
        //private float minConf;
        private Task solution;
        private Task goal;

        //            if (path.length == 0) return 0;
        //
        //            float min = Float.MAX_VALUE;
        //            for (final Sentence s : path) {
        //                float c = s.truth.getConfidence();
        //                if (c < min)
        //                    min = c;
        //            }
        //            return min;
        public ParticlePlan(Cause[] path, List<Term> sequence, double pathScore, double distance) {
            this.path = path;
            this.sequence = sequence;
            this.pathScore = pathScore;
            this.distance = distance;
            //this.minConf = 1.0f;
            
            TruthValue tr=null;
            for (final Cause s : path) {
                if(tr==null) {
                    tr=s.getTruth();
                } else {
                    tr=TruthFunctions.deduction(tr, s.getTruth());
                }
                //float c = s.getTruth().getConfidence();
                //if (c < minConf) {
                //    minConf = c;
                //}
            }
            truth=tr;
            
            //float freq = 1.0f;
            //float freq = 0.5f + (minConf)/2f;            
            //truth = new TruthValue(freq, minConf);
            budget = new BudgetValue(1.0f, Parameters.DEFAULT_GOAL_DURABILITY, 
                    BudgetFunctions.truthToQuality(truth));
            budget.andPriority(score() / path.length);
        }

        public float score() {
            return (float)(/*minConf * */pathScore);
        }

        @Override
        public final int compareTo(final ParticlePlan o) {
            int i = Double.compare(o.score(), score());
            if ((i == 0) && (o != this)) {
                return -1;
            }
            return i;
        }

        public TruthValue getTruth() {
            return truth;
        }

        
        @Override
        public String toString() {
            return "[" + score() + "|" + distance + "] " + sequence;
        }
        
        public Task planTask(Concept c, Task goal, Term goalTerm, char punctuation) {
            
            Cause currentEdge = path[path.length-1];

            Stamp stamp = new Stamp(goal.sentence.stamp, currentEdge.getStamp(), memory.time());

            //add all terms to derivation chain
            for(Term T : sequence) {
                stamp.chainAdd(T); //todo: if too long kick out the first n terms
            }
            //todo: evidental base hm

            //memory.setTheNewStamp(stamp);

            //memory.setCurrentTask(task);

            //remove final element from path if it's equal to target
            /*if (seq.get(seq.size()-1).equals(target)) {
                seq.remove(seq.size()-1);
            }*/

            Term subj = sequence.size() > 1 ?
                Conjunction.make(sequence.toArray(new Term[sequence.size()]), TemporalRules.ORDER_FORWARD)
                    :
                sequence.get(0);


            //val=TruthFunctions.abduction(val, newEvent.sentence.truth);

            Term imp = Implication.make(subj, goalTerm, TemporalRules.ORDER_FORWARD);

            if (imp == null) {
                throw new RuntimeException("Invalid implication: " + subj + " =\\> " + goalTerm);
            }


            this.goal = goal;
            this.solution = new Task(new Sentence(imp, punctuation, truth, stamp), budget, goal) {

                @Override public void end(boolean success) {
                    super.end(success);
                }

            };
            return solution;
        }
        
        /** the task that caused the need for this plan */
        public Task getGoal() { return goal; }
        
        /** the plan's created task */
        public Task getSolution() { return solution; }
                   
    }


    protected void particlePredict(final Term source, final double distance, final int particles) {
        ParticleActivation act = new ParticleActivation(implication, source, source, null);
        SortedSet<ParticlePath> paths = act.activate(true, particles, distance);
        if (!paths.isEmpty())
            System.out.println(source + " predicts: " + paths);
        
    }
    
    
    public TreeSet<ParticlePlan> particlePlan(final Term target, final double distance, final int particles) {
        return particlePlan(target, distance, particles, null);
    }
            
    /** 
     * 
     * @param target  starting goal vertex to plan from
     * @param distance
     * @param particles
     * @param source     required source vertex to reach, or null if none is required (accepts any)
     * @return 
     */
    public TreeSet<ParticlePlan> particlePlan(final Term target, final double distance, final int particles, final Term source) {        
                
        PostCondition targetPost = new PostCondition(target);
        
        if (!implication.containsVertex(targetPost)) {
            //System.out.println("  plan for " + target + ": missing postCondition vertex");
            return null;
        }
        
        ParticleActivation act = new ParticleActivation(implication, target, targetPost, source) {
            @Override public boolean validVertex(final Term x) {
                //additional restriction on path's vertices
                return !targetPost.equals(x);
            }            
        };
        
        SortedSet<ParticlePath> roots = act.activate(false, particles, distance);
    

        if (roots == null) {            
            return null;
        }
        
        
/*        if (memory.emitting(ParticlePath.class)) {            
            for (ParticlePath pp : roots) {
                memory.emit(ParticlePath.class, target, pp);
            }
        }*/
                
        TreeSet<ParticlePlan> plans = new TreeSet();
        for (final ParticlePath pp : roots) {

            Cause[] path = pp.bestPath;
            
            if (path.length == 0)
                throw new RuntimeException("ParticlePath empty: " + pp);
            
            int operations = 0;
            
            List<Term> seq = new ArrayList(path.length);
                        
            //Calculate path back to target
            long accumulatedDelay = 0;
                                                                       
            for (int i = path.length-1; i >=0; ) {
                Cause s = path[i];
                
                                
                Term term = s.cause;
                
                i--; //next impl                                
                                                
                if (isPlanTerm(term)) {                                        
                    boolean isInterval = term instanceof Interval;
                    if (!isInterval) {
                                                
                        
                        if (accumulatedDelay > 0) {
                            seq.addAll(Interval.intervalTimeSequence(
                                    accumulatedDelay, maxConsecutiveIntervalTerms, memory)  );
                            accumulatedDelay = 0;                            
                        }
                                                
                        seq.add(term);
                        
                    }
                    else {
                        Interval in = (Interval)term;
                        long time = in.getTime(memory);
                        accumulatedDelay += time;
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
                
                if (term instanceof Operation)
                    operations++;
            }            
                
            //TODO check this prior to above loop, to avoid wasting that effort
            if (operations == 0)
                continue;
                        
            if (seq.isEmpty())
                continue;
            
            int lastTerm = seq.size()-1;
            if (seq.get(lastTerm) instanceof Interval)
                seq.remove(lastTerm);

            
            
            //System.out.println("  cause: " + Arrays.toString(path));
            ParticlePlan rp = new ParticlePlan(path, seq, pp.score(), pp.distance);            
            //System.out.println(" +path: " + pp);
            plans.add(rp);
            

        }
        
        return plans;
    } 
    
    protected Task planTask(nars.core.control.NAL nal, ParticlePlan plan, Concept c, Task task, Term target, char punctuation) {
        
        Task newTask = plan.planTask(c, task, target, punctuation);
        
        nal.derivedTask(newTask, false, true, null, null);  //and if this is a implication then wrong
        memory.executive.addExecution(c, newTask);
        
        return newTask;
        
    }

   public int plan(final nars.core.control.NAL nal, Concept c, Task task, Term target, int particles, double searchDistance, char punctuation, int maxTasks) {

        TreeSet<ParticlePlan> plans = particlePlan(target, searchDistance, particles);
        
        int n = 0;
        
        boolean emittingPlans = memory.emitting(ParticlePlan.class);
                
        for (ParticlePlan p : plans) {
            planTask(nal, p, c, task, target, punctuation); 
            
            if (emittingPlans) {
                memory.emit(ParticlePlan.class, target, p);
            }        
                
            if (n++ == maxTasks)
                break;
        }
        
        memory.logic.PLAN_TASK_PLANNED.commit(n);
        
        return n;
       
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
