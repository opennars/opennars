package nars.tuprolog;

import junit.framework.TestCase;

public class ParserTestCase extends TestCase {
	
	public void testReadingTerms() throws InvalidTermException {
		Parser p = new Parser("hello.");
		Struct result = new Struct("hello");
		assertEquals(result, p.nextTerm(true));
	}
	
	public void testReadingEOF() throws InvalidTermException {
		Parser p = new Parser("");
		assertNull(p.nextTerm(false));
	}
	
	public void testUnaryPlusOperator() {
		Parser p = new Parser("n(+100).\n");
        // SICStus Prolog interprets "n(+100)" as "n(100)"
		// GNU Prolog interprets "n(+100)" as "n(+(100))"
		// The ISO Standard says + is not a unary operate
		try {
			assertNotNull(p.nextTerm(true));
			fail();
		} catch (InvalidTermException e) {}
	}
	
	public void testUnaryMinusOperator() throws InvalidTermException {
		Parser p = new Parser("n(-100).\n");
		// TODO Check the interpretation by other engines
		// SICStus Prolog interprets "n(+100)" as "n(100)"
		// GNU Prolog interprets "n(+100)" as "n(+(100))"
		// What does the ISO Standard say about that?
		Struct result = new Struct("n", new Int(-100));
		result.resolveTerm();
		assertEquals(result, p.nextTerm(true));
	}
	
	public void testBinaryMinusOperator() throws InvalidTermException {
		String s = "abs(3-11)";
		Parser p = new Parser(s);
		Struct result = new Struct("abs", new Struct("-", new Int(3), new Int(11)));
		assertEquals(result, p.nextTerm(false));
	}
	
	public void testListWithTail() throws InvalidTermException {
		Parser p = new Parser("[p|Y]");
		Struct result = new Struct(new Struct("p"), new Var("Y"));
		result.resolveTerm();
		assertEquals(result, p.nextTerm(false));
	}
	
	public void testBraces() throws InvalidTermException {
		String s = "{a,b,[3,{4,c},5],{a,b}}";
		Parser parser = new Parser(s);
		assertEquals(s, parser.nextTerm(false).toString());
	}
	
	public void testUnivOperator() throws InvalidTermException {
		Parser p = new Parser("p =.. q.");
		Struct result = new Struct("=..", new Struct("p"), new Struct("q"));
		assertEquals(result, p.nextTerm(true));
	}
	
	public void testDotOperator() throws InvalidTermException {
		String s = "class('java.lang.Integer').'MAX_VALUE'";
		DefaultOperatorManager om = new DefaultOperatorManager();
		om.opNew(".", "xfx", 600);
		Parser p = new Parser(om, s);
		Struct result = new Struct(".", new Struct("class", new Struct("java.lang.Integer")),
				                        new Struct("MAX_VALUE"));
		assertEquals(result, p.nextTerm(false));
	}
	
	public void testBracketedOperatorAsTerm() throws InvalidTermException {
		String s = "u (b1) b2 (b3)";
		DefaultOperatorManager om = new DefaultOperatorManager();
		om.opNew("u", "fx", 200);
		om.opNew("b1", "yfx", 400);
		om.opNew("b2", "yfx", 500);
		om.opNew("b3", "yfx", 300);
		Parser p = new Parser(om, s);
		Struct result = new Struct("b2", new Struct("u", new Struct("b1")), new Struct("b3"));
		assertEquals(result, p.nextTerm(false));
	}
	
	public void testBracketedOperatorAsTerm2() throws InvalidTermException {
		String s = "(u) b1 (b2) b3 a";
		DefaultOperatorManager om = new DefaultOperatorManager();
		om.opNew("u", "fx", 200);
		om.opNew("b1", "yfx", 400);
		om.opNew("b2", "yfx", 500);
		om.opNew("b3", "yfx", 300);
		Parser p = new Parser(om, s);
		Struct result = new Struct("b1", new Struct("u"), new Struct("b3", new Struct("b2"), new Struct("a")));
		assertEquals(result, p.nextTerm(false));
	}
	
	public void testIntegerBinaryRepresentation() throws InvalidTermException {
		String n = "0b101101";
		Parser p = new Parser(n);
		nars.tuprolog.Number result = new Int(45);
		assertEquals(result, p.nextTerm(false));
		String invalid = "0b101201";
		try {
			new Parser(invalid).nextTerm(false);
			fail();
		} catch (InvalidTermException expected) {}
	}
	
	public void testIntegerOctalRepresentation() throws InvalidTermException {
		String n = "0o77351";
		Parser p = new Parser(n);
		nars.tuprolog.Number result = new Int(32489);
		assertEquals(result, p.nextTerm(false));
		String invalid = "0o78351";
		try {
			new Parser(invalid).nextTerm(false);
			fail();
		} catch (InvalidTermException expected) {}
	}
	
	public void testIntegerHexadecimalRepresentation() throws InvalidTermException {
		String n = "0xDECAF";
		Parser p = new Parser(n);
		nars.tuprolog.Number result = new Int(912559);
		assertEquals(result, p.nextTerm(false));
		String invalid = "0xG";
		try {
			new Parser(invalid).nextTerm(false);
			fail();
		} catch (InvalidTermException expected) {}
	}
	
	public void testEmptyDCGAction() throws InvalidTermException {
		String s = "{}";
		Parser p = new Parser(s);
		Struct result = new Struct("{}");
		assertEquals(result, p.nextTerm(false));
	}
	
	public void testSingleDCGAction() throws InvalidTermException {
		String s = "{hello}";
		Parser p = new Parser(s);
		Struct result = new Struct("{}", new Struct("hello"));
		assertEquals(result, p.nextTerm(false));
	}
	
	public void testMultipleDCGAction() throws InvalidTermException {
		String s = "{a, b, c}";
		Parser p = new Parser(s);
		Struct result = new Struct("{}",
                                   new Struct(",", new Struct("a"),
                                       new Struct(",", new Struct("b"), new Struct("c"))));
		assertEquals(result, p.nextTerm(false));
	}
	
	// This is an error both in 2.0.1 and in 2.1... don't know why, though.
//	public void testDCGActionWithOperators() throws Exception {
//        String input = "{A =.. B, hotel, 2}";
//        Struct result = new Struct("{}",
//                            new Struct(",", new Struct("=..", new Var("A"), new Var("B")),
//                                new Struct(",", new Struct("hotel"), new Int(2))));
//        result.resolveTerm();
//        Parser p = new Parser(input);
//        assertEquals(result, p.nextTerm(false));
//	}
	
	public void testMissingDCGActionElement() {
		String s = "{1, 2, , 4}";
		Parser p = new Parser(s);
		try {
			p.nextTerm(false);
			fail();
		} catch (InvalidTermException expected) {}
	}
	
	public void testDCGActionCommaAsAnotherSymbol() {
		String s = "{1 @ 2 @ 4}";
		Parser p = new Parser(s);
		try {
			p.nextTerm(false);
			fail();
		} catch (InvalidTermException expected) {}
	}
	
	public void testUncompleteDCGAction() {
		String s = "{1, 2,}";
		Parser p = new Parser(s);
		try {
			p.nextTerm(false);
			fail();
		} catch (InvalidTermException expected) {}
		
		s = "{1, 2";
		p = new Parser(s);
		try {
			p.nextTerm(false);
			fail();
		} catch (InvalidTermException expected) {}
	}

	public void testMultilineComments() throws InvalidTermException {
		String theory = "t1." + "\n" +
		                "/*" + "\n" +
		                "t2" + "\n" +
		                "*/" + "\n" +
		                "t3." + "\n";
		Parser p = new Parser(theory);
		assertEquals(new Struct("t1"), p.nextTerm(true));
		assertEquals(new Struct("t3"), p.nextTerm(true));
	}
	
	public void testSingleQuotedTermWithInvalidLineBreaks() {
		String s = "out('"+
		           "can_do(X).\n"+
		           "can_do(Y).\n"+
	               "').";
		Parser p = new Parser(s);
		try {
			p.nextTerm(true);
			fail();
		} catch (InvalidTermException expected) {}
	}
	
	// TODO More tests on Parser
	
	// Character code for Integer representation
	
	// :-op(500, yfx, v). 3v2 NOT CORRECT, 3 v 2 CORRECT
	// 3+2 CORRECT, 3 + 2 CORRECT
	
	// +(2, 3) is now acceptable
	// what about f(+)

}
