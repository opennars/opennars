package nars.inference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
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
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.entity.Stamp;
import nars.entity.Task;
import nars.entity.TruthValue;
import nars.io.Symbols.NativeOperator;
import nars.io.Texts;
import nars.io.buffer.PriorityBuffer;
import nars.language.Conjunction;
import nars.language.Implication;
import nars.language.Interval;
import nars.language.Term;
import nars.operator.Operation;
import nars.operator.Operator;
import nars.util.graph.ImplicationGraph;
import nars.util.graph.ImplicationGraph.PostCondition;

public class GraphExecutive implements Observer {

    private final Memory memory;
    public final ImplicationGraph implication;
    
    PriorityBuffer<TaskConcept> tasks;
    
    
    int numTasks = 16;
            
    float searchDepth = 16;
    int particles = 128;
    
    @Deprecated Executive exec;
    
    public GraphExecutive(Memory memory, Executive exec) {
        super();
        this.exec=exec;
        this.memory = memory;
        tasks = new PriorityBuffer<TaskConcept>(new Comparator<TaskConcept>() {
            @Override
            public final int compare(final TaskConcept a, final TaskConcept b) {
                float bp = b.getMotivation();
                float ap = a.getMotivation();
                if (bp != ap) {
                    return Float.compare(bp, ap);
                } else {
                    float ad = a.getPriority();
                    float bd = b.getPriority();
                    if (ad!=bd)
                        return Float.compare(bd, ad);
                    else {
                        float add = a.getDurability();
                        float bdd = b.getDurability();
                        return Float.compare(bdd, add);                        
                    }
                }

            }
        }, numTasks) {

            @Override protected void reject(final TaskConcept t) {
                removeTask(t.t);
            }
            
        };

        implication = new ImplicationGraph(memory);
        memory.event.on(CycleEnd.class, this);
        memory.event.on(ConceptGoalAdd.class, this);
        memory.event.on(ConceptGoalRemove.class, this);
    }

    /** whether the Term is currently a valid goal for the implication graph to plan for */
    private boolean isPlannable(final Term goal) {
        /** must be in the graph and have at least one incoming edge */
        if (implication.containsVertex(goal)) {
            return implication.inDegreeOf(new PostCondition(goal)) > 0;
        }
        return false;
    }

    public static class TaskConcept {
        /** may be null for input tasks */
        public final Concept c;
        
        public final Task t;
        public int sequence;
        public long delayUntil = -1;
        private float motivationFactor = 1;
        
        public TaskConcept(final Concept c, final Task t) {
            this.c = c;
            this.t = t;
        }

        @Override public boolean equals(final Object obj) {
            if (obj instanceof TaskConcept) {
                return ((TaskConcept)obj).t.equals(t);
            }
            return false;
        }
        
        public final float getDesire() { 
            if (c == null) return 1.0f;
            return c.getDesire().getExpectation();         
        }
        public final float getPriority() { return t.getPriority();         }
        public final float getDurability() { return t.getDurability(); }
        public final float getMotivation() { return getDesire() * getPriority() * motivationFactor;         }
        public final void setMotivationFactor(final float f) { this.motivationFactor = f;  }

        @Override public int hashCode() {            return t.hashCode();         }

        @Override
        public String toString() {
            return "!" + Texts.n2(getMotivation()) + "! " + t.toString();
        }


        

        
    }

    public boolean inputTask(Concept concept, Task t) {
        if (concept != null) {
            if (concept.getDesire()!=null)
                if (concept.getDesire().getExpectation() < memory.param.decisionThreshold.get()) {
                    return false;
                }
        }
            

        if (!tasks.contains(t)) {
            //incoming task
            Term c = t.getContent();
            if ((c instanceof Operation) || isSequenceConjunction(c))  {
                addTask(concept, t);
                //return true;
            }
            /*else*/ {
                boolean plannable = isPlannable(t.getContent());
                System.out.println("Goal: " + t + " plannable=" + plannable + ", implgraph=" 
                        + implication.vertexSet().size() + "|" + implication.edgeSet().size());
                if (plannable) {                    
                    plan(concept, t, t.getContent(), searchDepth, '!');
                    return true;
                }
            }
        }
        return false;
    }
    
    
    protected void addTask(final Concept c, final Task t) {
        if (tasks.add(new TaskConcept(c, t))) {
            //added successfully
        }
    }
    protected void removeTask(final Task t) {
        //since TaskContent will equal according to its task, it will match 't' even though it's not a TaskContent
        tasks.remove(t);
    }
    
    protected void updatePriorities() {
        List<TaskConcept> t = new ArrayList(tasks);
        tasks.clear();
        for (TaskConcept x : t) {
            if ((x.getDesire() > 0) && (x.getPriority() > 0))
                tasks.add(x);
        }
    }

    @Override
    public void event(final Class event, final Object[] a) {

        if (event == ConceptGoalAdd.class) {
            Concept concept = (Concept)a[0];
            Task t = (Task) a[2];
            
            boolean revised = (Boolean)(((Object[])a[3])[0]);
            
            //if (!revised)
                inputTask(concept, t);
                        
        } else if (event == ConceptGoalRemove.class) {
            Task t = (Task) a[2];
            //removeTask(t);
            //System.out.println("Goal rem: " + a[0] + " " + a[1] + " " + t.budget);
        } else if (event == CycleEnd.class) {
//            if (tasks.size() > 0) {
//                plan();
//            }

            cycle();
            
        }
    }

    public boolean isSequenceConjunction(Term c) {
        if (c instanceof Conjunction) {
            if ( ((Conjunction)c).operator() == NativeOperator.SEQUENCE  )
                return true;
        }
        return false;
    }
    protected void execute(final Operation op, final Task task) {
        
        Operator oper = op.getOperator();
        
        System.out.println("ex2: " + op + " from " + task.toString());
        
        op.setTask(task);
                        
        oper.call(op, memory);
        
        
    }
    
    
    protected void cycle() {
        /*if (tasks.size() > 0)
            System.out.println("Tasks (pre): " + tasks);*/
        
        updatePriorities();

        if (tasks.size() == 0)
            return;
        
        if (tasks.size() > 1) {
            System.out.println("Tasks @ " + memory.getTime());
            for (TaskConcept tcc : tasks)
                System.out.println("  " + tcc.toString());
        }
        else {
            System.out.println("Task @ " + memory.getTime() + ": " + tasks.get(0));
        }
        
        
        TaskConcept topConcept = tasks.getFirst();
        Task top = topConcept.t;
        Term term = top.getContent();
        if (term instanceof Operation) {            
            execute((Operation)term, top); //directly execute
            top.setPriority(0);
        }
        else if (term instanceof Conjunction) {
            Conjunction c = (Conjunction)term;
            if (c.operator() == NativeOperator.SEQUENCE) {
                executeConjunctionSequence(topConcept, c);
            }
            
        }
        else if (term instanceof Implication) {
            Implication it = (Implication)term;
            if (it.getSubject() instanceof Conjunction) {
                Conjunction c = (Conjunction)it.getSubject();
                if (c.operator() == NativeOperator.SEQUENCE) {
                    executeConjunctionSequence(topConcept, c);
                    return;
                }
            }
            else if (it.getSubject() instanceof Operation) {
                execute((Operation)it.getSubject(), top); //directly execute
                top.setPriority(0);
                return;
            }
            throw new RuntimeException("Unrecognized executable term: " + it.getSubject() + "[" + it.getSubject().getClass() + "] from " + top);
        }
        
//        //Example prediction
//        if (memory.getCurrentBelief()!=null) {
//            Term currentTerm = memory.getCurrentBelief().content;
//            if (implication.containsVertex(currentTerm)) {
//                particlePredict(currentTerm, 12, particles);
//            }                
//        }
    }
    
    public static boolean validPlanComponent(final Term t) {
        return ((t instanceof Interval) || (t instanceof Operation));
    }

    private void executeConjunctionSequence(final TaskConcept task, final Conjunction c) {
        int s = task.sequence;
        Term currentTerm = c.term[s];
        if (currentTerm instanceof Operation) {
            execute((Operation)currentTerm, task.t);
            s++;
        }
        else if (currentTerm instanceof Interval) {
            Interval ui = (Interval)currentTerm;

            if (task.delayUntil == -1) {
                task.delayUntil = memory.getTime() + Interval.magnitudeToTime(ui.magnitude, memory.param.duration);
                //decrease priority in proportion to magnitude                
                task.setMotivationFactor(1/(ui.magnitude+1)); 
                
                //TODO raise priority when the delay is finished so that it can trigger below
            }
            else {
                if (task.delayUntil <= memory.getTime()) {
                    //delay finished, continue execution with normal motivation
                    task.setMotivationFactor(1.0f);
                    task.delayUntil = -1;
                    s++;
                }
                else {
                    //..
                }
            }
        }        
        else {
            s++;
            System.err.println("Non-executable term in sequence: " + currentTerm + " in " + c);
            task.t.setPriority(0);
        }

        if (s == c.term.length) {
            //end of task
            System.out.println("  Completed " + task);
            task.t.setPriority(0);
            removeTask(task.t);
        }
        else {            
            task.sequence = s;//update new value for next cycle
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

            //TODO cache pathways in the graph for faster traversal. must store source leading edge, destination(s) and their distances
            
            double particleActivation = 1.0 / iterations;

            List<Sentence> currentPath = new ArrayList();

            List<Sentence> nextEdges = new ArrayList();
            
            for (int i = 0; i < iterations; i++) {            

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

                    //remove edges which loop to the target goal precondition OR postcondition
                    for (final Sentence s : graphEdges) {
                        Term etarget = forward ?
                                graph.getEdgeTarget(s) :
                                graph.getEdgeSource(s);
                        
                        if (!etarget.equals(source)/* || etarget.equals(target)*/) {
                            nextEdges.add(s);
                        }
                        if (etarget instanceof Operation) {
                            operationTraversed = true;
                        }
                    }


                    if (nextEdges.isEmpty()) {
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

                        double totalProb = 0;
                        for (int j = 0; j < numEdges; j++) {
                            totalProb += 1.0 / graph.getEdgeWeight(nextEdges.get(j));
                        }
                        
                        double r = Memory.randomNumber.nextDouble() * totalProb;
                        System.out.print(totalProb + " " + r);

                        int j;
                        for (j = 0; j < numEdges; j++) {
                            nextEdge = nextEdges.get(j);
                            r -= 1.0 / graph.getEdgeWeight(nextEdge);
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

                if ((currentPath.isEmpty()) || (!operationTraversed))
                    continue;                        

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


            return new TreeSet(paths);
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
    
    protected void particlePredict(final Term source, final double distance, final int particles) {
        ParticleActivation act = new ParticleActivation(implication);
        SortedSet<ParticlePath> paths = act.activate(source, true, particles, distance);
        if (!paths.isEmpty())
            System.out.println(source + " predicts: " + paths);
        
    }
    
    protected ParticlePlan particlePlan(final Term target, final double distance, final int particles) {
        PostCondition targetPost = new PostCondition(target);
        
        if (!implication.containsVertex(targetPost)) {
            System.out.println("  plan for " + target + ": missing postCondition vertex");
            return null;
        }
        
        ParticleActivation act = new ParticleActivation(implication);
        SortedSet<ParticlePath> roots = act.activate(targetPost, false, particles, distance);
        

        if (roots == null) {
            System.out.println("  plan fail: no roots");
            return null;
        }
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
                    if (nonIntervalAdded) { ////ignore prefix intervals                        
                        int temporal = (s.content).getTemporalOrder();
                                                
                        //only accumulate delay if the temporal rule involves time difference
                        if ((temporal == TemporalRules.ORDER_FORWARD) || (temporal == TemporalRules.ORDER_BACKWARD)) {
                            
                            accumulatedDelay++;
                        }
                        
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
            
            if (seq.isEmpty())
                continue;

            System.out.println("  cause: " + Arrays.toString(path));

            return new ParticlePlan(path, seq, pp.activation, pp.distance);
        }
        
        System.out.println("  no eligible roots: " + roots.size());
        
        return null;
    }

    protected void plan(Concept c, Task task, Term target, double searchDistance, char punctuation) {

        if (!implication.containsVertex(target))
            return;

        ParticlePlan plan = particlePlan(target, searchDistance, particles);
        if (plan == null) {
            System.out.println("  plan failure, vert degree=" + implication.inDegreeOf(target) + "|" + implication.outDegreeOf(target));
            return;
        }
        Sentence[] path = plan.path;
        List<Term> seq = plan.sequence;
        if (seq.isEmpty()) {
            System.out.println("  plan failure: sequence empty");
            return;
        }
        
        Sentence currentEdge = path[path.length-1];

        Stamp stamp = Stamp.make(task.sentence.stamp, currentEdge.stamp, memory.getTime());
        //memory.setTheNewStamp(stamp);

        //memory.setCurrentTask(task);
        
        //remove final element from path if it's equal to target
        if (seq.get(seq.size()-1).equals(target)) {
            seq.remove(seq.size()-1);
        }

        Term subj = seq.size() > 1 ?
            Conjunction.make(seq.toArray(new Term[seq.size()]), TemporalRules.ORDER_FORWARD, memory)
                :
            seq.get(0);
        
        
        //System.out.println(" -> Graph PATH: " + subj + " =\\> " + target);

        //double planDistance = Math.min(plan.distance, searchDistance);
        float confidence = (float)plan.activation; // * planDistance/searchDistance;
        TruthValue val = new TruthValue(1.0f, confidence);
        
        //val=TruthFunctions.abduction(val, newEvent.sentence.truth);

        Term imp = Implication.make(subj, target, TemporalRules.ORDER_FORWARD, memory);

        if (imp == null) {
            throw new RuntimeException("Invalid implication: " + subj + " =\\> " + target);
        }
        
        BudgetValue bud = BudgetFunctions.forward(val, memory);
        //bud.andPriority(confidence);
        
        Task t = new Task(new Sentence(imp, punctuation, val, stamp), bud);
        
        System.out.println("  -> Plan: " + t);

        //memory.doublePremiseTask(imp, val, bud);
        //memory.inputTask(t);
        
        //exec.decisionMaking2(t);
        
        addTask(c, t);
        //memory.inputTask(t);
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
