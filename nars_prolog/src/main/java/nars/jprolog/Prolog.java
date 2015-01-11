package nars.jprolog;
import java.util.Hashtable;
import java.io.*;
import nars.jprolog.lang.CPFStack;
import nars.jprolog.lang.Failure;
import nars.jprolog.lang.HashtableOfTerm;
import nars.jprolog.lang.InternalDatabase;
import nars.jprolog.lang.JavaObjectTerm;
import nars.jprolog.lang.ListTerm;
import nars.jprolog.lang.Predicate;
import nars.jprolog.lang.PrologClassLoader;
import nars.jprolog.lang.PrologControl;
import nars.jprolog.lang.StructureTerm;
import nars.jprolog.lang.SymbolTerm;
import nars.jprolog.lang.SystemException;
import nars.jprolog.lang.Term;
import nars.jprolog.lang.Trail;
import nars.jprolog.lang.VariableTerm;
/**
 * Prolog engine.
 *
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 * @version 1.2
*/
public class Prolog implements Serializable {
    /** Prolog thread */
    public PrologControl control;
    /** Argument registers */
    public Term[] aregs;
    /** Continuation goal register */
    public Predicate cont;
    /** Choice point frame stack */
    public CPFStack stack;
    /** Trail stack */
    public Trail trail;
    /* Push down list */
    //    public PushDownList pdl;
    /** Cut pointer */
    public int B0;
    /** Class loader */
    public PrologClassLoader pcl;
    /** Internal Database */
    public InternalDatabase internalDB;

    /** Current time stamp of choice point frame */
    protected long CPFTimeStamp;

    /**
     * Exception level of continuation passing loop:
     * <li><code>0</code> for no exception,
     * <li><code>1</code> for <code>halt/0</code>,
     * <li><code>2</code> for <code>freeze/2</code> (not supported yet)
     * </ul>
     */
    public int exceptionRaised;

    /** <font color="red">Not supported yet</font>. Prolog implementation flag: <code>bounded</code>. */
    protected boolean bounded = false;
    /** Prolog implementation flag: <code>max_integer</code>. */
    protected int maxInteger = Integer.MAX_VALUE;
    /** Prolog implementation flag: <code>min_integer</code>. */
    protected int minInteger = Integer.MIN_VALUE;
    /** Prolog implementation flag: <code>integer_rounding_function</code>. */
    protected String integerRoundingFunction = "down";
    /** <font color="red">Not supported yet</font>. Prolog implementation flag: <code>char_conversion</code>. */
    protected String charConversion;
    /** Prolog implementation flag: <code>debug</code>. */
    protected String debug;
    /** Prolog implementation flag: <code>max_arity</code>. */
    protected int maxArity = 255;
    /** Prolog implementation flag: <code>unknown</code>. */
    protected String unknown;
    /** <font color="red">Not supported yet</font>. Prolog implementation flag: <code>double_quotes</code>. */
    protected String doubleQuotes;
    /** Prolog implementation flag: <code>print_stack_trace</code>. */
    protected String printStackTrace;

    /** Holds a list of frozen goals for <code>freeze/2</code> (not implemented yet). */
    protected Term pendingGoals; 
    /** Holds an exception term for <code>catch/3</code> and <code>throw/1</code>. */
    protected Term exception;

    /** Holds the start time as <code>long</code> for <code>statistics/2</code>. */
    protected long startRuntime;
    /** Holds the previous time as <code>long</code> for <code>statistics/2</code>. */
    protected long previousRuntime;

    /** Hashtable for creating a copy of term. */
    public final Hashtable<VariableTerm,VariableTerm> copyHash;

    /** The size of the pushback buffer used for creating input streams. */
    public static int PUSHBACK_SIZE = 3;
    //    public static int PUSHBACK_SIZE = 2;

    /** Standard input stream. */
    protected transient PushbackReader userInput;
    /** Standard output stream. */
    protected transient PrintWriter userOutput;
    /** Standard error stream. */
    protected transient PrintWriter userError;
    /** Current input stream. */
    protected transient PushbackReader currentInput;
    /** Current output stream. */
    protected transient PrintWriter currentOutput;
    /** Hashtable for managing input and output streams. */
    protected HashtableOfTerm streamManager;

    /** Hashtable for managing internal databases. */
    protected HashtableOfTerm hashManager;

    /** Holds an atom <code>[]<code> (empty list). */
    public static SymbolTerm Nil     = SymbolTerm.makeSymbol("[]");

    /* Some symbols for stream options */
    static SymbolTerm SYM_MODE_1     = SymbolTerm.makeSymbol("mode", 1);
    static SymbolTerm SYM_ALIAS_1    = SymbolTerm.makeSymbol("alias", 1);
    static SymbolTerm SYM_TYPE_1     = SymbolTerm.makeSymbol("type", 1);
    static SymbolTerm SYM_READ       = SymbolTerm.makeSymbol("read");
    static SymbolTerm SYM_APPEND     = SymbolTerm.makeSymbol("append");
    static SymbolTerm SYM_INPUT      = SymbolTerm.makeSymbol("input");
    static SymbolTerm SYM_OUTPUT     = SymbolTerm.makeSymbol("output");
    static SymbolTerm SYM_TEXT       = SymbolTerm.makeSymbol("text");
    static SymbolTerm SYM_USERINPUT  = SymbolTerm.makeSymbol("user_input");
    static SymbolTerm SYM_USEROUTPUT = SymbolTerm.makeSymbol("user_output");
    static SymbolTerm SYM_USERERROR  = SymbolTerm.makeSymbol("user_error");

    /** Constructs new Prolog engine. */
    public Prolog(PrologControl c, PrologClassLoader pcl) { 
	control    = c;
	aregs      = new Term[maxArity];
	cont       = null;
	stack      = new CPFStack(this);
	trail      = new Trail(this);
	//	pdl        = new PushDownList();
	this.pcl   = pcl;
	internalDB = new InternalDatabase();
        copyHash = new Hashtable<VariableTerm,VariableTerm>();
	initOnce();
    }

    /** 
     * Initializes some local instances only once.
     * This <code>initOnce</code> method is invoked in the constructor
     * and initializes the following instances:
     * <ul>
     *   <li><code>userInput</code>
     *   <li><code>userOutput</code>
     *   <li><code>userError</code>
     *   <li><code>copyHash</code>
     *   <li><code>hashManager</code>
     *   <li><code>streamManager</code>
     * </ul>
     */
    protected void initOnce() {
	userInput   = new PushbackReader(new BufferedReader(new InputStreamReader(System.in)), PUSHBACK_SIZE);
	userOutput  = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out)), true);
	userError   = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.err)), true);

	copyHash.clear();
	hashManager   = new HashtableOfTerm();
	streamManager = new HashtableOfTerm();

	streamManager.put(SYM_USERINPUT, new JavaObjectTerm(userInput));
	streamManager.put(new JavaObjectTerm(userInput), 
			  makeStreamProperty(SYM_READ, SYM_INPUT, SYM_USERINPUT, SYM_TEXT));
	streamManager.put(SYM_USEROUTPUT, new JavaObjectTerm(userOutput));
	streamManager.put(new JavaObjectTerm(userOutput),
			  makeStreamProperty(SYM_APPEND, SYM_OUTPUT, SYM_USEROUTPUT, SYM_TEXT));
	streamManager.put(SYM_USERERROR, new JavaObjectTerm(userError));
	streamManager.put(new JavaObjectTerm(userError),
			  makeStreamProperty(SYM_APPEND, SYM_OUTPUT, SYM_USERERROR, SYM_TEXT));
    }

    /** Initializes this Prolog engine. */
    public void init() { 
	stack.init();
	trail.init();
	//	pdl.init();
	B0 = stack.top();
	CPFTimeStamp = Long.MIN_VALUE;

	// Creates an initial choice point frame.
	Term[] noarg = {};
	stack.create(noarg, null);
	stack.setTR(trail.top());
	stack.setTimeStamp(++CPFTimeStamp);
	stack.setBP(new Failure(control));
	stack.setB0(B0);

	exceptionRaised = 0;

	charConversion  = "off";
	debug           = "off";
	unknown         = "error";
	doubleQuotes    = "codes";
	printStackTrace = "off";

	pendingGoals    = Nil;
	exception       = SymbolTerm.makeSymbol("$none");
	startRuntime    = System.currentTimeMillis();
	previousRuntime = 0;

	userOutput.flush();
	userError.flush();
	currentInput  = userInput;
	currentOutput = userOutput;
    }

    /** Sets the top of choice porint stack to <code>B0</code> (cut pointer). */
    public void setB0()    { B0 = stack.top(); }

    /** Discards all choice points after the value of <code>i</code>. */
    public void cut(int i) { stack.cut(i); }

    /** Discards all choice points after the value of <code>B0</code>. */
    public void neckCut()  { stack.cut(B0); }

    /**
     * Returns a copy of term <code>t</code>. 
     * @param t a term to be copied. It must be dereferenced.
     */
    public Term copy(Term t) {
	copyHash.clear();
	return t.copy(this);
    }

    /* 
    public boolean unify(Term a1, Term a2) {
	Term d1, d2;
	pdl.init();
	pdl.push(a1);
	pdl.push(a2);
	while (! pdl.empty()) {
	    d1 = pdl.pop().dereference();
	    d2 = pdl.pop().dereference();
	    if (d1 != d2) {
		if (d1.isVariable()) {
		    ((VariableTerm)d1).bind(d2, trail);
		} else if (d2.isVariable()) {
		    ((VariableTerm)d2).bind(d1, trail);
		} else if (d2.isList()) {
		    if (! d1.isList())
			return false;
		    pdl.push(((ListTerm)d1).cdr());
		    pdl.push(((ListTerm)d2).cdr());
		    pdl.push(((ListTerm)d1).car());
		    pdl.push(((ListTerm)d2).car());
		} else if (d2.isStructure()) {
		    if (! d1.isStructure())
			return false;
		    if (! ((StructureTerm)d1).functor.equals(((StructureTerm)d2).functor))
			return false;
		    for (int i=0; i<((StructureTerm)d1).arity; i++) {
			pdl.push(((StructureTerm)d1).args[i]);
			pdl.push(((StructureTerm)d2).args[i]);
		    }
		} else if (! d1.equals(d2)) {
		    return false;
		}
	    }
	}
	return true;
    }
    */

    /** 
     * Do backtrak.
     * This method restores the value of <code>B0</code>
     * and returns the backtrak point in current choice point.
     */
    public Predicate fail() {
	B0 = stack.getB0();     // restore B0
	return stack.getBP();   // execute next clause
    }

    /** 
     * Returns the <code>Predicate</code> object refered, respectively, 
     * <code>var</code>, <code>Int</code>, <code>flo</code>, 
     * <code>con</code>, <code>str</code>, or <code>lis</code>, 
     * depending on whether the dereferenced value of argument 
     * register <code>areg[1]</code> is a
     * variable, integer, float,
     * atom, compound term, or non-empty list, respectively.
     */
    public Predicate switch_on_term(Predicate var, 
				    Predicate Int, 
				    Predicate flo,
				    Predicate con, 
				    Predicate str, 
				    Predicate lis) {
	Term arg1 = aregs[1].dereference();
	if (arg1.isVariable())
	    return var;
	if (arg1.isInteger())
	    return Int;
	if (arg1.isDouble())
	    return flo;
	if (arg1.isSymbol())
	    return con;
	if (arg1.isStructure())
	    return str;
	if (arg1.isList())
	    return lis;
	return var;
    }

    /**
     * If the dereferenced value of arugment register <code>areg[1]</code>
     * is an integer, float, atom, or compound term (except for non-empty list),
     * this returns the <code>Predicate</code> object to which its key is mapped
     * in hashtable <code>hash</code>.
     *
     * The key is calculated as follows:
     * <ul>
     *   <li>integer - itself
     *   <li>float - itself
     *   <li>atom - itself
     *   <li>compound term - functor/arity
     * </ul>
     *
     * If there is no mapping for the key of <code>areg[1]</code>, 
     * this returns <code>otherwise</code>.
     */
    public Predicate switch_on_hash(Hashtable<Term,Predicate> hash, Predicate otherwise) {
	Term arg1 = aregs[1].dereference();
	Term key;
	if (arg1.isInteger() || arg1.isDouble() || arg1.isSymbol()) {
	    key = arg1;
	} else if (arg1.isStructure()) {
	    key = ((StructureTerm) arg1).functor();
	} else {
	    throw new SystemException("Invalid argument in switch_on_hash");
	}
	Predicate p = hash.get(key);
	if (p != null)
	    return p;
	else 
	    return otherwise;
    }

    /** Restores the argument registers and continuation goal register from the current choice point frame. */
    public void restore() {
	Term[] args = stack.getArgs();
	int i = args.length;
	System.arraycopy(args, 0, aregs, 1, i);
	cont = stack.getCont();
    }

    /** Creates a new choice point frame. */
    public Predicate jtry(Predicate p, Predicate next) {
	int i = p.arity();
	Term[] args = new Term[i];
	System.arraycopy(aregs, 1, args, 0, i);
	stack.create(args, cont);
	stack.setTR(trail.top());
	stack.setTimeStamp(++CPFTimeStamp);
	stack.setBP(next);
	stack.setB0(B0);
	return p;
    }

    /** 
     * Resets all necessary information from the current choice point frame,
     * updates its next clause field to <code>next</code>,
     * and then returns <code>p</code>.
     */
    public Predicate retry(Predicate p, Predicate next) {
	restore();
	trail.unwind(stack.getTR());
	stack.setBP(next);
	return p;
    }

    /** 
     * Resets all necessary information from the current choice point frame,
     * discard it, and then returns <code>p</code>.
     */
    public Predicate trust(Predicate p) {
	restore();
	trail.unwind(stack.getTR());
	stack.delete();
	return p;
    }

    Term makeStreamProperty(SymbolTerm _mode, SymbolTerm io, SymbolTerm _alias, SymbolTerm _type) {
	Term[] mode  = {_mode};
	Term[] alias = {_alias};
	Term[] type  = {_type};

	Term t = Nil;
	t = new ListTerm(new StructureTerm(SYM_MODE_1,  mode ), t);
	t = new ListTerm(io, t);
	t = new ListTerm(new StructureTerm(SYM_ALIAS_1, alias), t);
	t = new ListTerm(new StructureTerm(SYM_TYPE_1,  type ), t);
	return t;
    }

    /** Returns the current time stamp of choice point frame. */
    public long    getCPFTimeStamp() { return CPFTimeStamp; }

    /** Returns the value of Prolog implementation flag: <code>bounded</code>. */
    public boolean isBounded() { return bounded; }

    /** Returns the value of Prolog implementation flag: <code>max_integer</code>. */
    public int getMaxInteger() { return maxInteger; }

    /** Returns the value of Prolog implementation flag: <code>min_integer</code>. */
    public int getMinInteger() { return minInteger; }

    /** Returns the value of Prolog implementation flag: <code>integer_rounding_function</code>. */
    public String getIntegerRoundingFunction() { return integerRoundingFunction; }

    /** Returns the value of Prolog implementation flag: <code>char_conversion</code>. */
    public String getCharConversion() { return charConversion; }
    /** Sets the value of Prolog implementation flag: <code>char_conversion</code>. */
    public void setCharConversion(String mode) { charConversion = mode;}

    /** Returns the value of Prolog implementation flag: <code>debug</code>. */
    public String getDebug() { return debug; }
    /** Sets the value of Prolog implementation flag: <code>debug</code>. */
    public void setDebug(String mode) { debug = mode;}

    /** Returns the value of Prolog implementation flag: <code>max_arity</code>. */
    public int getMaxArity() { return maxArity; }

    /** Returns the value of Prolog implementation flag: <code>unknown</code>. */
    public String getUnknown() { return unknown; }
    /** Sets the value of Prolog implementation flag: <code>unknown</code>. */
    public void setUnknown(String mode) { unknown = mode;}

    /** Returns the value of Prolog implementation flag: <code>double_quotes</code>. */
    public String getDoubleQuotes() { return doubleQuotes; }
    /** Sets the value of Prolog implementation flag: <code>double_quotes</code>. */
    public void setDoubleQuotes(String mode) { doubleQuotes = mode;}

    /** Returns the value of Prolog implementation flag: <code>print_stack_trace</code>. */
    public String getPrintStackTrace() { return printStackTrace; }
    /** Sets the value of Prolog implementation flag: <code>print_stack_trace</code>. */
    public void setPrintStackTrace(String mode) { printStackTrace = mode;}

    /** Returns the value of <code>exception</code>. This is used in <code>catch/3</code>. */
    public Term getException() { return exception; }
    /** Sets the value of <code>exception</code>. This is used in <code>throw/1</code>. */
    public void setException(Term t) { exception = t;}

    /** Returns the value of <code>startRuntime</code>. This is used in <code>statistics/2</code>. */
    public long getStartRuntime() { return startRuntime; }

    /** Returns the value of <code>previousRuntime</code>. This is used in <code>statistics/2</code>. */
    public long getPreviousRuntime() { return previousRuntime; }
    /** Sets the value of <code>previousRuntime</code>. This is used in <code>statistics/2</code>. */
    public void setPreviousRuntime(long t) { previousRuntime = t; }

    /** Returns the standard input stream. */
    public PushbackReader  getUserInput() { return userInput; }
    /** Returns the standard output stream. */
    public PrintWriter     getUserOutput() { return userOutput; }
    /** Returns the standard error stream. */
    public PrintWriter     getUserError() { return userError; }

    /** Returns the current input stream. */
    public PushbackReader  getCurrentInput() { return currentInput; }
    /** Sets the current input stream to <code>in</code>. */
    public void            setCurrentInput(PushbackReader in) { currentInput = in; }

    /** Returns the current output stream. */
    public PrintWriter     getCurrentOutput() { return currentOutput; }
    /** Sets the current output stream to <code>out</code>. */
    public void            setCurrentOutput(PrintWriter out) { currentOutput = out; }

    /** Returns the stream manager. */
    public HashtableOfTerm getStreamManager() { return streamManager; }

    /** Returns the hash manager. */
    public HashtableOfTerm getHashManager() { return hashManager; }
}
