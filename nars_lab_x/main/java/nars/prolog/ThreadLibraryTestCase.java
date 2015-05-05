package prolog;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


/**
 * 
 */

/**
 * @author Eleonora Cau
 *
 */
public class ThreadLibraryTestCase {
	
	Prolog engine = null;
	String theory;
	
	@Before
	public void before() {
		try {
			engine = new Prolog();
			engine.loadLibrary("nars.prolog.lib.ThreadLibrary");
		} catch (InvalidLibraryException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Test method for {@link nars.tuprolog.lib.ThreadLibrary#thread_id_1(alice.tuprolog.Term)}.
	 * @throws InvalidTheoryException 
	 * @throws MalformedGoalException 
	 * @throws NoSolutionException 
	 */
	@Test
	public void testThread_id_1() throws InvalidTheoryException, MalformedGoalException, NoSolutionException {
		SolveInfo sinfo = engine.solve("thread_id(ID).");	//unifica ad ID l'identificativo del thread corrente (Root)
		assertTrue(sinfo.isSuccess());
		Term id = sinfo.getVarValue("ID");
		assertEquals(new Int(0), id);
	}

	/**
	 * Test method for {@link nars.tuprolog.lib.ThreadLibrary#thread_create_2(alice.tuprolog.Term, alice.tuprolog.Term)}.
	 * @throws MalformedGoalException 
	 * @throws InvalidTheoryException 
	 */
	@Test
	public void testThread_create_2() throws MalformedGoalException, InvalidTheoryException {
		theory = "genitore(bob,a).\n" +
		"genitore(bob,b).\n" +
		"genitore(bob,c).\n" +
		"genitore(bob,d).";
		engine.setTheory(new Theory(theory));
		
		SolveInfo sinfo = engine.solve("thread_create(ID, genitore(bob,X)).");
		assertTrue(sinfo.isSuccess());
		
		sinfo = engine.solve("thread_create(ID, genitore(bob,X)), thread_create(ID2, genitore(b,Y)).");
		assertTrue(sinfo.isSuccess());
	}
	
	/**
	 * Test method for {@link nars.tuprolog.lib.ThreadLibrary#thread_next_sol_1(alice.tuprolog.Term)}.
	 * @throws InvalidTheoryException 
	 * @throws MalformedGoalException 
	 * @throws NoSolutionException 
	 */
	@Test
	public void testThread_next_sol_1() throws InvalidTheoryException, MalformedGoalException, NoSolutionException {
		theory = "start(X) :- thread_create(ID, genitore(bob,X)),  loop(1,5,1,ID),  thread_read(ID, X).\n"+
		"loop(I, To, Inc, ThreadId) :- Inc >= 0, I > To, !.\n"+
		"loop(I, To, Inc, ThreadId) :- Inc < 0,  I < To, !.\n"+
		"loop(I, To, Inc, ThreadId) :- thread_read(ThreadId,A), thread_has_next(ThreadId), !, thread_next_sol(ThreadId), Next is I+Inc, loop(Next, To, Inc, ThreadId).\n"+
		"loop(I, To, Inc, ThreadId).\n"+
		"genitore(b,b).\n" +
		"genitore(bob,c).\n" +
		"genitore(b,d).\n" +
		"genitore(bob,gdh).\n"+
		"genitore(b,e).\n" +
		"genitore(b,f).";
		engine.setTheory(new Theory(theory));
		
		SolveInfo sinfo = engine.solve("start(X).");
		assertTrue(sinfo.isSuccess());
		
		Term X = sinfo.getVarValue("X");
		assertEquals(Term.createTerm("genitore(bob,gdh)"), X);
	}

	/**
	 * Test method for {@link nars.tuprolog.lib.ThreadLibrary#thread_join_2(alice.tuprolog.Term, alice.tuprolog.Term)}.
	 * @throws InvalidTheoryException 
	 * @throws MalformedGoalException 
	 * @throws NoSolutionException 
	 */
	@Test
	public void testThread_join_2() throws InvalidTheoryException, MalformedGoalException, NoSolutionException {
		theory = "genitore(bob,a).\n" +
		"genitore(b,b).";
		engine.setTheory(new Theory(theory));
		
		SolveInfo sinfo = engine.solve("thread_create(ID, genitore(bob,X)), thread_create(ID2, genitore(b,Y)), thread_join(ID2,Y), thread_join(ID,X).");
		assertTrue(sinfo.isSuccess());
		
		Term X = sinfo.getVarValue("X");
		assertEquals(Term.createTerm("genitore(bob,a)"), X);
		
		Term Y = sinfo.getVarValue("Y");
		assertEquals(Term.createTerm("genitore(b,b)"), Y);
		
		sinfo = engine.solve("thread_create(ID, genitore(bob,X)), thread_join(ID,X), thread_next_sol(ID).");	//il thread stato rimosso
		assertFalse(sinfo.isSuccess());
	}

	/**
	 * Test method for {@link nars.tuprolog.lib.ThreadLibrary#thread_read_2(alice.tuprolog.Term, alice.tuprolog.Term)}.
	 * @throws InvalidTheoryException 
	 * @throws MalformedGoalException 
	 * @throws NoSolutionException 
	 */
	@Test
	public void testThread_read_2() throws InvalidTheoryException, MalformedGoalException, NoSolutionException {
		theory = "genitore(bob,a).\n" +
		"genitore(b,b).\n" +
		"genitore(bob,f).\n" +
		"loop(I, To, Inc, Action) :- Inc >= 0, I > To, !.\n" +
		"loop(I, To, Inc, Action) :- Inc < 0,  I < To, !.\n" +
		"loop(I, To, Inc, Action) :- Action, Next is I+Inc, loop(Next, To, Inc, Action).";
		engine.setTheory(new Theory(theory));
		
		//SolveInfo sinfo = engine.solve("thread_create(genitore(bob,X), ID), thread_create(genitore(b,Y), ID2), thread_read(ID2,Y), thread_read(ID,X1), thread_next_sol(ID), thread_read(ID,X).");
		SolveInfo sinfo = engine.solve("thread_create(ID, genitore(bob,X)), thread_read(ID,X1), thread_create(ID2, loop(1,10,1, thread_read(ID,X2))),  thread_create(ID3, loop(1,2,1, thread_read(ID,X2))), thread_next_sol(ID), thread_read(ID,X).");

		assertTrue(sinfo.isSuccess());
		
		Term X = sinfo.getVarValue("X");
		assertEquals(Term.createTerm("genitore(bob,f)"), X);
		
		Term X1 = sinfo.getVarValue("X1");
		assertEquals(Term.createTerm("genitore(bob,a)"), X1);
		
		sinfo = engine.solve("thread_create(ID, genitore(bob,X)), thread_read(ID,X), thread_next_sol(ID).");	//Il thread non stato rimosso
		assertTrue(sinfo.isSuccess());
	}

	/**
	 * Test method for {@link nars.tuprolog.lib.ThreadLibrary#thread_has_next_1(alice.tuprolog.Term)}.
	 * @throws InvalidTheoryException 
	 * @throws MalformedGoalException 
	 * @throws NoSolutionException 
	 */
	@Test
	public void testThread_has_next_1() throws InvalidTheoryException, MalformedGoalException, NoSolutionException {
		theory = "start(X) :- thread_create(ID, X), thread_execute(ID), lettura(ID,X).\n" +
		"lettura(ID, X):- thread_join(ID, X).\n" +
		"thread_execute(ID) :- thread_read(ID,A), thread_has_next(ID), !, thread_next_sol(ID). \n" +
		"thread_execute(ID).\n" +
		"genitore(bob,a).\n" +
		"genitore(bob,b).\n" +
		"genitore(bob,d).";
		engine.setTheory(new Theory(theory));
	
		SolveInfo sinfo = engine.solve("start(genitore(bob,X)).");
		assertTrue(sinfo.isSuccess());
		
		Term X = sinfo.getVarValue("X");
		assertEquals(Term.createTerm("b"), X);
	}

	

	/**
	 * Test method for {@link nars.tuprolog.lib.ThreadLibrary#thread_detach_1(alice.tuprolog.Term)}.
	 * @throws InvalidTheoryException 
	 * @throws MalformedGoalException 
	 */
	@Test
	public void testThread_detach_1() throws InvalidTheoryException, MalformedGoalException {
		theory = "genitore(bob,a).\n" +
				"genitore(bob,b).";
		engine.setTheory(new Theory(theory));
		
		SolveInfo sinfo = engine.solve("thread_create(ID, genitore(bob,X)), thread_detach(ID), thread_next_sol(ID).");
		assertFalse(sinfo.isSuccess());
	}

	/**
	 * Test method for {@link nars.tuprolog.lib.ThreadLibrary#thread_sleep_1(alice.tuprolog.Term)}.
	 * @throws InvalidTheoryException 
	 * @throws MalformedGoalException 
	 */
	@Test
	public void testThread_sleep_1() throws InvalidTheoryException, MalformedGoalException {
		theory = "genitore(bob,a).\n" +
		"genitore(bob,b).";
		engine.setTheory(new Theory(theory));

		SolveInfo sinfo = engine.solve("thread_create(ID, genitore(bob,X)), thread_sleep(500).");
		assertTrue(sinfo.isSuccess());
	}

	/**
	 * Test method for {@link nars.tuprolog.lib.ThreadLibrary#thread_send_msg_2(alice.tuprolog.Term, alice.tuprolog.Term)}.
	 * @throws MalformedGoalException 
	 * @throws NoSolutionException 
	 * @throws InvalidTheoryException 
	 */
	@Test
	public void testThread_send_msg_2() throws MalformedGoalException, NoSolutionException, InvalidTheoryException {
		theory = "start(X) :- msg_queue_create('CODA'), thread_create(ID, thread1(X)), invio('CODA', 'messaggio molto importante'), lettura(ID,X).\n" +
		"thread1(X) :- thread_wait_msg('CODA', a(X)). \n " +
		"invio(ID, M):- thread_send_msg(ID, a(M)). \n" +		//Versione con 'CODA'
		"lettura(ID, X):- thread_join(ID, thread1(X)). ";
		engine.setTheory(new Theory(theory));
		
		SolveInfo sinfo = engine.solve("start(X).");
		assertTrue(sinfo.isSuccess());
		
		Term X = sinfo.getVarValue("X");
		assertEquals(new Struct("messaggio molto importante"), X);
		
		theory = "start(X) :- msg_queue_create('CODA'), thread_create(ID, thread1(X)), invio(ID, 'messaggio molto importante'), lettura(ID,X), thread_get_msg('CODA', a(X)).\n" +	//Posso nuovamente prelevare, in quanto il msg non stato eliminato
		"thread1(X) :- thread_wait_msg('CODA', a(X)). \n " +
		"invio(ID, M):- thread_send_msg(ID, a(M)). \n" +		//Versione con ID
		"lettura(ID, X):- thread_join(ID, thread1(X)). ";
		engine.setTheory(new Theory(theory));
		
		sinfo = engine.solve("start(X).");
		assertTrue(sinfo.isSuccess());
		
		Term X1 = sinfo.getVarValue("X");
		assertEquals(new Struct("messaggio molto importante"), X1);
	}

	/**
	 * Test method for {@link nars.tuprolog.lib.ThreadLibrary#thread_get_msg_2(alice.tuprolog.Term, alice.tuprolog.Term)}.
	 * @throws InvalidTheoryException 
	 * @throws MalformedGoalException 
	 * @throws NoSolutionException 
	 */
	@Test
	public void testThread_get_msg_2() throws InvalidTheoryException, MalformedGoalException, NoSolutionException {
		theory = "start(X) :- msg_queue_create('CODA'), thread_create(ID, thread1(X)), thread_sleep(500), invio('CODA', 'messaggio molto importante'), lettura(ID,X).\n" +
		"thread1(X) :- thread_get_msg('CODA', a(X)). \n " +
		"invio(ID, M):- thread_send_msg(ID, a(M)). \n" +		//Versione con 'CODA'
		"lettura(ID, X):- thread_join(ID, thread1(X)). ";
		engine.setTheory(new Theory(theory));
		
		SolveInfo sinfo = engine.solve("start(X).");
		assertTrue(sinfo.isSuccess());
		
		Term X = sinfo.getVarValue("X");
		assertEquals(new Struct("messaggio molto importante"), X);
	}

	/**
	 * Test method for {@link nars.tuprolog.lib.ThreadLibrary#thread_peek_msg_2(alice.tuprolog.Term, alice.tuprolog.Term)}.
	 * @throws InvalidTheoryException 
	 * @throws MalformedGoalException 
	 * @throws NoSolutionException 
	 */
	@Test
	public void testThread_peek_msg_2() throws InvalidTheoryException, MalformedGoalException, NoSolutionException {
		theory = "start(X) :- msg_queue_create('CODA'), thread_create(ID, thread1(X)), thread_sleep(500), invio('CODA', 'messaggio molto importante'), lettura(ID,X).\n" +
		"thread1(X) :- thread_peek_msg('CODA', a(X)). \n " +
		"invio(ID, M):- thread_send_msg(ID, a(M)). \n" +		//Versione con 'CODA'
		"lettura(ID, X):- thread_join(ID, thread1(X)). ";
		engine.setTheory(new Theory(theory));
		
		SolveInfo sinfo = engine.solve("start(X).");
		assertFalse(sinfo.isSuccess());
		
		theory = "start(X) :- msg_queue_create('CODA'), thread_create(ID, thread1(X)), invio(ID, 'messaggio molto importante'), lettura(ID,X), thread_get_msg('CODA', a(X)).\n" +	//Posso nuovamente prelevare, in quanto il msg non stato rimosso
		"thread1(X) :- thread_peek_msg('CODA', a(X)). \n " +
		"invio(ID, M):- thread_send_msg(ID, a(M)). \n" +		//Versione con ID
		"lettura(ID, X):- thread_join(ID, thread1(X)). ";
		engine.setTheory(new Theory(theory));
		
		sinfo = engine.solve("start(X).");
		assertTrue(sinfo.isSuccess());
		
		Term X = sinfo.getVarValue("X");
		assertEquals(new Struct("messaggio molto importante"), X);
		
		theory = "start(X) :- msg_queue_create('CODA'), thread_create(ID, thread1(X)), lettura(ID,X).\n" +	
		"thread1(X) :- thread_peek_msg('CODA', a(X)). \n " +
		"lettura(ID, X):- thread_join(ID, thread1(X)). ";
		engine.setTheory(new Theory(theory));
		
		sinfo = engine.solve("start(X).");
		assertFalse(sinfo.isSuccess());
	}

	/**
	 * Test method for {@link nars.tuprolog.lib.ThreadLibrary#thread_wait_msg_2(alice.tuprolog.Term, alice.tuprolog.Term)}.
	 * @throws InvalidTheoryException 
	 * @throws MalformedGoalException 
	 * @throws NoSolutionException 
	 */
	@Test
	public void testThread_wait_msg_2() throws InvalidTheoryException, MalformedGoalException, NoSolutionException {
		theory = "start(X) :- msg_queue_create('CODA'), thread_create(ID, thread1(X)), thread_sleep(500), invio('CODA', 'messaggio molto importante'), lettura(ID,X).\n" +
		"thread1(X) :- thread_wait_msg('CODA', a(X)). \n " +
		"invio(ID, M):- thread_send_msg(ID, a(M)). \n" +		//Versione con 'CODA'
		"lettura(ID, X):- thread_join(ID, thread1(X)). ";
		engine.setTheory(new Theory(theory));
		
		SolveInfo sinfo = engine.solve("start(X).");
		assertTrue(sinfo.isSuccess());
		
		theory = "start(X) :- msg_queue_create('CODA'), thread_create(ID, thread1(X)), invio(ID, 'messaggio molto importante'), lettura(ID,X), thread_get_msg('CODA', a(X)).\n" +	//Posso nuovamente prelevare, in quanto il msg non stato rimosso
		"thread1(X) :- thread_wait_msg('CODA', a(X)). \n " +
		"invio(ID, M):- thread_send_msg(ID, a(M)). \n" +		//Versione con ID
		"lettura(ID, X):- thread_join(ID, thread1(X)). ";
		engine.setTheory(new Theory(theory));
		
		sinfo = engine.solve("start(X).");
		assertTrue(sinfo.isSuccess());
		
		Term X = sinfo.getVarValue("X");
		assertEquals(new Struct("messaggio molto importante"), X);
		
		//SI BLOCCA IN ATTESA DEL MESSAGGIO
		/*theory = "start(X) :- message_queue_create('CODA'), thread_create(ID, thread1(X)), lettura(ID,X).\n" +	
		"thread1(X) :- thread_wait_msg('CODA', a(X)). \n " +
		"lettura(ID, X):- thread_join(ID, thread1(X)). ";
		engine.setTheory(new Theory(theory));
		
		sinfo = engine.solve("start(X).");
		assertFalse(sinfo.isSuccess());*/
	}

	/**
	 * Il metodo peek non riesce a prelevare la soluzione perch il messaggio stato rimosso
	 * 
	 * Test method for {@link nars.tuprolog.lib.ThreadLibrary#thread_remove_msg_2(alice.tuprolog.Term, alice.tuprolog.Term)}.
	 * @throws InvalidTheoryException 
	 * @throws MalformedGoalException 
	 */
	@Test
	public void testThread_remove_msg_2() throws InvalidTheoryException, MalformedGoalException {
		theory = "start(X) :- msg_queue_create('CODA'),  invio('CODA', 'messaggio molto importante'), thread_create(ID, thread1(X)), thread_sleep(50), thread_peek_message(ID, X).\n" +
		"thread1(X) :- thread_remove_msg('CODA', a(X)). \n " +
		"invio(ID, M):- thread_send_msg(ID, a(M)). \n" +		//Versione con 'CODA'
		"lettura(ID, X):- thread_join(ID, thread1(X)). ";
		engine.setTheory(new Theory(theory));
		
		SolveInfo sinfo = engine.solve("start(X).");
		assertFalse(sinfo.isSuccess());
	}

	/**
	 * start(X) -> prelevo la soluzione, poi distruggo la coda.
	 * start2(X) -> distruggo la coda, poi prelevo la soluzione.
	 * Test method for {@link nars.tuprolog.lib.ThreadLibrary#message_queue_destroy_1(alice.tuprolog.Term)}.
	 * @throws InvalidTheoryException 
	 * @throws MalformedGoalException 
	 */
	@Test
	public void testMsg_queue_destroy_1() throws InvalidTheoryException, MalformedGoalException {
		theory = "start(X) :- msg_queue_create('CODA'), thread_create(ID, thread1(X)), invio('CODA', 'messaggio molto importante'), lettura(ID,X), msg_queue_destroy('CODA').\n" +
		"start2(X) :- msg_queue_create('CODA'), invio('CODA', 'messaggio molto importante'), msg_queue_destroy('CODA'), thread_create(ID, thread1(X)),  lettura(ID,X).\n" +
		"thread1(X) :- thread_wait_msg('CODA', a(X)). \n " +
		"invio(ID, M):- thread_send_msg(ID, a(M)). \n" +		//Versione con 'CODA'
		"lettura(ID, X):- thread_join(ID, thread1(X)). ";
		engine.setTheory(new Theory(theory));
		
		SolveInfo sinfo = engine.solve("start(X).");
		assertTrue(sinfo.isSuccess());
		
		sinfo = engine.solve("start2(X).");
		assertFalse(sinfo.isSuccess());
	}

	/**
	 * Test method for {@link nars.tuprolog.lib.ThreadLibrary#msg_queue_size_2(alice.tuprolog.Term, alice.tuprolog.Term)}.
	 * @throws InvalidTheoryException 
	 * @throws MalformedGoalException 
	 * @throws NoSolutionException 
	 */
	@Test
	public void testMsg_queue_size_2() throws InvalidTheoryException, MalformedGoalException, NoSolutionException {
		theory = "start(X, S) :- msg_queue_create('CODA'), thread_create(ID, thread1(X)), loop(1,5,1,invio('CODA', 'messaggio molto importante')), lettura(ID,X), msg_queue_size('CODA', S).\n" +
		"loop(I, To, Inc, Action) :- Inc >= 0, I > To, !.\n"+
		"loop(I, To, Inc, Action) :- Inc < 0,  I < To, !.\n"+
		"loop(I, To, Inc, Action) :- Action, Next is I+Inc, loop(Next, To, Inc, Action).\n"+
		"thread1(X) :- thread_wait_msg('CODA', a(X)). \n " +
		"invio(ID, M):- thread_send_msg(ID, a(M)). \n" +		//Versione con 'CODA'
		"lettura(ID, X):- thread_join(ID, thread1(X)). ";
		engine.setTheory(new Theory(theory));
		
		SolveInfo sinfo = engine.solve("start(X, S).");
		assertTrue(sinfo.isSuccess());
		
		Term X = sinfo.getVarValue("S");
		assertEquals(new Int(5), X);
	}

	/**
	 * Test method for {@link nars.tuprolog.lib.ThreadLibrary#mutex_destroy_1(alice.tuprolog.Term)}.
	 * @throws InvalidTheoryException 
	 * @throws MalformedGoalException 
	 */
	@Test
	public void testMutex_destroy_1() throws InvalidTheoryException, MalformedGoalException {
		theory = "start(M, X) :- mutex_create(M), mutex_lock(M), thread_create(ID, thread1(M)), thread_sleep(500), message_queue_create('CODA'), invio('CODA', 'messaggio molto importante'), lettura(ID, X). \n" +
		"thread1(M) :- mutex_destroy(M). \n" +
		"invio(Q, M):- thread_send_msg(Q, a(M)), mutex_unlock('mutex'). \n" +
		"lettura(ID, X):- thread_read(ID, thread1(X))."	;
		engine.setTheory(new Theory(theory));
		
		SolveInfo sinfo = engine.solve("start('mutex', X).");
		assertFalse(sinfo.isSuccess());
	}

	/**
	 * 
	 * @throws InvalidTheoryException 
	 * @throws MalformedGoalException 
	 */
	@Test
	public void testMutex_lock_unlock_1() throws InvalidTheoryException, MalformedGoalException {
		theory = "start(X) :- mutex_lock('mutex'), thread_create(ID, thread1(X)), msg_queue_create('CODA'), invio('CODA', 'messaggio molto importante'), lettura(ID,X). \n" +
		"thread1(X) :- mutex_lock('mutex'), thread_peek_msg('CODA', a(X)), mutex_unlock('mutex'). \n" +
		"invio(Q, M):- thread_send_msg(Q, a(M)), mutex_unlock('mutex'). \n" +
		"lettura(ID, X):- thread_read(ID, thread1(X))."	;
		engine.setTheory(new Theory(theory));
		
		SolveInfo sinfo = engine.solve("start(X).");
		assertTrue(sinfo.isSuccess());
	}

	/**
	 * Test method for {@link nars.tuprolog.lib.ThreadLibrary#mutex_trylock_1(alice.tuprolog.Term)}.
	 * @throws InvalidTheoryException 
	 * @throws MalformedGoalException 
	 */
	@Test
	public void testMutex_trylock_1() throws InvalidTheoryException, MalformedGoalException {
		theory = "start(X) :- mutex_lock('mutex'), thread_create(ID, thread1(X)), message_queue_create('CODA'), invio('CODA', 'messaggio molto importante'), lettura(ID,X). \n" +
		"thread1(X) :- mutex_trylock('mutex'), thread_peek_msg('CODA', a(X)), mutex_unlock('mutex'). \n" +
		"invio(Q, M):- thread_send_msg(Q, a(M)). \n" +
		"lettura(ID, X):- thread_read(ID, thread1(X))."	;
		engine.setTheory(new Theory(theory));
		
		SolveInfo sinfo = engine.solve("start(X).");
		assertFalse(sinfo.isSuccess());
	}

	/**
	 * Test method for {@link nars.tuprolog.lib.ThreadLibrary#mutex_unlock_all_0()}.
	 * @throws InvalidTheoryException 
	 * @throws MalformedGoalException 
	 */
	
	@Test
	public void testMutex_unlock_all_0() throws InvalidTheoryException, MalformedGoalException {
		theory = "start(X) :- thread_create(ID, thread1(X)), mutex_lock('mutex1'). \n" +
		"thread1(X, M1, M2) :- mutex_lock('mutex1'), mutex_lock('mutex2'), mutex_unlock_all." ;
		engine.setTheory(new Theory(theory));
		
		SolveInfo sinfo = engine.solve("start(X).");
		assertTrue(sinfo.isSuccess());
	}
	
	@Test
    public void testFattoriale() throws InvalidTheoryException, MalformedGoalException, NoSolutionException{
            theory = "start(N,X,M,Y):- thread_create(ID, fact1(N,X)), thread_join(ID, fact1(N,X)),thread_create(ID2, fact1(M,Y)), thread_join(ID2, fact1(M,Y)).\n" +
                            "fact1(0,1):-!.\n" +
                            "fact1(N,X):-M is N-1,fact1(M,Y),X is Y*N.";
            engine.setTheory(new Theory(theory));
            
            SolveInfo sinfo = engine.solve("start(7,X,8,Y).");
            assertTrue(sinfo.isSuccess());
            
            Term X = sinfo.getVarValue("X");
            assertEquals(new Int(5040), X);
            
            Term Y = sinfo.getVarValue("Y");
            assertEquals(new Int(40320), Y);
    }
	
	@Test 
	public void testMutex1() throws InvalidTheoryException, MalformedGoalException, NoSolutionException{
		theory = "start(X) :- thread_create(ID, genitore(bob,X)), mutex_lock('mutex'), thread_create(ID2, lettura(ID,X)), loop(1,3,1,ID),  mutex_unlock('mutex').\n" +
		"genitore(bob,c).\n" +
		"genitore(bob,gdh).\n" +
		"loop(I, To, Inc, ThreadId) :- Inc >= 0, I > To, !.\n" +
		"loop(I, To, Inc, ThreadId) :- Inc < 0,  I < To, !.\n" +
		"loop(I, To, Inc, ThreadId) :- (thread_has_next(ThreadId) -> thread_next_sol(ThreadId), Next is I+Inc, loop(Next, To, Inc, ThreadId); !).\n" +
		"lettura(ID, X):- mutex_lock('mutex'), thread_read(ID,X), mutex_unlock('mutex').";
		engine.setTheory(new Theory(theory));
		
		SolveInfo sinfo = engine.solve("start(X).");
		assertTrue(sinfo.isSuccess());
	}
	
	@Test
	public void testMutex2() throws InvalidTheoryException, MalformedGoalException, NoSolutionException{
		theory = "start :- thread_create(ID1, figlio(bob,X)), mutex_lock('mutex')," +
				"thread_create(ID2, lettura(ID1,X)), loop(1,5,1,ID1),  mutex_unlock('mutex').\n" +
				"loop(I, To, Inc, ThreadId) :- Inc >= 0, I > To, !.\n" +
				"loop(I, To, Inc, ThreadId) :- Inc < 0,  I < To, !.\n" +
				"loop(I, To, Inc, ThreadId) :- (thread_has_next(ThreadId) ->" +
				"thread_next_sol(ThreadId), Next is I+Inc, loop(Next, To, Inc, ThreadId); !).\n" +
				"lettura(ID, X) :- mutex_lock('mutex'), thread_read(ID,X)," +
				"mutex_unlock('mutex').\n" +
				"figlio(bob,alex).\n" +
				"figlio(bob,anna).\n" +
				"figlio(bob,maria).";
		engine.setTheory(new Theory(theory));
		
		SolveInfo sinfo = engine.solve("start.");
		assertTrue(sinfo.isSuccess());
		
	}
	
	@Test
	public void testMutex3() throws InvalidTheoryException, MalformedGoalException, NoSolutionException{
		theory = "start :- thread_create(ID1, figlio(bob,X))," +
				"loop(1,5,1,ID1), thread_create(ID2, lettura(ID1,X)).\n" +
				"loop(I, To, Inc, ThreadId) :- Inc >= 0, I > To, !.\n" +
				"loop(I, To, Inc, ThreadId) :- Inc < 0,  I < To, !.\n" +
				"loop(I, To, Inc, ThreadId) :- (thread_has_next(ThreadId) ->" +
				"thread_next_sol(ThreadId), Next is I+Inc, loop(Next, To, Inc, ThreadId); !).\n" +
				"lettura(ID, X) :- thread_read(ID,X).\n" +
				"figlio(bob,alex).\n" +
				"figlio(bob,anna).\n" +
				"figlio(bob,maria).";
		engine.setTheory(new Theory(theory));
		
		SolveInfo sinfo = engine.solve("start.");
		assertTrue(sinfo.isSuccess());
	}
	
	@Test
	public void concurrentTest1() throws InvalidTheoryException, MalformedGoalException, NoSolutionException{
		theory= "bubble(L1,L2) :- bubble(L1,0,L2).\n" +
				"bubble(L1,0,L2) :- sweep(L1,0,L2).\n" +
				"bubble(L1,0,L2) :- sweep(L1,1,LTMP),bubble(LTMP,0,L2).\n" +
				"sweep([X|[]],0,[X|[]]).\n" +
				"sweep([X,Y|REST1],CHANGED,[X|REST2]) :- X =< Y,sweep([Y|REST1],CHANGED,REST2).\n" +
				"sweep([X,Y|REST1],1,[Y|REST2]) :- X > Y,sweep([X|REST1],_,REST2).\n"+
				"plain(L1,L2) :- plain(L1,[],L2).\n" +
				"plain([],ACC,ACC).\n" +
				"plain([H|REST],ACC,L2) :- H = [_|_],plain(H,ACC,ACC1),plain(REST,ACC1,L2).\n" +
				"plain([H|REST],ACC,L2) :- append(ACC,[H],ACC1),plain(REST,ACC1,L2).\n" +
				"plain(X,ACC,L2) :- append(ACC,[X],L2).\n"+
				
				"ordina(L, N, T) :- thread_create(ID, firstResp(L, N)), secondResp(L, N, T).\n" +
				"secondResp(L, 0, T):- !.\n" +
				"secondResp([H|Tail], N, T) :- occorr(T,H,Count), C is N-1, secondResp(Tail,C, T).\n"+
				
				"firstResp(L, 0) :- !.\n"+
				"firstResp([H|Tail], N) :- plain(H,L_plain), bubble(L_plain,L_ord), C is N - 1, firstResp(Tail, C).\n"+
				"occorr(T,L,N) :- occorr(T,L,0,N).\n" +
				"occorr(_,[],ACC,ACC).\n" +
				"occorr(T,[T|REST],ACC,N) :-ACC1 is ACC+1,occorr(T,REST,ACC1,N).\n" +
				"occorr(T,[_|REST],ACC,N) :- occorr(T,REST,ACC,N).";
				engine.setTheory(new Theory(theory));
		
				SolveInfo sinfo = engine.solve("ordina([[[2,2],2,2,1],[4,[3],2],[9,8,9,2]],3, 2).");
				assertTrue(sinfo.isSuccess());
				
	}
	
	@Test
	public void concurrentTest2() throws InvalidTheoryException, MalformedGoalException, NoSolutionException{
		theory= "coppie(L,C,DX,SX):-thread_create(ID1, coppieDX(L,C,DX)), thread_create(ID2, coppieSX(L,C,SX)).\n" +
		
				"coppieDX([],_,[]).\n " +
				"coppieDX([[X,X]|T],X,[X|Td]) :- !,coppieDX(T,X,Td).\n " +
				"coppieDX([[X,Y]|T],X,[Y|Td]) :- !,coppieDX(T,X,Td).\n " +
				"coppieDX([_|T],X,Td) :- coppieDX(T,X,Td).\n" +
				
				"coppieSX([],_,[]).\n " +
				"coppieSX([[X,X]|T],X,[X|Ts]) :- !,coppieSX(T,X,Ts). \n" +
				"coppieSX([[Y,X]|T],X,[Y|Ts]) :- !,coppieSX(T,X,Ts). \n" +
				"coppieSX([_|T],X,Ts) :- coppieSX(T,X,Ts).";
				engine.setTheory(new Theory(theory));

				SolveInfo sinfo = engine.solve("coppie([[2,3],[5,2]], 2, DX,SX).");
				assertTrue(sinfo.isSuccess());
	}
	
	@Test
	public void concurrentTest3() throws InvalidTheoryException, MalformedGoalException, NoSolutionException{
		theory="study(L_stud, L_exams, N,Num) :- thread_create(ID, loop(1, N, 1, L_stud, L_exams)), length(L_exams,Num). \n"+
				
				"length([],0).\n"+
				"length([_|Queue],N):-length(Queue,Nqueue),N is Nqueue + 1.\n"+
			
				"loop(I, To, Inc, L_stud, L_exams) :- Inc >= 0, I > To, !.\n"+
				"loop(I, To, Inc, L_stud, L_exams) :- Inc < 0,  I < To, !.\n"+
				"loop(I, To, Inc, [H|Tail], L_exams) :- thread_create((totStud(H,L_exams,N,T), N > 0, AV is T/N),ID),  Next is I+Inc, loop(Next, To, Inc, Tail, L_exams).\n"+
				
				"totStud(_,[],0,0) :- !. \n" +
				"totStud(S,[exam(S,_,V)|R],N,T) :- !, totStud(S,R,NN,TT), N is NN + 1, T is TT + V. \n" +
				"totStud(S,[_|R],N,T) :- totStud(S,R,N,T).";
		
				engine.setTheory(new Theory(theory));
		
				SolveInfo sinfo = engine.solve("study([s1,s2,s3,s4,s5],[exam(s2,f1,30), exam(s1,f1,27), exam(s3,f1,25), exam(s1,f2,30),exam(s4,f1,25),exam(s3,f2,20),exam(s5,f1,20),exam(s2,f5,30), exam(s1,f5,27), exam(s3,f5,25), exam(s1,f4,30),exam(s4,f5,25),exam(s3,f8,20),exam(s5,f7,20)], 5,Num).");
				assertTrue(sinfo.isSuccess());			
	}

	
}
