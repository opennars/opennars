package nars.prolog;


import java.util.LinkedList;
import java.util.ListIterator;

public class TermQueue {

	private LinkedList<Term> queue;
	
	public TermQueue(){
		queue=new LinkedList<Term>();
	}
	
	public synchronized boolean get(Term t, Prolog engine, EngineRunner er){
		return searchLoop(t,engine,true, true, er);
	}
	
	private synchronized boolean searchLoop(Term t, Prolog engine, boolean block, boolean remove, EngineRunner er){
		boolean found=false;
		do{
			found=search(t,engine,remove);
			if (found) return true;
			er.setSolving(false);
			try {
				wait();
			} catch (InterruptedException e) {break;}
		}while (block);
		return false;
	}
	
	
	private synchronized boolean search(Term t, Prolog engine, boolean remove){
		boolean found=false;
		Term msg=null;
		ListIterator<Term> it=queue.listIterator();
		while (!found){
			if (it.hasNext()){
				msg=it.next();
			}
			else{
				return false;
			}
			found=engine.unify(t,msg);
		}
		if (remove) {
			queue.remove(msg);
		}
		return true;
	}
	
	
	public synchronized boolean peek(Term t, Prolog engine){
		return search(t,engine,false);
	}
	
	public synchronized boolean remove (Term t, Prolog engine){
		return search(t, engine, true);
	}
	
	public synchronized boolean wait (Term t, Prolog engine, EngineRunner er){
		return searchLoop(t,engine, true, false, er);
	}
	
	public synchronized void store (Term t){
		queue.addLast(t);
    	notifyAll();	
	}
	
	public synchronized int size(){
		return queue.size();
	}
	public synchronized void clear(){
		queue.clear();
	}
}
