/*
 *
 *
 */
package nars.prolog;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Alex Benini
 *
 * Core engine
 */
@SuppressWarnings("serial")
public class EngineRunner implements java.io.Serializable, Runnable{
    
    private Prolog                              mediator;
    private TheoryManager       theoryManager;
    private PrimitiveManager    primitiveManager;
    private LibraryManager      libraryManager;
    private EngineManager             engineManager;
    
    private boolean relinkVar = false;
	private ArrayList<Term> bagOFres;
	private ArrayList<String> bagOFresString;
	private Term bagOFvarSet;
	private Term bagOfgoal;
	private Term bagOfBag;

    private int id;
    private int pid;
    private boolean detached;
    private boolean solving;
    private Term query;
    private TermQueue msgs;
    private ArrayList<Boolean> next;
    private int countNext;
    private Lock lockVar;               
    private Condition cond;
    private Object semaphore;
    
    /* Current environment */
    Engine env;
    /* Last environment used */
    private Engine last_env;
    /* Stack environments of nidicate solving */
    private LinkedList<Engine> stackEnv = new LinkedList<>();
    private SolveInfo sinfo;
    private String sinfoSetOf;
    
    /**
         * States
         */
    final State INIT;
    final State GOAL_EVALUATION;
    final State EXCEPTION;
    final State RULE_SELECTION;
    final State GOAL_SELECTION;
    final State BACKTRACK;
    final State END_FALSE;
    final State END_TRUE;
    final State END_TRUE_CP;
    final State END_HALT;
    
    public static final int HALT    = -1;
    public static final int FALSE   =  0;
    public static final int TRUE    =  1;
    public static final int TRUE_CP =  2;
    
    
    public EngineRunner(int id) {
        /* Istanzio gli stati */
        INIT            = new StateInit(this);
        GOAL_EVALUATION = new StateGoalEvaluation(this);
        EXCEPTION        = new StateException(this);
        RULE_SELECTION  = new StateRuleSelection(this);
        GOAL_SELECTION  = new StateGoalSelection(this);
        BACKTRACK       = new StateBacktrack(this);
        END_FALSE       = new StateEnd(this,FALSE);
        END_TRUE        = new StateEnd(this,TRUE);
        END_TRUE_CP     = new StateEnd(this,TRUE_CP);
        END_HALT        = new StateEnd(this,HALT);
                
                this.id = id;
    }
    
    
    /**
     * Config this Manager
     */
    void initialize(Prolog vm) {
        mediator = vm;
        theoryManager    = vm.getTheoryManager();
        primitiveManager = vm.getPrimitiveManager();
        libraryManager   = vm.getLibraryManager();
        engineManager = vm.getEngineManager();
        
        detached = false;
        solving = false;
        sinfo = null;
        msgs = new TermQueue();
        next = new ArrayList<>();
        countNext = 0;
        lockVar = new ReentrantLock();  
        cond = lockVar.newCondition();
        semaphore = new Object();
    }
    
    void spy(String action, Engine env) {        
        mediator.spy(action,env);
    }
    
    void warn(String message) {
        mediator.warn(message);
    }
    
    void exception(String message) {
        mediator.exception(message);
    }
        
    public void detach(){
        detached = true;
    }
    
    public boolean isDetached(){
        return detached;
    }
        
    /**
     *  Solves a query
     *
     * @param g the term representing the goal to be demonstrated
     * @return the result of the demonstration
     * @see SolveInfo
     **/
   private void threadSolve() {        
        sinfo = solve();
        solving = false;    
        
        lockVar.lock();
                try{
                        cond.signalAll();
                }
                finally{
                        lockVar.unlock();
                }
            
        if (sinfo.hasOpenAlternatives()) {
            if(next.isEmpty() || !next.get(countNext)){
                synchronized(semaphore){        
                try {
                        semaphore.wait();       //Mi metto in attesa di eventuali altre richieste
                        } catch (InterruptedException e) {
                                e.printStackTrace();
                        }  
                }
            }         
        }
    }
    
    public SolveInfo solve() {
        return solve(0);
    }
    public SolveInfo solve(double maxTimeSeconds) {
        try {
            query.resolveTerm();
            
            libraryManager.onSolveBegin(query);
            primitiveManager.identifyPredicate(query);
//            theoryManager.transBegin();
            
            freeze();
            env = new Engine(this, query);
            StateEnd result = env.run(maxTimeSeconds);
            defreeze();
            
            sinfo = new SolveInfo(
                    query,
                    result.getResultGoal(),
                    result.getResultDemo(),
                    result.getResultVars()
            );
            if(this.sinfoSetOf!=null)
            	sinfo.setSetOfSolution(sinfoSetOf);
            if (!sinfo.hasOpenAlternatives()) 
                solveEnd();
           return sinfo;
        } catch (Exception ex) {
            ex.printStackTrace();
            return new SolveInfo(query);
        }
    }
    
    /**
     * Gets next solution
     *
     * @return the result of the demonstration
     * @throws NoMoreSolutionException if no more solutions are present
     * @see SolveInfo
     **/
    private void threadSolveNext() throws NoMoreSolutionException {
    	solving = true;
        next.set(countNext, false);
        countNext++;
   
                sinfo = solveNext();
                
                solving = false;

                lockVar.lock();
                try{
                        cond.signalAll();
                }
                finally{
                        lockVar.unlock();
                }
        
        if (sinfo.hasOpenAlternatives()){
        	if(countNext>(next.size()-1) || !next.get(countNext)){
                try{
	                synchronized(semaphore){
	                        semaphore.wait();       //Mi metto in attesa di eventuali altre richieste
	                }
                }
            catch(InterruptedException e) {}
        	}
        }
    }
    
    public SolveInfo solveNext() throws NoMoreSolutionException {
        return solveNext(0);
    }
    
    public SolveInfo solveNext(double maxTimeSec) throws NoMoreSolutionException {
        if (hasOpenAlternatives()) {
            refreeze();
            env.nextState = BACKTRACK;
            StateEnd result = env.run(maxTimeSec);
            defreeze();
            sinfo = new SolveInfo(
                    env.query,
                    result.getResultGoal(),
                    result.getResultDemo(),
                    result.getResultVars()
            );
            if(this.sinfoSetOf!=null)
            	sinfo.setSetOfSolution(sinfoSetOf);
            
            if (!sinfo.hasOpenAlternatives()){
                solveEnd();             
            }
            return sinfo;

        } else
            throw new NoMoreSolutionException();       
    }
   
    
    /**
     * Halts current solve computation
     */
    public void solveHalt() {
        env.mustStop();
        libraryManager.onSolveHalt();
    }
    
    /**
     * Accepts current solution
     */
    public void solveEnd() {
//        theoryManager.transEnd(sinfo.isSuccess());
//        theoryManager.optimize();
        libraryManager.onSolveEnd();
    }
    
    
    private void freeze() {
        if(env==null) return;
        try {
            if (stackEnv.getLast()==env) return;
        } catch(NoSuchElementException e) {}
        stackEnv.addLast(env);
    }
    
    private void refreeze() {
        freeze();
        env = last_env;            
    }
    
    private void defreeze() {
        last_env = env;
        if (stackEnv.isEmpty()) return;
        env = (Engine)(stackEnv.removeLast());
    }
    
    
    /*
     * Utility functions for Finite State Machine
     */
    
    List<ClauseInfo> find(Term t) {
        return theoryManager.find(t);
    }
    
    void identify(Term t) {
        primitiveManager.identifyPredicate(t);
    }
    
//    void saveLastTheoryStatus() {
//        theoryManager.transFreeze();
//    }
    
    void pushSubGoal(SubGoalTree goals) {
        env.currentContext.goalsToEval.pushSubGoal(goals);
    }
    
    
    void cut() {
        env.choicePointSelector.cut(env.currentContext.choicePointAfterCut);
    }
    
    
    ExecutionContext getCurrentContext() {
        return (env==null)? null : env.currentContext;
    }
    
    
    /**
     * Asks for the presence of open alternatives to be explored
     * in current demostration process.
     *
     * @return true if open alternatives are present
     */
    boolean hasOpenAlternatives() {
        if (sinfo==null) return false;
        return sinfo.hasOpenAlternatives();
    }
    
    
    /**
     * Checks if the demonstration process was stopped by an halt command.
     * 
     * @return true if the demonstration was stopped
     */
    boolean isHalted() {
        if (sinfo==null) return false;
        return sinfo.isHalted();
    }


        @Override
        public void run() {
                solving = true;
                pid = (int) Thread.currentThread().getId();
                
                if (sinfo == null) {
                        threadSolve();
                }
                try {
                        while(hasOpenAlternatives())
                                if(next.get(countNext))
                                        threadSolveNext();
                } catch (NoMoreSolutionException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                }
        }    
        
        public int getId(){
                return id;
        }
        
        public int getPid(){
                return pid;
        }
        
        public SolveInfo getSolution(){
                return sinfo;
        }
        
        public void setGoal(Term goal){
                this.query = goal;
        }

        public boolean nextSolution() {
                solving = true;
                next.add(true);
                
                synchronized(semaphore){        
                        semaphore.notify();                     
                }
                return true;
        }
        
        public SolveInfo read(){
                lockVar.lock();
                try{
                        while(solving || sinfo==null)   
                        try {
                                cond.await();
                        } catch (InterruptedException e) {
                                e.printStackTrace();
                        }
                }
                finally{
                        lockVar.unlock();
                }
                
                return sinfo;
        }
        
        public void setSolving(boolean solved){
                solving = solved;
        }
        
        
        public void sendMsg(Term t){                    
                msgs.store(t);
        }
        
        
        public boolean getMsg(Term t){
                msgs.get(t, mediator, this);
                return true;
        }
        
        
        public boolean peekMsg (Term t){
                return msgs.peek(t, mediator);
        }
        
        
        public boolean removeMsg(Term t){
                return msgs.remove(t, mediator);
        }

        
        public boolean waitMsg(Term msg) {
                msgs.wait(msg, mediator, this);
                return true;
        }
        
        
        public int msgQSize(){
                return msgs.size();
        }
        
        TheoryManager getTheoryManager() {
            return theoryManager;
         }
        
        public boolean getRelinkVar(){
    		return this.relinkVar;
    	}
        public void setRelinkVar(boolean b){
    		this.relinkVar=b;
    	}
        
        public ArrayList<Term> getBagOFres(){
    		return this.bagOFres;
    	}
        public void setBagOFres(ArrayList<Term> l){
    		this.bagOFres=l;
    	}
        public ArrayList<String> getBagOFresString(){
    		return this.bagOFresString;
    	}
        public void setBagOFresString(ArrayList<String> l){
    		this.bagOFresString=l;
    	}
        public Term getBagOFvarSet(){
    		return this.bagOFvarSet;
    	}
        public void setBagOFvarSet(Term l){
    		this.bagOFvarSet=l;
    	}
        public Term getBagOFgoal(){
    		return this.bagOfgoal;
    	}
        public void setBagOFgoal(Term l){
    		this.bagOfgoal=l;
    	}
        public Term getBagOFBag(){
    		return this.bagOfBag;
    	}
        public void setBagOFBag(Term l){
    		this.bagOfBag=l;
    	}
        public EngineManager getEngineMan(){
    		return this.engineManager;
    	}
        public String getSetOfSolution() {
        	if(sinfo!=null)
        		return sinfo.getSetOfSolution();
        	else return null;
        }
        public void setSetOfSolution(String s) {
        	if(sinfo!=null)
        		sinfo.setSetOfSolution(s);
        	this.sinfoSetOf=s;
        }
        public void clearSinfoSetOf() {
        	this.sinfoSetOf=null;
        }
}