/*
 *
 *
 */
package nars.tuprolog;


import nars.nal.term.Term;

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Alex Benini
 *         <p>
 *         Core engine
 */
@SuppressWarnings("serial")
public class Engine implements java.io.Serializable, Runnable {


    private Theories theories;
    private Primitives primitives;
    private Libraries libraries;
    private Prolog engineManager;



    private boolean relinkVar = false;
    private ArrayList<Term> bagOFres;
    private ArrayList<String> bagOFresString;
    private PTerm bagOFvarSet;
    private PTerm bagOfgoal;
    private PTerm bagOfBag;

    private int id;
    private int pid;
    private boolean detached;
    private boolean solving;
    private Term query;
    private final TermQueue msgs;
    private final ArrayList<Boolean> next;
    private int countNext;
    private Lock lockVar;
    private Condition cond;
    private final Object semaphore;

    /* Current environment */
    private State env;
    /* Last environment used */
    private State last_env;
    /* Stack environments of nidicate solving */
    private final List<State> stackEnv = new ArrayList();
    private SolveInfo sinfo;
    private String sinfoSetOf;

    /**
     * States
     */
    final Stage INIT;
    final Stage GOAL_EVALUATION;
    final Stage EXCEPTION;
    final Stage RULE_SELECTION;
    final Stage GOAL_SELECTION;
    final Stage BACKTRACK;
    final Stage END_FALSE;
    final Stage END_TRUE;
    final Stage END_TRUE_CP;
    final Stage END_HALT;

    public static final int HALT = -1;
    public static final int FALSE = 0;
    public static final int TRUE = 1;
    public static final int TRUE_CP = 2;

    public State getEnv() {
        return env;
    }

    /**
     * @author Alex Benini
     */
    public static class State {

        //PrintStream log;
        Stage nextState;
        Term query;
        Struct startGoal;
        Collection<Var> goalVars;
        int nDemoSteps;
        ExecutionContext currentContext;
        //ClauseStore clauseSelector;
        ChoicePointContext currentAlternative;
        ChoicePointStore choicePointSelector;
        boolean mustStop;
        Engine manager;

        long cyclesToCheckForTimeout = 512;

        public State(Engine manager, Term query) {
            this.manager = manager;
            this.nextState = manager.INIT;
            this.query = query;
            this.mustStop = false;
            this.manager.getTheories().clearRetractDB();
        }

        public String toString() {
            try {
                return "ExecutionStack: \n" + currentContext + '\n'
                        + "ChoicePointStore: \n" + choicePointSelector + "\n\n";
            } catch (Exception ex) {
                return "";
            }
        }

        void mustStop() {
            mustStop = true;
        }

        /**
         * Core of engine. Finite State Machine
         */
        StageEnd run(double maxTimeSeconds) {
            String action;

            long timeoutNS = maxTimeSeconds > 0 ? (long) (maxTimeSeconds * 1e9) : 0;

            long start = System.nanoTime();

            /**
             * only check every N iterations because System.nanotime could be
             * expensive in an inner-loop like this
             */
            long cycle = 1; //skip first

            do {

                if ((timeoutNS > 0) && (cycle % cyclesToCheckForTimeout == 0)) {
                    long now = System.nanoTime();
                    if (now - start > timeoutNS) {
                        mustStop = true;
                    }
                }

                if (mustStop) {
                    nextState = manager.END_FALSE;
                    break;
                }
                action = nextState.toString();

                nextState.run(this);
                manager.spy(action, this);

                cycle++;

            } while (!(nextState instanceof StageEnd));

            nextState.run(this);

            return (StageEnd) (nextState);
        }

        StageEnd run() {
            return run(0);
        }

        /*
         * Methods for spyListeners
         */
        public Term getQuery() {
            return query;
        }

        public int getNumDemoSteps() {
            return nDemoSteps;
        }

        public List<ExecutionContext> getExecutionStack() {
            ArrayList<ExecutionContext> l = new ArrayList<>();
            ExecutionContext t = currentContext;
            while (t != null) {
                l.add(t);
                t = t.fatherCtx;
            }
            return l;
        }

        public ChoicePointStore getChoicePointStore() {
            return choicePointSelector;
        }

        final AbstractMap<Var, Var> goalMap = new LinkedHashMap<>();

        void prepareGoal() {
            goalMap.clear();
            startGoal = (Struct) ((PTerm)query).copyGoal(goalMap, 0);
            this.goalVars = goalMap.values();
        }

        //    void cut() {
        //        choicePointSelector.cut(currentContext.depth -1);
        //    }
        void initialize(ExecutionContext eCtx) {
            currentContext = eCtx;
            choicePointSelector = new ChoicePointStore();
            nDemoSteps = 1;
            currentAlternative = null;
        }

        public String getNextStateName() {
            return nextState.stateName;
        }

    }


    public Engine(int id, Prolog vm) {
        /* Istanzio gli stati */
        INIT = new StageInit(this);
        GOAL_EVALUATION = new StageGoalEvaluation(this);
        EXCEPTION = new StageException(this);
        RULE_SELECTION = new StageRuleSelection(this);
        GOAL_SELECTION = new StageGoalSelection(this);
        BACKTRACK = new StageBacktrack(this);
        END_FALSE = new StageEnd(this, FALSE);
        END_TRUE = new StageEnd(this, TRUE);
        END_TRUE_CP = new StageEnd(this, TRUE_CP);
        END_HALT = new StageEnd(this, HALT);

        this.id = id;

        theories = vm.getTheories();
        primitives = vm.getPrimitives();
        libraries = vm.getLibraries();
        engineManager = vm;

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



    void spy(String action, State env) {
        engineManager.spy(action, env);
    }

    void warn(String message) {
        engineManager.warn(message);
    }

    public boolean isWarning() {
        return engineManager.isWarning();
    }

    void exception(String message) {
        engineManager.exception(message);
    }

    public void detach() {
        detached = true;
    }

    public boolean isDetached() {
        return detached;
    }

    /**
     * Solves a query
     *
     * @param g the term representing the goal to be demonstrated
     * @return the result of the demonstration
     * @see SolveInfo
     **/
    private void threadSolve() {
        sinfo = solve();
        solving = false;

        lockVar.lock();
        try {
            cond.signalAll();
        } finally {
            lockVar.unlock();
        }

        if (sinfo.hasOpenAlternatives()) {
            if (next.isEmpty() || !next.get(countNext)) {
                synchronized (semaphore) {
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
            if (query instanceof PTerm)
                ((PTerm)query).resolveTerm();

            libraries.onSolveBegin(query);
            primitives.identifyPredicate(query);
//            theoryManager.transBegin();

            freeze();
            env = new State(this, query);
            StageEnd result = env.run(maxTimeSeconds);
            defreeze();

            sinfo = new SolveInfo(
                    query,
                    result.getResultGoal(),
                    result.getResultDemo(),
                    result.getResultVars()
            );
            if (this.sinfoSetOf != null)
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
        try {
            cond.signalAll();
        } finally {
            lockVar.unlock();
        }

        if (sinfo.hasOpenAlternatives()) {
            if (countNext > (next.size() - 1) || !next.get(countNext)) {
                try {
                    synchronized (semaphore) {
                        semaphore.wait();       //Mi metto in attesa di eventuali altre richieste
                    }
                } catch (InterruptedException e) {
                }
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
            StageEnd result = env.run(maxTimeSec);
            defreeze();
            sinfo = new SolveInfo(
                    env.query,
                    result.getResultGoal(),
                    result.getResultDemo(),
                    result.getResultVars()
            );
            if (this.sinfoSetOf != null)
                sinfo.setSetOfSolution(sinfoSetOf);

            if (!sinfo.hasOpenAlternatives()) {
                solveEnd();
            }
            return sinfo;

        } else {
            solveEnd();
            throw new NoMoreSolutionException();
        }
    }


    /**
     * Halts current solve computation
     */
    public void solveHalt() {
        env.mustStop();
        libraries.onSolveHalt();
    }

    /**
     * Accepts current solution
     */
    public void solveEnd() {

//        theoryManager.transEnd(sinfo.isSuccess());
//        theoryManager.optimize();
        libraries.onSolveEnd();
    }


    private void freeze() {
        if (env == null) return;
        int s = stackEnv.size();
        if (s == 0) return;
        if (stackEnv.get(s - 1) == env) return;
        stackEnv.add(env);
    }

    private void refreeze() {
        freeze();
        env = last_env;
    }

    private void defreeze() {
        last_env = env;
        if (stackEnv.isEmpty()) return;
        env = stackEnv.remove(stackEnv.size() - 1);
    }
    
    
    /*
     * Utility functions for Finite State Machine
     */

    public Iterator<Clause> find(PTerm t) {
        return theories.find(t);
    }

    void identify(Term t) {
        primitives.identifyPredicate(t);
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
        return (env == null) ? null : env.currentContext;
    }


    /**
     * Asks for the presence of open alternatives to be explored
     * in current demostration process.
     *
     * @return true if open alternatives are present
     */
    public boolean hasOpenAlternatives() {
        if (sinfo == null) return false;
        return sinfo.hasOpenAlternatives();
    }


    /**
     * Checks if the demonstration process was stopped by an halt command.
     *
     * @return true if the demonstration was stopped
     */
    public boolean isHalted() {
        if (sinfo == null) return false;
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
            while (hasOpenAlternatives())
                if (next.get(countNext))
                    threadSolveNext();
        } catch (NoMoreSolutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }

    public int getPid() {
        return pid;
    }

    public SolveInfo getSolution() {
        return sinfo;
    }

    public void setGoal(Term goal) {
        this.query = goal;
    }

    public boolean nextSolution() {
        solving = true;
        next.add(true);

        synchronized (semaphore) {
            semaphore.notify();
        }
        return true;
    }

    public SolveInfo read() {
        lockVar.lock();
        try {
            while (solving || sinfo == null)
                try {
                    cond.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        } finally {
            lockVar.unlock();
        }

        return sinfo;
    }

    public void setSolving(boolean solved) {
        solving = solved;
    }


    public void sendMsg(Term t) {
        msgs.store(t);
    }


    public boolean getMsg(PTerm t) {
        msgs.get(t, engineManager, this);
        return true;
    }


    public boolean peekMsg(PTerm t) {
        return msgs.peek(t, engineManager);
    }


    public boolean removeMsg(PTerm t) {
        return msgs.remove(t, engineManager);
    }


    public boolean waitMsg(PTerm msg) {
        msgs.wait(msg, engineManager, this);
        return true;
    }


    public int msgQSize() {
        return msgs.size();
    }

    Theories getTheories() {
        return theories;
    }

    public boolean getRelinkVar() {
        return this.relinkVar;
    }

    public void setRelinkVar(boolean b) {
        this.relinkVar = b;
    }

    public ArrayList<Term> getBagOFres() {
        return this.bagOFres;
    }

    public void setBagOFres(ArrayList<Term> l) {
        this.bagOFres = l;
    }

    public ArrayList<String> getBagOFresString() {
        return this.bagOFresString;
    }

    public void setBagOFresString(ArrayList<String> l) {
        this.bagOFresString = l;
    }

    public PTerm getBagOFvarSet() {
        return this.bagOFvarSet;
    }

    public void setBagOFvarSet(PTerm l) {
        this.bagOFvarSet = l;
    }

    public PTerm getBagOFgoal() {
        return this.bagOfgoal;
    }

    public void setBagOFgoal(PTerm l) {
        this.bagOfgoal = l;
    }

    public PTerm getBagOFBag() {
        return this.bagOfBag;
    }

    public void setBagOFBag(PTerm l) {
        this.bagOfBag = l;
    }

    public Prolog getEngineMan() {
        return this.engineManager;
    }

    public String getSetOfSolution() {
        if (sinfo != null)
            return sinfo.getSetOfSolution();
        else return null;
    }

    public void setSetOfSolution(String s) {
        if (sinfo != null)
            sinfo.setSetOfSolution(s);
        this.sinfoSetOf = s;
    }

    public void clearSinfoSetOf() {
        this.sinfoSetOf = null;
    }
}