package nars.tuprolog;


import nars.nal.term.Term;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;

public class TermQueue {

	private final ArrayDeque<Term> queue;
	
	public TermQueue(){
		queue=new ArrayDeque<>();
	}
	
	public boolean get(PTerm t, Prolog engine, Engine er){
		return searchLoop(t,engine,true, true, er);
	}
	
	private boolean searchLoop(PTerm t, Prolog engine, boolean block, boolean remove, Engine er){
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
	
	
	private boolean search(PTerm t, Prolog engine, boolean remove){
		boolean found=false;
		Term msg=null;
		Iterator<Term> it=queue.iterator();
		ArrayList<Var> v1 = new ArrayList();
		ArrayList<Var> v2 = new ArrayList();
		while (!found){
			if (it.hasNext()){
				msg=it.next();
			}
			else{
				return false;
			}
			found=engine.unify(t,msg,v1,v2);
		}
		if (remove) {
			it.remove();
		}
		return true;
	}
	
	
	public boolean peek(PTerm t, Prolog engine){
		return search(t,engine,false);
	}
	
	public boolean remove (PTerm t, Prolog engine){
		return search(t, engine, true);
	}
	
	public boolean wait (PTerm t, Prolog engine, Engine er){
		return searchLoop(t,engine, true, false, er);
	}
	
	public void store (Term t){
		queue.addLast(t);
    	notifyAll();	
	}
	
	public int size(){
		return queue.size();
	}
	public void clear(){
		queue.clear();
	}
}
