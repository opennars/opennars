package nars.tuprolog.net;

import nars.nal.NALOperator;
import nars.nal.term.Term;
import nars.tuprolog.PTerm;
import nars.tuprolog.Var;

import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

@SuppressWarnings("serial")

public class Client_Socket extends AbstractSocket {
	private Socket socket;

	public Client_Socket(Socket s){
		socket=s;
	}
	
	@Override
	public boolean isClientSocket() {
		return true;
	}

	@Override
	public boolean isServerSocket() {
		return false;
	}

	@Override
	public Socket getSocket() {
		return socket;
	}
	
	
	public boolean unify(List<Var> varsUnifiedArg1, List<Var> varsUnifiedArg2, Term t) {
		t = t.getTerm();
        if (t instanceof Var) {
            return ((Var)t).unify(varsUnifiedArg1, varsUnifiedArg2, this);
        } else if (t instanceof AbstractSocket && ((AbstractSocket) t).isServerSocket()) {
        	InetAddress addr= ((AbstractSocket) t).getAddress();
            return socket.getInetAddress().toString().equals(addr.toString());
        } else {
            return false;
        }
	}
	
	@Override
	public InetAddress getAddress() {
		if(socket.isBound())return socket.getInetAddress();
		else return null;
	}

	@Override
	public boolean isDatagramSocket() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public NALOperator operator() {
		return null;
	}

	@Override
	public short getComplexity() {
		return 0;
	}



	@Override
	public Term cloneDeep() {
		return null;
	}

	@Override
	public String toString(){
		return socket.toString();
	}
	

}
