package nars.tuprolog.net;

import nars.nal.term.Term;
import nars.tuprolog.Var;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

public class Datagram_Socket extends AbstractSocket {
	private static final long serialVersionUID = 1L;

	private DatagramSocket socket;

	
	public Datagram_Socket(DatagramSocket socket) {
		super();
		this.socket = socket;
	}

	@Override
	public boolean isClientSocket() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isServerSocket() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDatagramSocket() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public DatagramSocket getSocket() {
		// TODO Auto-generated method stub
		return socket;
	}

	@Override
	public InetAddress getAddress() {
		if(socket.isBound())return socket.getInetAddress();
		else return null;
	}

	@Override
	public boolean unify(List<Var> varsUnifiedArg1, List<Var> varsUnifiedArg2, Term t) {
		t = t.getTerm();
        if (t instanceof Var) {
            return ((Var)t).unify(varsUnifiedArg1, varsUnifiedArg2, this);
        } else if (t instanceof AbstractSocket && ((AbstractSocket) t).isDatagramSocket()) {
        	InetAddress addr= ((AbstractSocket) t).getAddress();
            return socket.getInetAddress().toString().equals(addr.toString());
        } else {
            return false;
        }
	}
	
}
