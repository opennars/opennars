package prolog;

import junit.framework.TestCase;
import nars.prolog.Int;
import nars.prolog.Prolog;
import nars.prolog.SolveInfo;
import nars.prolog.Struct;
import nars.prolog.Term;
import nars.prolog.Var;

public class SolveInfoTestCase extends TestCase {

	public void testGetSubsequentQuery() {
		Prolog engine = new Prolog();
		Term query = new Struct("is", new Var("X"), new Struct("+", new Int(1), new Int(2)));
		SolveInfo result = engine.solve(query);
		assertTrue(result.isSuccess());
		assertEquals(query, result.getQuery());
		query = new Struct("functor", new Struct("p"), new Var("Name"), new Var("Arity"));
		result = engine.solve(query);
		assertTrue(result.isSuccess());
		assertEquals(query, result.getQuery());
	}

}
