package nars.prolog.net;

import nars.prolog.Term;
import nars.prolog.TermVisitor;
import nars.prolog.Var;

import java.net.InetAddress;
import java.util.AbstractMap;
import java.util.ArrayList;
@SuppressWarnings("serial")



public abstract class AbstractSocket extends Term{
	
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
	public Term copy(AbstractMap<Var, Var> vMap, int idExecCtx) {
		return this;
	}

	@Override
	public Term copy(AbstractMap<Var, Var> vMap, AbstractMap<Term, Var> substMap) {
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


}


