package nars.tuprolog.lib;

import nars.nal.term.Term;
import nars.tuprolog.*;
import nars.tuprolog.interfaces.ISocketLib;
import nars.tuprolog.net.AbstractSocket;
import nars.tuprolog.net.Client_Socket;
import nars.tuprolog.net.Datagram_Socket;
import nars.tuprolog.net.Server_Socket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.regex.Pattern;

@SuppressWarnings("serial")
/**
 * 
 * @author Mirco Bordoni
 * 
 * This library implements TCP socket synchronous and asynchronous communication between Prolog hosts.
 *
 */

public class SocketLibrary extends Library implements ISocketLib {
	private String addrRegex;
	private LinkedList<ThreadReader> readers;			// Active readers
	private LinkedList<ServerSocket> serverSockets;		// Opened ServerSockets
	private LinkedList<Socket> clientSockets;			// Opened Sockets

	public SocketLibrary() {
		addrRegex = "[\\. :]";	// Address:Port parsed using regex
		readers = new LinkedList<>();
		serverSockets=new LinkedList<>();
		clientSockets=new LinkedList<>();
	}

	public String getTheory() {
		return "";
	}

	

	/* SocketLib UDP extension by Adelina Benedetti */
	
	// Open an udp socket

	public boolean udp_socket_open_2(Struct Address, PTerm Socket) throws PrologError
	{
		if (!(Socket.getTerm() instanceof nars.tuprolog.Var)) { // Socket has to be a variable
			throw PrologError.instantiation_error(engine, 1);
		}
		byte[] address = new byte[4];
		int port;

		// Transform IP:Port to byte[] array and port number
		Pattern p = Pattern.compile(addrRegex);
		String[] split = p.split(Address.getName());
		if (split.length != 5)
			throw PrologError.instantiation_error(engine, 1);
		for (int i = 0; i < split.length - 1; i++) {
			address[i] = Byte.parseByte(split[i]);
		}
		port = Integer.parseInt(split[split.length - 1]);

		try {
			DatagramSocket s=new DatagramSocket(port, InetAddress.getByAddress(address));

			Socket.unify(this.getEngine(), new Datagram_Socket(s));
		} catch (UnknownHostException e) {
			e.printStackTrace();
			throw PrologError.instantiation_error(engine, 1);
		} catch (IOException e) {
			e.printStackTrace();
			throw PrologError.instantiation_error(engine, 1);
		}

		return true;
	}
	
	// send an udp data
	
	public boolean udp_send_3(PTerm Socket, PTerm Data, Struct AddressTo) throws PrologError
	{
		if (!(Socket.getTerm() instanceof nars.tuprolog.Var)) { // Socket has to be a variable
			throw PrologError.instantiation_error(engine, 1);
		}
		byte[] address = new byte[4];
		int port;

		// Transform IP:Port to byte[] array and port number
		Pattern p = Pattern.compile(addrRegex);
		String[] split = p.split(AddressTo.getName());
		if (split.length != 5)
			throw PrologError.instantiation_error(engine, 1);
		for (int i = 0; i < split.length - 1; i++) {
			address[i] = Byte.parseByte(split[i]);
		}
		port = Integer.parseInt(split[split.length - 1]);
		{
			DatagramSocket s = ((Datagram_Socket) Socket.getTerm()).getSocket();
			 ByteArrayOutputStream baos = new ByteArrayOutputStream();
			 ObjectOutputStream oos;
			try {
				oos = new ObjectOutputStream(baos);
				 oos.writeObject(Data);
				 oos.flush();
				 byte[] Buf= baos.toByteArray();
				 DatagramPacket packet = new DatagramPacket(Buf, Buf.length,port);
				 s.send(packet);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	}	


	return true;
}

// udp socket close
public boolean udp_socket_close_1(PTerm Socket) throws PrologError {
	if (Socket.getTerm() instanceof nars.tuprolog.Var) {
		throw PrologError.instantiation_error(engine, 1);
	}
	if (!(((Server_Socket) Socket.getTerm()).isDatagramSocket())) {		
		throw PrologError.instantiation_error(engine, 1);
	}
	DatagramSocket s=((Datagram_Socket) Socket.getTerm()).getSocket();
	s.close();
	return true;
}

//udp receive data
@Override
public boolean udp_receive(PTerm Socket, PTerm Data, Struct AddressFrom,
		Struct Options) throws PrologError {
	if (!(Socket.getTerm() instanceof nars.tuprolog.Var)) {
		throw PrologError.instantiation_error(engine, 1);
	}
	byte[] address = new byte[4];
	@SuppressWarnings("unused")
	int port;

	// Transform IP:Port to byte[] array and port number
	Pattern p = Pattern.compile(addrRegex);
	String[] split = p.split(AddressFrom.getName());
	if (split.length != 5)
		throw PrologError.instantiation_error(engine, 1);
	for (int i = 0; i < split.length - 1; i++) {
		address[i] = Byte.parseByte(split[i]);
	}
	port = Integer.parseInt(split[split.length - 1]);
	DatagramSocket s= ((Datagram_Socket) Socket.getTerm()).getSocket();
	byte[] buffer = new byte[100000];
	DatagramPacket packet = new DatagramPacket(buffer, buffer.length );
	try {
		
		s.receive(packet);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	List<Term> list = StructToList(Options);
	for (Term t : list) { // Explore options list
		if (((Struct) t).getName().equals("timeout")) { // If a timeout has been specified
			int time = Integer.parseInt(((Struct) t).getTermX(0).toString());
			try {
				s.setSoTimeout(time);
			} catch (SocketException e) {
				e.printStackTrace();
				
			}
		}
		if(((Struct) t).getName().equals("size")){//if a datagram size has been specified
			int size=Integer.parseInt(((Struct) t).getTermX(0).toString());
			packet.setLength(size);
		}
	}
		
	
	return true;
}

/**
 * Create a ServerSocket bound to the specified Address.
 * 
 * @throws PrologError if Socket is not a variable
 */

public boolean tcp_socket_server_open_3(Struct Address, PTerm Socket, Struct Options) throws PrologError {
	int backlog=0;

	if (!(Socket.getTerm() instanceof nars.tuprolog.Var)) { // Socket has to be a variable
		throw PrologError.instantiation_error(engine, 1);
	}

	byte[] address = new byte[4];
	int port;

	// Transform IP:Port to byte[] array and port number
	Pattern p = Pattern.compile(addrRegex);
	String[] split = p.split(Address.getName());
	if (split.length != 5)
		throw PrologError.instantiation_error(engine, 1);
	for (int i = 0; i < split.length - 1; i++) {
		address[i] = Byte.parseByte(split[i]);
	}
	port = Integer.parseInt(split[split.length - 1]);


	List<Term> list = StructToList(Options); 			// Convert Options Struct to a LinkedList
	for (Term t : list) { 									// Explore Options list
		if (((Struct) t).getName().equals("backlog")) { 	// If a backlog has been specified
			backlog = Integer.parseInt(((Struct) t).getTermX(0).toString());
		}
	}

	// Create a server socket.
	try {
		ServerSocket s=new ServerSocket(port, backlog, InetAddress.getByAddress(address));
		addServerSocket(s);
		Socket.unify(this.getEngine(), new Server_Socket(s));
	} catch (UnknownHostException e) {
		e.printStackTrace();
		throw PrologError.instantiation_error(engine, 1);
	} catch (IOException e) {
		e.printStackTrace();
		throw PrologError.instantiation_error(engine, 1);
	}

	return true;
}

// Add a newly created ServerSocket to the list serverSockets, so they can be closed when the engine 
// has solved a goal or is halted.
private void addServerSocket(ServerSocket s){
	for(ServerSocket sock: serverSockets){
		if(sock.equals(s))return;
	}
	serverSockets.add(s);
}

// Add a newly created ClientSocket to the list clientSockets, so they can be closed when the engine 
// has solved a goal or is halted.
private void addClientSocket(Socket s){
	for(Socket sock: clientSockets){
		if(sock.equals(s))return;
	}
	clientSockets.add(s);
}


/**
 * Accept a connection to the specified ServerSocket. This method blocks
 * until a connection is received.
 * 
 * @throws PrologError if ServerSock is a variable or it is not a Server_Socket
 */
public boolean tcp_socket_server_accept_3(PTerm ServerSock, PTerm Client_Addr, PTerm Client_Slave_Socket) throws PrologError {

	if (ServerSock.getTerm() instanceof nars.tuprolog.Var) { 	// ServerSock has to be bound
		throw PrologError.instantiation_error(engine, 1);
	}

	AbstractSocket as= (AbstractSocket)ServerSock.getTerm();
	if(!as.isServerSocket()){									// ServerSock has to be a Server_Socket
		throw PrologError.instantiation_error(engine, 1);
	}

	ServerSocket s = ((Server_Socket) ServerSock.getTerm()).getSocket();
	Socket client;
	try {
		client = s.accept();
		Client_Addr.unify(this.getEngine(), new Struct(client.getInetAddress().getHostAddress() + ':' + client.getPort()));
		Client_Slave_Socket.unify(this.getEngine(), new Client_Socket(client));
		addClientSocket(client);
	} catch (IOException e) {
		//e.printStackTrace();
		return false;
	}
	return true;
}

/**
 * Create a Client_Socket and connect it to a specified address.
 * @throws PrologError if Socket is not a variable
 */
public boolean tcp_socket_client_open_2(Struct Address, PTerm SocketTerm) throws PrologError {
	if (!(SocketTerm.getTerm() instanceof nars.tuprolog.Var)) { // Socket has to be a variable
		throw PrologError.instantiation_error(engine, 2);
	}

	byte[] address = new byte[4];
	int port;

	// IP:Port --> IP in byte[] array and port number
	Pattern p = Pattern.compile(addrRegex);
	String[] split = p.split(Address.getName());
	if (split.length != 5)
		throw PrologError.instantiation_error(engine, 1);
	for (int i = 0; i < split.length - 1; i++) {
		address[i] = Byte.parseByte(split[i]);
	}
	port = Integer.parseInt(split[split.length - 1]);

	Socket s;
	try {
		s = new Socket(InetAddress.getByAddress(address), port);
		SocketTerm.unify(this.getEngine(), new Client_Socket(s));
		addClientSocket(s);
	} catch (UnknownHostException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		return false;
	} catch (IOException e) {
		e.printStackTrace();
		return false;
	}
	return true;
}

/**
 * Close a Server_Socket
 * @throws PrologError if serverSocket is a variable or it is not a Server_Socket
 */
public synchronized boolean tcp_socket_server_close_1(PTerm serverSocket) throws PrologError {
	if (serverSocket.getTerm() instanceof nars.tuprolog.Var) { 			// serverSocket has to be bound
		throw PrologError.instantiation_error(engine, 1);
	}
	if (!(((Server_Socket) serverSocket.getTerm()).isServerSocket())) {		// serverSocket has to be a Server_Socket
		throw PrologError.instantiation_error(engine, 1);
	}
	try {
		ServerSocket s=((Server_Socket) serverSocket.getTerm()).getSocket();
		s.close();
		// Remove closed ServerSocket from serverSockets list
		for(int i=0;i<serverSockets.size();i++){
			if(serverSockets.get(i).equals(s)){
				serverSockets.remove(i);
				return true;
			}
		}
	} catch (IOException e) {
		e.printStackTrace();
		return false;
	}
	return true;
}

/**
 * Send Msg through the socket Socket. Socket has to be connected!
 * @throws PrologError if Socket is a variable or it is not a Client_Socket or Msg is not bound
 */
public boolean write_to_socket_2(PTerm Socket, PTerm Msg) throws PrologError {
	if (Socket.getTerm() instanceof nars.tuprolog.Var) { // Socket has to be bound
		throw PrologError.instantiation_error(engine, 1);
	}
	if (((AbstractSocket) Socket.getTerm()).isServerSocket()) { // Only Client_Sockets can send data
		throw PrologError.instantiation_error(engine, 1);
	}
	if (Msg.getTerm() instanceof nars.tuprolog.Var) { // Record has to be bound
		throw PrologError.instantiation_error(engine, 2);

	} else {
		Socket sock = ((Client_Socket) Socket.getTerm()).getSocket();
		try {
			ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
			out.writeObject(Msg);		// Write message in OutputStream
		} catch (IOException e) {
			//e.printStackTrace();
			return false;
		}

	}
	return true;
}

/**
 * Synchronous reading from Socket. This is a blocking operation.
 * @param Options The user can specify a timeout using [timeout(millis)]. If timeout expires the
 * 					predicate fails
 * @throws PrologError if Socket is not bound or it is not a Client_Socket or Msg is bound
 */
public boolean read_from_socket_3(PTerm Socket, PTerm Msg, Struct Options) throws PrologError {
	if (Socket.getTerm() instanceof nars.tuprolog.Var) { // Socket has to be bound
		throw PrologError.instantiation_error(engine, 1);
	}
	if (!(Msg.getTerm() instanceof nars.tuprolog.Var)) { // Message has to be a variable
		throw PrologError.instantiation_error(engine, 2);
	}
	if (!((AbstractSocket) Socket.getTerm()).isClientSocket()) { // Only Client_Sockets can receive data
		throw PrologError.instantiation_error(engine, 1);
	} else {
		Socket sock = ((Client_Socket) Socket.getTerm()).getSocket();

		// Check if a Reader associated to the Socket passed already exists
		ThreadReader r = readerExist(sock);
		// If a thread is already waiting for data on the same socket return false
		if (r != null) {
			if (r.started())
				return false;
		}

		List<Term> list = StructToList(Options); // Convert Options Struct to a LinkedList
		for (Term t : list) { // Explore options list
			if (((Struct) t).getName().equals("timeout")) { // If a timeout has been specified
				int time = Integer.parseInt(((Struct) t).getTermX(0).toString());
				try {
					sock.setSoTimeout(time); // Set socket timeout
				} catch (SocketException e) {
					e.printStackTrace();
					return false;
				}
			}
		}



		try {
			ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
			PTerm m = (PTerm) in.readObject();
			Msg.unify(this.getEngine(), m);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		}

	}
	return true;
}

/**
 * Asynchronous read from Socket. When a message is received an assertA
 * (by default) is executed to put it in the theory. The user can set the
 * option "assertZ" to use assertZ instead of assertA.
 * 
 * @param Socket
 *            Socket used to read
 * @param Options
 *            a timeout can be specified for the socket with the option
 *            [timeout(millis)]. If timeout expires while reading, nothing
 *            is read and nothing is asserted.
 *            The user can insert the option assertZ to change the way the 
 *            received message is asserted
 * @return true if no error happens
 * @throws PrologError if Socket is not bound or it is not a Client_Socket
 */
public boolean aread_from_socket_2(PTerm Socket, Struct Options) throws PrologError {
	ThreadReader r;
	if (Socket.getTerm() instanceof nars.tuprolog.Var) { // Socket has to be bound
		throw PrologError.instantiation_error(engine, 1);
	}
	if (!((AbstractSocket) Socket.getTerm()).isClientSocket()) { // Only Client_Sockets can receive data
		throw PrologError.instantiation_error(engine, 1);
	} else {
		// Retrieve socket from the term Socket passed to this method
		Socket sock = ((Client_Socket) Socket.getTerm()).getSocket();

		// Find reader associated with the socket if already exists,
		// otherwise create a new reader
		r = readerExist(sock);
		if (r == null) {
			synchronized (this) {
				readers.add(new ThreadReader(sock, this.getEngine()));
				r = readers.getLast();
			}
		}

		// If reader already reading return true, otherwise start reading
		if (r.started())
			return true;

		try {
			sock.setSoTimeout(0); // Set socket timeout to infinite
		} catch (SocketException e1) {
			e1.printStackTrace();
		}

		List<Term> list = StructToList(Options); // Convert Options Struct to a LinkedList
		for (Term t : list) { // Explore options list
			if (((Struct) t).getName().equals("timeout")) { // If a timeout has been specified
				int time = Integer.parseInt(((Struct) t).getTermX(0).toString());
				try {
					sock.setSoTimeout(time); // Set socket timeout
				} catch (SocketException e) {
					e.printStackTrace();
					return false;
				}
			}
			// If assertZ is specified what is read is written in the theory
			// with assertZ instead of assertA
			if (((Struct) t).getName().equals("assertZ")) {
				r.assertZ();
			}
		}

		r.startRead();
	}
	return true;
}


/*
 * Transform the Struct s in a LinkedList
 */
private LinkedList<Term> StructToList(Struct s) {
	LinkedList<Term> list = new LinkedList<>();
	Term temp;
	temp = s;
	while (true) {
		if (((Struct) temp).getName().equals(".")) {
			list.add(((Struct) temp).getTermX(0));
		} else
			break;
		temp = ((Struct) temp).getTermX(1);

	}
	return list;
}


/*
 * Check whether a reader associated to socket s already exists
 */
private ThreadReader readerExist(Socket s) {
	for (ThreadReader r : readers) {
		if (r.compareSocket(s))
			return r;
	}
	return null;
}


/*
 * When a goal is solved close all ServerSockets and stop all readers
 */
@Override
public void onSolveEnd(){
	for(ServerSocket s:serverSockets){
		try {
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	serverSockets=new LinkedList<>();
	for(Socket s:clientSockets){
		try {
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	clientSockets = new LinkedList<>();
	for(ThreadReader r:readers)r.stopRead();

}

/*
 * If the user stops the computation call onSolveEnd() to close all sockets and stop all readers
 */
@Override
public void onSolveHalt(){
	onSolveEnd();
}

public boolean getAddress_2(PTerm sock, PTerm addr) throws PrologError {
	if (sock.getTerm() instanceof nars.tuprolog.Var) { // Socket has to be bound
		throw PrologError.instantiation_error(engine, 1);
	}
	AbstractSocket abs = (AbstractSocket) sock.getTerm();
	if (abs.isClientSocket()) {
		Socket s = ((Client_Socket) sock.getTerm()).getSocket();
		addr.unify(this.engine, new Struct(s.getInetAddress().toString(), new Struct(new Int(s.getLocalPort()).toString())));
		return true;
	}
	if (abs.isServerSocket()) {
		ServerSocket s = ((Server_Socket) sock.getTerm()).getSocket();
		addr.unify(this.getEngine(), new Struct(s.getInetAddress().toString(), new Struct(new Int(s.getLocalPort()).toString())));
		return true;
	}
	if (abs.isDatagramSocket()) {
		DatagramSocket s = ((Datagram_Socket) sock.getTerm()).getSocket();
		addr.unify(this.getEngine(), new Struct(s.getInetAddress().toString(), new Struct(new Int(s.getLocalPort()).toString())));
		return true;
	}

	return true;
}



/*
 * Definition of thread Reader. It waits until a message is received and assert it.
 */
private class ThreadReader extends Thread {
	private Socket socket;				// Socket associated to the Reader
	private Prolog mainEngine;
	private boolean assertA;			// Should it use assertA or assertZ?
	private volatile boolean started;	// True if the thread is already waiting on a socket
	private Semaphore sem;

	protected ThreadReader(Socket socket, Prolog mainEngine) {
		this.socket = socket;
		this.mainEngine = mainEngine;
		assertA = true;					// assertA by default
		started = false;
		sem = new Semaphore(0);
		this.start();
	}

	// Set the boolean variable started and release the semaphore where the thread is waiting
	protected synchronized void startRead() {
		if(started)return;
		started = true;
		sem.release();
	}

	protected boolean started() {
		return started;
	}

	// Close the socket (to stop the thread if it is waiting on the read method) 
	// and interrupt the thread (if it is waiting on the semaphore)
	protected synchronized void stopRead(){
		this.interrupt();
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected synchronized void assertZ() {
		assertA = false;
	}

	protected boolean compareSocket(Socket s) {
		return s.equals(socket);
	}

	public void run() {
		while (true) {
			while (!started) {
				try {
					sem.acquire();
					if(this.isInterrupted())return;
				} catch (InterruptedException e1) {
					//e1.printStackTrace();
					return;
				}
			}
			try {
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
				if(this.isInterrupted())return;
				PTerm msg = (PTerm) in.readObject();
				if(this.isInterrupted())return;					
				Struct s = (Struct) PTerm.createTerm(msg.getTerm().toString());
				if (assertA)
					mainEngine.getTheories().assertA(s, true, "", false);
				else
					mainEngine.getTheories().assertZ(s, true, "", false);
				assertA = true; // By default use assertA!
				started = false;
			} catch (IOException e) {
				//e.printStackTrace();
				started = false;
				return;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				started = false;
				return;
			}
		}
	}

}




}
