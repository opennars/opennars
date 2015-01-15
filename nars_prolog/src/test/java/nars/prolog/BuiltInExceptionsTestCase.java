package nars.prolog;

import junit.framework.TestCase;

/**
 * @author Matteo Iuliani
 * 
 *         Test del funzionamento delle eccezioni lanciate dai predicati della
 *         classe BuiltIn
 */
public class BuiltInExceptionsTestCase extends TestCase {

	// verifico che asserta(X) lancia un errore di instanziazione
	public void test_asserta_1_1() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(asserta(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("asserta", new Var("X"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
	}

	// verifico che asserta(1) lancia un errore di tipo
	public void test_asserta_1_2() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(asserta(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("asserta", new Int(1))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("clause")));
		Int culprit = (Int) info.getTerm("Culprit");
		assertTrue(culprit.intValue() == 1);
	}

	// verifico che assertz(X) lancia un errore di instanziazione
	public void test_assertz_1_1() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(assertz(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("assertz", new Var("X"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
	}

	// verifico che assertz(1) lancia un errore di tipo
	public void test_assertz_1_2() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(assertz(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
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

	// verifico che '$retract'(X) lancia un errore di instanziazione
	public void test_$retract_1_1() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('$retract'(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("$retract", new Var("X"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
	}

	// verifico che '$retract'(1) lancia un errore di tipo
	public void test_$retract_1_2() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('$retract'(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("$retract", new Int(1))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("clause")));
		Int culprit = (Int) info.getTerm("Culprit");
		assertTrue(culprit.intValue() == 1);
	}

	// verifico che abolish(X) lancia un errore di instanziazione
	public void test_abolish_1_1() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(abolish(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("abolish", new Var("X"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
	}

	// verifico che abolish(1) lancia un errore di tipo
	public void test_abolish_1_2() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(abolish(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("abolish", new Int(1))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("predicate_indicator")));
		Int culprit = (Int) info.getTerm("Culprit");
		assertTrue(culprit.intValue() == 1);
	}

	// verifico che abolish(p(X)) lancia un errore di tipo
	public void test_abolish_1_3() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(abolish(p(X)), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("abolish",
				new Struct("p", new Var("X")))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("predicate_indicator")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("p", new Var("X"))));
	}

	// verifico che halt(X) lancia un errore di instanziazione
	public void test_halt_1_1() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(halt(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("halt", new Var("X"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
	}

	// verifico che halt(1.5) lancia un errore di tipo
	public void test_halt_1_2() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(halt(1.5), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("halt", new Double(1.5))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("integer")));
		Double culprit = (Double) info.getTerm("Culprit");
		assertTrue(culprit.doubleValue() == 1.5);
	}

	// verifico che load_library(X) lancia un errore di instanziazione
	public void test_load_library_1_1() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(load_library(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("load_library", new Var("X"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
	}

	// verifico che load_library(1) lancia un errore di tipo
	public void test_load_library_1_2() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(load_library(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("load_library", new Int(1))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("atom")));
		Int culprit = (Int) info.getTerm("Culprit");
		assertTrue(culprit.intValue() == 1);
	}

	// verifico che load_library_1 lancia un errore di esistenza se la libreria
	// LibraryName non esiste
	public void test_load_library_1_3() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(load_library('a'), error(existence_error(ObjectType, Culprit), existence_error(Goal, ArgNo, ObjectType, Culprit, Message)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("load_library", new Struct("a"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
		Struct validType = (Struct) info.getTerm("ObjectType");
		assertTrue(validType.isEqual(new Struct("class")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
		Term message = info.getTerm("Message");
		assertTrue(message.isEqual(new Struct("InvalidLibraryException: a at -1:-1")));
	}

	// verifico che unload_library(X) lancia un errore di instanziazione
	public void test_unload_library_1_1() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(unload_library(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("unload_library", new Var("X"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
	}

	// verifico che unload_library(1) lancia un errore di tipo
	public void test_unload_library_1_2() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(unload_library(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("unload_library", new Int(1))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("atom")));
		Int culprit = (Int) info.getTerm("Culprit");
		assertTrue(culprit.intValue() == 1);
	}

	// verifico che unload_library(LibraryName) lancia un errore di esistenza se
	// la libreria LibraryName non esiste
	public void test_unload_library_1_3() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(unload_library('a'), error(existence_error(ObjectType, Culprit), existence_error(Goal, ArgNo, ObjectType, Culprit, Message)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("unload_library", new Struct("a"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
		Struct validType = (Struct) info.getTerm("ObjectType");
		assertTrue(validType.isEqual(new Struct("class")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
		Term message = info.getTerm("Message");
		assertTrue(message.isEqual(new Struct("InvalidLibraryException: null at 0:0")));
	}

	// verifico che '$call'(X) lancia un errore di instanziazione
	public void test_$call_1_1() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('$call'(X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("$call", new Var("X"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
	}

	// verifico che '$call'(1) lancia un errore di tipo
	public void test_$call_1_2() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('$call'(1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("$call", new Int(1))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("callable")));
		Int culprit = (Int) info.getTerm("Culprit");
		assertTrue(culprit.intValue() == 1);
	}

	// verifico che is(X, Y) lancia un errore di instanziazione
	public void test_is_2_1() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(is(X, Y), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("is", new Var("X"), new Var("Y"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
	}

	// verifico che is(X, a) lancia un errore di tipo
	public void test_is_2_2() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(is(X, a), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("is", new Var("X"), new Struct("a"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("evaluable")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}
	
	// verifico che is(X, 1/0) lancia l'errore di valutazione "zero_divisor"
	public void test_is_2_3() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(is(X, 1/0), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("is", new Var("X"), new Struct("/", new Int(1), new Int(0)))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
		Struct error = (Struct) info.getTerm("Error");
		assertTrue(error.isEqual(new Struct("zero_divisor")));
	}
	
	// verifico che is(X, 1//0) lancia l'errore di valutazione "zero_divisor"
	public void test_is_2_4() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(is(X, 1//0), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("is", new Var("X"), new Struct("//", new Int(1), new Int(0)))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
		Struct error = (Struct) info.getTerm("Error");
		assertTrue(error.isEqual(new Struct("zero_divisor")));
	}
	
	// verifico che is(X, 1 div 0) lancia l'errore di valutazione "zero_divisor"
	public void test_is_2_5() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(is(X, 1 div 0), error(evaluation_error(Error), evaluation_error(Goal, ArgNo, Error)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("is", new Var("X"), new Struct("div", new Int(1), new Int(0)))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
		Struct error = (Struct) info.getTerm("Error");
		assertTrue(error.isEqual(new Struct("zero_divisor")));
	}
	
	// verifico che '$tolist'(X, List) lancia un errore di instanziazione
	public void test_$tolist_2_1() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('$tolist'(X, List), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("$tolist", new Var("X"),
				new Var("List"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
	}

	// verifico che '$tolist'(1, List) lancia un errore di tipo
	public void test_$tolist_2_2() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('$tolist'(1, List), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g
				.isEqual(new Struct("$tolist", new Int(1), new Var("List"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("struct")));
		Int culprit = (Int) info.getTerm("Culprit");
		assertTrue(culprit.intValue() == 1);
	}

	// verifico che '$fromlist'(Struct, X) lancia un errore di instanziazione
	public void test_$fromlist_2_1() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('$fromlist'(Struct, X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("$fromlist", new Var("Struct"),
				new Var("X"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
	}

	// verifico che '$fromlist'(Struct, a) lancia un errore di tipo
	public void test_$fromlist_2_2() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('$fromlist'(Struct, a), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("$fromlist", new Var("Struct"),
				new Struct("a"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("list")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

	// verifico che '$append'(a, X) lancia un errore di instanziazione
	public void test_$append_2_1() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('$append'(a, X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("$append", new Struct("a"),
				new Var("X"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
	}

	// verifico che '$append'(a, b) lancia un errore di tipo
	public void test_$append_2_2() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('$append'(a, b), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("$append", new Struct("a"), new Struct(
				"b"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("list")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("b")));
	}

	// verifico che '$find'(X, []) lancia un errore di instanziazione
	public void test_$find_2_1() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('$find'(X, []), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("$find", new Var("X"), new Struct())));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
	}

	// verifico che '$find'(p(X), a) lancia un errore di tipo
	public void test_$find_2_2() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('$find'(p(X), a), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("$find", new Struct("p", new Var("X")),
				new Struct("a"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("list")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

	// verifico che set_prolog_flag(X, 1) lancia un errore di instanziazione
	public void test_set_prolog_flag_2_1() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(set_prolog_flag(X, 1), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("set_prolog_flag", new Var("X"),
				new Int(1))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
	}

	// verifico che set_prolog_flag(a, X) lancia un errore di instanziazione
	public void test_set_prolog_flag_2_2() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(set_prolog_flag(a, X), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("set_prolog_flag", new Struct("a"),
				new Var("X"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
	}

	// verifico che set_prolog_flag(1, 1) lancia un errore di tipo
	public void test_set_prolog_flag_2_3() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(set_prolog_flag(1, 1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("set_prolog_flag", new Int(1), new Int(
				1))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("struct")));
		Int culprit = (Int) info.getTerm("Culprit");
		assertTrue(culprit.intValue() == 1);
	}

	// verifico che set_prolog_flag(a, p(X)) lancia un errore di tipo
	public void test_set_prolog_flag_2_4() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(set_prolog_flag(a, p(X)), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("set_prolog_flag", new Struct("a"),
				new Struct("p", new Var("X")))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("ground")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("p", new Var("X"))));
	}

	// verifico che set_prolog_flag(Flag, Value) lancia un errore di dominio se
	// il Flag non e' definito nel motore
	public void test_set_prolog_flag_2_5() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(set_prolog_flag(a, 1), error(domain_error(ValidDomain, Culprit), domain_error(Goal, ArgNo, ValidDomain, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("set_prolog_flag", new Struct("a"),
				new Int(1))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
		Struct validDomain = (Struct) info.getTerm("ValidDomain");
		assertTrue(validDomain.isEqual(new Struct("prolog_flag")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

	// verifico che set_prolog_flag(bounded, a) lancia un errore di dominio
	public void test_set_prolog_flag_2_6() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(set_prolog_flag(bounded, a), error(domain_error(ValidDomain, Culprit), domain_error(Goal, ArgNo, ValidDomain, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("set_prolog_flag",
				new Struct("bounded"), new Struct("a"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
		Struct validDomain = (Struct) info.getTerm("ValidDomain");
		assertTrue(validDomain.isEqual(new Struct("flag_value")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

	// verifico che set_prolog_flag(bounded, false) lancia un errore di permesso
	public void test_set_prolog_flag_2_7() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(set_prolog_flag(bounded, false), error(permission_error(Operation, ObjectType, Culprit), permission_error(Goal, Operation, ObjectType, Culprit, Message)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("set_prolog_flag",
				new Struct("bounded"), new Struct("false"))));
		Struct operation = (Struct) info.getTerm("Operation");
		assertTrue(operation.isEqual(new Struct("modify")));
		Struct objectType = (Struct) info.getTerm("ObjectType");
		assertTrue(objectType.isEqual(new Struct("flag")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("bounded")));
		Term message = info.getTerm("Message");
		assertTrue(message.isEqual(new Int(0)));
	}

	// verifico che get_prolog_flag(X, Value) lancia un errore di instanziazione
	public void test_get_prolog_flag_2_1() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(get_prolog_flag(X, Value), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("get_prolog_flag", new Var("X"),
				new Var("Value"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
	}

	// verifico che get_prolog_flag(1, Value) lancia un errore di tipo
	public void test_get_prolog_flag_2_2() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(get_prolog_flag(1, Value), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("get_prolog_flag", new Int(1), new Var(
				"Value"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("struct")));
		Int culprit = (Int) info.getTerm("Culprit");
		assertTrue(culprit.intValue() == 1);
	}

	// verifico che get_prolog_flag(Flag, Value) lancia un errore di dominio se
	// il Flag non e' definito nel motore
	public void test_get_prolog_flag_2_3() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch(get_prolog_flag(a, Value), error(domain_error(ValidDomain, Culprit), domain_error(Goal, ArgNo, ValidDomain, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("get_prolog_flag", new Struct("a"),
				new Var("Value"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
		Struct validDomain = (Struct) info.getTerm("ValidDomain");
		assertTrue(validDomain.isEqual(new Struct("prolog_flag")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

	// verifico che '$op'(Priority, yfx, '+') lancia un errore di instanziazione
	public void test_$op_3_1() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('$op'(Priority, yfx, '+'), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("$op", new Var("Priority"), new Struct(
				"yfx"), new Struct("+"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
	}

	// verifico che '$op'(600, Specifier, '+') lancia un errore di
	// instanziazione
	public void test_$op_3_2() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('$op'(600, Specifier, '+'), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("$op", new Int(600), new Var(
				"Specifier"), new Struct("+"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
	}

	// verifico che '$op'(600, yfx, Operator) lancia un errore di instanziazione
	public void test_$op_3_3() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('$op'(600, yfx, Operator), error(instantiation_error, instantiation_error(Goal, ArgNo)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("$op", new Int(600), new Struct("yfx"),
				new Var("Operator"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 3);
	}

	// verifico che '$op'(a, yfx, '+') lancia un errore di tipo
	public void test_$op_3_4() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('$op'(a, yfx, '+'), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("$op", new Struct("a"), new Struct(
				"yfx"), new Struct("+"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("integer")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

	// verifico che '$op'(600, 1, '+') lancia un errore di tipo
	public void test_$op_3_5() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('$op'(600, 1, '+'), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("$op", new Int(600), new Int(1),
				new Struct("+"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("atom")));
		Int culprit = (Int) info.getTerm("Culprit");
		assertTrue(culprit.intValue() == 1);
	}

	// verifico che '$op'(600, yfx, 1) lancia un errore di tipo
	public void test_$op_3_6() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('$op'(600, yfx, 1), error(type_error(ValidType, Culprit), type_error(Goal, ArgNo, ValidType, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("$op", new Int(600), new Struct("yfx"),
				new Int(1))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 3);
		Struct validType = (Struct) info.getTerm("ValidType");
		assertTrue(validType.isEqual(new Struct("atom_or_atom_list")));
		Int culprit = (Int) info.getTerm("Culprit");
		assertTrue(culprit.intValue() == 1);
	}

	// verifico che '$op'(1300, yfx, '+') lancia un errore di dominio
	public void test_$op_3_7() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('$op'(1300, yfx, '+'), error(domain_error(ValidDomain, Culprit), domain_error(Goal, ArgNo, ValidDomain, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("$op", new Int(1300),
				new Struct("yfx"), new Struct("+"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 1);
		Struct validDomain = (Struct) info.getTerm("ValidDomain");
		assertTrue(validDomain.isEqual(new Struct("operator_priority")));
		Int culprit = (Int) info.getTerm("Culprit");
		assertTrue(culprit.intValue() == 1300);
	}

	// verifico che '$op'(600, a, '+') lancia un errore di dominio
	public void test_$op_3_8() throws Exception {
		Prolog engine = new Prolog();
		String goal = "catch('$op'(600, a, '+'), error(domain_error(ValidDomain, Culprit), domain_error(Goal, ArgNo, ValidDomain, Culprit)), true).";
		SolveInfo info = engine.solve(goal);
		assertTrue(info.isSuccess());
		Struct g = (Struct) info.getTerm("Goal");
		assertTrue(g.isEqual(new Struct("$op", new Int(600), new Struct("a"),
				new Struct("+"))));
		Int argNo = (Int) info.getTerm("ArgNo");
		assertTrue(argNo.intValue() == 2);
		Struct validDomain = (Struct) info.getTerm("ValidDomain");
		assertTrue(validDomain.isEqual(new Struct("operator_specifier")));
		Struct culprit = (Struct) info.getTerm("Culprit");
		assertTrue(culprit.isEqual(new Struct("a")));
	}

}