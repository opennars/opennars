package nars.tuprolog.net;

import nars.nal.NALOperator;
import nars.nal.term.Term;
import nars.tuprolog.PTerm;
import nars.tuprolog.TermVisitor;
import nars.tuprolog.Var;

import java.net.InetAddress;
import java.util.Map;
import java.util.ArrayList;
@SuppressWarnings("serial")



public abstract class AbstractSocket implements PTerm {
	
	public abstract boolean isClientSocket();
	
	public abstract boolean isServerSocket();
	
	public abstract boolean isDatagramSocket();
	
	public abstract Object getSocket();
	
	public abstract InetAddress getAddress();
	

	@Override
	public boolean isEmptyList() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAtomic() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCompound() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAtom() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isList() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isGround() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isGreater(Term t) {
		// TODO Auto-generated method stub
		return false;
	}
	public boolean isGreaterRelink(Term t, ArrayList<String> vorder) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEqual(Term t) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Term getTerm() {
		return this;
	}

	@Override
	public void free() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public long resolveTerm(long count) {
		return count;
	}

	@Override
	public PTerm copy(Map<Var, Var> vMap, int idExecCtx) {
		return this;
	}

	@Override
	public PTerm copy(Map<Var, Var> vMap, Map<PTerm, Var> substMap) {
		return this;
	}



	@Override
	public void accept(TermVisitor tv) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String toString(){
		return getSocket().toString();
	}

	@Override
	public int hashCode() {
		return getSocket().hashCode();
	}

	@Override
	public PTerm clone() {
		return null;
	}

	@Override
	public Term cloneDeep() {
		return null;
	}

	@Override
	public NALOperator operator() {
		return null;
	}


	@Override
	public short getComplexity() {
		return 1;
	}

	@Override
	public void recurseSubterms(nars.nal.term.TermVisitor v) {

	}

	@Override
	public void recurseSubterms(nars.nal.term.TermVisitor v, Term parent) {

	}
}



