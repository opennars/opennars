/*
 * tuProlog - Copyright (C) 2001-2007 aliCE team at deis.unibo.it
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package nars.tuprolog;

import nars.nal.term.Term;
import nars.tuprolog.util.Tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;

/**
 * Library of built-in predicates
 * 
 * @author Alex Benini
 */
@SuppressWarnings("serial")
public class BuiltIn extends Library {

	private final Prolog engineManager;
	private final Theories theories;
	private final Libraries libraries;
	private final Flags flagManager;
	private final Primitives primitives;
	private final Operators operatorManager;

	public BuiltIn(Prolog mediator) {
		super();
		setEngine(mediator);
		engineManager = mediator;
		theories = mediator.getTheories();
		libraries = mediator.getLibraries();
		flagManager = mediator.getFlags();
		primitives = mediator.getPrimitives();
		operatorManager = mediator.getOperators();
	}


	/**
	 * Defines some synonyms
	 */
	final public String[][] getSynonymMap() {
		 return new String[][] { { "!", "cut", "predicate" },
				 { "=", "unify", "predicate" },
				 { "\\=", "deunify", "predicate" },
				 { ",", "comma", "predicate" }, { "op", "$op", "predicate" },
				 { "solve", "initialization", "directive" },
				 { "consult", "include", "directive" },
				 { "load_library", "$load_library", "directive" } };
	 }

	 /*
	  * PREDICATES
	  */

	 public boolean fail_0() {
		 return false;
	 }

	 public boolean true_0() {
		 return true;
	 }

	 /*Castagna 06/2011*/
	 /*
	public boolean halt_0() throws HaltException {
		throw new HaltException();
	}
	  */

	 public boolean halt_0() {
		 System.exit(0);
		 return true;
	 }
	 /**/

	 public boolean cut_0() {
		 engineManager.cut();
		 return true;
	 }

	 public boolean asserta_1(Term arg0) throws PrologError {
		 arg0 = arg0.getTerm();
		 if (arg0 instanceof Struct ) {
			
			 if (((Struct) arg0).getName().equals(":-"))
			 {
				 for(int i=0; i<(((Struct) arg0).toList().listSize())-1; i++)
				 {
					 Term argi=((Struct) arg0).getTermX(i);
					 if (!(argi instanceof Struct) )
					 {
						 if (argi instanceof Var)
							 throw PrologError.instantiation_error(engineManager, 1);
						 else
							 throw PrologError.type_error(engineManager, 1, "clause", arg0); 
					 }
				 }
			 }
			 theories.assertA((Struct) arg0, true, null, false);
			 return true;
		 }
		 if (arg0 instanceof Var)
			 throw PrologError.instantiation_error(engineManager, 1);
		 else
			 throw PrologError.type_error(engineManager, 1, "clause", arg0);
	 }

	 public boolean assertz_1(Term arg0) throws PrologError {
		 arg0 = arg0.getTerm();
		 if (arg0 instanceof Struct) {
			 if (((Struct) arg0).getName().equals(":-"))
			 {
				 for(int i=0; i<(((Struct) arg0).toList().listSize())-1; i++)
				 {
					 Term argi=((Struct) arg0).getTermX(i);
					 if (!(argi instanceof Struct) )
					 {
						 if (argi instanceof Var)
							 throw PrologError.instantiation_error(engineManager, 1);
						 else
							 throw PrologError.type_error(engineManager, 1, "clause", arg0); 
					 }
				 }
			 }
			 theories.assertZ((Struct) arg0, true, null, false);
			 return true;
		 }
		 if (arg0 instanceof Var)
			 throw PrologError.instantiation_error(engineManager, 1);
		 else
			 throw PrologError.type_error(engineManager, 1, "clause", arg0);
	 }

	 public boolean $retract_1(Term arg0) throws PrologError {
		 arg0 = arg0.getTerm();
		 if (!(arg0 instanceof Struct)) {
			 if (arg0 instanceof Var)
				 throw PrologError.instantiation_error(engineManager, 1);
			 else
				 throw PrologError.type_error(engineManager, 1, "clause", arg0);
		 }
		 Struct sarg0 = (Struct) arg0;
		 Clause c = theories.retract(sarg0);
		 // if clause to retract found -> retract + true
		 if (c != null) {
			 Struct clause = null;
			 if (!sarg0.isClause()) {
				 clause = new Struct(":-", arg0, new Struct("true"));
			 } else {
				 clause = sarg0;
			 }
			 unify(clause, c.getClause());
			 return true;
		 }
		 return false;
	 }

	 public boolean abolish_1(Term arg0) throws PrologError {
		 arg0 = arg0.getTerm();
		 if (arg0 instanceof Var)
			 throw PrologError.instantiation_error(engineManager, 1);
		 if (!(arg0 instanceof Struct) || !((Struct)arg0).isGround())
			 throw PrologError.type_error(engineManager, 1, "predicate_indicator", arg0);
		 
		 if( ((Struct)arg0).getTermX(0).toString().equals("abolish") )
			 throw PrologError.permission_error(engineManager, "modify", "static_procedure", arg0, new Struct(""));
		 
		 return theories.remove((Struct) arg0);
	 }

	 /*Castagna 06/2011*/	
	 /*
	public boolean halt_1(Term arg0) throws HaltException, PrologError {
		if (arg0 instanceof Int)
			throw new HaltException(((Int) arg0).intValue());
		if (arg0 instanceof Var)
			throw PrologError.instantiation_error(engineManager, 1);
		else {
			throw PrologError.type_error(engineManager, 1, "integer", arg0);
		}
	}
	  */

	 public boolean halt_1(PTerm arg0) throws PrologError {
		 if (arg0 instanceof Int)
			 System.exit(((Int) arg0).intValue());
		 if (arg0 instanceof Var)
			 throw PrologError.instantiation_error(engineManager, 1);
		 else {
			 throw PrologError.type_error(engineManager, 1, "integer", arg0);
		 }
	 }
	 /**/

	 /*
	  * loads a tuprolog library, given its java class name
	  */
	 public boolean load_library_1(Term arg0) throws PrologError {
		 arg0 = arg0.getTerm();
		 if (!arg0.isAtom()) {
			 if (arg0 instanceof Var)
				 throw PrologError.instantiation_error(engineManager, 1);
			 else
				 throw PrologError.type_error(engineManager, 1, "atom", arg0);
		 }
		 try {
			 libraries.load(((Struct) arg0).getName());
			 return true;
		 } catch (Exception ex) {
			 throw PrologError.existence_error(engineManager, 1, "class", arg0,
					 new Struct(ex.getMessage()));
		 }
	 }
	 
	 /*
	  * loads a tuprolog library, given its java class name and the list of the paths where may be contained
	  */
	 public boolean load_library_2(Term arg0, Term arg1) throws PrologError {
		 arg0 = arg0.getTerm();
		 arg1 = arg1.getTerm();
		 if (!arg0.isAtom()) {
			 if (arg0 instanceof Var)
				 throw PrologError.instantiation_error(engineManager, 1);
			 else
				 throw PrologError.type_error(engineManager, 1, "atom", arg0);
		 }
		 if(!arg1.isList())
		 {
			 throw PrologError.type_error(engineManager, 2, "list", arg1);
		 }
		 
		 try {
			 String[] paths = getStringArrayFromStruct((Struct) arg1);
			 if(paths == null || paths.length == 0)
				 throw PrologError.existence_error(engineManager, 2, "paths", arg1, new Struct("Invalid paths' list."));
			 libraries.load(((Struct) arg0).getName(), paths);
			 return true;
			
		 } catch (Exception ex) {
			 throw PrologError.existence_error(engineManager, 1, "class", arg0,
					 new Struct(ex.getMessage()));
		 }
	 }

	 private String[] getStringArrayFromStruct(Struct list) {
	        String args[] = new String[list.listSize()];
	        Iterator<? extends Term> it = list.listIterator();
	        int count = 0;
	        while (it.hasNext()) {
	        	String path = Tools.removeApices(it.next().toString());
	            args[count++] = path;
	        }
	        return args;
	 }
	 /*
	  * unloads a tuprolog library, given its java class name
	  */
	 public boolean unload_library_1(Term arg0) throws PrologError {
		 arg0 = arg0.getTerm();
		 if (!arg0.isAtom()) {
			 if (arg0 instanceof Var)
				 throw PrologError.instantiation_error(engineManager, 1);
			 else
				 throw PrologError.type_error(engineManager, 1, "atom", arg0);
		 }
		 try {
			 libraries.unloadLibrary(((Struct) arg0).getName());
			 return true;
		 } catch (Exception ex) {
			 throw PrologError.existence_error(engineManager, 1, "class", arg0,
					 new Struct(ex.getMessage()));
		 }
	 }

	 /*
	  * get flag list: flag_list(-List)
	  */
	 public boolean flag_list_1(Term arg0) {
		 arg0 = arg0.getTerm();
		 Struct flist = flagManager.getPrologFlagList();
		 return unify(arg0, flist);
	 }

	 public boolean comma_2(Term arg0, Term arg1) {
		 arg0 = arg0.getTerm();
		 arg1 = arg1.getTerm();
		 Struct s = new Struct(",", arg0, arg1);
		 engineManager.pushSubGoal(Clause.extractBody(s));
		 return true;
	 }

	 /**
	  * It is the same as call/1, but it is not opaque to cut.
	  * 
	  * @throws PrologError
	  */
	 public boolean $call_1(Term goal) throws PrologError {
		 goal = goal.getTerm();
		 if (goal instanceof Var)
			 throw PrologError.instantiation_error(engineManager, 1);
		 if (!isCallable(goal))
			 throw PrologError.type_error(engineManager, 1, "callable", goal);
		 goal = convertTermToGoal(goal);
		 if (goal == null)
			 throw PrologError.type_error(engineManager, 1, "callable", goal);
		 engineManager.identify(goal);
		 engineManager.pushSubGoal(Clause.extractBody(goal));
		 return true;
	 }

	 /**
	  * Convert a term to a goal before executing it by means of call/1. See
	  * section 7.6.2 of the ISO Standard for details.
	  * <ul>
	  * <li>If T is a variable then G is the control construct call, whose
	  * argument is T.</li>
	  * <li>If the principal functor of T is t ,?/2 or ;/2 or ->/2, then each
	  * argument of T shall also be converted to a goal.</li>
	  * <li>If T is an atom or compound term with principal functor FT, then G is
	  * a predication whose predicate indicator is FT, and the arguments, if any,
	  * of T and G are identical.</li>
	  * </ul>
	  * Note that a variable X and a term call(X) are converted to identical
	  * bodies. Also note that if T is a number, then there is no goal which
	  * corresponds to T.
	  */
	 public static Term convertTermToGoal(Term term) {
		 if (term instanceof PNum)
			 return null;
		 if(term instanceof Var && ((Var)term).getLink() instanceof PNum)
			 return null;
		 term = term.getTerm(); 
		 if (term instanceof Var)
			 return new Struct("call", term);
		 if (term instanceof Struct) {
			 Struct s = (Struct) term;
			 String pi = s.getPredicateIndicator();
			 if (pi.equals(";/2") || pi.equals(",/2") || pi.equals("->/2")) {
				 for (int i = 0; i < s.size(); i++) {
					 Term t = s.getTermX(i);
					 Term arg = convertTermToGoal(t);
					 if (arg == null)
						 return null;
					 s.setTerm(i, arg);
				 }
			 }
		 }
		 return term;
	 }

	 /**
	  * A callable term is an atom of a compound term. See the ISO Standard
	  * definition in section 3.24.
	  */
	 private boolean isCallable(Term goal) {
		 return (goal.isAtom() || goal.isCompound());
	 }

	 private void handleError(Throwable t) throws PrologError {
		 // errore durante la valutazione
		 if (t instanceof ArithmeticException) {
			 ArithmeticException cause = (ArithmeticException) t;
			 //            System.out.println(cause.getMessage());
			 if (cause.getMessage().equals("/ by zero"))
				 throw PrologError.evaluation_error(engineManager, 2, "zero_divisor");
		 }
	 }

	 public boolean is_2(Term arg0, Term arg1) throws PrologError {
		 if (arg1.getTerm() instanceof Var)
			 throw PrologError.instantiation_error(engineManager, 2);
		 Term val1 = null;
		 try {
			 val1 = evalExpression(arg1);
		 } catch (Throwable t) {
			 handleError(t);
		 }
		 if (val1 == null)
			 throw PrologError.type_error(engineManager, 2, "evaluable", arg1.getTerm());
		 else
			 return unify(arg0.getTerm(), val1);
	 }

	 public boolean unify_2(Term arg0, Term arg1) {
		 return unify(arg0, arg1);
	 }

	 // \=
	 public boolean deunify_2(Term arg0, Term arg1) {
		 return !unify(arg0, arg1);
	 }

	 // $tolist
	 public boolean $tolist_2(Term arg0, Term arg1) throws PrologError {
		 // transform arg0 to a list, unify it with arg1
		 arg0 = arg0.getTerm();
		 arg1 = arg1.getTerm();
		 if (arg0 instanceof Var)
			 throw PrologError.instantiation_error(engineManager, 1);
		 if (arg0 instanceof Struct) {
			 PTerm val0 = ((Struct) arg0).toList();
			 return (val0 != null && unify(arg1, val0));
		 }
		 throw PrologError.type_error(engineManager, 1, "struct", arg0);
	 }

	 // $fromlist
	 public boolean $fromlist_2(Term arg0, Term arg1) throws PrologError {
		 // get the compound representation of the list
		 // provided as arg1, and unify it with arg0
		 arg0 = arg0.getTerm();
		 arg1 = arg1.getTerm();
		 if (arg1 instanceof Var)
			 throw PrologError.instantiation_error(engineManager, 2);
		 if (!arg1.isList()) {
			 throw PrologError.type_error(engineManager, 2, "list", arg1);
		 }
		 PTerm val1 = ((Struct) arg1).fromList();
		 if (val1 == null)
			 //throw PrologError.type_error(engineManager, 2, "list", arg1);
			 return false;
		 return (unify(arg0, val1));
	 }

	 public boolean copy_term_2(Term arg0, Term arg1) {
		 // unify arg1 with a renamed copy of arg0
		 arg0 = arg0.getTerm();
		 arg1 = arg1.getTerm();
		 int id = engineManager.getEnv().nDemoSteps;
		 if (arg0 instanceof PTerm)
		 	return unify(arg1, ((PTerm)arg0).copy(new IdentityHashMap<>(), id));
		 return false;
	 }

	 // $append
	 public boolean $append_2(Term arg0, Term arg1) throws PrologError {
		 // append arg0 to arg1
		 arg0 = arg0.getTerm();
		 arg1 = arg1.getTerm();
		 if (arg1 instanceof Var)
			 throw PrologError.instantiation_error(engineManager, 2);
		 if (!arg1.isList()) {
			 throw PrologError.type_error(engineManager, 2, "list", arg1);
		 }
		 ((Struct) arg1).append(arg0);
		 return true;
	 }

	 // $find
	 public boolean $find_2(PTerm arg0, Term arg1) throws PrologError {
		 // look for clauses whose head unifies whith arg0 and enqueue them to
		 // list arg1
		 Term xarg0 = arg0.getTerm();
		 if (!(xarg0 instanceof PTerm)) return false; //can't match() at the end
		 arg0 = (PTerm)xarg0;

		 arg1 = arg1.getTerm();
		 if (arg0 instanceof Var)
			 throw PrologError.instantiation_error(engineManager, 1);
		 if (/* !arg0 instanceof Struct || */!arg1.isList())
			 throw PrologError.type_error(engineManager, 2, "list", arg1);
		 Iterator<Clause> l = null;
		 try {
			 l = theories.find(arg0);
			 if (l == null)
				 return false;
		 } catch (RuntimeException e) {
			 e.printStackTrace();
			 return false;
		 }

		 ArrayList<Var> v1 = new ArrayList(), v2 = new ArrayList();
		 long now = System.currentTimeMillis();
		 java.util.Iterator<Clause> it = l;
		 while (it.hasNext()) {
			 Clause b = it.next();
			 if (match(arg0, b.getHead(), now, v1, v2)) {
				 b.getClause().resolveTerm();
				 ((Struct) arg1).append(b.getClause());
			 }
		 }
		 return true;
	 }

	 // set_prolog_flag(+Name,@Value)
	 public boolean set_prolog_flag_2(Term arg0, Term arg1) throws PrologError {
		 arg0 = arg0.getTerm();
		 arg1 = arg1.getTerm();
		 if (!(arg1 instanceof PTerm))
			 throw PrologError.type_error(engineManager, 2, "struct", arg1);
		 PTerm parg1 = (PTerm) arg1;

		 if (arg0 instanceof Var)
			 throw PrologError.instantiation_error(engineManager, 1);
		 if (arg1 instanceof Var)
			 throw PrologError.instantiation_error(engineManager, 2);
		 if ((!arg0.isAtom() && !(arg0 instanceof Struct)))
			 throw PrologError.type_error(engineManager, 1, "struct", arg0);
		 if (!PTerm.isGround(arg1))
			 throw PrologError.type_error(engineManager, 2, "ground", arg1);
		 String name = arg0.toString();
		 if (flagManager.getFlag(name) == null)
			 throw PrologError.domain_error(engineManager, 1, "prolog_flag",
					 arg0);
		 if (!flagManager.isValidValue(name, parg1))
			 throw PrologError
			 .domain_error(engineManager, 2, "flag_value", arg1);
		 if (!flagManager.isModifiable(name))
			 throw PrologError.permission_error(engineManager, "modify", "flag",
					 arg0, new Int(0));
		 return flagManager.setFlag(name, parg1);
	 }

	 // get_prolog_flag(@Name,?Value)
	 public boolean get_prolog_flag_2(Term arg0, Term arg1) throws PrologError {
		 arg0 = arg0.getTerm();
		 arg1 = arg1.getTerm();
		 if (arg0 instanceof Var)
			 throw PrologError.instantiation_error(engineManager, 1);
		 if (!arg0.isAtom() && !(arg0 instanceof Struct)) {
			 throw PrologError.type_error(engineManager, 1, "struct", arg0);
		 }
		 String name = arg0.toString();
		 PTerm value = flagManager.getFlag(name);
		 if (value == null)
			 throw PrologError.domain_error(engineManager, 1, "prolog_flag",
					 arg0);
		 return unify(value, arg1);
	 }

	 public boolean $op_3(Term arg0, Term arg1, Term arg2) throws PrologError {
		 arg0 = arg0.getTerm();
		 arg1 = arg1.getTerm();
		 arg2 = arg2.getTerm();
		 if (arg0 instanceof Var)
			 throw PrologError.instantiation_error(engineManager, 1);
		 if (arg1 instanceof Var)
			 throw PrologError.instantiation_error(engineManager, 2);
		 if (arg2 instanceof Var)
			 throw PrologError.instantiation_error(engineManager, 3);
		 if (!(arg0 instanceof Int))
			 throw PrologError.type_error(engineManager, 1, "integer", arg0);
		 if (!arg1.isAtom())
			 throw PrologError.type_error(engineManager, 2, "atom", arg1);
		 if (!arg2.isAtom() && !arg2.isList())
			 throw PrologError.type_error(engineManager, 3, "atom_or_atom_list",
					 arg2);
		 int priority = ((Int) arg0).intValue();
		 if (priority < Operators.OP_LOW || priority > Operators.OP_HIGH)
			 throw PrologError.domain_error(engineManager, 1, "operator_priority", arg0);
		 String specifier = ((Struct) arg1).getName();
		 if (!specifier.equals("fx") && !specifier.equals("fy")
				 && !specifier.equals("xf") && !specifier.equals("yf")
				 && !specifier.equals("xfx") && !specifier.equals("yfx")
				 && !specifier.equals("xfy"))
			 throw PrologError.domain_error(engineManager, 2,
					 "operator_specifier", arg1);
		 if (arg2.isList()) {
			 for (Iterator<? extends Term> operators = ((Struct) arg2).listIterator(); operators
			 .hasNext();) {
				 Struct operator = (Struct) operators.next();
				 operatorManager.opNew(operator.getName(), specifier, priority);
			 }
		 } else
			 operatorManager.opNew(((Struct) arg2).getName(), specifier, priority);
		 return true;
	 }

	 /*
	  * DIRECTIVES
	  */

	 public void op_3(PTerm arg0, PTerm arg1, PTerm arg2) throws PrologError {
		 $op_3(arg0, arg1, arg2);
	 }

	 public void flag_4(Term flagName, Term flagSet, PTerm flagDefault,
			 Term flagModifiable) {
		 flagName = flagName.getTerm();
		 flagSet = flagSet.getTerm();

		 flagDefault = PTerm.p(flagDefault.getTerm());


		 flagModifiable = flagModifiable.getTerm();
		 if (flagSet.isList()
				 && (flagModifiable.equals(PTerm.TRUE) || flagModifiable
						 .equals(PTerm.FALSE))) {
			 // TODO libName che futuro deve avere?? --------------------
			 String libName = "";
			 // ------------
			 flagManager.defineFlag(flagName.toString(), (Struct) flagSet,
					 flagDefault, flagModifiable.equals(PTerm.TRUE), libName);
		 }
	 }

	 public void initialization_1(Term goal) {
		 goal = goal.getTerm();
		 if (goal instanceof Struct) {
			 primitives.identifyPredicate(goal);
			 theories.addStartGoal((Struct) goal);
		 }
	 }

	 public void $load_library_1(Term lib) throws InvalidLibraryException {
		 lib = lib.getTerm();
		 if (lib.isAtom())
			 libraries.load(((Struct) lib).getName());
	 }

	 public void include_1(Term theory) throws
			 InvalidTheoryException, IOException {
		 theory = theory.getTerm();
         String path = Tools.removeApices(theory.toString());
         if(! new File(path).isAbsolute()) {
            path = engine.getCurrentDirectory()  + File.separator + path;
         }
         engine.pushDirectoryToList(new File(path).getParent());
		 engine.addTheory(new Theory(new FileInputStream(path)));
         engine.popDirectoryFromList();
	 }

}