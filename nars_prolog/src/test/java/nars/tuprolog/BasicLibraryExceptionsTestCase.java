package nars.tuprolog;


import junit.framework.TestCase;

/**
 * @author Matteo Iuliani
 * 
 *         Test del funzionamento delle eccezioni lanciate dai predicati della
 *         BasicLibrary
 */
public class BasicLibraryExceptionsTestCase extends TestCase {

	// verifico che set_theory(X) lancia un errore di instanziazione
	public void test_set_theory_1_1() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(set_theory(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("set_theory", new Var("X"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
	}

	// verifico che set_theory(1) lancia un errore di tipo
	public void test_set_theory_1_2() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(set_theory(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("set_theory", new Int(1))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("atom")));
		Int culprit = (Int) info.getTerm("Culprit");
		assertTrue(culprit.intValue() == 1);
	}

	// verifico che set_theory(a) lancia un errore di sintassi
	public void test_set_theory_1_3() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(set_theory(a), error(syntax_error(Message), syntax_error(Goal, Line, Position, Message)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("set_theory", new Struct("a"))));
		Int line = (Int) info.getTerm("Line");
		assertEquals("line intValue", 1, line.intValue());
		Int position = (Int) info.getTerm("Line");
		assertEquals("position intValue", 1, position.intValue());
		Struct message = (Struct) info.getTerm("Message");
		assertTrue(message.isEqual(new Struct("The term 'a' is not ended with a period.")));
	}

	// verifico che add_theory(X) lancia un errore di instanziazione
	public void test_add_theory_1_1() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(add_theory(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("add_theory", new Var("X"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
	}

	// verifico che add_theory(1) lancia un errore di tipo
	public void test_add_theory_1_2() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(add_theory(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("add_theory", new Int(1))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("atom")));
		Int culprit = (Int) info.getTerm("Culprit");
		assertTrue(culprit.intValue() == 1);
	}

	// verifico che add_theory(a) lancia un errore di sintassi
	public void test_add_theory_1_3() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(add_theory(a), error(syntax_error(Message), syntax_error(Goal, Line, Position, Message)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("add_theory", new Struct("a"))));
		Int line = (Int) info.getTerm("Line");
		assertTrue(line.intValue() == 1);
		Int position = (Int) info.getTerm("Line");
		assertTrue(position.intValue() == 1);
		Struct message = (Struct) info.getTerm("Message");
		assertTrue(message.isEqual(new Struct("The term 'a' is not ended with a period.")));
	}

	// verifico che agent(X) lancia un errore di instanziazione
	public void test_agent_1_1() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(agent(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("agent", new Var("X"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
	}

	// verifico che agent(1) lancia un errore di tipo
	public void test_agent_1_2() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(agent(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("agent", new Int(1))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("atom")));
		Int culprit = (Int) info.getTerm("Culprit");
		assertTrue(culprit.intValue() == 1);
	}

	// verifico che agent(X, a) lancia un errore di instanziazione
	public void test_agent_2_1() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(agent(X, a), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g
				.isEqual(new Struct("agent", new Var("X"), new Struct("a"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
	}

	// verifico che agent(a, X) lancia un errore di instanziazione
	public void test_agent_2_2() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(agent(a, X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g
				.isEqual(new Struct("agent", new Struct("a"), new Var("X"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
	}

	// verifico che agent(1, a) lancia un errore di tipo
	public void test_agent_2_3() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(agent(1, a), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("agent", new Int(1), new Struct("a"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("atom")));
		Int culprit = (Int) info.getTerm("Culprit");
		assertTrue(culprit.intValue() == 1);
	}

	// verifico che agent(a, 1) lancia un errore di tipo
	public void test_agent_2_4() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(agent(a, 1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("agent", new Struct("a"), new Int(1))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("struct")));
		Int culprit = (Int) info.getTerm("Culprit");
		assertTrue(culprit.intValue() == 1);
	}

	// verifico che '=:='(X, 1) lancia un errore di instanziazione
	public void test_expression_comparison_2_1() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('=:='(X, 1), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_equality", new Var("X"),
				new Int(1))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
	}

	// verifico che '=:='(1, X) lancia un errore di instanziazione
	public void test_expression_comparison_2_2() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('=:='(1, X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_equality", new Int(1),
				new Var("X"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
	}

	// verifico che '=:='(a, 1) lancia un errore di tipo
	public void test_expression_comparison_2_3() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('=:='(a, 1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_equality", new Struct("a"),
				new Int(1))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("evaluable")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

	// verifico che '=:='(1, a) lancia un errore di tipo
	public void test_expression_comparison_2_4() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('=:='(1, a), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_equality", new Int(1),
				new Struct("a"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("evaluable")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

	// verifico che '=\='(X, 1) lancia un errore di instanziazione
	public void test_expression_comparison_2_5() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('=\\='(X, 1), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_equality", new Var("X"),
				new Int(1))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
	}

	// verifico che '=\='(1, X) lancia un errore di instanziazione
	public void test_expression_comparison_2_6() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('=\\='(1, X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_equality", new Int(1),
				new Var("X"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
	}

	// verifico che '=\='(a, 1) lancia un errore di tipo
	public void test_expression_comparison_2_7() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('=\\='(a, 1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_equality", new Struct("a"),
				new Int(1))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("evaluable")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

	// verifico che '=\='(1, a) lancia un errore di tipo
	public void test_expression_comparison_2_8() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('=\\='(1, a), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_equality", new Int(1),
				new Struct("a"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("evaluable")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

	// verifico che '>'(X, 1) lancia un errore di instanziazione
	public void test_expression_comparison_2_9() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('>'(X, 1), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_greater_than",
				new Var("X"), new Int(1))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
	}

	// verifico che '>'(1, X) lancia un errore di instanziazione
	public void test_expression_comparison_2_10() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('>'(1, X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_greater_than", new Int(1),
				new Var("X"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
	}

	// verifico che '>'(a, 1) lancia un errore di tipo
	public void test_expression_comparison_2_11() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('>'(a, 1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_greater_than", new Struct(
				"a"), new Int(1))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("evaluable")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

	// verifico che '>'(1, a) lancia un errore di tipo
	public void test_expression_comparison_2_12() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('>'(1, a), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_greater_than", new Int(1),
				new Struct("a"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("evaluable")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

	// verifico che '<'(X, 1) lancia un errore di instanziazione
	public void test_expression_comparison_2_13() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('<'(X, 1), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_less_than", new Var("X"),
				new Int(1))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
	}

	// verifico che '<'(1, X) lancia un errore di instanziazione
	public void test_expression_comparison_2_14() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('<'(1, X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_less_than", new Int(1),
				new Var("X"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
	}

	// verifico che '<'(a, 1) lancia un errore di tipo
	public void test_expression_comparison_2_15() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('<'(a, 1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_less_than",
				new Struct("a"), new Int(1))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("evaluable")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

	// verifico che '<'(1, a) lancia un errore di tipo
	public void test_expression_comparison_2_16() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('<'(1, a), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_less_than", new Int(1),
				new Struct("a"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("evaluable")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

	// verifico che '>='(X, 1) lancia un errore di instanziazione
	public void test_expression_comparison_2_17() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('>='(X, 1), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_greater_or_equal_than",
				new Var("X"), new Int(1))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
	}

	// verifico che '>='(1, X) lancia un errore di instanziazione
	public void test_expression_comparison_2_18() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('>='(1, X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_greater_or_equal_than",
				new Int(1), new Var("X"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
	}

	// verifico che '>='(a, 1) lancia un errore di tipo
	public void test_expression_comparison_2_19() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('>='(a, 1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_greater_or_equal_than",
				new Struct("a"), new Int(1))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("evaluable")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

	// verifico che '>='(1, a) lancia un errore di tipo
	public void test_expression_comparison_2_20() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('>='(1, a), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_greater_or_equal_than",
				new Int(1), new Struct("a"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("evaluable")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

	// verifico che '=<'(X, 1) lancia un errore di instanziazione
	public void test_expression_comparison_2_21() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('=<'(X, 1), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_less_or_equal_than",
				new Var("X"), new Int(1))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
	}

	// verifico che '=<'(1, X) lancia un errore di instanziazione
	public void test_expression_comparison_2_22() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('=<'(1, X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_less_or_equal_than",
				new Int(1), new Var("X"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
	}

	// verifico che '=<'(a, 1) lancia un errore di tipo
	public void test_expression_comparison_2_23() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('=<'(a, 1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_less_or_equal_than",
				new Struct("a"), new Int(1))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("evaluable")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

	// verifico che '=<'(1, a) lancia un errore di tipo
	public void test_expression_comparison_2_24() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('=<'(1, a), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_less_or_equal_than",
				new Int(1), new Struct("a"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("evaluable")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

	// verifico che '=:='(1, 1/0) lancia l'errore di valutazione "zero_divisor"
	public void test_expression_comparison_2_25() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('=:='(1, 1/0), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_equality", new Int(1),
				new Struct("/", new Int(1), new Int(0)))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
		Struct validType = (Struct) info.getTerm("Error");
		assertTrue(validType.isEqual(new Struct("zero_divisor")));
	}

	// verifico che '=\='(1, 1/0) lancia l'errore di valutazione "zero_divisor"
	public void test_expression_comparison_2_26() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('=\\='(1, 1/0), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_equality", new Int(1),
				new Struct("/", new Int(1), new Int(0)))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
		Struct validType = (Struct) info.getTerm("Error");
		assertTrue(validType.isEqual(new Struct("zero_divisor")));
	}

	// verifico che '>'(1, 1/0) lancia l'errore di valutazione "zero_divisor"
	public void test_expression_comparison_2_27() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('>'(1, 1/0), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_greater_than", new Int(1),
				new Struct("/", new Int(1), new Int(0)))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
		Struct validType = (Struct) info.getTerm("Error");
		assertTrue(validType.isEqual(new Struct("zero_divisor")));
	}

	// verifico che '<'(1, 1/0) lancia l'errore di valutazione "zero_divisor"
	public void test_expression_comparison_2_28() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('<'(1, 1/0), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_less_than", new Int(1),
				new Struct("/", new Int(1), new Int(0)))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
		Struct validType = (Struct) info.getTerm("Error");
		assertTrue(validType.isEqual(new Struct("zero_divisor")));
	}

	// verifico che '>='(1, 1/0) lancia l'errore di valutazione "zero_divisor"
	public void test_expression_comparison_2_29() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('>='(1, 1/0), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_greater_or_equal_than",
				new Int(1), new Struct("/", new Int(1), new Int(0)))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
		Struct validType = (Struct) info.getTerm("Error");
		assertTrue(validType.isEqual(new Struct("zero_divisor")));
	}

	// verifico che '=<'(1, 1/0) lancia l'errore di valutazione "zero_divisor"
	public void test_expression_comparison_2_30() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('=<'(1, 1/0), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_less_or_equal_than",
				new Int(1), new Struct("/", new Int(1), new Int(0)))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
		Struct validType = (Struct) info.getTerm("Error");
		assertTrue(validType.isEqual(new Struct("zero_divisor")));
	}

	// verifico che '=:='(1, 1//0) lancia l'errore di valutazione "zero_divisor"
	public void test_expression_comparison_2_31() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('=:='(1, 1//0), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_equality", new Int(1),
				new Struct("//", new Int(1), new Int(0)))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
		Struct validType = (Struct) info.getTerm("Error");
		assertTrue(validType.isEqual(new Struct("zero_divisor")));
	}

	// verifico che '=\='(1, 1//0) lancia l'errore di valutazione "zero_divisor"
	public void test_expression_comparison_2_32() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('=\\='(1, 1//0), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_equality", new Int(1),
				new Struct("//", new Int(1), new Int(0)))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
		Struct validType = (Struct) info.getTerm("Error");
		assertTrue(validType.isEqual(new Struct("zero_divisor")));
	}

	// verifico che '>'(1, 1//0) lancia l'errore di valutazione "zero_divisor"
	public void test_expression_comparison_2_33() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('>'(1, 1//0), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_greater_than", new Int(1),
				new Struct("//", new Int(1), new Int(0)))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
		Struct validType = (Struct) info.getTerm("Error");
		assertTrue(validType.isEqual(new Struct("zero_divisor")));
	}

	// verifico che '<'(1, 1//0) lancia l'errore di valutazione "zero_divisor"
	public void test_expression_comparison_2_34() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('<'(1, 1//0), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_less_than", new Int(1),
				new Struct("//", new Int(1), new Int(0)))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
		Struct validType = (Struct) info.getTerm("Error");
		assertTrue(validType.isEqual(new Struct("zero_divisor")));
	}

	// verifico che '>='(1, 1//0) lancia l'errore di valutazione "zero_divisor"
	public void test_expression_comparison_2_35() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('>='(1, 1//0), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_greater_or_equal_than",
				new Int(1), new Struct("//", new Int(1), new Int(0)))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
		Struct validType = (Struct) info.getTerm("Error");
		assertTrue(validType.isEqual(new Struct("zero_divisor")));
	}

	// verifico che '=<'(1, 1//0) lancia l'errore di valutazione "zero_divisor"
	public void test_expression_comparison_2_36() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('=<'(1, 1//0), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_less_or_equal_than",
				new Int(1), new Struct("//", new Int(1), new Int(0)))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
		Struct validType = (Struct) info.getTerm("Error");
		assertTrue(validType.isEqual(new Struct("zero_divisor")));
	}

	// verifico che '=:='(1 div 0, 1) lancia l'errore di valutazione
	// "zero_divisor"
	public void test_expression_comparison_2_37() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('=:='(1 div 0, 1), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_equality", new Struct(
				"div", new Int(1), new Int(0)), new Int(1))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
		Struct validType = (Struct) info.getTerm("Error");
		assertTrue(validType.isEqual(new Struct("zero_divisor")));
	}

	// verifico che '=\='(1 div 0, 1) lancia l'errore di valutazione
	// "zero_divisor"
	public void test_expression_comparison_2_38() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('=\\='(1 div 0, 1), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_equality", new Struct(
				"div", new Int(1), new Int(0)), new Int(1))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
		Struct validType = (Struct) info.getTerm("Error");
		assertTrue(validType.isEqual(new Struct("zero_divisor")));
	}

	// verifico che '>'(1 div 0, 1) lancia l'errore di valutazione
	// "zero_divisor"
	public void test_expression_comparison_2_39() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('>'(1 div 0, 1), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_greater_than", new Struct(
				"div", new Int(1), new Int(0)), new Int(1))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
		Struct validType = (Struct) info.getTerm("Error");
		assertTrue(validType.isEqual(new Struct("zero_divisor")));
	}

	// verifico che '<'(1 div 0, 1) lancia l'errore di valutazione
	// "zero_divisor"
	public void test_expression_comparison_2_40() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('<'(1 div 0, 1), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_less_than", new Struct(
				"div", new Int(1), new Int(0)), new Int(1))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
		Struct validType = (Struct) info.getTerm("Error");
		assertTrue(validType.isEqual(new Struct("zero_divisor")));
	}

	// verifico che '>='(1 div 0, 1) lancia l'errore di valutazione
	// "zero_divisor"
	public void test_expression_comparison_2_41() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('>='(1 div 0, 1), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_greater_or_equal_than",
				new Struct("div", new Int(1), new Int(0)), new Int(1))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
		Struct validType = (Struct) info.getTerm("Error");
		assertTrue(validType.isEqual(new Struct("zero_divisor")));
	}

	// verifico che '=<'(1 div 0, 1) lancia l'errore di valutazione
	// "zero_divisor"
	public void test_expression_comparison_2_42() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('=<'(1 div 0, 1), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("expression_less_or_equal_than",
				new Struct("div", new Int(1), new Int(0)), new Int(1))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
		Struct validType = (Struct) info.getTerm("Error");
		assertTrue(validType.isEqual(new Struct("zero_divisor")));
	}

	// verifico che text_concat(X, a, b) lancia un errore di instanziazione
	public void test_text_concat_3_1() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(text_concat(X, a, b), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("text_concat", new Var("X"),
				new Struct("a"), new Struct("b"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
	}

	// verifico che text_concat(a, X, b) lancia un errore di instanziazione
	public void test_text_concat_3_2() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(text_concat(a, X, b), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("text_concat", new Struct("a"),
				new Var("X"), new Struct("b"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
	}

	// verifico che text_concat(1, a, b) lancia un errore di tipo
	public void test_text_concat_3_3() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(text_concat(1, a, b), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("text_concat", new Int(1), new Struct(
				"a"), new Struct("b"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("atom")));
		Int culprit = (Int) info.getTerm("Culprit");
		assertTrue(culprit.intValue() == 1);
	}

	// verifico che text_concat(a, 1, b) lancia un errore di tipo
	public void test_text_concat_3_4() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(text_concat(a, 1, b), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("text_concat", new Struct("a"),
				new Int(1), new Struct("b"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("atom")));
		Int culprit = (Int) info.getTerm("Culprit");
		assertTrue(culprit.intValue() == 1);
	}

	// verifico che num_atom(a, X) lancia un errore di tipo
	public void test_num_atom_2_1() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(num_atom(a, X), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("num_atom", new Struct("a"), new Var(
				"X"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("number")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

	// verifico che num_atom(1, 1) lancia un errore di tipo
	public void test_num_atom_2_2() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(num_atom(1, 1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("num_atom", new Int(1), new Int(1))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("atom")));
		Int culprit = (Int) info.getTerm("Culprit");
		assertTrue(culprit.intValue() == 1);
	}

	// verifico che num_atom(1, a) lancia un errore di dominio
	public void test_num_atom_2_3() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(num_atom(1, a), error(domain_error(ValidDomain, Culprit), domain_error(Goal, ArgNo, ValidDomain, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g
				.isEqual(new Struct("num_atom", new Int(1), new Struct("a"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
		Struct validDomain = (Struct) info.getTerm("ValidDomain");
		assertTrue(validDomain.isEqual(new Struct("num_atom")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

	// verifico che arg(X, p(1), 1) lancia un errore di instanziazione
	public void test_arg_3_1() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(arg(X, p(1), 1), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("arg_guard", new Var("X"), new Struct(
				"p", new Int(1)), new Int(1))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
	}

	// verifico che arg(1, X, 1) lancia un errore di instanziazione
	public void test_arg_3_2() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(arg(1, X, 1), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("arg_guard", new Int(1), new Var("X"),
				new Int(1))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
	}

	// verifico che arg(a, p(1), 1) lancia un errore di tipo
	public void test_arg_3_3() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(arg(a, p(1), 1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("arg_guard", new Struct("a"),
				new Struct("p", new Int(1)), new Int(1))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("integer")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

	// verifico che arg(1, p, 1) lancia un errore di tipo
	public void test_arg_3_4() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(arg(1, p, 1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("arg_guard", new Int(1),
				new Struct("p"), new Int(1))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("compound")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("p")));
	}

	// verifico che arg(0, p(0), 1) lancia un errore di dominio
	public void test_arg_3_5() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(arg(0, p(0), 1), error(domain_error(ValidDomain, Culprit), domain_error(Goal, ArgNo, ValidDomain, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("arg_guard", new Int(0), new Struct(
				"p", new Int(0)), new Int(1))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
		Struct validType = (Struct) info.getTerm("ValidDomain");
		assertTrue(validType.isEqual(new Struct("greater_than_zero")));
		Int culprit = (Int) info.getTerm("Culprit");
		assertTrue(culprit.intValue() == 0);
	}

	// verifico che clause(X, true) lancia un errore di instanziazione
	public void test_clause_2_1() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(clause(X, true), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("clause_guard", new Var("X"),
				new Struct("true"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
	}

	// verifico che call(X) lancia un errore di instanziazione
	public void test_call_1_1() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(call(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("call_guard", new Var("X"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
	}

	// verifico che call(1) lancia un errore di tipo
	public void test_call_1_2() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(call(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("call_guard", new Int(1))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("callable")));
		Int culprit = (Int) info.getTerm("Culprit");
		assertTrue(culprit.intValue() == 1);
	}

	// verifico che findall(a, X, L) lancia un errore di instanziazione
	public void test_findall_3_1() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(findall(a, X, L), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("all_solutions_predicates_guard",
				new Struct("a"), new Var("X"), new Var("L"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
	}

	// verifico che findall(a, 1, L) lancia un errore di tipo
	public void test_findall_3_2() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(findall(a, 1, L), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("all_solutions_predicates_guard",
				new Struct("a"), new Int(1), new Var("L"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("callable")));
		Int culprit = (Int) info.getTerm("Culprit");
		assertTrue(culprit.intValue() == 1);
	}

	// verifico che setof(a, X, L) lancia un errore di instanziazione
	public void test_setof_3_1() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(setof(a, X, L), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("all_solutions_predicates_guard",
				new Struct("a"), new Var("X"), new Var("L"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
	}

	// verifico che setof(a, 1, L) lancia un errore di tipo
	public void test_setof_3_2() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(setof(a, 1, L), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("all_solutions_predicates_guard",
				new Struct("a"), new Int(1), new Var("L"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("callable")));
		Int culprit = (Int) info.getTerm("Culprit");
		assertTrue(culprit.intValue() == 1);
	}

	// verifico che bagof(a, X, L) lancia un errore di instanziazione
	public void test_bagof_3_1() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(bagof(a, X, L), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("all_solutions_predicates_guard",
				new Struct("a"), new Var("X"), new Var("L"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
	}

	// verifico che bagof(a, 1, L) lancia un errore di tipo
	public void test_bagof_3_2() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(bagof(a, 1, L), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("all_solutions_predicates_guard",
				new Struct("a"), new Int(1), new Var("L"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("callable")));
		Int culprit = (Int) info.getTerm("Culprit");
		assertTrue(culprit.intValue() == 1);
	}

	// verifico che assert(X) lancia un errore di instanziazione
	public void test_assert_1_1() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(assert(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("assertz", new Var("X"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
	}

	// verifico che assert(1) lancia un errore di tipo
	public void test_assert_1_2() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(assert(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("assertz", new Int(1))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("clause")));
		Int culprit = (Int) info.getTerm("Culprit");
		assertTrue(culprit.intValue() == 1);
	}

	// verifico che retract(X) lancia un errore di instanziazione
	public void test_retract_1_1() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(retract(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("retract_guard", new Var("X"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
	}

	// verifico che retract(1) lancia un errore di tipo
	public void test_retract_1_2() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(retract(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("retract_guard", new Int(1))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("clause")));
		Int culprit = (Int) info.getTerm("Culprit");
		assertTrue(culprit.intValue() == 1);
	}

	// verifico che retractall(X) lancia un errore di instanziazione
	public void test_retractall_1_1() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(retractall(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("retract_guard", new Var("X"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
	}

	// verifico che retractall(1) lancia un errore di tipo
	public void test_retractall_1_2() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(retractall(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("retract_guard", new Int(1))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("clause")));
		Int culprit = (Int) info.getTerm("Culprit");
		assertTrue(culprit.intValue() == 1);
	}

	// verifico che member(a, 1) lancia un errore di tipo
	public void test_member_2_1() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(member(a, 1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("member_guard", new Struct("a"),
				new Int(1))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("list")));
		Int culprit = (Int) info.getTerm("Culprit");
		assertTrue(culprit.intValue() == 1);
	}

	// verifico che reverse(a, []) lancia un errore di tipo
	public void test_reverse_2_1() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(reverse(a, []), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("reverse_guard", new Struct("a"),
				new Struct())));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("list")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

	// verifico che delete(a, a, []) lancia un errore di tipo
	public void test_delete_3_1() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(delete(a, a, []), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("delete_guard", new Struct("a"),
				new Struct("a"), new Struct())));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("list")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

	// verifico che element(1, a, a) lancia un errore di tipo
	public void test_element_3_1() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(element(1, a, a), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("element_guard", new Int(1),
				new Struct("a"), new Struct("a"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("list")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

}