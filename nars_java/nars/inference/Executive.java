package nars.inference;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.TreeSet;
import nars.core.Memory;
import nars.core.NAR;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.entity.Stamp;
import nars.entity.Task;
import nars.entity.TruthValue;
import static nars.inference.BudgetFunctions.rankBelief;
import nars.io.Symbols;
import nars.io.Texts;
import nars.io.buffer.PriorityBuffer;
import nars.language.Conjunction;
import nars.language.Implication;
import nars.language.Interval;
import nars.language.Term;
import static nars.language.Terms.equalSubTermsInRespectToImageAndProduct;
import nars.operator.Operation;
import nars.operator.Operator;

/**
 * Operation execution and planning support.  
 * Strengthens and accelerates goal-reaching activity
 */
public class Executive {
    
    public final GraphExecutive graph;
    
    public final Memory memory;
    
    /** previous events, for temporal induction */
    public final Deque<Task> shortTermMemory = new ArrayDeque<>();

    /** memory for faster execution of &/ statements (experiment) */
    public final Deque<TaskConceptContent> next = new ArrayDeque<>();

    PriorityBuffer<TaskExecution> tasks;

    boolean planningEnabled = true;
    
    /** number of tasks that are active in the sorted priority buffer for execution */
    int numActiveTasks = 8;

    /** max number of tasks that a plan can generate. chooses the N most confident */
    int maxPlannedTasks = 1;
    
    float searchDepth = 16;
    int particles = 32;
    
    
    public static class TaskConceptContent {
        
        public final Task task;
        public final Concept concept;
        public final Term content;

        public static TaskConceptContent NULL = new TaskConceptContent();
        
        /** null placeholder */
        protected TaskConceptContent() {
            this.task = null;
            this.concept = null;
            this.content = null;
        }

        public TaskConceptContent(Task task, Concept concept, Term content) {
            this.task = task;
            this.concept = concept;
            this.content = content;
        }
        
    }
    
    
    public Executive(Memory mem) {
        this.memory = mem;        
        this.graph = new GraphExecutive(mem,this);

        this.tasks = new PriorityBuffer<TaskExecution>(new Comparator<TaskExecution>() {
            @Override
            public final int compare(final TaskExecution a, final TaskExecution b) {
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
        }, numActiveTasks) {

            @Override protected void reject(final TaskExecution t) {
                removeTask(t.t);
            }
            
        };
        
    }
    
    public class TaskExecution {
        /** may be null for input tasks */
        public final Concept c;
        
        public final Task t;
        public int sequence;
        public long delayUntil = -1;
        private float motivationFactor = 1;
        
        public TaskExecution(final Concept concept, Task t) {
            this.c = concept;
            
            
            //Check if task is 
            Term term = t.getContent();
            if (term instanceof Implication) {
                Implication it = (Implication)term;
                if ((it.getTemporalOrder() == TemporalRules.ORDER_FORWARD) || (it.getTemporalOrder() == TemporalRules.ORDER_CONCURRENT)) {
                    if (it.getSubject() instanceof Conjunction) {
                        t = inlineConjunction(t, (Conjunction)it.getSubject());
                    }
                }
            }
            else if (term instanceof Conjunction) {
                t = inlineConjunction(t, (Conjunction)term);
            }
            
            this.t = t;

        }

        protected Task inlineConjunction(Task t, final Conjunction c) {
            List<Term> inlined = new ArrayList();
            boolean wasInlined = false;
            if (c.operator() == Symbols.NativeOperator.SEQUENCE) {
                for (Term e : c.term) {
                    if (!isPlanTerm(e)) {
                        if (graph.isPlannable(e)) {
                            
                            TreeSet<GraphExecutive.ParticlePlan> plans = graph.particlePlan(e, searchDepth, particles);
                            if (plans.size() > 0) {
                                //use the first
                                GraphExecutive.ParticlePlan pp = plans.first();
                                inlined.addAll(pp.sequence);
                                //System.err.println("Inline " + e + " in " + t.getContent() + " = " + pp.sequence);  
                                wasInlined = true;
                            }
                            else {
                                //System.err.println("Inline: no planavailable");
                                //no plan available, this wont be able to execute
                                setMotivationFactor(0);
                            }
                        }
                        else {
                            //this won't be able to execute here
                            setMotivationFactor(0);
                        }
                    }
                    else {
                        //executable term, add
                        inlined.add(e);
                    }
                }                            
            }            
            
            if (wasInlined) {
                Conjunction nc = c.cloneReplacingTerms(inlined.toArray(new Term[inlined.size()]));
                t = t.clone(t.sentence.clone(nc) );
                //System.err.println("  replaced task: " + t);
            }
            return t;
        }
        
        @Override public boolean equals(final Object obj) {
            if (obj instanceof TaskExecution) {
                return ((TaskExecution)obj).t.equals(t);
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
            return "!" + Texts.n2(getMotivation()) + "." + sequence + "! " + t.toString();
        }


        

        
    }

    
    
    protected void addTask(final Concept c, final Task t) {
        if (tasks.add(new TaskExecution(c, t))) {
            //added successfully
        }
    }
    protected void removeTask(final Task t) {
        //since TaskContent will equal according to its task, it will match 't' even though it's not a TaskContent
        tasks.remove(t);
    }
    
    protected void updateTasks() {
        List<TaskExecution> t = new ArrayList(tasks);
        tasks.clear();
        for (TaskExecution x : t) {
            if ((x.getDesire() > 0) && (x.getPriority() > 0)) {
                tasks.add(x);
                if ((x.delayUntil!=-1) && (x.delayUntil <= memory.getTime())) {
                    //restore motivation so task can resume processing
                    x.motivationFactor = 1.0f;
                }
            }
        }
    }
    

    public void reset() {
        next.clear();
        shortTermMemory.clear();        
    }   
    

//    public void manageExecution()  {
//        
//        if (next.isEmpty()) {
//            return;
//        }
//        
//        TaskConceptContent n = next.pollFirst();
//        
//        
//        if (n.task==null) {
//            //we have to wait
//            return; 
//        }                
//        
//        if (!(n.content instanceof Operation)) {
//            throw new RuntimeException("manageExecution: Term content is not Operation: " + n.content); 
//        }
//        
//        System.out.println("manageExecution: " + n.task);
//
//        //ok it is time for action:
//        execute((Operation)n.content, n.concept, n.task, true);
//    }    

    protected void execute(final Operation op, final Task task) {
        
        Operator oper = op.getOperator();
        
        if (NAR.DEBUG)
            System.out.println("exe: " + task.getExplanation().trim());
        
        op.setTask(task);
                        
        oper.call(op, memory);        
        
    }
    
    protected void forget(final Task t) {
        t.setPriority(0);

        Concept c = memory.concept(t.getContent());
        if (c!=null) {
            memory.conceptProcessor.forget(c);
            System.err.println("forgetting: " + t);
        }
        else {
            System.err.println("task has no corresponding concept: " + t);
        }
        
    }
            
        
    /** Add plausibility estimation */
    public void decisionMaking(final Task t, final Concept concept) {
                        
        Term content = concept.term;        
        
        TruthValue desireValue = concept.getDesire();
        
        if (desireValue.getExpectation() < memory.param.decisionThreshold.get()) {
            return;
        }
        
        //forget(t);
        
        if (content instanceof Operation) {
            //immediately execute
            execute((Operation)content, t);
            return;
        }
        else if (isSequenceConjunction(content)) {
            if (!tasks.contains(t)) {
                if (memory.getRecorder().isActive())
                   memory.getRecorder().append("Goal Scheduled", t.toString());
                addTask(concept, t);
            }
        }
        else {
            if (planningEnabled) {
                boolean plannable = graph.isPlannable(t.getContent());
                if (plannable) {                    
                    if (memory.getRecorder().isActive())
                       memory.getRecorder().append("Goal Planned", t.toString());
                    
                    graph.plan(concept, t, t.getContent(), 
                            particles, searchDepth, '.', maxPlannedTasks);
                }                
            }
        }              
    }
    
    public void cycle() {
        
        updateTasks();

        if (tasks.size() == 0)
            return;
        
        if (NAR.DEBUG) {
            //TODO make a print function
            if (tasks.get(0).delayUntil==-1) {
                if (tasks.size() > 1)  {
                    System.out.println("Tasks @ " + memory.getTime());
                    for (TaskExecution tcc : tasks)
                        System.out.println("  " + tcc.toString());
                }
                else {
                    System.out.println("Task @ " + memory.getTime() + ": " + tasks.get(0));
                }
            }
        }
        
        
        TaskExecution topConcept = tasks.getFirst();
        Task top = topConcept.t;
        Term term = top.getContent();
        if (term instanceof Operation) {            
            execute((Operation)term, top); //directly execute
            top.setPriority(0);
            return;
        }
        else if (term instanceof Conjunction) {
            Conjunction c = (Conjunction)term;
            if (c.operator() == Symbols.NativeOperator.SEQUENCE) {
                executeConjunctionSequence(topConcept, c);
                return;
            }
            
        }
        else if (term instanceof Implication) {
            Implication it = (Implication)term;
            if ((it.getTemporalOrder() == TemporalRules.ORDER_FORWARD) || (it.getTemporalOrder() == TemporalRules.ORDER_CONCURRENT)) {
                if (it.getSubject() instanceof Conjunction) {
                    Conjunction c = (Conjunction)it.getSubject();
                    if (c.operator() == Symbols.NativeOperator.SEQUENCE) {
                        executeConjunctionSequence(topConcept, c);
                        return;
                    }
                }
                else if (it.getSubject() instanceof Operation) {
                    execute((Operation)it.getSubject(), top); //directly execute
                    top.setPriority(0);
                    return;
                }
            }
            throw new RuntimeException("Unrecognized executable term: " + it.getSubject() + "[" + it.getSubject().getClass() + "] from " + top);
        }
        else {
            throw new RuntimeException("Unknown Task type: "+ top);
        }
        
        
//        //Example prediction
//        if (memory.getCurrentBelief()!=null) {
//            Term currentTerm = memory.getCurrentBelief().content;
//            if (implication.containsVertex(currentTerm)) {
//                particlePredict(currentTerm, 12, particles);
//            }                
//        }
    }

   
    public static boolean isPlanTerm(final Term t) {
        return ((t instanceof Interval) || (t instanceof Operation));
    }
    
    public static boolean isExecutableTerm(final Term t) {
        return (t instanceof Operation) || isSequenceConjunction(t);
 //task.sentence.content instanceof Operation || (task.sentence.content instanceof Conjunction && task.sentence.content.getTemporalOrder()==TemporalRules.ORDER_FORWARD)))
    }
    
    public static boolean isSequenceConjunction(final Term c) {
        if (c instanceof Conjunction) {
            Conjunction cc = ((Conjunction)c);
            if ( cc.operator() == Symbols.NativeOperator.SEQUENCE  ) {
                return (cc.getTemporalOrder()==TemporalRules.ORDER_FORWARD) || (cc.getTemporalOrder()==TemporalRules.ORDER_CONCURRENT);
            }
        }
        return false;
    }
    

    
    private void executeConjunctionSequence(final TaskExecution task, final Conjunction c) {
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
                task.setMotivationFactor(1f/(ui.magnitude+1f)); 
                
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
            System.err.println("Non-executable term in sequence: " + currentTerm + " in " + c + " from task " + task.t);
            task.t.setPriority(0);
        }

        if (s == c.term.length) {
            //end of task
            //System.out.println("  Completed " + task);
            task.t.setPriority(0);
            removeTask(task.t);
        }
        else {            
            task.sequence = s;//update new value for next cycle
        }
    }
 
//    
//      /** Add plausibility estimation */
//    public void decisionMaking2(final Task task) {
//        
//        //System.out.println("decision making: " + task + " c=" + concept);
//                
//        Term content = ((Statement)task.sentence.content).getSubject();
//        
//    
//        
//        
//        //FAST EXECUTION OF OPERATOR SEQUENCE LIKE STM PROVIDES
//        if ((content instanceof Conjunction) && (content.getTemporalOrder()==TemporalRules.ORDER_FORWARD)) {
//            
//            //1. get first operator and execute it
//            CompoundTerm cont = (CompoundTerm) content;
//            
//            for (final Term t : cont.term) {
//                if(!(t instanceof Operation) && !(t instanceof Interval)) {
//                    return;
//                }
//            }
//            
//            final AtomicDuration duration = memory.param.duration;
//            
//            for (final Term t : cont.term) {
//                
//                if(t instanceof Interval) {
//                    Interval intv=(Interval) t;
//                    
//                    long wait_steps = intv.getTime(duration);
//                           
//                    for(long i=0;i<wait_steps; i++) {
//                        next.addLast(TaskConceptContent.NULL);
//                    }
//                }
//                else if(t instanceof Operation) {
//                    next.addLast(new TaskConceptContent(task, null, t));
//                }
//            }
//            return;           
//        }
//        //END FAST EXECUTION OF OPERATOR SEQUENCE LIKE STM PROVIDES
//        if (!(content instanceof Operation)) {
//            //throw new RuntimeException("decisionMaking: Term content is not Operation: " + content);          
//            return;
//        }
//        
//        if(next.isEmpty())
//            execute((Operation)content, null, task, false);
//    }
    
    
    @Deprecated public boolean isActionable(final Task task, final Task newEvent) {
        
        if ((task.sentence!=null) && (task.sentence.content!=null) && (task.sentence.content instanceof Implication))
            return true;
        
        if (task.sentence.stamp.getOccurrenceTime() == Stamp.ETERNAL) {
            return false;
        }
        
        if (!task.sentence.isJudgment()) {
            return false;
        }

            
        if ((newEvent == null)
                || (rankBelief(newEvent.sentence) < rankBelief(task.sentence))) {
            
            return 
                ((shortTermMemory.isEmpty()                     
                    ||                    
                !equalSubTermsInRespectToImageAndProduct(
                            shortTermMemory.getLast().getContent(),
                            task.getContent()))
            );
        }        
        
        return false;        
    }

    public boolean isActionable(final Task task) {
        
        if ((task.sentence!=null) && (task.sentence.content!=null) && (task.sentence.content instanceof Implication))
            return true;
        
        if (task.sentence.stamp.getOccurrenceTime() == Stamp.ETERNAL) {
            return false;
        }
        
        if (!task.sentence.isJudgment()) {
            return false;
        }

        return false;        
    }
    
//    public boolean planShortTerm(final Concept concept, final Task task, final Memory mem) {
//        return graph.inputTask(concept, task);
//    }
    
    public boolean planShortTerm(final Task newEvent, Memory mem) {

        if (newEvent == null)
            return false;
        
                
        boolean actionable = isActionable(newEvent,mem);
        //boolean actionable = true;
        
        if (!actionable) {
            return false;
        }
        
        
        

        final int maxStmSize =  memory.param.shortTermMemorySize.get();
        int stmSize = shortTermMemory.size();

        if (stmSize!=0) { 
            //also here like in rule tables: we dont want to derive useless statements
                        
            Task stmLast = shortTermMemory.getLast();

            if(equalSubTermsInRespectToImageAndProduct(newEvent.sentence.content,stmLast.sentence.content)) {
                return false;
            }
            
            
            memory.setTheNewStamp(Stamp.make(newEvent.sentence.stamp, stmLast.sentence.stamp, memory.getTime()));
            
            memory.setCurrentTask(newEvent);
                        
            
            Sentence currentBelief = stmLast.sentence;
            
            memory.setCurrentBelief(currentBelief);

            TemporalRules.temporalInduction(newEvent.sentence, currentBelief, memory);

            //SHORT TERM MEMORY: REMEMBER OWN ACTION SEQUENCES
            /*if(!(newEvent.sentence.content instanceof Operation)) {

                final AtomicDuration duration = memory.param.duration;


                ArrayList<Term> cur=new ArrayList<Term>();
                
                Iterator<Task> t = shortTermMemory.descendingIterator();
                Task curT = t.next(), nextT = null;
                int i = stmSize;
                do {
                    i--;
                    
                    if (t.hasNext())
                        nextT = t.next();
                    else
                        nextT = null;
                    
                    cur.add(curT.getContent());
                    
                    if (nextT!=null) {
                        long diff = curT.getCreationTime() - nextT.getCreationTime();
                        
                        if (diff >= duration.get()) {
                            cur.add( Interval.intervalTime(diff, duration) );
                        }
                    }
                    
                    if (t.hasNext()) {
                        curT = nextT;
                        continue; //just use last one
                    }
                    else {
                        //Finalize
                        
                        memory.setCurrentBelief(curT.sentence);

                        TruthValue val = curT.sentence.truth;

                        //for(int j=i+1;j+1<n;j++) { 
                        //    val=TruthFunctions.abduction(val,shortTermMemory.get(j+1).sentence.truth);
                        // ///lets let it abduction instead

                        long diff = newEvent.getCreationTime() - stmLast.getCreationTime();

                        if (diff >= duration.get()) {
                            cur.add(0, Interval.intervalTime(diff, duration) );
                        }

                        //while (cur.size() < maxStmSize) {
                        //    cur.add( Interval.intervalMagnitude(i) );
                        //    //cur.add( Interval.intervalTime(i) );
                        //

                        //if (cur.size() > 1) {
                        //term = reverse of cur
                        Term[] terms=new Term[cur.size()];
                        for(int j=0;j<cur.size();j++) {
                            terms[cur.size()-j-1]=cur.get(j);
                        }
                        int u=0, o=0;
                        for(int j=0;j<terms.length;j++) {
                            if(terms[j] instanceof Operation  || terms[j] instanceof Interval) {
                                u++;
                            }
                        }
                        
                        if(u!=0) {
                            Term[] terms_temp=new Term[u];
                            for(int j=0;j<terms.length;j++) { //only for operator/wait chains
                                if(terms[j] instanceof Operation || terms[j] instanceof Interval) {
                                    terms_temp[o]=terms[j];
                                    o++;
                                }
                            }
                            terms=terms_temp;

                            if (terms.length > 1) {

                                Conjunction subj=(Conjunction) Conjunction.make(terms, TemporalRules.ORDER_FORWARD, memory);
                                val=TruthFunctions.abduction(val, newEvent.sentence.truth);

                                Term imp=Implication.make(subj, newEvent.sentence.content, TemporalRules.ORDER_FORWARD, memory);

                                BudgetValue bud=BudgetFunctions.forward(val, memory);

                                memory.doublePremiseTask(imp,val,bud);
                            }
                        }
                    }
                    
                    curT = nextT;
                                        
                } while (curT!=null);
            }*/
            //END SHORT TERM MEMORY
        }

        //for this heuristic, only use input events & task effects of operations
        if (actionable) { 
            shortTermMemory.add(newEvent);
            
            if(shortTermMemory.size()>maxStmSize) {
                shortTermMemory.removeFirst();
            }
        
            return true;
        }
        return false;
    }
    
    //import from older revision where temporal induction was working fine:
    public boolean isActionable(final Task newEvent, Memory mem) {
        if(!((newEvent.isInput()) || (newEvent.getCause()!=null))) {
            return false;
        }
        /*Term newcontent=newEvent.sentence.content;
        if(newcontent instanceof Operation) {
            Term pred=((Operation)newcontent).getPredicate();
            if(pred.equals(mem.getOperator("^want")) || pred.equals(mem.getOperator("^believe"))) {
                return false;
            }
        }*/
        return true;
    }
    /*
    public boolean isActionable(final Task newEvent, Memory mem) {
        if(!((newEvent.isInput()))) {
            return false;
        }
        Term newcontent=newEvent.sentence.content;
        if(newcontent instanceof Operation) {
            Term pred=((Operation)newcontent).getPredicate();
            if(pred.equals(mem.getOperator("^want")) || pred.equals(mem.getOperator("^believe"))) {
                return false;
            }
        }
        return true;
    }*/
    
}
