package nars.prolog;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Eleonora Cau
 *
 */

public class SocketLibTestCase {
	
	Prolog engine = null;
	String theory;
	
	
	@Before
	public void before() throws InvalidLibraryException, MalformedGoalException, NoSolutionException, UnknownVarException {
		try {
			engine = new Prolog();
			engine.loadLibrary("nars.prolog.lib.SocketLibrary");
			engine.loadLibrary("nars.prolog.lib.ThreadLibrary");
		} catch (InvalidLibraryException e) {
			e.printStackTrace();
		}
	}
	
	@Test 
	public void test_server_write() throws InvalidTheoryException, MalformedGoalException, NoSolutionException, UnknownVarException{
		String theory = 
		"server(Y):- thread_create(ID1, Y). \n"+
		"doServer(S) :- tcp_socket_server_open('127.0.0.1:4444', S, []), " +
					"tcp_socket_server_accept(S, '127.0.0.1:4444', ClientSock),  " +
					"write_to_socket(ClientSock, 'msg inviato dal server'), " +
					"thread_sleep(1), " +
					"mutex_lock('mutex'), " +
					"tcp_socket_server_close(S)," +
					"mutex_unlock('mutex').\n" +
		"client(X):- thread_create(ID2,X), " +
					"thread_read(ID2,X).\n"+
		"doClient(Sock, Msg) :- tcp_socket_client_open('127.0.0.1:4444',Sock), " +
					"mutex_lock('mutex'), " +
					"read_from_socket(Sock, Msg, []), " +
					"mutex_unlock('mutex')." ;
		
		engine.setTheory(new Theory(theory));
		
		SolveInfo result = engine.solve("server(doServer(SS)), client(doClient(CS,Msg)).");	
		assertTrue(result.isSuccess());

		/*Var clientSock = (Var) result.getTerm("CS");	
		System.out.println("[SocketLibTest] Client Socket: "+ clientSock);
		
		Var serverSock = (Var) result.getTerm("SS");	
		System.out.println("[SocketLibTest] Server Socket: "+ serverSock);*/
		
		Struct msg = (Struct) result.getTerm("Msg");	
		assertEquals(Term.createTerm("'msg inviato dal server'"), msg);
	
	}
	
	@Test 
	public void test_client_write() throws InvalidTheoryException, MalformedGoalException, NoSolutionException, UnknownVarException{
		String theory = 
		"server(ID1):- thread_create(ID1, doServer(SS, Msg)). \n"+
		"doServer(S, Msg) :- tcp_socket_server_open('127.0.0.1:4444', S, []), " +
					"tcp_socket_server_accept(S, '127.0.0.1:4444', ClientSock), " +
					"mutex_lock('mutex'), " +
					"read_from_socket(ClientSock, Msg, []), " +
					"mutex_unlock('mutex'), " +
					"tcp_socket_server_close(S).\n" +
		"client(X):- thread_create(ID2,X), " +
					"thread_read(ID2,X).\n"+
		"doClient(Sock) :- tcp_socket_client_open('127.0.0.1:4444',Sock),  " +
					"write_to_socket(Sock, 'msg inviato dal client'), " +
					"thread_sleep(1).\n" +
		"read(ID1,Y):- thread_read(ID1,Y)." ;
		engine.setTheory(new Theory(theory));
		
		SolveInfo result = engine.solve("server(ID1), client(doClient(CS)), read(ID1,doServer(SS,Msg)).");	
		assertTrue(result.isSuccess());
		
		/*Var clientSock = (Var) result.getTerm("CS");	
		System.out.println("[SocketLibTest] Client Socket: "+ clientSock);
		
		Var serverSock = (Var) result.getTerm("SS");	
		System.out.println("[SocketLibTest] Server Socket: "+ serverSock);*/
		
		Struct msg = (Struct) result.getTerm("Msg");	
		assertEquals(Term.createTerm("'msg inviato dal client'"), msg);
	}
}

