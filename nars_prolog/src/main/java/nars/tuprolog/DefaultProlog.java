package nars.tuprolog;

//import java.io.File;
//import java.io.IOException;

import com.gs.collections.api.map.primitive.MutableIntIntMap;
import com.gs.collections.api.map.primitive.MutableIntObjectMap;
import com.gs.collections.impl.map.mutable.primitive.IntIntHashMap;
import com.gs.collections.impl.map.mutable.primitive.IntObjectHashMap;
import nars.nal.term.Term;

import nars.tuprolog.event.QueryEvent;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings("serial")
/** Prolog core with multithreaded concurrency */
public class DefaultProlog extends Prolog  {

    protected MutableIntObjectMap<Engine> runners;    //key: id; obj: runner
    protected MutableIntIntMap threads;    //key: pid; obj: id
    protected Engine er1;
    protected int id = 0;
    protected int rootID = 0;

    private Map<String, TermQueue> queues = new HashMap();
    private Map<String, ReentrantLock> locks = new HashMap();
    private MutableClauses dynamicTheory, staticTheory;

    public DefaultProlog() throws InvalidLibraryException {
        this("nars.tuprolog.lib.BasicLibrary","nars.tuprolog.lib.ISOLibrary", "nars.tuprolog.lib.IOLibrary", "nars.tuprolog.lib.JavaLibrary");
    }

    protected DefaultProlog(String... libs) throws InvalidLibraryException {
        super(libs);

        setSpy(false);
        setWarning(true);
        runners = new IntObjectHashMap().asSynchronized();
        threads = new IntIntHashMap().asSynchronized();

        er1 = new Engine(rootID, this);

    }

    @Override
    public Clauses getDynamicTheory() {
        if (dynamicTheory==null)
            dynamicTheory = new MutableClauses();
        return dynamicTheory;
    }
    @Override
    public Clauses getStaticTheory() {
        if (staticTheory==null)
            staticTheory = new MutableClauses();
        return staticTheory;
    }


    public synchronized boolean threadCreate(PTerm threadID, Term goal) {
        id += 1;

        if (goal == null) return false;
        if (goal instanceof Var)
            goal = goal.getTerm();

        Engine er = new Engine(id, this);

        if (!unify(threadID, new Int(id))) return false;

        er.setGoal(goal);
        addRunner(er, id);
        Thread t = new Thread(er, threadID.toString() + goal.toString());
        addThread(t.getId(), id);

        t.start();
        return true;
    }

    public SolveInfo join(int id) {
        Engine er = findRunner(id);
        if (er == null || er.isDetached()) return null;
        /*toSPY
		 * System.out.println("Thread id "+runnerId()+" - prelevo la soluzione (join)");*/
        SolveInfo solution = er.read();
		/*toSPY
		 * System.out.println("Soluzione: "+solution);*/
        removeRunner(id);
        return solution;
    }

    public SolveInfo read(int id) {
        Engine er = findRunner(id);
        if (er == null || er.isDetached()) return null;
		/*toSPY
		 * System.out.println("Thread id "+runnerId()+" - prelevo la soluzione (read) del thread di id: "+er.getId());
		 */
        SolveInfo solution = er.read();
		/*toSPY
		 * System.out.println("Soluzione: "+solution);
		 */
        return solution;
    }

    public boolean hasNext(int id) {
        Engine er = findRunner(id);
        if (er == null || er.isDetached()) return false;
        return er.hasOpenAlternatives();
    }

    public boolean nextSolution(int id) {
        Engine er = findRunner(id);
        if (er == null || er.isDetached()) return false;
		/*toSPY
		 * System.out.println("Thread id "+runnerId()+" - next_solution: risveglio il thread di id: "+er.getId());
		 */
        boolean bool = er.nextSolution();
        return bool;
    }

    public void detach(int id) {
        Engine er = findRunner(id);
        if (er == null) return;
        er.detach();
    }

    public boolean sendMsg(int dest, Term msg) {
        Engine er = findRunner(dest);
        if (er == null) return false;
        Term msgcopy;
        if (msg instanceof PTerm)
            msgcopy = ((PTerm)msg).copy(new LinkedHashMap<>(), 0);
        else
            msgcopy = msg;
        er.sendMsg(msgcopy);
        return true;
    }

    public boolean sendMsg(String name, Term msg) {
        TermQueue queue = queues.get(name);
        if (queue == null) return false;
        Term msgcopy;

        if (msg instanceof PTerm)
            msgcopy = ((PTerm)msg).copy(new LinkedHashMap<>(), 0);
        else
            msgcopy = msg; //no copy

        queue.store(msgcopy);
        return true;
    }

    public boolean getMsg(int id, PTerm msg) {
        Engine er = findRunner(id);
        if (er == null) return false;
        return er.getMsg(msg);
    }

    public boolean getMsg(String name, PTerm msg) {
        Engine er = findRunner();
        if (er == null) return false;
        TermQueue queue = queues.get(name);
        if (queue == null) return false;
        return queue.get(msg, this, er);
    }

    public boolean waitMsg(int id, PTerm msg) {
        Engine er = findRunner(id);
        if (er == null) return false;
        return er.waitMsg(msg);
    }

    public boolean waitMsg(String name, PTerm msg) {
        Engine er = findRunner();
        if (er == null) return false;
        TermQueue queue = queues.get(name);
        if (queue == null) return false;
        return queue.wait(msg, this, er);
    }

    public boolean peekMsg(int id, PTerm msg) {
        Engine er = findRunner(id);
        if (er == null) return false;
        return er.peekMsg(msg);
    }

    public boolean peekMsg(String name, PTerm msg) {
        TermQueue queue = queues.get(name);
        if (queue == null) return false;
        return queue.peek(msg, this);
    }

    public boolean removeMsg(int id, PTerm msg) {
        Engine er = findRunner(id);
        if (er == null) return false;
        return er.removeMsg(msg);
    }

    public boolean removeMsg(String name, PTerm msg) {
        TermQueue queue = queues.get(name);
        if (queue == null) return false;
        return queue.remove(msg, this);
    }

    private void removeRunner(int id) {
        Engine er = runners.get(id);

        if (er == null) return;
        runners.remove(id);

        int pid = er.getPid();

        threads.remove(pid);

    }

    protected void addRunner(Engine er, int id) {
        runners.put(id, er);
    }

    protected void addThread(long pid, int id) {
        threads.put((int) pid, id);
    }

    public void cut() {
        findRunner().cut();
    }

    @Override public ExecutionContext getCurrentContext() {
        Engine runner = findRunner();
        return runner.getCurrentContext();
    }

    public boolean hasOpenAlternatives() {
        Engine runner = findRunner();
        return runner.hasOpenAlternatives();
    }

    public boolean isHalted() {
        Engine runner = findRunner();
        return runner.isHalted();
    }

    public void pushSubGoal(SubGoalTree goals) {
        Engine runner = findRunner();
        runner.pushSubGoal(goals);

    }


    /**
     * Solves a query
     *
     * @param g the term representing the goal to be demonstrated
     * @return the result of the demonstration
     * @see SolveInfo
     **/
    public SolveInfo solve(PTerm query, double maxTimeSeconds) {
        //System.out.println("ENGINE SOLVE #0: "+g);
        if (query == null) return null;

        this.clearSinfoSetOf();
        er1.setGoal(query);

        SolveInfo sinfo = er1.solve(maxTimeSeconds);
        //System.out.println("ENGINE MAN solve(Term) risultato: "+s);

        //return er1.solve();

        notifyNewQueryResultAvailable(new QueryEvent(this, sinfo));

        return sinfo;

    }

    public SolveInfo solve(PTerm g) {
        return solve(g, 0);
    }

    @Override
    public void solveEnd() {
        er1.solveEnd();
        if (!runners.isEmpty()) {
            for (Engine e : runners.values()) {
                e.solveEnd();
            }
            queues.clear();
            locks.clear();
            id = 0;
        }
    }

    @Override
    public void solveHalt() {
        er1.solveHalt();
        if (!runners.isEmpty()) {
            for (Engine e : runners.values()) {
                e.solveHalt();
            }
        }
    }



    /**
     * Gets next solution
     *
     * @return the result of the demonstration
     * @throws NoMoreSolutionException if no more solutions are present
     * @see SolveInfo
     **/
    public SolveInfo solveNext(double maxTimeSec) throws NoMoreSolutionException {
        if (hasOpenAlternatives()) {
            SolveInfo sinfo = er1.solveNext(maxTimeSec);
            QueryEvent ev = new QueryEvent(this, sinfo);
            notifyNewQueryResultAvailable(ev);
            return sinfo;
        } else
            throw new NoMoreSolutionException();
    }



    /**
     * @return L'EngineRunner associato al thread di id specificato.
     */

    private Engine findRunner(int id) {
        if (!runners.containsKey(id)) return null;
        return runners.get(id);
    }

    private Engine findRunner() {
        int pid = (int) Thread.currentThread().getId();
        int id = threads.getIfAbsent(pid, -1);
        if (id == -1)
            return er1;

        return runners.get(id);
    }

    //Ritorna l'identificativo del thread corrente
    public int runnerId() {
        Engine er = findRunner();
        return er.getId();
    }

    public boolean createQueue(String name) {
        synchronized (queues) {
            if (queues.containsKey(name)) return true;
            TermQueue newQ = new TermQueue();
            queues.put(name, newQ);
        }
        return true;
    }

    public void destroyQueue(String name) {
        synchronized (queues) {
            queues.remove(name);
        }
    }

    public int queueSize(int id) {
        Engine er = findRunner(id);
        return er.msgQSize();
    }

    public int queueSize(String name) {
        TermQueue q = queues.get(name);
        if (q == null) return -1;
        return q.size();
    }

    public boolean createLock(String name) {
        synchronized (locks) {
            if (locks.containsKey(name)) return true;
            ReentrantLock mutex = new ReentrantLock();
            locks.put(name, mutex);
        }
        return true;
    }

    public void destroyLock(String name) {
        synchronized (locks) {
            locks.remove(name);
        }
    }

    public boolean mutexLock(String name) {
        while (true) {
            ReentrantLock mutex = locks.get(name);
            if (mutex == null) {
                createLock(name);
                continue;
            }
            mutex.lock();
        /*toSPY
		 * System.out.println("Thread id "+runnerId()+ " - mi sono impossessato del lock");
		 */
            return true;
        }
    }


    public boolean mutexTryLock(String name) {
        ReentrantLock mutex = locks.get(name);
        if (mutex == null) return false;
		/*toSPY
		 * System.out.println("Thread id "+runnerId()+ " - provo ad impossessarmi del lock");
		 */
        return mutex.tryLock();
    }

    public boolean mutexUnlock(String name) {
        ReentrantLock mutex = locks.get(name);
        if (mutex == null) return false;
        try {
            mutex.unlock();
			/*toSPY
			 * System.out.println("Thread id "+runnerId()+ " - Ho liberato il lock");
			 */
            return true;
        } catch (IllegalMonitorStateException e) {
            return false;
        }
    }

    public boolean isLocked(String name) {
        ReentrantLock mutex = locks.get(name);
        if (mutex == null) return false;
        return mutex.isLocked();
    }

    public void unlockAll() {
        synchronized (locks) {
            Set<String> mutexList = locks.keySet();
            Iterator<String> it = mutexList.iterator();

            while (it.hasNext()) {
                ReentrantLock mutex = locks.get(it.next());
                boolean unlocked = false;
                while (!unlocked) {
                    try {
                        mutex.unlock();
                    } catch (IllegalMonitorStateException e) {
                        unlocked = true;
                    }
                }
            }
        }
    }

    @Override public Engine.State getEnv() {
        Engine er = findRunner();
        return er.getEnv();
    }

    @Override public void identify(Term t) {
        Engine er = findRunner();
        er.identify(t);
    }

    public boolean getRelinkVar() {
        Engine r = this.findRunner();
        return r.getRelinkVar();
    }

    public void setRelinkVar(boolean b) {
        Engine r = this.findRunner();
        r.setRelinkVar(b);
    }

    public ArrayList<Term> getBagOFres() {
        Engine r = this.findRunner();
        return r.getBagOFres();
    }

    public void setBagOFres(ArrayList<Term> l) {
        Engine r = this.findRunner();
        r.setBagOFres(l);
    }

    public ArrayList<String> getBagOFresString() {
        Engine r = this.findRunner();
        return r.getBagOFresString();
    }

    public void setBagOFresString(ArrayList<String> l) {
        Engine r = this.findRunner();
        r.setBagOFresString(l);
    }

    public PTerm getBagOFvarSet() {
        Engine r = this.findRunner();
        return r.getBagOFvarSet();
    }

    public void setBagOFvarSet(PTerm l) {
        Engine r = this.findRunner();
        r.setBagOFvarSet(l);
    }

    public PTerm getBagOFgoal() {
        Engine r = this.findRunner();
        return r.getBagOFgoal();
    }

    public void setBagOFgoal(PTerm l) {
        Engine r = this.findRunner();
        r.setBagOFgoal(l);
    }

    public PTerm getBagOFbag() {
        Engine r = this.findRunner();
        return r.getBagOFBag();
    }

    public void setBagOFbag(PTerm l) {
        Engine r = this.findRunner();
        r.setBagOFBag(l);
    }

    public String getSetOfSolution() {
        Engine r = this.findRunner();
        return r.getSetOfSolution();
    }

    public void setSetOfSolution(String s) {
        Engine r = this.findRunner();
        r.setSetOfSolution(s);
    }

    public void clearSinfoSetOf() {
        Engine r = this.findRunner();
        r.clearSinfoSetOf();
    }


}

