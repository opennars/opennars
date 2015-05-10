/**
 * @author Eleonora Cau
 *
 */

package nars.tuprolog.lib;

import nars.nal.term.Term;
import nars.tuprolog.*;


@SuppressWarnings("serial")
public class ThreadLibrary extends Library<DefaultProlog> {

	protected DefaultProlog engineManager;
	
	public void setEngine(DefaultProlog en) {
        engine = en;
        engineManager = en;
	}
	
	//Tenta di unificare a t l'identificativo del thread corrente
	public boolean thread_id_1 (PTerm t) throws PrologError{
        int id = engineManager.runnerId();
        unify(t,new Int(id));
		return true;
	}
	
	//Crea un nuovo thread di identificatore id che comincia ad eseguire il goal dato
	public boolean thread_create_2 (PTerm id, Term goal){
		return engineManager.threadCreate(id, goal);
	}
	
	/*Aspetta la terminazione del thread di identificatore id e ne raccoglie il risultato, 
	unificando il goal risolto a result. Il thread viene eliminato dal sistema*/
	public boolean thread_join_2(Term id, PTerm result) throws PrologError{
		id = id.getTerm();
		if (!(id instanceof Int)) 
			throw PrologError.type_error(engine, 1, "integer", id);
		SolveInfo res = engineManager.join(((Int)id).intValue());
		if (res == null) return false;
		Term status;
		try {
			status = res.getSolution();
		} catch (NoSolutionException e) {
			//status = new Struct("FALSE");		
			return false;
		}
		try{
			unify (result, status);
		} catch (InvalidTermException e) {
			throw PrologError.syntax_error(engine,-1, e.line, e.pos, result);
		}
		return true;
	}
		
	public boolean thread_read_2(Term id, PTerm result) throws PrologError{
		id=id.getTerm();
		if (!(id instanceof Int)) 
			throw PrologError.type_error(engine, 1,
                    "integer", id);
		SolveInfo res=engineManager.read( ((Int)id).intValue());
		if (res==null) return false;
		Term status;
		try {
			status = res.getSolution();
		} catch (NoSolutionException e) {
			//status = new Struct("FALSE");
			return false;
		}
		try{
			unify (result, status);
		} catch (InvalidTermException e) {
			throw PrologError.syntax_error(engine,-1, e.line, e.pos, result);
		}
		return true;
	}
	
	public boolean thread_has_next_1(Term id) throws PrologError{
		id=id.getTerm();
		if (!(id instanceof Int)) 
			throw PrologError.type_error(engine, 1,"integer", id);
		return engineManager.hasNext(((Int)id).intValue());
	}
	
	
	public boolean thread_next_sol_1(Term id) throws PrologError{
		id=id.getTerm();
		if (!(id instanceof Int)) 
			throw PrologError.type_error(engine, 1,
                    "integer", id);
		return engineManager.nextSolution(((Int)id).intValue());
	}
	
	public boolean thread_detach_1 (Term id) throws PrologError{
		id=id.getTerm();
		if (!(id instanceof Int)) 
			throw PrologError.type_error(engine, 1,
                    "integer", id);
		engineManager.detach(((Int)id).intValue());
		return true;
	}
	
	public boolean thread_sleep_1(Term millisecs) throws PrologError{
		millisecs=millisecs.getTerm();
		if (!(millisecs instanceof Int)) 
			throw PrologError.type_error(engine, 1,
                    "integer", millisecs);
		long time=((Int)millisecs).intValue();
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			System.out.println("ERRORE SLEEP");
			return false;
		}
		return true;
	}
	
	public boolean thread_send_msg_2(Term id, Term msg) throws PrologError{
		id=id.getTerm();
		if (id instanceof Int) 
			return engineManager.sendMsg(((Int)id).intValue(), msg);	
		if (!id.isAtomic() || !id.isAtom())
			throw PrologError.type_error(engine, 1,
                    "atom, atomic or integer", id);
		return engineManager.sendMsg(id.toString(), msg);
	}
	
	public  boolean  thread_get_msg_2(Term id, PTerm msg) throws PrologError{
		id=id.getTerm();
		if (id instanceof Int) 
			return engineManager.getMsg(((Int)id).intValue(), msg);
		if (!id.isAtom() || !id.isAtomic())
			throw PrologError.type_error(engine, 1,
                    "atom, atomic or integer", id);
		return engineManager.getMsg(id.toString(), msg);
	}	
	
	public  boolean  thread_peek_msg_2(Term id, PTerm msg) throws PrologError{
		id=id.getTerm();
		if (id instanceof Int) 
			return engineManager.peekMsg(((Int)id).intValue(), msg);
		if (!id.isAtom() || !id.isAtomic())
			throw PrologError.type_error(engine, 1,
                    "atom, atomic or integer", id);
		return engineManager.peekMsg(id.toString(), msg);
	}

	public  boolean  thread_wait_msg_2(Term id, PTerm msg) throws PrologError{
		id=id.getTerm();
		if (id instanceof Int) 
			return engineManager.waitMsg(((Int)id).intValue(), msg);
		if (!id.isAtom() || !id.isAtomic()) 
			throw PrologError.type_error(engine, 1,
                    "atom, atomic or integer", id);
		return engineManager.waitMsg(id.toString(), msg);
	}

	public  boolean  thread_remove_msg_2(Term id, PTerm msg) throws PrologError{
		id=id.getTerm();
		if (id instanceof Int) 
			return engineManager.removeMsg(((Int)id).intValue(), msg);
		if (!id.isAtom() || !id.isAtomic()) 
			throw PrologError.type_error(engine, 1,
                    "atom, atomic or integer", id);
		return engineManager.removeMsg(id.toString(), msg);
	}
	
	public boolean msg_queue_create_1(Term q) throws PrologError{
		q= q.getTerm();
		if (!q.isAtomic() || !q.isAtom()) 
			throw PrologError.type_error(engine, 1,
                    "atom or atomic", q);
		return engineManager.createQueue(q.toString());
	}
	
	public boolean msg_queue_destroy_1 (Term q) throws PrologError{
		q=q.getTerm();
		if (!q.isAtomic() || !q.isAtom()) 
			throw PrologError.type_error(engine, 1,
                    "atom or atomic", q);
		engineManager.destroyQueue(q.toString());
		return true;
	}
	
	public boolean msg_queue_size_2(Term id, PTerm n) throws PrologError{
		id=id.getTerm();
		int size;
		if (id instanceof Int) 
			size=engineManager.queueSize(((Int)id).intValue());
		else{
			if (!id.isAtom() || !id.isAtomic())
				throw PrologError.type_error(engine, 1,
	                    "atom, atomic or integer", id);
			size=engineManager.queueSize(id.toString());
		}
		if (size<0) return false;
		return unify(n, new Int(size));
	}	
	
	public boolean mutex_create_1(Term mutex) throws PrologError{
		mutex=mutex.getTerm();
		if (!mutex.isAtom() || !mutex.isAtomic()) 
			throw PrologError.type_error(engine, 1,
                    "atom or atomic", mutex);
		return engineManager.createLock(mutex.toString());
	}
	
	public boolean mutex_destroy_1(Term mutex) throws PrologError{
		mutex=mutex.getTerm();
		if (!mutex.isAtom() || !mutex.isAtomic()) 
			throw PrologError.type_error(engine, 1,
                    "atom or atomic", mutex);
		engineManager.destroyLock(mutex.toString());
		return true;
	}
	
	public boolean mutex_lock_1(Term mutex) throws PrologError{
		mutex=mutex.getTerm();
		if (!mutex.isAtom() || !mutex.isAtomic()) 
			throw PrologError.type_error(engine, 1,
                    "atom or atomic", mutex);
		return engineManager.mutexLock(mutex.toString());
	}
	
	public boolean mutex_trylock_1(Term mutex) throws PrologError{
		mutex=mutex.getTerm();
		if (!mutex.isAtom() || !mutex.isAtomic()) 
			throw PrologError.type_error(engine, 1,
                    "atom or atomic", mutex);
		return engineManager.mutexTryLock(mutex.toString());
	}
	
	public boolean mutex_unlock_1(Term mutex) throws PrologError{
		mutex=mutex.getTerm();
		if (!mutex.isAtom() || !mutex.isAtomic()) 
			throw PrologError.type_error(engine, 1,
                    "atom or atomic", mutex);
		return engineManager.mutexUnlock(mutex.toString());
	}
	
	public boolean mutex_isLocked_1(Term mutex) throws PrologError{
		mutex=mutex.getTerm();
		if (!mutex.isAtom() || !mutex.isAtomic()) 
			throw PrologError.type_error(engine, 1,
                    "atom or atomic", mutex);
		return engineManager.isLocked(mutex.toString());
	}
	
	public boolean mutex_unlock_all_0(){
		engineManager.unlockAll();
		return true;
	}
	
	public String getTheory(){
		return 
		"thread_execute(ID, GOAL):- thread_create(ID, GOAL), '$next'(ID). \n" +
		"'$next'(ID). \n"+
		"'$next'(ID) :- '$thread_execute2'(ID). \n"+
		"'$thread_execute2'(ID) :- not thread_has_next(ID),!,false. \n" +
		"'$thread_execute2'(ID) :- thread_next_sol(ID). \n" +
		"'$thread_execute2'(ID) :- '$thread_execute2'(ID). \n" +
	
		"with_mutex(MUTEX,GOAL):-mutex_lock(MUTEX), call(GOAL), !, mutex_unlock(MUTEX).\n" +
		"with_mutex(MUTEX,GOAL):-mutex_unlock(MUTEX), fail."		
		;
	
	}
}
