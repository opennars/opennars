package nars.tuprolog.net;

import nars.nal.term.Term;
import nars.tuprolog.Var;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.List;

@SuppressWarnings("serial")

public class Server_Socket extends AbstractSocket{
	private ServerSocket socket;
	
	public Server_Socket(ServerSocket s){
		socket=s;
	}
	@Override
	public ServerSocket getSocket(){
		return socket;
	}
	@Override
	public boolean isClientSocket() {
		return false;
	}

	@Override
	public boolean isServerSocket() {
		return true;
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
	public String toString(){
		return socket.toString();
	}


}
